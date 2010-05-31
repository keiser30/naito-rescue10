package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class MoveToAnyRefugeJob extends Job
{
	List<Refuge> refuges;

	public MoveToAnyRefugeJob(NAITOHumanoidAgent owner, StandardWorldModel world, List<Refuge> refuges){
		super(owner, world);
		this.refuges = refuges;
	}
	
	
	@Override
	public void doJob(){
		try{
			List<EntityID> path = owner.getSearch().breadthFirstSearch(owner.getLocation(), refuges);
			if(path != null){
				owner.move(path);
			}else{
				logger.info("MoveToAnyRefugesJob().doJob() target (" + refuges + ") is not adjucent to " + owner.getLocation());
			}
		}catch(Exception e){
			logger.info("MoveToAnyRefugeMoveJob: " + e);
		}
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		logger.info("MoveToAyRefugeJob.isFinished();");
		StandardEntity location = owner.getLocation();

		if(illegal == true){
			logger.debug("There are something illegal.");
			return true;
		}
		logger.debug("location = " + location);
		if(location instanceof Refuge){
			return true;
		}else{
			return false;
		}
	}
}
