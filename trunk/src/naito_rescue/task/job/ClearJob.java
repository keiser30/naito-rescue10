package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.misc.geometry.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;
import naito_rescue.object.*;
import java.util.*;
import java.io.*;

public class ClearJob extends Job
{
	NAITOArea target;
	int       repairDistance;
	
	public ClearJob(NAITOHumanoidAgent owner, NAITOArea target){
		super(owner);
		this.target = target;
		this.repairDistance = owner.getMaxRepairDistance();
	}
	
	@Override
	public boolean isFinished(){
		//logger.info("*+*+*+ ClearJob.isFinished(); *+*+*+");
		//logger.info("Target = " + target.getStandardArea());
		
		//そのエリアにまだ訪れたことがなければ問答無用でfalse ... とりあえず行ってみなきゃわからない
		if(!target.hasVisited()){
			//logger.info(target.getStandardArea() + " has not visited. return false;");
			//logger.info("*+*+*+ ClearJob.isFinished(); end. *+*+*+");
			return false;
		}
		if(!target.isBlockadesDefined()){
			//logger.info(target.getStandardArea() + " is not defined Blockade. return true;");
			//logger.info("*+*+*+ ClearJob.isFinished(); end. *+*+*+");
			return true;
		}
		if(target.getBlockades() != null && !(target.getBlockades().isEmpty())){
			//logger.info("Get Blockades in " + target.getStandardArea() + " is not null. return false;");
			
			int x = owner.getX();
			int y = owner.getY();
			for(EntityID bID : target.getBlockades()){
				Blockade blockade = (Blockade)(model.getEntity(bID));
				//logger.debug("Blockade = " + bID + ", distance = " + owner.findDistanceTo(blockade, x, y) + " (repairDistance = " + repairDistance + ")");
			}
			//logger.info("*+*+*+ ClearJob.isFinished(); end. *+*+*+");
			return false;
		}else{
			//logger.info("Get Blockades in " + target.getStandardArea() + " is null. return true;");
			//logger.info("*+*+*+ ClearJob.isFinished(); end. *+*+*+");
			return true;
		}
	}
	
	@Override
	public void act(){
		//logger.info("**** ClearJob.act(); ****");
		List<Blockade> blockades = new ArrayList<Blockade>();
		if(target.getBlockades() != null){
			for(EntityID bID : target.getBlockades()){
				Blockade b = (Blockade)(model.getEntity(bID));
				//logger.info("Blockade (" + b + ") added to list.");
				blockades.add(b);
			}
		}else{
			//logger.info("There is No blockade in [" + target + "].");
			//logger.info("**** ClearJob.act(); end. ****");
			return;
		}
		int x = owner.getX();
		int y = owner.getY();
		for(Blockade blockade : blockades){
			int distance = owner.findDistanceTo(blockade, x, y);
			//logger.debug("owner.findDistanceTo(" + blockade + ", " + x + ", " + y + ") = " + distance);
			//logger.debug("repairDistance = " + repairDistance);
			if(distance < repairDistance){
				//logger.info("Blockade (" + blockade + ") is in repairDistance.CLEAR.");
				owner.clear(blockade.getID());
				//logger.info("**** ClearJob.act(); end. ****");
				return;
			}
		}
		//logger.info("There is No Blockade in repairDistance. (All Blockades are out of distance.)");
		//閉塞まで移動しましょう．
		//logger.info("Move to far Blockade.");
		
		int minDistance = Integer.MAX_VALUE;
		Blockade targetBlockade = null;
		for(Blockade blockade : blockades){
			int distance = owner.findDistanceTo(blockade, x, y);
			if(distance < minDistance){
				targetBlockade = blockade;
				minDistance = distance;
			}
		}
		if(targetBlockade != null){
			int targetX = targetBlockade.getX();
			int targetY = targetBlockade.getY();
			
			//logger.info("Target Blockade = " + targetBlockade);
			//logger.debug("Target  Area = " + target);
			//logger.debug("Current Area = " + owner.getLocation());
			//logger.info("Move to (" + targetX + ", " + targetY + ").");
			owner.move(target.getStandardArea(), targetX, targetY);
		}
		//logger.info("**** ClearJob.act(); end. ****");
	}
	
	@Override
	public String toString(){
		return "ClearJob(" + target.getID().getValue() + ", " + isFinished() + ")";
	}
}
