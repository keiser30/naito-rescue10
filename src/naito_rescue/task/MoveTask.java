package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import naito_rescue.router.*;
import java.util.*;
import java.io.*;

public class MoveTask extends Task
{
	Area target;
	public MoveTask(NAITOHumanoidAgent owner, Area target){
		super(owner);
		this.target = target;
		this.priority = 1500;
	}
	public Area getTarget(){
		return target;
	}
	@Override
	public List<Job> createJobList(){
		ArrayList<Job> list = new ArrayList<Job>();
		list.add(new MoveJob(owner, target));
		return list;
	}
	
	@Override
	public void updatePriority(){
		//0 to 2500
		//logger.info("=== MoveTask.updatePriority(); ===");
		//int distance = model.getDistance(owner.getLocation(), target);
		//logger.debug("Distance to target = " + distance);
		int p = 0;
		PassableChecker checker = owner.getPassableChecker();
		List<EntityID> path = owner.getSearch().getRoute(target);
		if(path != null){
			if(!checker.isPassable(path)){
				p = -100;
			}else{
				p = 100;
			}
		}
		priority += p;
		if(priority < 1000) priority = 1000;
		else if(priority > 1500) priority = 1500;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("MoveTask(" + target.getID().getValue() + "," + priority + "," + isFinished() + ")");
		return sb.toString();
	}
	@Override
	public boolean equals(Object other){
		logger.info("[[[ MoveTask.equals(); ]]]");
		if(!(other instanceof MoveTask)){
			logger.info("Other is not equal MoveTask. return false; ");
			logger.info("[[[ MoveTask.equals(); end. ]]]");
			return false;
		}
		MoveTask otherTask = (MoveTask)other;
		if(this.target.getID().getValue() != otherTask.getTarget().getID().getValue()){
			logger.info("Other MoveTask is not equal target. return false; ");
			logger.info("[[[ MoveTask.equals(); end. ]]]");
			return false;
		}
		logger.info("return true; ");
		logger.info("[[[ MoveTask.equals(); end. ]]]");
		return true;
	}
}
