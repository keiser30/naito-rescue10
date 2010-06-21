package naito_rescue.agent;

import java.util.*;
import java.io.*;
import rescuecore2.worldmodel.*;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;

import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;


public class NAITOFireBrigade extends NAITOHumanoidAgent<FireBrigade>
{
	private static final String MAX_WATER_KEY = "fire.tank.maximum";
	private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
	private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";

	private int maxWater;
	private int maxDistance;
	private int maxPower;

	private Building target;
	// private Collection<StandardEntity> allBuildings;

	@Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
	}

	@Override
	public String toString(){
		return "NAITOFireBrigade." + me().getID() + "";
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);

		logger.info("NAITOFireBrigade.think();");
        if (time <= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			if(time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
				logger.info("sendSubscribe(1): " + time);
           	 	sendSubscribe(time, 1);
			}else{
				return;
			}
        }
		logger.info("NAITOFireBrigade.hearing...");

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
					if(message.getAddrAgent() != me().getID().getValue() && message.getAddrType() != ADDR_FB){
						logger.info("Ignore message.");
						continue; //自分(もしくは自分と同じ種別のエージェント)宛のメッセージでなかったら無視
					}

					if(message.getType() == TYPE_FIRE){
						logger.info("TYPE_FIRE messsage has received.");
						EntityID target_id = ((ExtinguishMessage)message).getTarget();
						StandardEntity target = model.getEntity(target_id);
						logger.info("currentTaskList.add(ExtinguishTask(" + target + ")");
						currentTaskList.add(new ExtinguishTask(this, model, (Building)target, maxPower, maxDistance));
					}
				}//end inner-for.
			}
		}//end outer-for.
		
        FireBrigade me = me();
        // Are we currently filling with water?
        if (me.isWaterDefined() && me.getWater() < (maxWater*0.6) && location() instanceof Refuge) {
            logger.info("Filling with water at " + location());
            rest();
            return;
        }
        // Are we out of water?
        if ((me.isWaterDefined() && me.getWater() == 0) ||
            (me.getHP() < (me.getStamina() / 5) )) {
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
		// 自分の視界にある建物についてExtinguishTaskを追加する
		List<Building> burningBuildings = getBurningBuildings(changed);
		if(!burningBuildings.isEmpty()){
			logger.info("burningBuildings.isNotEmpty()=>add(new ExtinguishTask());");
			for(Building b : burningBuildings){
				extinguish(b.getID(), maxPower);
				//currentTaskList.add(new ExtinguishTask(this, model, b, maxPower, maxDistance));
			}
		}

		currentTask = action();
		if(currentTask != null && currentTask.getRank() < 0){
			logger.info("currentTask.rank < MIN_VALUE;");
			logger.info("実行しても仕方ない(実行不可能なんだから)");
			move(randomWalk());
			return;
		}
		currentJob = currentTask.currentJob();
		if(currentJob != null){
			logger.info("currentJob = " + currentJob);
			currentJob.doJob();
		}else{
			logger.debug("currentJob is null.");
		}
	}
	@Override
	public Task action(){
		if(currentTask != null && currentTask instanceof ExtinguishTask && !currentTask.isFinished()){
			return currentTask;
		}
		int maxRank = -1;
		Task resultTask = null;
		//ExtinguishTaskがあったら, 最高優先度のものを問答無用で実行する
		for(Task actionTask : currentTaskList){
			if(actionTask instanceof ExtinguishTask && actionTask.getRank() > maxRank){
				resultTask = actionTask;
				maxRank = actionTask.getRank();
			}
		}
		if(resultTask != null){
			return resultTask;
		}
		logger.info("ExtinguishTaskがないだと?");
		taskRankUpdate();
		return getHighestRankTask();
	}
	// FireBrigadeのtaskRankUpdate
	@Override
	public void taskRankUpdate(){
		logger.info("FireBrigade.taskRankUpdate();");
		int distance;
		int rank;
		double width = (w_width > w_height? w_width : w_height);
		
		for(Task t : currentTaskList){
			
			if(t instanceof ExtinguishTask){
				logger.info("taskRankUpdate=>ExtinguishTask");
				// ExtinguishTask: 割り当て10000 ... 5000
				//とりあえず近いところから消していく
				distance = model.getDistance(getLocation(), ((ExtinguishTask)t).getTarget());
				rank = taskRankAssignWithDistance(10000, 5000, distance, width);
				logger.info("t.setRank(" + rank + ");");
				
				//消火タスクのターゲット内に市民がいたら優先度あげる
				//どれくらい上げればいいんだろうねぇ
				//今は市民一体につき/*200*/あげるようにする
				for(StandardEntity c : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)){
					if (c instanceof Civilian){
						if( ((Civilian)c).getPosition() == ((ExtinguishTask) t).getTarget().getID() ){
							rank += 1000;
						}
					}
				}
				
				t.setRank(rank);
			}else if(t instanceof MoveTask){
				//MoveTask:
				logger.info("taskRankUpdate=>MoveTask");
				Area target = ((MoveTask)t).getTarget();
				logger.info("MoveTask=>target = " + target);
				if(!search.isPassable(getLocation(), target)){
					//通行不可能な場合，実行しない
					logger.debug("MoveTask=>!isPassable(); => setRank(Integer.MIN_VALUE);");
					t.setRank(Integer.MIN_VALUE);
					continue;
				}
				distance = model.getDistance(getLocation(), target);
				if(isOnTeam){
					//isMemberなら，割り当て9000...5000
					logger.debug("taskRankUpdate=>MoveTask=>isOnTeam");
					rank = taskRankAssignWithDistance(9000, 5000, distance, width);
				}else{
					//isMemberでなかったら，割り当て4000...1000(defualt)
					logger.debug("taskRankUpdate=>MoveTask=>!isOnTeam");
					rank = taskRankAssignWithDistance(4000, 1000, distance, width);
				}
				logger.info("t.setRank(" + rank + ");");
				t.setRank(rank);
			}
			/*
			else if(t instanceof RestTask){
				logger.info("taskRankUpdate=>RestTask");
				logger.info("t.setRank(Integer.MAX_VALUE);");
				t.setRank(Integer.MAX_VALUE);
			}
			*/
		}
	}
	
	//ExtinguishTask, MoveTaskなどにおいて，自分から対象までの距離を元にしたタスク優先度の割り当てをおこなう
	//(距離が遠くなるほど優先度は低くなる)
	private int taskRankAssignWithDistance(int maxRank, int minRank, int distance, double world_width){
		logger.debug("taskRankAssignWithDistance();");
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
	
/*
	private List<Building> getBurningBuildings(ChangeSet changed){
		Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		List<Building> result = new ArrayList<Building>();
		for (StandardEntity next : e) {
		    if (next instanceof Building) {
		        Building b = (Building)next;
		        if (b.isOnFire()) {
		            result.add(b);
		        }   
		    }   
		}   
		// Sort by distance
		Collections.sort(result, new DistanceSorter(location(), model));
		return result;
	}
*/
	private List<Building> getBurningBuildings(ChangeSet changed){
		ArrayList<Building> result = new ArrayList<Building>();
		//まず視界にある建物について追加する
		for(EntityID id : changed.getChangedEntities()){
			StandardEntity entity = model.getEntity(id);
			if(entity instanceof Building){
				Building b = (Building)entity;
				if(b.isOnFire()){
					result.add(b);
				}
			}
		}
		//次にワールドモデルから.
		for(StandardEntity building : allBuildings){
			if(building instanceof Building && ((Building)building).isOnFire()){
				result.add((Building)building);
			}
		}
		return result;
	}
	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
	}

    private List<EntityID> planPathToFire(Building target) {
        // Try to get to anything within maxDistance of the target
        Collection<StandardEntity> targets = model.getObjectsInRange(target, maxDistance);
        if (targets.isEmpty()) {
            return null;
        }
        return search.breadthFirstSearch(location(), targets);
    }

}
