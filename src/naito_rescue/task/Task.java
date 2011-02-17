package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.*;
import java.io.*;

public abstract class Task
{
	protected NAITOHumanoidAgent owner;
	protected StandardWorldModel model;
	protected int                priority;
	protected List<Job>          jobList;
	
	public Task(NAITOHumanoidAgent owner){
		this.owner = owner;
		this.model = owner.getWorldModel();
		this.jobList = new ArrayList<Job>();
	}
	public Job currentJob(){
		if(jobList.isEmpty()){
			jobList.addAll(createJobList());
		}
		for(Job j : jobList){
			if(!j.isFinished()) return j;
		}
		return null;
	}
	public boolean isFinished(){
		if(jobList.isEmpty()){
			jobList.addAll(createJobList());
		}
		for(Job j : jobList){
			if(!j.isFinished()) return false;
		}
		return true;
	}
	public abstract List<Job> createJobList();
	public abstract void      updatePriority();
	
}
