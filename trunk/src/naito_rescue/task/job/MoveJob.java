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

	public MoveJob(NAITOHumanoidAgent owner, StandardWorldModel world, Area target, PrintWriter logger){
		super(owner, world, logger);
		this.target = target;
	}
	
	public Area getTarget(){ return target; }
	
	@Override
	public void doJob(){
		owner.move(target);
		logger.println("\t\t\t MoveJob.doJob(){ owner.move(" + target.toString() + ") }");
		logger.flush();
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		try{
			logger.println("\t\t\t MoveJob.isFinished(){");
		}catch(Exception ioe){
			logger.println("Exception at MoveJob.isFinished();");
		}
		int location_id = owner.getLocation().getID().getValue();
		int target_id = target.getID().getValue();

		if(location_id == target_id){
			try{
				logger.println("\t\t\t    |_ return true;");
				logger.println("\t\t\t }");
				logger.flush();
			}catch(Exception ioe){
				logger.println("Exception at MoveJob.isFinished(){ if(...) }");
				logger.flush();
				logger.close();
				System.exit(-1);
			}
			return true;
		}else{
			try{
				logger.println("\t\t\t    |_ return false;");
				logger.println("\t\t\t }");
				logger.flush();
			}catch(Exception ioe){
				logger.println("Exception at MoveJob.isFinished(){ if(...) }");
				logger.flush();
				logger.close();
				System.exit(-1);			
			}
			return false;
		}
	}
}
