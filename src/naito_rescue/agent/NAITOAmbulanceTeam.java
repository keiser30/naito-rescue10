package naito_rescue.agent;

import java.util.*;

import rescuecore2.worldmodel.*;
import rescuecore2.standard.messages.*;
import rescuecore2.messages.*;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.*;

import naito_rescue.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;

public class NAITOAmbulanceTeam extends NAITOHumanoidAgent<AmbulanceTeam>
{
	private Collection<StandardEntity> unexploredBuildings;
	private StandardEntity target_building;
	private int            team;

	@Override
	protected void postConnect(){
        super.postConnect();
		model.indexClass(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.REFUGE, StandardEntityURN.BUILDING);
		unexploredBuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);

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
				AKSpeak speak = (AKSpeak)next;
				
			}
		}
		//RescueTaskのテスト
		if(once){
			logger.debug("DEBUG: RescueTask is valid?");
			currentTaskList.add(new RescueTask(this, model, (Building)model.getEntity(new EntityID(254))));
			once = false;
		}
		//TODO: 視界情報にある建物にいる市民の探訪
		
		currentTask = action();
		currentJob = currentTask.currentJob();
		logger.info("currentTask = " + currentTask);
		logger.info("currentJob  = " + currentJob);
		if(currentJob != null)
			currentJob.doJob();
		else
			logger.debug("currentJob is null.");
	}
	
	@Override
	public void taskRankUpdate(){
		logger.info("AmbulanceTeam.taskRankUpdate();");
		int distance;
		int rank;
		double width = (w_width > w_height? w_width : w_height);
		
		for(Task t : currentTaskList){
			//RescueTask: 割り当て10000...5000
			if(t instanceof RescueTask){
				logger.info("taskRankUpdate=>RescueTask");
				distance = model.getDistance(getLocation(), ((RescueTask)t).getTarget());
				rank = basicRankAssign(10000, 5000, distance, width);
				logger.info("t.setRank(" + rank + ");");
				t.setRank(rank);
			}
			//MoveTask:
			else if(t instanceof MoveTask){
				logger.info("taskRankUpdate=>MoveTask");
				distance = model.getDistance(getLocation(), ((MoveTask)t).getTarget());
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
/*
	protected EntityID extractVisitedBuildingID(String str){
		EntityID result;
		if(str.startsWith("VISITED_")){
			int id = Integer.parseInt(str.substring(8));
			result = new EntityID(id);
			if(model.getEntity(result) instanceof Building){
				logger.debug("Visited Building ID is found. id = " + id);
				return result;
			}else{
				logger.debug("Visited Building ID is not found. id = " + id);
				return null;
			}
		}else{
			logger.debug("str_data is not started from 'VISITED_'");
			return null;
		}
	}
*/
/*
	protected StandardEntity getTargetBuilding(){
		logger.debug("getTargetBuilding();");
		StandardEntity result = null;
		if(team == 0){
			int distance = Integer.MAX_VALUE;
			result = null;
			for(StandardEntity target : targetBuildings){
				int dist_temp = model.getDistance(getLocation(), target);
				if(dist_temp < distance){
					distance = dist_temp;
					result = target;
				}
			}
		}else{
			int distance = 0;
			result = null;
			for(StandardEntity target : targetBuildings){
				int dist_temp = model.getDistance(getLocation(), target);
				if(dist_temp > distance){
					distance = dist_temp;
					result = target;
				}
			}
		}
		logger.debug("result = " + result);
		return result;
	}
*/
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}
/*
    private boolean someoneOnBoard() {
        for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            if (((Human)next).getPosition().equals(getID())) {
                Logger.debug(next + " is on board");
                return true;
            }
        }
        return false;
    }
*/
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
