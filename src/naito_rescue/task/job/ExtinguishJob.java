package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class ExtinguishJob extends Job
{
	Building target;
	int      power;
	int      distance;

	public ExtinguishJob(NAITOHumanoidAgent owner, StandardWorldModel model, Building target, int power, int distance){
		super(owner, model);
		this.target = target;
		this.power = power;
		this.distance = distance;
	}

	@Override
	public void doJob(){
		if(world.getDistance(owner.getLocation().getID(), target.getID()) < distance)
			owner.extinguish(target.getID(), power);
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		return !target.isOnFire();	
	}
}
