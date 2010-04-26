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
	private Collection<StandardEntity> allArea;
	private HashSet<Building> visited;

	@Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        Logger.info("NAITOFireBrigade connected: max extinguish distance = " + maxDistance + ", max power = " + maxPower + ", max tank = " + maxWater);

/*
		visited = new HashSet<Building>();
		allArea = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		if(location() instanceof Building){
			visited.add((Building)location());
		}
		
*/
	} 

	@Override
	public String toString(){
		return "NAITOFireBrigade: " + me().getID() + "";
	}

// 建物探訪をどうするか?
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time,changed,heard);

		// 無線 or ボイスデータの処理
		if(heard.size() > 0){
			
		}
		List<Building> burnings = getBurningBuildings();
		for(Building next : burnings){
			currentTaskList.add(new ExtinguishTask(this, model, next, maxPower, maxDistance));
		}
		
		taskRankUpdate();
		currentTask = getHighestRankTask();
		currentJob = currentTask.currentJob();
		currentJob.doJob();
	}

	public void taskRankUpdate(){
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

}
