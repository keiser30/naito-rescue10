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
	
	public MoveJob(NAITOHumanoidAgent owner, Area target){
		super(owner);
		this.target = target;
	}
	
	@Override
	public boolean isFinished(){
		Area position = (Area)(owner.getLocation());
		return position.getID().getValue() == target.getID().getValue();
	}
	
	@Override
	public void act(){
		owner.move(target);
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("MoveJob(" + target.getID().getValue() + "," + isFinished() + ")");
		return sb.toString();
	}
	public Area getTarget(){
		return target;
	}
}

