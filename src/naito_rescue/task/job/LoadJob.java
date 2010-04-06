package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class LoadJob extends Job
{
	Civilian target;

	@Override 
	public void doJob(){
		owner.load(target.getID());
	}

	@Override 
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
	}
}
