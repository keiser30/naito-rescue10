package naito_rescue.agent;

import java.util.*;
import java.io.*;
import rescuecore2.worldmodel.*;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;

import naito_rescue.task.*;
import naito_rescue.task.job.*;

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
           		 // Subscribe to channel 2
				logger.info("sendSubscribe(1): " + time);
           	 	sendSubscribe(time, 1);
			}else{
				return;
			}
        }

//		logger.info("NAITOFireBrigade.hearing...");
//		for(Command next : heard){
//			logger.debug("heard->next = " + next);
//			if(next instanceof AKSpeak){
//				/**
//				*  無線or声データの処理
//				*/
//				AKSpeak speak = (AKSpeak)next;
//				
//			}
//		}
/*
        FireBrigade me = me();
        // Are we currently filling with water?
        if (me.isWaterDefined() && me.getWater() < maxWater && location() instanceof Refuge) {
            logger.info("Filling with water at " + location());
            sendRest(time);
            return;
        }
        // Are we out of water?
        if (me.isWaterDefined() && me.getWater() == 0) {
            // Head for a refuge
            List<EntityID> path = search.breadthFirstSearch(location(), getRefuges());
            if (path != null) {
                logger.info("Moving to refuge");
                //sendMove(time, path);
				move(path);
                return;
            }
            else {
                logger.debug("Couldn't plan a path to a refuge.");
                path = randomWalk();
                logger.info("Moving randomly");
                //sendMove(time, path);
				move(path);
                return;
            }
        }
		//とにかく近いところから，初期出火から消していく.
		Building target = null;
		Collection<Building> burningBuildings = getBurningBuildings();
		int distance = Integer.MAX_VALUE;
		if(!(burningBuildings == null)){
			logger.debug("Search target building...");
			for(Building b : burningBuildings){
				logger.debug("ターゲット候補:" + b);
				int dist_temp = model.getDistance(getLocation(), b);
				if(distance > dist_temp){
					target = b;
					distance = dist_temp;
				}
			}
			if(target != null){
				logger.debug("ターゲット決定: " + target);
				currentTask = new ExtinguishTask(this, model, target, maxPower, maxDistance);
				currentJob = currentTask.currentJob();
				currentJob.doJob();
				logger.debug("ExtinguishTask.currentJob.doJob();");
			}
		}else{
		}
        // Find all buildings that are on fire
        Collection<Building> all = getBurningBuildings();
        // Can we extinguish any right now?
        for (Building next : all) {
            if (model.getDistance(me, next) <= maxDistance) {
                Logger.info("Extinguishing " + next);
                sendExtinguish(time, next.getID(), maxPower);
                sendSpeak(time, 1, ("Extinguishing " + next.getID()).getBytes());
                return;
            }
        }
        // Plan a path to a fire
        for (Building next : all) {
            List<EntityID> path = planPathToFire(next);
            if (path != null) {
                logger.info("Moving to target. path = " + path);
                //sendMove(time, path);
				move(path);
                return;
            }
        }
        List<EntityID> path = null;
        logger.debug("Couldn't plan a path to a fire.");
        path = randomWalk();
		logger.info("Moving randomly");
        Logger.info("Moving randomly");
        //sendMove(time, path);
		move(path);
*/
	}

	public void taskRankUpdate(){
		super.taskRankUpdate();	
	}
	private List<Building> getBurningBuildings(){
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
