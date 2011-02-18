package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.*;
import java.io.*;

public class MoveToExtinguishPointJob extends Job
{
	Building target;
	
	public MoveToExtinguishPointJob(NAITOFireBrigade owner, Building target){
		super(owner);
		this.target = target;
	}
	
	@Override
	public boolean isFinished(){
		int distance = model.getDistance(owner.getLocation(), target);
		return distance < owner.maxExtinguishDistance;
	}
	
	@Override
	public void act(){
		owner.move(target);
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("\t MoveToExtinguishPointJob:\n");
		sb.append("\t\t Target = " + target + "\n");
		sb.append("\t\t Distance from owner = " + model.getDistance(owner.getLocation(), target));
		sb.append("\t\t (maxDistance = " + owner.maxExtinguishDistance + ")\n");
		sb.append("\t => isFinished? " + isFinished());
		
		return sb.toString();
	}
}
