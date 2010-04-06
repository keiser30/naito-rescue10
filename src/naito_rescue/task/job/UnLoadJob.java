package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class UnLoadJob extends Job
{

	public UnLoadJob(NAITOHumanoidAgent owner, StandardWorldModel world, PrintWriter logger){
		super(owner, world, logger);
	}
	
	@Override
	public void doJob(){
		owner.unload();
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		return false;
	}
}
