package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import naito_rescue.router.*;
import naito_rescue.object.*;
import java.util.*;
import java.io.*;


public class ClearPathTask extends Task
{
	NAITOArea goal;
	PassableChecker checker;
	public ClearPathTask(NAITOHumanoidAgent owner, NAITOArea goal){
		super(owner);
		this.goal = goal;
		
		this.checker = owner.getPassableChecker();
	}
	
	@Override
	public List<Job> createJobList(){
		List<Job> list = new ArrayList<Job>();
		//logger.info("_|_|_|_ ClearPathTask.createJobList(); _|_|_|_");

		Area location = (Area)(owner.getLocation());
		NAITOArea nLocation = (NAITOArea)(owner.allNAITOAreas.get(location.getID()));
		//NAITOArea nLocation = owner.getNAITOAreaMap().get(location.getID());
		if(nLocation != null){
			//logger.info("Add ClearJob(" + location + ")");
			list.add(new ClearJob(owner, nLocation));
		}else{
			//logger.info("Invalid NAITOArea, Agent's location = " + location);
			//logger.info("_|_|_|_ ClearPathTask.createJobList(); end. _|_|_|_");
			return new ArrayList<Job>();
		}
		
		Area target = goal.getStandardArea();
		List<EntityID> path = owner.getSearch().getRoute(target);
		//エージェントが今いる場所から次の場所へ移動できない場合，
		//まずそこを啓開するジョブを追加しましょう
		
		if(path != null && !checker.isPassable(Arrays.asList(path.get(0)))){
			//logger.info("Crossed Blockade in location(" + location + ") to next (" + path.get(0) + ").");
			NAITOArea nArea = (NAITOArea)(owner.allNAITOAreas.get(path.get(0)));
			//logger.info("Add ClearJob(" + nArea.getID().getValue() + ") to cross location.");
			list.add(new ClearJob(owner, nArea));
		}else{
			//logger.info("Passable from location (" + location + ") to next (" + path.get(0) + ").");
		}
		
		
		//logger.info("Path From: " + location);
		//logger.info("Path To:   " + target);
		if(path == null){
			//logger.info("There is No Path From " + location + " : to " + target + " : .");
			//logger.info("_|_|_|_ ClearPathTask.createJobList(); end. _|_|_|_");
			return new ArrayList<Job>();
		}
		for(EntityID id : path){
			Area area = (Area)(model.getEntity(id));
			NAITOArea nArea = (NAITOArea)(owner.allNAITOAreas.get(id));
			
			//logger.info("Current Area ID = " + id + ", Add MoveJob and ClearJob...");
			//logger.debug("Add MoveJob(" + area + ")");
			//logger.debug("Add ClearJob(" + area + ")");
			list.add(new MoveJob(owner, area));
			list.add(new ClearJob(owner, nArea));
		}
		//logger.info("_|_|_|_ ClearPathTask.createJobList(); end. _|_|_|_");
		return list;
	}
	
	@Override
	public void updatePriority(){
		priority = 3000;
	}
	
	//Jobを返す必要がある度に経路を再計算するため，currentJobをオーバライドする
	@Override
	public Job currentJob(){
		//logger.info("8<8<8< ClearPathTask.currentJob(); 8<8<8<");
		jobList.clear();
		jobList.addAll(createJobList());
		
		//logger.info("Print Job List...");
		StringBuffer sb = new StringBuffer();
		for(Job j : jobList){
			sb.append(j + ", ");
		}
		//logger.info(sb.toString());
		for(Job j : jobList){
			//logger.trace(j.toString() + " is now checking...");
			if(!j.isFinished()){
				//logger.info(j + " is not finished. return;");
				//logger.info("8<8<8< ClearPathTask.currentJob(); end. 8<8<8<");
				return j;
			}
		}
		//logger.info("All Jobs has been finished. return null;");
		//logger.info("8<8<8< ClearPathTask.currentJob(); end. 8<8<8<");
		return null;
	}
	public NAITOArea getTarget(){
		return goal;
	}
	
	@Override
	public boolean equals(Object other){
		if(!(other instanceof ClearPathTask)){
			//logger.info("ClearPathTask.equals() => other is not ClearPathTask. return false; ");
			return false;
		}
		
		ClearPathTask otherTask = (ClearPathTask)other;
		if(this.goal.getID().getValue() != otherTask.getTarget().getID().getValue()){
		//logger.info("ClearPathTask.equals() => otherTask's target is not equal to this target. return false; ");
			return false;
		}
		//logger.info("ClearPathTask.equals() => return true;");
		return true;
	}
	
	@Override
	public String toString(){
		return "ClearPathTask(" + goal.getID().getValue() + "," + priority + "," + isFinished() + ")";
	}
}
