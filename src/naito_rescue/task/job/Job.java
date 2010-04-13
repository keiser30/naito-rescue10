package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;

import naito_rescue.agent.*;

import java.io.*;

public abstract class Job
{
	// Ownerを持たせる
	// ...WorldModelは別に持たせる必要がある?
	NAITOHumanoidAgent owner;
	StandardWorldModel world;
	PrintWriter        logger;
	
	public Job(NAITOHumanoidAgent owner, StandardWorldModel world, PrintWriter logger){
		this.owner = owner;
		this.world = world;
		this.logger = logger;
	}
	public StandardAgent getOwner(){
		return owner;
	}
	public boolean isFinished(){
		return isFinished(owner, world);
	}

	protected abstract boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world);
	public abstract void doJob();

}