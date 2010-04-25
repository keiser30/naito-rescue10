package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class MoveToExtinguishPointJob extends Job
{
	Area target;
	int  maxDistance;

	public MoveToExtinguishPointJob(NAITOHumanoidAgent owner, StandardWorldModel world, Area target, int distance){
		super(owner, world);
		this.target = target;
		this.maxDistance = distance;
	}
	
	public Area getTarget(){ return target; }
	
	@Override
	public void doJob(){
		owner.move(target);
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		return world.getDistance(owner.getLocation(), target) < maxDistance;
	}
}
