package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.ArrayList;

import java.io.*;

public abstract class Task
{
	NAITOHumanoidAgent owner;
	StandardWorldModel world;
	PrintWriter        logger;
	ArrayList<Job>     jobs;
	int                processIdx;
	int                rank;
	int                tryCount;

	public Task(NAITOHumanoidAgent owner, StandardWorldModel world){
		this.owner = owner;
		this.world = world;
		jobs = new ArrayList<Job>();
		processIdx = 0;
		rank = 1000; //ランクのデフォルト値は1000
		tryCount = 0;
	}

	// 各タスクで実装
	//   |_ currentJob()で呼び出し
	public abstract ArrayList<Job> createJobList();
	
	public boolean isFinished(){
		if(jobs.size() > 0  && processIdx >= jobs.size()){
			return true;
		}
		else return isFinished(owner, world);
	}
	protected abstract boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world);

	public Job currentJob(){
		if(jobs.size() == 0){
			jobs.addAll(createJobList());
		}
		while(processIdx < jobs.size()){
			if(!jobs.get(processIdx).isFinished()){
				return jobs.get(processIdx);
			}
			processIdx++;
		}
		return null;
	}

	public void setRank(int rank){
		this.rank = rank;
	}
	public int getRank(){
		return this.rank;
	}
	public void reset(){
		this.processIdx = 0;
	}

	public String toString(){
		return this.getClass().toString();
	}
}
