package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.*;
import java.io.*;

public class ExtinguishJob extends Job
{
	Building target;
	public ExtinguishJob(NAITOFireBrigade owner, Building target){
		super(owner);
		this.target = target;
	}
	
	@Override
	public boolean isFinished(){
		return !target.isOnFire();
	}
	
	@Override
	public void act(){
		owner.extinguish(target.getID());
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("ExtinguishJob(" + target.getID().getValue() + "," + isFinished() + ")");
		return sb.toString();
	}
}
