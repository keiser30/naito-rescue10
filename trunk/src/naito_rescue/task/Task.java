package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import naito_rescue.*;
import java.util.*;
import java.io.*;

public abstract class Task implements Comparable<Task>
{
	protected NAITOHumanoidAgent owner;
	protected StandardWorldModel model;
	protected MyLogger           logger;
	protected int                priority = 0;
	protected List<Job>          jobList;
	
	public Task(NAITOHumanoidAgent owner){
		this.owner = owner;
		this.model = owner.getWorldModel();
		this.logger = owner.getLogger();
		this.jobList = new ArrayList<Job>();
		
		//updatePriority();
	}
	public Job currentJob(){
		logger.trace("*** currentJob(); ***");
		if(jobList.isEmpty()){
			jobList.addAll(createJobList());
		}
		logger.trace("Print Job List...");
		for(Job j : jobList){
			logger.trace(j.toString());
			if(!j.isFinished()){
				logger.trace(j.getClass().getName() + " is not finished. return;");
				return j;
			}
		}
		return null;
	}
	public boolean isFinished(){
		if(jobList.isEmpty()){
			jobList.addAll(createJobList());
		}
		for(Job j : jobList){
			if(!j.isFinished()){
				return false;
			}
		}
		return true;
	}
	public void setPriority(int p){
		this.priority = p;
	}
	public int getPriority(){
		return priority;
	}
	public abstract List<Job> createJobList();
	public abstract void      updatePriority();
	
	@Override
	public final int compareTo(Task otherTask){
		return otherTask.getPriority() - this.getPriority();
		//return this.getPriority() - otherTask.getPriority();
	} 
	
	@Override
	public boolean equals(Object other){
		logger.info("[[[ Task.equals(); ]]]");
		
		if(!(other instanceof Task)){
			logger.info("Other is not equals to Task. return false; ");
			logger.info("[[[ Task.equals(); end. ]]]");
			return false;
		}
		return this.equals(other);
	}
}
