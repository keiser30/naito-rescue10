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
	private boolean isPreferredVoice;
	private boolean        isPreferredNear;
	private FireBrigade me;
	// private Collection<StandardEntity> allBuildings;

    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        
		if(isMember && crowlingBuildings.isEmpty()){
			//自分がisMemberなのにも関わらずcrowlingBuidlingが空っぽ...
			//かわいそうなので声データ優先にします
			
			isPreferredVoice = true;
		}else if(fbList.size() < 5){
			isPreferredVoice = (fbList.indexOf(me()) % 3) < 2;
		}else{
			//全体の3/4が声データ優先
			isPreferredVoice = (fbList.indexOf(me()) % 4) < 3;
		}
		isPreferredNear = (atList.indexOf(me()) % 2) == 0;
		me = me();
		
		logger.info("isPreferredVoice = " + isPreferredVoice);
		logger.info("isPreferredNear  = " + isPreferredNear);
	}

	public String toString(){
		return "NAITOFireBrigade." + me().getID() + "";
	}

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
					if(message.getAddrAgent() != me().getID().getValue() && message.getAddrType() != ADDR_FB){
						
						continue; //自分(もしくは自分と同じ種別のエージェント)宛のメッセージでなかったら無視
					}

					if(message.getType() == TYPE_FIRE){
						
						EntityID target_id = ((ExtinguishMessage)message).getTarget();
						StandardEntity target = model.getEntity(target_id);
						
						logger.info("Receive ExtinguishMessage.");
						logger.info(((ExtinguishMessage)message).toString());
						currentTaskList.add(new ExtinguishTask(this, model, (Building)target, maxPower, maxDistance));
					}
				}//end inner-for.
			}
		}//end outer-for.
		
		// 自分の視界にある建物について消火する
		List<Building> burningBuildings = getBurningBuildings(changed);
		if(!burningBuildings.isEmpty()){
			
			for(Building b : burningBuildings){
				if(model.getDistance(me.getPosition(), b.getID()) <= maxDistance){
					extinguish(b.getID(), maxPower);
				}
				//currentTaskList.add(new ExtinguishTask(this, model, b, maxPower, maxDistance));
			}
		}
        // Are we currently filling with water?
        if (me.isWaterDefined() && me.getWater() < (maxWater*0.4) && location() instanceof Refuge) {
            
            rest();
            return;
        }
        // Are we out of water?
        if ((me.isWaterDefined() && me.getWater() == 0) ||
            (me.getHP() < (me.getStamina() / 5) )) {
            // Head for a refuge
            List<EntityID> path = search.breadthFirstSearch(getLocation(), allRefuges);
            if (path != null) {
                
                move(path);
                return;
            }
            else {
                
                path = randomWalk();
                
                move(path);
                return;
            }
        }
		currentTask = action();
		if(currentTask == null){
			move(randomWalk());
			return;
		}
		currentJob = currentTask.currentJob();
		if(currentJob != null){
			currentJob.doJob();
		}else{
			
		}
	}
	
	private List<ExtinguishTask> collectExtinguishTask(){
		ArrayList<ExtinguishTask> result = new ArrayList<ExtinguishTask>();
		for(Task t : currentTaskList){
			if(t instanceof ExtinguishTask){
				result.add((ExtinguishTask)t);
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
	private Task extinguishTaskStrategy(List<ExtinguishTask> extinguishTasks){
		int maxDistance = Integer.MIN_VALUE;
		int distance_temp;
		ExtinguishTask result = null;
		
		//(ExtinguishTaskに関する優先度決定)
		for(ExtinguishTask ex : extinguishTasks){
			distance_temp = model.getDistance(me.getPosition(), ex.getTarget().getID());
			if(distance_temp > maxDistance){
				maxDistance = distance_temp;
				result = ex;
			}
		}
		return result; //自分から一番遠いところにターゲットがあるExtinguishTask
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
				
				currentTaskList.remove(result);
			}
		}
		return result; //自分から一番近いところがターゲットになっているMoveTask
	}
	public Task action(){
		List<MoveTask> moveTasks = collectMoveTask();
		List<ExtinguishTask> extinguishTasks = collectExtinguishTask();
		
		if(currentTask != null && !currentTask.isFinished()){
			return currentTask;
		}
		if(isPreferredVoice){
			//声データ ... つまりVoiceからのExtinguishTask優先
			if(!extinguishTasks.isEmpty()){
				return extinguishTaskStrategy(extinguishTasks);
			}else if(!moveTasks.isEmpty()){
				return moveTaskStrategy(moveTasks);
			}
			return null; //randomWalk();
		}else{
			//建物探訪優先
			if(!moveTasks.isEmpty()){
				return moveTaskStrategy(moveTasks);
			}else if(!extinguishTasks.isEmpty()){
				return extinguishTaskStrategy(extinguishTasks);
			}
			isPreferredVoice = true;
			return null;
		}
	}

	// FireBrigadeのtaskRankUpdate
	
	public void taskRankUpdate(){
		
		int distance;
		int rank;
		double width = (w_width > w_height? w_width : w_height);
		
		for(Task t : currentTaskList){
			
			if(t instanceof ExtinguishTask){
				
				// ExtinguishTask: 割り当て10000 ... 5000
				//とりあえず近いところから消していく
				distance = model.getDistance(getLocation(), ((ExtinguishTask)t).getTarget());
				rank = taskRankAssignWithDistance(10000, 5000, distance, width);
				
				
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
				
				Area target = ((MoveTask)t).getTarget();
				
				if(!search.isPassable(getLocation(), target)){
					//通行不可能な場合，実行しない
					
					t.setRank(Integer.MIN_VALUE);
					continue;
				}
				distance = model.getDistance(getLocation(), target);
				if(isOnTeam){
					//isMemberなら，割り当て9000...5000
					
					rank = taskRankAssignWithDistance(9000, 5000, distance, width);
				}else{
					//isMemberでなかったら，割り当て4000...1000(defualt)
					
					rank = taskRankAssignWithDistance(4000, 1000, distance, width);
				}
				
				t.setRank(rank);
			}
			/*
			else if(t instanceof RestTask){
				
				
				t.setRank(Integer.MAX_VALUE);
			}
			*/
		}
	}
	
	//ExtinguishTask, MoveTaskなどにおいて，自分から対象までの距離を元にしたタスク優先度の割り当てをおこなう
	//(距離が遠くなるほど優先度は低くなる)
	private int taskRankAssignWithDistance(int maxRank, int minRank, int distance, double world_width){
		
		
		
		
		
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
