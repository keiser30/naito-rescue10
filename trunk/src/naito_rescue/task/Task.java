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
		logger.info("Task.isFinished();");
		logger.debug("jobs = " + jobs);
		for(Iterator<Job> it = jobs.iterator(); it.hasNext();){
			Job next = it.next();
			logger.debug(next + ".isFinished() = " + next.isFinished());
			if(next.isFinished()){
				//終了しているジョブを削除する
				logger.debug("remove(" + next + ");");
				it.remove(); //nextが削除される
			}else{
				return false;
			}
		}
		//パスがここに到達した時点で, jobs.isEmpty()は
		//保証されている.
		logger.info("======> return true;");
		return true;
	}
	protected abstract boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world);

	public Job currentJob(){
		logger.info("Task.currentJob();");
		if(jobs.isEmpty()){
			logger.debug("jobs.isEmpty()...");
			if(isCreateJobsNow){
				logger.debug("jobs.addAll(createJobList());");
				jobs.addAll(createJobList());
				logger.debug("|____ jobs = " + jobs);
				isCreateJobsNow = false;
				return jobs.get(0);
			}else{
				//ここにパスが到達するということは,
				//このタスクは終わっていなければならない.
				// --> 異常としてnullを返す
				logger.debug("jobs.isEmpty() && !isCreateJobsNow");
				logger.debug("なぜここにパスが通るし");
				return null;
			}
		}else{
			Job j = jobs.get(0);
			logger.debug("return " + j);
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
