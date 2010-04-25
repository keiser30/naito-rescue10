package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class MoveJob extends Job
{
	Area target;

	public MoveJob(NAITOHumanoidAgent owner, StandardWorldModel world, Area target){
		super(owner, world);
		this.target = target;
	}
	
	public Area getTarget(){ return target; }
	
	@Override
	public void doJob(){
		owner.move(target);
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		int location_id = owner.getLocation().getID().getValue();
		int target_id = target.getID().getValue();

		if(location_id == target_id){
			return true;
		}else{
			return false;
		}
	}
}
