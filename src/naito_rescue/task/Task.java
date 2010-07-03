package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.*;

import java.io.*;
import naito_rescue.*;
import naito_rescue.agent.*;

public abstract class Task
{
	NAITOHumanoidAgent owner;
	StandardWorldModel world;
	MyLogger           logger;
	ArrayList<Job>     jobs;
	boolean            illegal = false;
	boolean            isCreateJobsNow = false;
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
		this.logger = owner.getLogger();
		isCreateJobsNow = true;
	}

	// 各タスクで実装
	//   |____ currentJobで(1回だけ)呼び出し
	public abstract ArrayList<Job> createJobList();
	
	public boolean isFinished(){
		
		
		for(Iterator<Job> it = jobs.iterator(); it.hasNext();){
			Job next = it.next();
			
			if(next.isFinished()){
				//終了しているジョブを削除する
				
				it.remove(); //nextが削除される
			}else{
				return false;
			}
		}
		//パスがここに到達した時点で, jobs.isEmpty()は
		//保証されている.
		
		return true;
	}
	protected abstract boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world);

	public Job currentJob(){
		
		if(jobs.isEmpty()){
			
			if(isCreateJobsNow){
				
				jobs.addAll(createJobList());
				
				isCreateJobsNow = false;
				return jobs.get(0);
			}else{
				//ここにパスが到達するということは,
				//このタスクは終わっていなければならない.
				// --> 異常としてnullを返す
				
				
				return null;
			}
		}else{
			Job j = jobs.get(0);
			
			return j;
		}
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
	public List<Job> getJobs(){
		return jobs;
	}
	public String toString(){
		return this.getClass().toString();
	}
}
