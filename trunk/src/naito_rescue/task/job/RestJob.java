package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class RestJob extends Job
{

	public RestJob(NAITOHumanoidAgent owner, StandardWorldModel model){
		super(owner, model);
	}
	@Override
	public void doJob(){
		owner.rest();
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		return false;
	}
}
