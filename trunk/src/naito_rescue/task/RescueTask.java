package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.ArrayList;

import java.io.*;

public class RescueTask extends Task
{
	//移動先
	Building target;

	public RescueTask(NAITOHumanoidAgent owner, StandardWorldModel world, Building target){
		super(owner, world);
		this.target = target;
	}

	@Override
	public ArrayList<Job> createJobList(){
		ArrayList<Job> jobs = new ArrayList<Job>();
		
		//ターゲットとなる建物へ移動
		jobs.add(new MoveJob(owner, world, target));
		//市民をRescueする
		jobs.add(new RescueJob(owner, world, target));
		//市民をLoadする
		jobs.add(new LoadJob(owner, world, target));
		//避難所へ移動
		jobs.add(new MoveToAnyRefugeJob(owner, world));
		//市民をUnloadする
		jobs.add(new UnLoadJob(owner, world));
		return jobs;
	}
	public Building getTarget(){
		return target;
	}
	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		return ;
	}
}
