package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.ArrayList;

import java.io.*;

public abstract class Task
{
	//Human -> StandardAgentに変更.
	//sendMove()とか使いたいときのために
	NAITOHumanoidAgent owner;
	StandardWorldModel world;
	PrintWriter        logger;
	ArrayList<Job>     jobs;
	int                processIdx;

	public Task(NAITOHumanoidAgent owner, StandardWorldModel world, PrintWriter logger){
		this.owner = owner;
		this.world = world;
		this.logger = logger;
		jobs = new ArrayList<Job>();
		processIdx = 0;
		
		try{
			logger.println("\t\t Task() is constructed;");
		}catch(Exception e){
			logger.flush();
			logger.close();
		}
	}

	// 各タスクで実装
	//   |_ currentJob()で呼び出し
	public abstract ArrayList<Job> createJobList();

	public boolean isFinished(){
		if(jobs.size() > 0  && processIdx+1 > jobs.size()){
			logger.println("\t\t Task.isFinished(){");
			logger.println("\t\t     processIdx+1 > jobs.size()");
			logger.println("\t\t     (processIdx=" + processIdx + ",jobs.size()=" + jobs.size() + ")");
			logger.println("\t\t }");
			return true;
		}
		else return isFinished(owner, world);
	}
	protected abstract boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world);

	public Job currentJob(){
		logger.println("\t\t Task.currentJob(){");
		if(jobs.size() == 0){
			try{
				logger.println("\t\t     jobs.size() == 0;");
				logger.println("\t\t     jobs.addAll(createJobList());");
			}catch(Exception e){
				logger.println("Exception at currentJob();");
				logger.flush();
				logger.close();
				System.exit(-1);
			}
			jobs.addAll(createJobList());
		}
		while(processIdx+1 <= jobs.size()){
			if(!jobs.get(processIdx).isFinished()){
				try{
					logger.println("\t\t     !jobs.get(" + processIdx + ").isFinished();");
				}catch(Exception e){
					logger.println("IOException at currentJob(){ while(...)...} ");
					logger.flush();
					logger.close();
					System.exit(-1);
				}
				logger.println("\t\t     return jobs.get(" + processIdx + ")");
				logger.println("\t\t }");
				return jobs.get(processIdx);
			}
			processIdx++;
		}
		return null;
	}
}
