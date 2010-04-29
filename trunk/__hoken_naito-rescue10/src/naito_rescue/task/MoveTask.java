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

	public MoveTask(NAITOHumanoidAgent owner, StandardWorldModel world, Area target){
		super(owner, world);
		this.target = target;
	}

	@Override
	public ArrayList<Job> createJobList(){
		ArrayList<Job> jobs = new ArrayList<Job>();
		jobs.add(new MoveJob(owner, world, target));
		return jobs;
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		int location_id = owner.getLocation().getID().getValue();
		int target_id = target.getID().getValue();
		if(location_id == target_id){
			return true;
		}else{
			return false;
		}
	}
	public Area getTarget(){
		return target;
	}
}
