package naito_rescue.agent;

import java.util.*;
import java.io.*;
import rescuecore2.worldmodel.*;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.*;

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

	private static int fblog_num = 0;
	private int distance = 0;
	private Building target;
	private Collection<StandardEntity> allArea;
	private HashSet<Building> visited;

	private File fblog;
	//private String LOGFILENAME = LOGFILE_BASE+"FB"+me().getID().getValue()+".log";
	private String LOGFILENAME = LOGFILE_BASE+"FB"+(++fblog_num)+".log";
	private PrintWriter writer;
	
	@Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        Logger.info("NAITOFireBrigade connected: max extinguish distance = " + maxDistance + ", max power = " + maxPower + ", max tank = " + maxWater);
    	
		visited = new HashSet<Building>();
		allArea = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		if(location() instanceof Building){
			visited.add((Building)location());
		}
		
		try{
			fblog = new File(LOGFILENAME);
			writer = new PrintWriter(fblog);
		}catch(IOException ioe){
			System.err.println("IOException: NAITOFireBrigade");
			System.exit(-1);
		}
	} 

	@Override
	public String toString(){
		return "NAITOFireBrigade: " + me().getID() + "";
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		writer.println("---- " + time + " ----");
		super.think(time,changed,heard);

		//TaskJobテストコード: MoveTask
		//自分から遠い建物について巡っていく．
		if(currentTask != null && !currentTask.isFinished()){
			writer.println("NAITOFireBrigade.think(){");
			writer.println("    currentTask != null && !currentTask.isFinished()");
			writer.println("    act();");
			writer.println("}");
			act();
		}
		for(StandardEntity building : allArea){
			int dist_temp = model.getDistance(location(), building);
			if(distance < dist_temp && !visited.contains((Building)building)){
				target = (Building)building;
				distance = dist_temp;
			}
		}
		currentTask = new MoveTask(this, model, target, writer);
		try{
			writer.println("NAITOFireBrigade.think(){");
			writer.println("    currentTask = new MoveTask();");
			writer.println("    target = " + target.toString());
		}catch(Exception ioe){
			System.err.println("Exception: NAITOFireBrigade, think();");
			writer.flush();
			writer.close();
			System.exit(-1);
		}
		visited.add(target);

		writer.println("    act();");
		writer.println("}");
		act();
	}

	//yabAPIのスタイルを踏襲
	protected void act(){
		writer.println("NAITOFireBrigade.act(){");
		if(!currentTask.isFinished()){
			writer.println("    !currentTask.isFinished();");
			Job currentJob = currentTask.currentJob();
			writer.println("    currentJob.doJob();");
			currentJob.doJob();
			writer.flush();
		}else{
			writer.println("    currentTask.isFinished()");
			writer.flush();
		}
		writer.println("}");
		writer.flush();
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
	}

}
