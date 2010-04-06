package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

/**
 * 道路啓開Job
 */
public class ClearJob extends Job
{
	Blockage target;

	@Override
	public void doJob(){
		owner.clear(target.getID());
	}

	@Override 
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
	}
}
