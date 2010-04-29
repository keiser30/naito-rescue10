package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class RescueJob extends Job
{
	Civilian target;

	public RescueJob(NAITOHumanoidAgent owner, StandardWorldModel world, Civilian target){
		super(owner, world);
		this.target = target;
	}

	@Override
	public void doJob()
	{
		owner.rescue(target.getID());
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		return false;
	}
}
