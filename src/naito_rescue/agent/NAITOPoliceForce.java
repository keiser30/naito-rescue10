package naito_rescue.agent;

import java.util.*;
import java.io.*;

import rescuecore2.worldmodel.*;
import rescuecore2.standard.messages.*;
import rescuecore2.messages.*;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.*;
import rescuecore2.misc.geometry.*;

import naito_rescue.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;
import naito_rescue.message.manager.*;

/**
*  啓開隊だよ
*
*/
public class NAITOPoliceForce extends NAITOHumanoidAgent<PoliceForce> implements MessageConstants
{
    private static final String DISTANCE_KEY = "clear.repair.distance";
	private int distance; //閉塞解除が可能な距離...?
	private boolean isPreferredVoice;
	private boolean        isPreferredNear;
	private PoliceForce me;
	@Override
	public String toString(){
		return "NAITOPoliceForce." + me().getID() + "";
	}

	@Override
	protected void postConnect(){
        super.postConnect();
		distance = config.getIntValue(DISTANCE_KEY);
		
		if(isMember && crowlingBuildings.isEmpty()){
			//自分がisMemberなのにも関わらずcrowlingBuidlingが空っぽ...
			//かわいそうなので声データ優先にします
			logger.info("Kawaisou => true");
			isPreferredVoice = true;
			me = me();
			return;
		}
		//isPreferredVoice = ((pfList.indexOf(me()) % 2) == 0);
		isPreferredVoice = true;
		isPreferredNear = ((pfList.indexOf(me()) % 2) == 0);
		me = me();
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);
		logger.info("NAITOPoliceForce.think();");

        if (time <= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			if(time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
           		 // Subscribe to channel 3
				logger.info("sendSubscribe(" + time + ", 1");
           	 	sendSubscribe(time, 1);
			}else{
				return;
			}
        }
        
        StandardEntity location = getLocation();
        if(location instanceof Building && crowlingBuildings != null && !crowlingBuildings.isEmpty()){
        	crowlingBuildings.remove((Building)location);
        }
		
		logger.info("NAITOPoliceForce.hearing...");

		for(Command next : heard){
			logger.debug("heard->next = " + next);
			if(next instanceof AKSpeak){
				/**
				*  無線or声データの処理
				*/
				logger.info("Receive AKSpeak.");
				AKSpeak speak = (AKSpeak)next;
				//ノイズ対策
				if(speak.getContent() == null || speak.getContent().length <= 0){
					logger.debug("speak.getContent() => null or length<=0 ");
					continue;
				}
				List<naito_rescue.message.Message> msgList = msgManager.receiveMessage(speak);
				
				//ノイズ対策
				if(msgList == null){
					logger.debug("msgList == null (maybe )");
					continue;
				}
				logger.info("Extracting messages size = " + msgList.size());
				for(naito_rescue.message.Message message : msgList){
					if(message.getAddrAgent() != me().getID().getValue() && message.getAddrType() != ADDR_PF){
						logger.info("Ignore message.");
						continue; //自分(もしくは自分と同じ種別のエージェント)宛のメッセージでなかったら無視
					}

					if(message.getType() == TYPE_CLEAR){
						logger.info("TYPE_CLEAR messsage has received.");
						EntityID target_id = ((ClearMessage)message).getTarget();
						StandardEntity target = model.getEntity(target_id);
						logger.info("PF.currentTaskList.add(ClearTask(" + target + ")");
						currentTaskList.add(new ClearTask(this, model, (Area)target, distance));
					}
				}
			}
		}
		if(getLocation() instanceof Refuge && (me.getHP() < (me.getStamina() * 0.8))){
			rest();
			return;
		}
		if((me.getHP() < (me.getStamina() / 5))){
            // Head for a refuge
            List<EntityID> path = search.breadthFirstSearch(getLocation(), allRefuges);
            if (path != null) {
                logger.info("Moving to refuge");
                move(path);
                return;
            }
            else {
                logger.debug("Couldn't plan a path to a refuge.");
                path = randomWalk();
                logger.info("Moving randomly");
                move(path);
                return;
            }
		}
        // Am I near a blockade?
        Blockade target = getTargetBlockade();
        if (target != null && !isPreferredVoice) {
            logger.info("Clearing blockade " + target);
            //sendSpeak(time, 1, ("Clearing " + target).getBytes());
            sendClear(time, target.getID());
            return;
        }

        // Plan a path to a blocked area
        List<EntityID> path = search.breadthFirstSearch(getLocation(), getBlockedRoads(changed));
        if (path != null && !isPreferredVoice) {
            logger.info("Moving to target");
            Road r = (Road)model.getEntity(path.get(path.size() - 1));
            Blockade b = getTargetBlockade(r, -1);
            sendMove(time, path, b.getX(), b.getY());
            logger.debug("Path: " + path);
            logger.debug("Target coordinates: " + b.getX() + ", " + b.getY());
            return;
        }

		//Nothing to do の時にTask-Jobを実行する
		currentTask = action();
		if(currentTask == null){
			logger.info("currentTask.rank < MIN_VALUE or currentTask == null;");
			logger.info("==>randomWalk();");
			move(randomWalk());
			return;
		}
		currentJob = currentTask.currentJob();
		logger.info("currentTask = " + currentTask);
		logger.info("currentJob  = " + currentJob);
		if(currentJob != null){
			logger.info("=> currentJob.do();");
			currentJob.doJob();
			return;
		}else{
			logger.debug("currentJob is null. => randomWalk();");
		}	
		move(randomWalk());
	}
	private List<ClearTask> collectClearTask(){
		ArrayList<ClearTask> result = new ArrayList<ClearTask>();
		for(Task t : currentTaskList){
			if(t instanceof ClearTask){
				result.add((ClearTask)t);
			}
		}
		return result;
	}
	private List<MoveTask> collectMoveTask(){
		ArrayList<MoveTask> result = new ArrayList<MoveTask>();
		for(Task t : currentTaskList){
			if(t instanceof MoveTask){
				result.add((MoveTask)t);
			}
		}
		return result;
	}
	//currentTaskListにClearTaskが含まれていればtrue
	private boolean containsClearTask(){
		logger.info("containsClearTask();");
		for(Task t : currentTaskList){
			if(t instanceof ClearTask){
				logger.info("=>contains ClearTask(" + t + ")");
				return true;
			}
		}
		logger.info("=> return false;");
		return false;
	}
	private Task clearTaskStrategy(List<ClearTask> clearTasks){
		int maxDistance = Integer.MIN_VALUE;
		int distance_temp;
		ClearTask result = null;
		
		//一番遠井ところから実行していく
		for(ClearTask cl : clearTasks){
			distance_temp = model.getDistance(me.getPosition(), cl.getTarget().getID());
			if(distance_temp > maxDistance){
				maxDistance = distance_temp;
				result = cl;
			}
		}
		return result; //自分から一番遠いところにターゲットがあるClearTask
	}
	private Task moveTaskStrategy(List<MoveTask> moveTasks){
		//自分に近いところから巡っていく
		List<EntityID> path = null;
		int minDistance = Integer.MAX_VALUE;
		int distance_temp;
		MoveTask result = null;
		while(path == null){
			for(MoveTask mt : moveTasks){
				distance_temp = model.getDistance(me.getPosition(), mt.getTarget().getID());
				if(distance_temp < minDistance){
					minDistance = distance_temp;
					result = mt;
				}
			}
			path = search.breadthFirstSearch(getLocation(), result.getTarget());
			if(path == null){
				logger.info("path==null => remove MoveTask");
				currentTaskList.remove(result);
			}
		}
		return result; //自分から一番近いところがターゲットになっているMoveTask
	}
	@Override
	public Task action(){

		List<MoveTask> moveTasks = collectMoveTask();
		List<ClearTask> clearTasks = collectClearTask();
		if(currentTask != null && !currentTask.isFinished()){
			return currentTask;
		}
		if(isPreferredVoice){
			//声データ ... つまりVoiceからのClearTask優先
			if(!clearTasks.isEmpty()){
				return clearTaskStrategy(clearTasks);
			}else if(!moveTasks.isEmpty()){
				return moveTaskStrategy(moveTasks);
			}
			isPreferredVoice = false;
			return null;
		}else{
			//建物探訪優先
			if(!moveTasks.isEmpty()){
				moveTaskStrategy(moveTasks);
			}else if(!clearTasks.isEmpty()){
				clearTaskStrategy(clearTasks);
			}
			//isPreferredVoice = true;
			return null;
		}
	}
	@Override
	public void taskRankUpdate(){
		logger.info("PoliceForce.taskRankUpdate();");
		int distance;
		int rank;
		double width = (w_width > w_height? w_width : w_height);
		
		for(Task t : currentTaskList){
			//ClearTask: 割り当て10000...5000
			if(t instanceof ClearTask){
				logger.info("taskRankUpdate=>ClearTask");
				int distance_temp;
		    	List<ClearTask> clearTasks = collectClearTask();
		    	int maxDistance = Integer.MIN_VALUE;
		    	EntityID from = me.getPosition(), to;
		    	
		    	ClearTask task_temp = null;
		    	for(ClearTask target : clearTasks){
		    		
		    		to = target.getTarget().getID();
		    		distance_temp = model.getDistance(from, to);
		    		target.setRank(basicRankAssign(10000, 7000, distance_temp, width));
		    		logger.info("target = " + target + ", distance = " + distance_temp);
		    		if(distance_temp > maxDistance){
		    			task_temp = target;
		    			maxDistance = distance_temp;
		    		}
		    		
		    	}
		    	if(task_temp != null){
		    		task_temp.setRank(Integer.MAX_VALUE);
		    	}
			}
			//MoveTask:
			else if(t instanceof MoveTask){
				logger.info("taskRankUpdate=>MoveTask");
				Area target = ((MoveTask)t).getTarget();
				if(!search.isPassable(getLocation(), target)){
					//通行不可能な場合，実行しない
					logger.debug("MoveTask=>!isPassable(); => setRank(Integer.MIN_VALUE");
					t.setRank(Integer.MIN_VALUE);
					continue;
				}
				distance = model.getDistance(getLocation(), target);
				if(isOnTeam){
					//割り当て9000...5000
					logger.debug("taskRankUpdate=>MoveTask=>isOnTeam");
					rank = basicRankAssign(9000, 5000, distance, width);
				}else{
					//割り当て4000...1000(default)
					logger.debug("taskRankUpdate=>MoveTask=>!isOnTeam");
					rank = basicRankAssign(4000, 1000, distance, width);
				}
				logger.info("t.setRank(" + rank + ");");
				t.setRank(rank);
			}
		}
	}
	
	private boolean isMyArea(Area area){
		return (area.getX() > x && area.getX() <= (x + width) && area.getY() > y && area.getY() <= (y + height));
	}
	//ClearTask, MoveTaskなどにおいて，自分から対象までの距離を元にしたタスク優先度の割り当てをおこなう
	//(距離が遠くなるほど優先度は低くなる)
	private int basicRankAssign(int maxRank, int minRank, int distance, double world_width){
		logger.debug("basicRankAssign();");
		logger.debug("maxRank  = " + maxRank);
		logger.debug("minRank  = " + minRank);
		logger.debug("distance = " + distance);
		
		int rank = maxRank;
		logger.trace("distance = " + distance);
		if(distance > 0){
			int increment = (int)((maxRank - minRank) * (distance / world_width));
			if(increment > minRank){
				increment = minRank;
			}
			logger.trace("increment = " + increment);
			rank = maxRank - increment;
		}
		logger.debug("rank = " + rank);
		return rank;
	}
	
    @Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    } 

	private List<Road> getBlockedRoads(ChangeSet changed){
		logger.info("getBlockedRoadsInView();");
		ArrayList<Road> result = new ArrayList<Road>();
		//まず視界情報について検査
		for(EntityID id : changed.getChangedEntities()){
			StandardEntity entity = model.getEntity(id);
			if(entity instanceof Road){
				Road r = (Road)entity;
				if(r.isBlockadesDefined() && !r.getBlockades().isEmpty()){
					logger.debug("Blocked Road => " + r);
					result.add(r);
				}
			}
		}
		//次にallRoadsについて検査
		for(StandardEntity road : allRoads){
			if(road instanceof Road){
				Road r = (Road)road;
				if(r.isBlockadesDefined() && !r.getBlockades().isEmpty()){
					logger.debug("Blocked Road(allRoads) => " + r);
					result.add(r);
				}
			}
		}
		if(result.isEmpty()){
			logger.info("getBlockedRoadsInView(); => There is no blocked road.");
		}
		return result;
	}
	
	//MoveToClearPointJobとの整合性に注意
    public Blockade getTargetBlockade() {
       // logger.debug("Looking for target blockade");
	   	logger.info("NAITOPoliceForce.getTargetBlockade();");
        Area location = (Area)location();
        //logger.debug("Looking in current location");
        Blockade result = getTargetBlockade(location, distance);
        if (result != null) {
			logger.info("There is blockade in this.location;");
			logger.debug("" + result);
            return result;
        }
        //logger.debug("Looking in neighbouring locations");
        for (EntityID next : location.getNeighbours()) {
            location = (Area)model.getEntity(next);
            result = getTargetBlockade(location, distance);
            if (result != null) {
				logger.info("There is blockade in this.location.getNeighbours();");
				logger.debug("" + result);
                return result;
            }
        }
		logger.info("There is not blockade. return null;");
        return null;
    }
/*
    public Blockade getTargetBlockade(Area area, int maxDistance) {
        //logger.debug("Looking for nearest blockade in " + area);
        logger.info("NAITOPoliceForce.getTargetBlockade(" + area + ", " + maxDistance + ")");
		if (!area.isBlockadesDefined()) {
            //Logger.debug("Blockades undefined");
			logger.info("!area.isBlockadesDefined(); ==> return null;");
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        // Find the first blockade that is in range.
        int x = me().getX();
        int y = me().getY();
        for (EntityID next : ids) {
            Blockade b = (Blockade)model.getEntity(next);
            double d = findDistanceTo(b, x, y);
            //logger.debug("Distance to " + b + " = " + d);
            if (maxDistance < 0 || d < maxDistance) {
                //logger.debug("In range");
				logger.info("There is blockade.");
				logger.debug("" + b);
                return b;
            }
        }
        logger.info("No blockades in range");
        return null;
    }
*/	
	public int getDistance(){
		return distance;
	}
}
