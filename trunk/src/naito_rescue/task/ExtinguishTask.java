package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.*;
import java.io.*;

import static naito_rescue.debug.DebugUtil.*;

public class ExtinguishTask extends Task
{
	Building target;
	
	public ExtinguishTask(NAITOFireBrigade owner, Building target){
		super(owner);
		this.target = target;
	}
	
	public Building getTarget(){
		return target;
	}
	@Override
	public List<Job> createJobList(){
		ArrayList<Job> list = new ArrayList<Job>();
		list.add(new MoveToExtinguishPointJob((NAITOFireBrigade)owner, target));
		list.add(new ExtinguishJob((NAITOFireBrigade)owner, target));
		return list;
	}
	
	@Override
	public void updatePriority(){
		// int distance = model.getDistance(owner.getLocation(), target);
		priority = 3000;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("ExtinguishTask(" + target.getID().getValue() + "," + priority + "," + isFinished() + ")");
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object other){
		logger.info("[[[ ExtinguishTask.equals(); ]]]");
		if(!(other instanceof ExtinguishTask)){
			logger.info("Other is not instanceof ExtinguishTask. return false;");
			logger.info("[[[ ExtinguishTask.equals(); end. ]]]");
			return false;
		}
		ExtinguishTask otherTask = (ExtinguishTask)other;
		if(this.target.getID().getValue() != otherTask.getTarget().getID().getValue()){
			logger.info("Other ExtinguishTask is not equal target. return false;");
			logger.info("[[[ ExtinguishTask.equals(); ]]]");
			return false;
		}
		logger.info("return true;");
		logger.info("[[[ ExtinguishTask.equals(); ]]]");
		return true;
	}
}
