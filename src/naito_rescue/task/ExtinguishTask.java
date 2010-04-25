package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.ArrayList;

import java.io.*;

public class ExtinguishTask extends Task
{
	//移動先
	Building target;
	int power;
	int distance;

	public ExtinguishTask(NAITOHumanoidAgent owner, StandardWorldModel world, Building target, int power, int distance){
		super(owner, world);
		this.target = target;
		this.power = power;
		this.distance = distance;
	}

	@Override
	public ArrayList<Job> createJobList(){
		ArrayList<Job> jobs = new ArrayList<Job>();
		jobs.add(new MoveToExtinguishPointJob(owner, world, target, distance));
		jobs.add(new ExtinguishJob(owner, world, target, power));
		return jobs;
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		return !target.isOnFire();
	}
}
