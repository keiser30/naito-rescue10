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
	private boolean        isOverrideVoice;
	private boolean        isOverrideNear;
	
	
	@Override
	protected void postConnect(){
        super.postConnect();
		model.indexClass(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.REFUGE, StandardEntityURN.BUILDING);
		unexploredBuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		
		if(crowlingBuildings.isEmpty()){
			//自分がisMemberなのにも関わらずcrowlingBuidlingが空っぽ...
			//かわいそうなので声データ優先にします
			logger.info("Kawaisou => true");
			isOverrideVoice = true;
		}else if(atList.size() < 5){
			isOverrideVoice = (atList.indexOf(me()) % 3) < 2;
		}else{
			//全体の3/4が声データ優先
			isOverrideVoice = (atList.indexOf(me()) % 4) < 3;
		}
		isOverrideNear = (atList.indexOf(me()) % 2) == 0;
		me = me();
	}
	@Override
	public String toString(){
		return "NAITOAmbulanceTeam." + me().getID() + "";
	}

	boolean once = true;
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);
		
		logger.info("NAITOAmbulanceTeam.think();");
        if (time <= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			if(time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
           		 // Subscribe to channel 3
				logger.info("sendSubscribe(" + time + ", 1");
           	 	sendSubscribe(time, 1);
			}else{
				return;
			}
        }

		logger.info("NAITOAmbulanceTeam.hearing...");
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

					if(message.getAddrAgent() != me().getID().getValue() && message.getAddrType() != ADDR_AT){
						logger.info("Ignore message.");
						continue; //自分(もしくは自分と同じ種別のエージェント)宛のメッセージでなかったら無視
					}

					if(message.getType() == TYPE_RESCUE){
						logger.info("TYPE_RESCUE message has received.");
						RescueMessage resc = (RescueMessage)message;
						EntityID target = resc.getTarget();
						StandardEntity target_entity = model.getEntity(target);
						logger.debug("=> target = " + target_entity);
						logger.info("=> currentTaskList.add(new RescueTask(" + target_entity + "));");
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
                logger.info("Unloading");
                unload();
                return;
            }
            else {
                // Move to a refuge
                List<EntityID> path = search.breadthFirstSearch(getLocation(), allRefuges);
                if (path != null) {
                    logger.info("Moving to refuge");
                    move(path);
                    return;
                }
                // What do I do now? Might as well carry on and see if we can dig someone else out.
                logger.debug("Failed to plan path to refuge");
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
		
        for (Human next : getTargets()) {
            if (next.getPosition().equals(location().getID())) {
                // Targets in the same place might need rescueing or loading
                if ((next instanceof Civilian) && next.getBuriedness() == 0 && !(location() instanceof Refuge)) {
                    // Load
                    logger.info("Loading " + next);
                    load(next.getID());
                    return;
                }
                if (next.getBuriedness() > 0) {
                    // Rescue
                    logger.info("Rescueing " + next);
                    rescue(next.getID());
                    return;
                }
            }
            else {
                // Try to move to the target
                List<EntityID> path = search.breadthFirstSearch(getLocation(), model.getEntity(next.getPosition()));
                if (path != null) {
                    logger.info("Moving to target");
                    move(path);
                    return;
                }
            }
        }

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
		if(currentJob != null)
			currentJob.doJob();
		else
			logger.debug("currentJob is null.");
		
		//Nothing to do.	
		move(randomWalk());
	}
	
	@Override
	public Task action(){
	/*
		if(currentTask != null && currentTask instanceof RescueTask && !currentTask.isFinished()){
			return currentTask;
		}
		int maxRank = -1;
		Task resultTask = null;
		//RescueTaskがあったら, 最高優先度のものを問答無用で実行する
		for(Task actionTask : currentTaskList){
			if(actionTask instanceof RescueTask && actionTask.getRank() > maxRank){
				resultTask = actionTask;
				maxRank = actionTask.getRank();
			}
		}
		if(resultTask != null){
			return resultTask;
		}
		logger.info("RescueTaskがないだと?");
		taskRankUpdate();
		return getHighestRankTask();
	*/
		List<MoveTask> moveTasks = collectMoveTask();
		List<RescueTask> rescueTasks = collectRescueTask();
		
		if(currentTask != null && !currentTask.isFinished()){
			return currentTask;
		}
		if(isOverrideVoice){
			//声データ ... つまりVoiceからのClearTask優先
			if(!rescueTasks.isEmpty()){
				int maxDistance = Integer.MIN_VALUE;
				int distance_temp;
				RescueTask task_temp = null;
				
				//一番遠井ところから実行していく
				for(RescueTask cl : rescueTasks){
					distance_temp = model.getDistance(me.getPosition(), cl.getTarget().getID());
					if(distance_temp > maxDistance){
						maxDistance = distance_temp;
						task_temp = cl;
					}
				}
				return task_temp; //自分から一番遠いところにターゲットがあるClearTask
			}else{
				if(!moveTasks.isEmpty()){
					if(isOverrideNear){
						//自分に近いところから巡っていく
						List<EntityID> path = null;
						int minDistance = Integer.MAX_VALUE;
						int distance_temp;
						MoveTask task_temp = null;
						while(path == null){
							for(MoveTask mt : moveTasks){
								distance_temp = model.getDistance(me.getPosition(), mt.getTarget().getID());
								if(distance_temp < minDistance){
									minDistance = distance_temp;
									task_temp = mt;
								}
							}
							path = search.breadthFirstSearch(getLocation(), task_temp.getTarget());
							if(path == null){
								logger.info("path==null => remove MoveTask");
								currentTaskList.remove(task_temp);
							}
						}
						return task_temp; //自分から一番近いところがターゲットになっているMoveTask
					}else{
						//自分に遠井ところから実行していく
						List<EntityID> path = null;
						int maxDistance = Integer.MIN_VALUE;
						int distance_temp;
						MoveTask task_temp = null;
						while(path == null){
							for(MoveTask mt : moveTasks){
								distance_temp = model.getDistance(me.getPosition(), mt.getTarget().getID());
								if(distance_temp >= maxDistance){
									maxDistance = distance_temp;
									task_temp = mt;
								}
							}
							path = search.breadthFirstSearch(getLocation(), task_temp.getTarget());
							if(path == null){
								logger.info("path==null => remove MoveTask");
								currentTaskList.remove(task_temp);
							}
						}
						return task_temp; //自分から一番近いところがターゲットになっているMoveTask
					}
				}
				//moveTasks.isEmpty() ... ランダムに建物を選んで行かせる
				try{
					Object[] buildings = allBuildings.toArray();
					for(int i = 0;i < (allBuildings.size() / 4);i++){
						int rand_idx = (int)(Math.random() * allBuildings.size());
						currentTaskList.add(new MoveTask(this, model, (Building)(buildings[rand_idx])));
					}
				}catch(Exception e){
					return null; //randomWalk();
				}
				if(!currentTaskList.isEmpty())
					return currentTaskList.get(0);
				else
					return null; //randomWalk;
			}
		}else{
			//建物探訪優先
			if(!moveTasks.isEmpty()){
					if(isOverrideNear){
						//自分に近いところから巡っていく
						List<EntityID> path = null;
						int minDistance = Integer.MAX_VALUE;
						int distance_temp;
						MoveTask task_temp = null;
						while(path == null){
							for(MoveTask mt : moveTasks){
								distance_temp = model.getDistance(me.getPosition(), mt.getTarget().getID());
								if(distance_temp < minDistance){
									minDistance = distance_temp;
									task_temp = mt;
								}
							}
							path = search.breadthFirstSearch(getLocation(), task_temp.getTarget());
							if(path == null){
								logger.info("path==null => remove MoveTask");
								currentTaskList.remove(task_temp);
							}
						}
						return task_temp; //自分から一番近いところがターゲットになっているMoveTask
					}else{
						//自分に遠井ところから実行していく
						List<EntityID> path = null;
						int maxDistance = Integer.MIN_VALUE;
						int distance_temp;
						MoveTask task_temp = null;
						while(path == null){
							for(MoveTask mt : moveTasks){
								distance_temp = model.getDistance(me.getPosition(), mt.getTarget().getID());
								if(distance_temp >= maxDistance){
									maxDistance = distance_temp;
									task_temp = mt;
								}
							}
							path = search.breadthFirstSearch(getLocation(), task_temp.getTarget());
							if(path == null){
								logger.info("path==null => remove MoveTask");
								currentTaskList.remove(task_temp);
							}
						}
						return task_temp; //自分から一番近いところがターゲットになっているMoveTask
					}
			}else{
				try{
					Object[] buildings = allBuildings.toArray();
					for(int i = 0;i < (allBuildings.size() / 4);i++){
						int rand_idx = (int)(Math.random() * allBuildings.size());
						currentTaskList.add(new MoveTask(this, model, (Building)(buildings[rand_idx])));
					}
				}catch(Exception e){
					return null; //randomWalk();
				}
				isOverrideVoice = true; //声優先
				if(!currentTaskList.isEmpty())
					return currentTaskList.get(0);
				else
					return null; //randomWalk;
			}
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
	@Override
	public void taskRankUpdate(){
		logger.info("AmbulanceTeam.taskRankUpdate();");
		int distance;
		int rank;
		double width = (w_width > w_height? w_width : w_height);
		
		for(Task t : currentTaskList){
			//RescueTask: 割り当て10000...5000
			//実行中のRescueTask(運搬中のもの)には最大値を割り当てる
			if(t instanceof RescueTask){
				logger.info("taskRankUpdate=>RescueTask");
				if(currentTask != null && currentTask.equals(t) && !currentTask.isFinished() && someoneOnBoard() != null){
					logger.debug("RescueTask: agent is moving to refuge now. ただいま運搬中");
					logger.debug("=> t.setRank(Integer.MAX_VALUE);");
					t.setRank(Integer.MAX_VALUE); //何が何でも実行する
					continue;
				}
				distance = model.getDistance(getLocation(), ((RescueTask)t).getTarget());
				rank = basicRankAssign(10000, 5000, distance, width);
				logger.info("t.setRank(" + rank + ");");
				t.setRank(rank);
			}
			//MoveTask:
			else if(t instanceof MoveTask){
				logger.info("taskRankUpdate=>MoveTask");
				Area target = ((MoveTask)t).getTarget();
				if(!search.isPassable(getLocation(), target)){
					//通行不可能な場合，実行しない
					logger.debug("MoveTask=>!isPassable(); => setRank(Integer.MIN_VALUE)");
					t.setRank(Integer.MIN_VALUE);
					//currentTaskList.remove(t);
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
			/*
			//RestTask:
			else if(t instanceof RestTask){
				logger.info("taskRankUpdate=>RestTask");
				logger.info("t.setRank(Integer.MAX_VALUE);");
				t.setRank(Integer.MAX_VALUE);
			}
			*/
		}
	}
	
	//RescueTask, MoveTaskなどにおいて，自分から対象までの距離を元にしたタスク優先度の割り当てをおこなう
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
                logger.debug("targets.add(" + h + ")");
                targets.add(h);
            }
        }
        Collections.sort(targets, new DistanceSorter(location(), model));
        return targets;
    }
/*
	private void updateTargetBuildings(){
		logger.debug("updateTargetBuildings();");
		logger.debug("visitedBuildings = " + visitedBuildings);
		for(StandardEntity visited : visitedBuildings){
			targetBuildings.remove(visited);
		}
	}
*/
    private void updateUnexploredBuildings(ChangeSet changed) {
    	logger.trace("called updateUnexploredBuildings();");
        for (EntityID next : changed.getChangedEntities()) {
            StandardEntity e = model.getEntity(next);
            if (e != null) {
                unexploredBuildings.remove(e);
            }
        }
    }
}
