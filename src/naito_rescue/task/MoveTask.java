package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.ArrayList;

import java.io.*;

public class MoveTask extends Task
{
	//移動先
	Area target;

	public MoveTask(NAITOHumanoidAgent owner, StandardWorldModel world, Area target, PrintWriter logger){
		
		super(owner, world, logger);
		logger.println("\t MoveTask() is constructed;");
		this.target = target;
	}

	@Override
	public ArrayList<Job> createJobList(){
		ArrayList<Job> jobs = new ArrayList<Job>();
		jobs.add(new MoveJob(owner, world, target, logger));
		return jobs;
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		int location_id = owner.getLocation().getID().getValue();
		int target_id = target.getID().getValue();
		logger.println("\t MoveTask.isFinished{");
		logger.println("\t     location_id = " + location_id);
		logger.println("\t     target_id = " + target_id);
		//logger.println("\t }");
		if(location_id == target_id){
			logger.println("\t     |_ return true;");
			logger.println("\t }");
			return true;
		}else{
			logger.println("\t     |_ return false;");
			logger.println("\t }");
			return false;
		}
	}
}
