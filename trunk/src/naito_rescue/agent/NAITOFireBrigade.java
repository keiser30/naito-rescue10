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
		super.think(time,changed,heard);

		logger.info("********** " + time + " **********");
		logger.info("NAITOFireBrigade.think();");
		if (time <= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
			rest();
			return;
		}

		// 無線 or ボイスデータの処理
		if(heard.size() > 0){
			
		}

		FireBrigade me = me();
		if(me.isWaterDefined() && me.getWater() == 0){
			List<Refuge> refuges = getRefuges();
			Refuge refuge = refuges.get(0); //最短の避難所にいくようにしたい
			currentTask = new MoveTask(this, model, (Area)refuge);
			currentJob = currentTask.currentJob();
			currentJob.doJob();
			return;
		}

		//建物探訪
		for(StandardEntity building : allBuildings){
			if(building == null){
				logger.info("******************** building is null!");
			}else if(visited == null){
				logger.info("#################### visited is null!");
			}
			if(!(visited.contains(building))){
				logger.debug("建物探訪: building = " + building);
				currentTaskList.add(new MoveTask(this, model, (Area)building));
			}
		}

		//燃えている建物全部に対して消火タスクを設定する
		List<Building> burnings = getBurningBuildings();
		logger.debug("getBurningBuildings().size = " + burnings.size());
		for(Building next : burnings){
			currentTaskList.add(new ExtinguishTask(this, model, next, maxPower, maxDistance));
		}
		
		taskRankUpdate();
		currentTask = getHighestRankTask();
		currentJob = currentTask.currentJob();

		logger.debug("currentTask = " + currentTask.toString());
		logger.debug("currentJob = " + currentJob.toString());
		currentJob.doJob();
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

}
