package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;

import naito_rescue.agent.*;

import java.io.*;

public abstract class Job
{
	NAITOHumanoidAgent owner;
	StandardWorldModel world;
	
	public Job(NAITOHumanoidAgent owner, StandardWorldModel world){
		this.owner = owner;
		this.world = world;
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
