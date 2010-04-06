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

	public ExtinguishJob(NAITOHumanoidAgent owner, StandardWorldModel model, Building target, int power, PrintWriter logger){
		super(owner, model, logger);
		this.target = target;
		this.power = power;
	}
	@Override
	public void doJob(){
		owner.extinguish(target.getID(), power);
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		return false;
	}
}
