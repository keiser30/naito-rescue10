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
    //private static final String DISTANCE_KEY = "clear.repair.distance";
	//private int distance; //閉塞解除が可能な距離...?
	private boolean isPreferredVoice;
	private boolean isPreferredNear;
	private PoliceForce me;
	@Override
	public String toString(){
		return "NAITOPoliceForce." + me().getID() + "";
	}

	@Override
	protected void postConnect(){
        super.postConnect();
		//distance = config.getIntValue(DISTANCE_KEY);
		
		if(isMember && crowlingBuildings.isEmpty()){
			//自分がisMemberなのにも関わらずcrowlingBuidlingが空っぽ...
			//かわいそうなので声データ優先にします
			
			isPreferredVoice = true;
			me = me();
			return;
		}
		//isPreferredVoice = ((pfList.indexOf(me()) % 2) == 0);
		isPreferredVoice = true;
		isPreferredNear = ((pfList.indexOf(me()) % 2) == 0);
		me = me();
		
		logger.info("isPreferredVoice = " + isPreferredVoice);
		logger.info("isPreferredNear  = " + isPreferredNear);
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);
		

        if (time <= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			if(time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
           		 // Subscribe to channel 1
           	 	sendSubscribe(time, 1);
			}else{
				return;
			}
        }
        
        StandardEntity location = getLocation();
        if(location instanceof Building && crowlingBuildings != null && !crowlingBuildings.isEmpty()){
        	logger.info("Remove Building in crowlingBuildings. building = " + location);
        	crowlingBuildings.remove((Building)location);
        }
        
		for(Command next : heard){
			
			if(next instanceof AKSpeak){
				/**
				*  無線or声データの処理
				*/
				
				AKSpeak speak = (AKSpeak)next;
				//ノイズ対策
				if(speak.getContent() == null || speak.getContent().length <= 0){
					
					continue;
				}
				List<naito_rescue.message.Message> msgList = msgManager.receiveMessage(speak);
				
				//ノイズ対策
				if(msgList == null){
					continue;
				}
				logger.info("Receive Message List = " + msgList);
				for(naito_rescue.message.Message message : msgList){
					if(message.getAddrAgent() != me().getID().getValue() && message.getAddrType() != ADDR_PF){
						
						continue; //自分(もしくは自分と同じ種別のエージェント)宛のメッセージでなかったら無視
					}

					if(message.getType() == TYPE_CLEAR){
					
						EntityID target_id = ((ClearMessage)message).getTarget();
						StandardEntity target = model.getEntity(target_id);
						
						logger.info("Receive ClearMessage.");
						logger.info(((ClearMessage)message).toString());
						currentTaskList.add(new ClearTask(this, model, (Area)target, maxRepairDistance));
					}
				}
			}
		}
		if(getLocation() instanceof Refuge && (me.getHP() < (me.getStamina() * 0.8))){
			logger.info("location = " + getLocation() + ", getHP() = " + me.getHP() + " => rest();");
			rest();
			return;
		}
		if((me.getHP() < (me.getStamina() / 5))){
            // Head for a refuge
            List<EntityID> path = search.breadthFirstSearch(getLocation(), allRefuges);
            if (path != null) {
            	logger.info("me.getHP() = " + me.getHP() + " => go refuge. " + path);
                move(path);
                return;
            }
            else {
                path = randomWalk();
                
                logger.info("Cannot go refuge. => randomWalk(); " + path);
                move(path);
                return;
            }
		}

		currentTask = action();
		if(currentTask != null){
			currentJob = currentTask.currentJob();
			
			
			if(currentJob != null){
				
				currentJob.doJob();
				return;
			}else{
				
			}
		}
        // Am I near a blockade?
        Blockade target = getTargetBlockade();
        if (target != null /*&& !isPreferredVoice*/) {
            
            //sendSpeak(time, 1, ("Clearing " + target).getBytes());
            sendClear(time, target.getID());
            return;
        }

        // Plan a path to a blocked area
        List<EntityID> path = search.breadthFirstSearch(getLocation(), getBlockedRoads(changed));
        if (path != null /*&& !isPreferredVoice*/) {
            
            Road r = (Road)model.getEntity(path.get(path.size() - 1));
            Blockade b = getTargetBlockade(r, -1);
            sendMove(time, path, b.getX(), b.getY());
            
            
            return;
        }

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
		
		for(Task t : currentTaskList){
			if(t instanceof ClearTask){
				
				return true;
			}
		}
		
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
				logger.debug("task = " + result + ", maxDistance = " + maxDistance);
			}
		}
		logger.debug("=> return " + result);
		return result; //自分から一番遠いところにターゲットがあるClearTask
	}
	private Task moveTaskStrategy(List<MoveTask> moveTasks){
		List<EntityID> path = null;
		int minDistance = Integer.MAX_VALUE;
		int maxDistance = Integer.MIN_VALUE;
		int distance_temp;
		MoveTask result = null;
		if(isPreferredNear){
			while(path == null){
				for(MoveTask mt : moveTasks){
					distance_temp = model.getDistance(me.getPosition(), mt.getTarget().getID());
					if(distance_temp < minDistance){
						minDistance = distance_temp;
						result = mt;
						logger.debug("task = " + mt + ", minDistance = " + minDistance);
					}
				}
				path = search.breadthFirstSearch(getLocation(), result.getTarget());
				if(path == null){
					logger.debug("Remove MoveTask: " + result);
					currentTaskList.remove(result);
				}
			}
			logger.debug("return " + result);
			return result; //自分から一番近いところがターゲットになっているMoveTask
		}else{
			while(path == null){
				for(MoveTask mt : moveTasks){
					distance_temp = model.getDistance(me.getPosition(), mt.getTarget().getID());
					if(distance_temp > maxDistance){
						maxDistance = distance_temp;
						result = mt;
						logger.debug("task = " + mt + ", maxDistance = " + maxDistance);
					}
				}
				path = search.breadthFirstSearch(getLocation(), result.getTarget());
				if(path == null){
					logger.debug("Remove MoveTask: " + result);
					currentTaskList.remove(result);
				}
			}
			logger.debug("return " + result);
			return result; //自分から一番遠いところがターゲットになっているMoveTask
		}
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
				logger.info("isPreferredVoice => !clearTasks.isEmpty() => return clearTaskStrategy();");
				logger.debug("clearTasks = " + clearTasks);
				return clearTaskStrategy(clearTasks);
			}else if(!moveTasks.isEmpty()){
				logger.info("isPreferredVoice => clearTasks.isEmpty() && !moveTasks.isEmpty() => return moveTaskStrategy();");
				logger.debug("moveTasks = " + moveTasks);
				return moveTaskStrategy(moveTasks);
			}
			isPreferredVoice = false;
			logger.info("isPreferredVoice => clearTasks.isEmpty() && moveTasks.isEmpty() => return null;");
			return null;
		}else{
			//建物探訪優先
			if(!moveTasks.isEmpty()){
				logger.info("!isPreferredVoice => !moveTasks.isEmpty() => return moveTaskStrategy();");
				moveTaskStrategy(moveTasks);
			}else if(!clearTasks.isEmpty()){
			logger.info("!isPreferredVoice => !clearTasks.isEmpty() => return clearTaskStrategy();");
				clearTaskStrategy(clearTasks);
			}
			//isPreferredVoice = true;
			logger.info("!isPreferredVoice => clearTasks.isEmpty() && moveTasks.isEmpty() => return null;");
			return null;
		}
	}
	@Override
	public void taskRankUpdate(){
		
		int distance;
		int rank;
		double width = (w_width > w_height? w_width : w_height);
		
		for(Task t : currentTaskList){
			//ClearTask: 割り当て10000...5000
			if(t instanceof ClearTask){
				
				int distance_temp;
		    	List<ClearTask> clearTasks = collectClearTask();
		    	int maxDistance = Integer.MIN_VALUE;
		    	EntityID from = me.getPosition(), to;
		    	
		    	ClearTask task_temp = null;
		    	for(ClearTask target : clearTasks){
		    		
		    		to = target.getTarget().getID();
		    		distance_temp = model.getDistance(from, to);
		    		target.setRank(basicRankAssign(10000, 7000, distance_temp, width));
		    		
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
				
				Area target = ((MoveTask)t).getTarget();
				if(!search.isPassable(getLocation(), target)){
					//通行不可能な場合，実行しない
					
					t.setRank(Integer.MIN_VALUE);
					continue;
				}
				distance = model.getDistance(getLocation(), target);
				if(isOnTeam){
					//割り当て9000...5000
					
					rank = basicRankAssign(9000, 5000, distance, width);
				}else{
					//割り当て4000...1000(default)
					
					rank = basicRankAssign(4000, 1000, distance, width);
				}
				
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
		
		
		
		
		
		int rank = maxRank;
		
		if(distance > 0){
			int increment = (int)((maxRank - minRank) * (distance / world_width));
			if(increment > minRank){
				increment = minRank;
			}
			
			rank = maxRank - increment;
		}
		
		return rank;
	}
	
    @Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    } 

	private List<Road> getBlockedRoads(ChangeSet changed){
		
		ArrayList<Road> result = new ArrayList<Road>();
		//まず視界情報について検査
		for(EntityID id : changed.getChangedEntities()){
			StandardEntity entity = model.getEntity(id);
			if(entity instanceof Road){
				Road r = (Road)entity;
				if(r.isBlockadesDefined() && !r.getBlockades().isEmpty()){
					
					result.add(r);
				}
			}
		}
		//次にallRoadsについて検査
		for(StandardEntity road : allRoads){
			if(road instanceof Road){
				Road r = (Road)road;
				if(r.isBlockadesDefined() && !r.getBlockades().isEmpty()){
					
					result.add(r);
				}
			}
		}
		if(result.isEmpty()){
			
		}
		return result;
	}
/*
	//MoveToClearPointJobとの整合性に注意
    public Blockade getTargetBlockade() {
       // 
	   	
        Area location = (Area)location();
        //
        Blockade result = getTargetBlockade(location, maxRepairDistance);
        if (result != null) {
			
			
            return result;
        }
        //
        for (EntityID next : location.getNeighbours()) {
            location = (Area)model.getEntity(next);
            result = getTargetBlockade(location, distance);
            if (result != null) {
				
				
                return result;
            }
        }
		
        return null;
    }
*/
/*
    public Blockade getTargetBlockade(Area area, int maxDistance) {
        //
        
		if (!area.isBlockadesDefined()) {
            //Logger.debug("Blockades undefined");
			
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        // Find the first blockade that is in range.
        int x = me().getX();
        int y = me().getY();
        for (EntityID next : ids) {
            Blockade b = (Blockade)model.getEntity(next);
            double d = findDistanceTo(b, x, y);
            //
            if (maxDistance < 0 || d < maxDistance) {
                //
				
				
                return b;
            }
        }
        
        return null;
    }
*/	
	public int getDistance(){
		return maxRepairDistance;
	}
}
