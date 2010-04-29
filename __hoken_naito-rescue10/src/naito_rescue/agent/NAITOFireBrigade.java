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
        Logger.info("NAITOFireBrigade connected: max extinguish distance = " + maxDistance + ", max power = " + maxPower + ", max tank = " + maxWater);

	//	allBuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		if(location() instanceof Building){
			visited.add((Building)location());
		}
	}

	@Override
	public String toString(){
		return "NAITOFireBrigade." + me().getID() + "";
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to channel 1
            sendSubscribe(time, 1);
        }
        for (Command next : heard) {
            Logger.debug("Heard " + next);
        }
        FireBrigade me = me();
        // Are we currently filling with water?
        if (me.isWaterDefined() && me.getWater() < maxWater && location() instanceof Refuge) {
            Logger.info("Filling with water at " + location());
            sendRest(time);
            return;
        }
        // Are we out of water?
        if (me.isWaterDefined() && me.getWater() == 0) {
            // Head for a refuge
            List<EntityID> path = search.breadthFirstSearch(location(), getRefuges());
            if (path != null) {
                Logger.info("Moving to refuge");
                sendMove(time, path);
                return;
            }
            else {
                Logger.debug("Couldn't plan a path to a refuge.");
                path = randomWalk();
                Logger.info("Moving randomly");
                sendMove(time, path);
                return;
            }
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
                Logger.info("Moving to target");
                sendMove(time, path);
                return;
            }
        }
        List<EntityID> path = null;
        Logger.debug("Couldn't plan a path to a fire.");
        path = randomWalk();
        Logger.info("Moving randomly");
        sendMove(time, path);
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
