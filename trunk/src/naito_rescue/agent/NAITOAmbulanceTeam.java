package naito_rescue.agent;

import java.util.*;

import rescuecore2.worldmodel.*;
import rescuecore2.standard.messages.*;
import rescuecore2.messages.*;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.*;

import naito_rescue.*;
import naito_rescue.message.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;

/**
*  救急隊だよ
*
*/
public class NAITOAmbulanceTeam extends NAITOHumanoidAgent<AmbulanceTeam>
{
	private Collection<StandardEntity> unexploredBuildings;
	private StandardEntity target_building;
	private int            team;
	private AmbulanceTeam  me;
	private boolean        isPreferredVoice;
	private boolean        isPreferredNear;
	
	
	protected void postConnect(){
        super.postConnect();
		model.indexClass(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.REFUGE, StandardEntityURN.BUILDING);
		unexploredBuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		
		if(isMember && crowlingBuildings.isEmpty()){
			//自分がisMemberなのにも関わらずcrowlingBuidlingが空っぽ...
			//かわいそうなので声データ優先にします
			
			isPreferredVoice = true;
		}else if(atList.size() < 5){
			isPreferredVoice = (atList.indexOf(me()) % 3) < 2;
		}else{
			//全体の3/4が声データ優先
			isPreferredVoice = (atList.indexOf(me()) % 4) < 3;
		}
		isPreferredNear = (atList.indexOf(me()) % 2) == 0;
		me = me();
		
		logger.info("isPreferredVoice = " + isPreferredVoice);
		logger.info("isPreferredNear  = " + isPreferredNear);
	}
	
	public String toString(){
		return "NAITOAmbulanceTeam." + me().getID() + "";
	}

	boolean once = true;
	
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);
		
		
        if (time <= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			if(time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
           		 // Subscribe to channel 3
				
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
					if(message.getAddrAgent() != me().getID().getValue() && message.getAddrType() != ADDR_AT){
						continue; //自分(もしくは自分と同じ種別のエージェント)宛のメッセージでなかったら無視
					}

					if(message.getType() == TYPE_RESCUE){
						RescueMessage resc = (RescueMessage)message;
						EntityID target = resc.getTarget();
						StandardEntity target_entity = model.getEntity(target);
						
						logger.info("Receive RescueMessage.");
						logger.info(((RescueMessage)message).toString());
						currentTaskList.add(new RescueTask(this, model, (Building)target_entity));
					}
				}
			}
		}
		
        // Am I transporting a civilian to a refuge?
        if (someoneOnBoard() != null) {
            // Am I at a refuge?
            if (getLocation() instanceof Refuge) {
                // Unload!
                
                unload();
                return;
            }
            else {
                // Move to a refuge
                List<EntityID> path = search.breadthFirstSearch(getLocation(), allRefuges);
                if (path != null) {
                    
                    move(path);
                    return;
                }
                // What do I do now? Might as well carry on and see if we can dig someone else out.
                
            }
        }
		if(getLocation() instanceof Refuge && (me.getHP() < (me.getStamina() * 0.8))){
			if(someoneOnBoard() != null){
				unload();
				return;
			}
			rest();
			return;
		}
		//誰も乗せていないことが肝要
		if((me.getHP() < (me.getStamina() / 10)) && someoneOnBoard() == null){
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
		
        for (Human next : getTargets()) {
            if (next.getPosition().equals(location().getID())) {
                // Targets in the same place might need rescueing or loading
                if ((next instanceof Civilian) && next.getBuriedness() == 0 && !(location() instanceof Refuge)) {
                    // Load
                    
                    load(next.getID());
                    return;
                }
                if (next.getBuriedness() > 0) {
                    // Rescue
                    
                    rescue(next.getID());
                    return;
                }
            }
            else {
                // Try to move to the target
                List<EntityID> path = search.breadthFirstSearch(getLocation(), model.getEntity(next.getPosition()));
                if (path != null) {
                    
                    move(path);
                    return;
                }
            }
        }

		currentTask = action();
		if(currentTask == null){
			
			
			move(randomWalk());
			return;
		}
		currentJob = currentTask.currentJob();
		
		
		if(currentJob != null)
			currentJob.doJob();
		else
			
		
		//Nothing to do.	
		move(randomWalk());
	}
	private Task rescueTaskStrategy(List<RescueTask> rescueTasks){
		int maxDistance = Integer.MIN_VALUE;
		int distance_temp;
		RescueTask result = null;
		
		//一番遠井ところから実行していく
		for(RescueTask rt : rescueTasks){
			distance_temp = model.getDistance(me.getPosition(), rt.getTarget().getID());
			if(distance_temp > maxDistance){
				maxDistance = distance_temp;
				result = rt;
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
				
				currentTaskList.remove(result);
			}
		}
		return result; //自分から一番近いところがターゲットになっているMoveTask
	}
	
	public Task action(){

		List<MoveTask> moveTasks = collectMoveTask();
		List<RescueTask> rescueTasks = collectRescueTask();
		
		if(currentTask != null && !currentTask.isFinished()){
			return currentTask;
		}
		if(isPreferredVoice){
			//声データ ... つまりVoiceからのClearTask優先
			if(!rescueTasks.isEmpty()){
				return rescueTaskStrategy(rescueTasks);
			}else if(!moveTasks.isEmpty()){
				return moveTaskStrategy(moveTasks);
			}
			return null; //randomWalk();
		}else{
			if(!moveTasks.isEmpty()){
				return moveTaskStrategy(moveTasks);
			}else if(!rescueTasks.isEmpty()){
				return rescueTaskStrategy(rescueTasks);
			}
			return null; //randomWalk();
		}
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
	private List<RescueTask> collectRescueTask(){
		ArrayList<RescueTask> result = new ArrayList<RescueTask>();
		for(Task t : currentTaskList){
			if(t instanceof RescueTask){
				result.add((RescueTask)t);
			}
		}
		return result;
	}
	
	public void taskRankUpdate(){
		
		int distance;
		int rank;
		double width = (w_width > w_height? w_width : w_height);
		
		for(Task t : currentTaskList){
			//RescueTask: 割り当て10000...5000
			//実行中のRescueTask(運搬中のもの)には最大値を割り当てる
			if(t instanceof RescueTask){
				
				if(currentTask != null && currentTask.equals(t) && !currentTask.isFinished() && someoneOnBoard() != null){
					
					
					t.setRank(Integer.MAX_VALUE); //何が何でも実行する
					continue;
				}
				distance = model.getDistance(getLocation(), ((RescueTask)t).getTarget());
				rank = basicRankAssign(10000, 5000, distance, width);
				
				t.setRank(rank);
			}
			//MoveTask:
			else if(t instanceof MoveTask){
				
				Area target = ((MoveTask)t).getTarget();
				if(!search.isPassable(getLocation(), target)){
					//通行不可能な場合，実行しない
					
					t.setRank(Integer.MIN_VALUE);
					//currentTaskList.remove(t);
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
			/*
			//RestTask:
			else if(t instanceof RestTask){
				
				
				t.setRank(Integer.MAX_VALUE);
			}
			*/
		}
	}
	
	//RescueTask, MoveTaskなどにおいて，自分から対象までの距離を元にしたタスク優先度の割り当てをおこなう
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

    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}

    private List<Human> getTargets() {
        List<Human> targets = new ArrayList<Human>();
        for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM)) {
            Human h = (Human)next;
            if (h == me()) {
                continue;
            }
            if (h.isHPDefined()
                && h.isBuriednessDefined()
                && h.isDamageDefined()
                && h.isPositionDefined()
                && h.getHP() > 0
                && (h.getBuriedness() > 0 || h.getDamage() > 0)
				&& h.getBuriedness() > 0){
                
                targets.add(h);
            }
        }
        Collections.sort(targets, new DistanceSorter(location(), model));
        return targets;
    }
    private void updateUnexploredBuildings(ChangeSet changed) {
    	
        for (EntityID next : changed.getChangedEntities()) {
            StandardEntity e = model.getEntity(next);
            if (e != null) {
                unexploredBuildings.remove(e);
            }
        }
    }
}
