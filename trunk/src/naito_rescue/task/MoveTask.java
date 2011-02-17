package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.*;
import java.io.*;

public class MoveTask extends Task
{
	Area target;
	public MoveTask(NAITOHumanoidAgent owner, Area target){
		super(owner);
		this.target = target;
	}
	
	@Override
	public List<Job> createJobList(){
		ArrayList<Job> list = new ArrayList<Job>();
		list.add(new MoveJob(owner, target));
		return list;
	}
	
	@Override
	public void updatePriority(){
	}
}
