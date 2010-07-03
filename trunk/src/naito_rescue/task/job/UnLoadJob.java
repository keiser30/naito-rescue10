package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class UnLoadJob extends Job
{

	public UnLoadJob(NAITOHumanoidAgent owner, StandardWorldModel world){
		super(owner, world);
	}
	
	@Override
	public void doJob(){
		//if(owner.getLocation() instanceof Refuge){

		
		if(owner.someoneOnBoard() != null){
			owner.unload();
		}else{
			
			
			illegal = true;
		}
/*
			Collection<StandardEntity> civilians = world.getEntitiesOfType(StandardEntityURN.CIVILIAN);
			
			for(StandardEntity next : civilians){
				Civilian c = (Civilian)next;
				
				if(c.getPosition().equals(owner.getID())){
					
					
					owner.unload();
					return;
				}
			}
			
		}
		
*/
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		
		if(illegal){
			
			
			return true;
		}
		if(!(owner.getLocation() instanceof Refuge)){
			
			
			return false;
		}
		
		for(StandardEntity next : world.getEntitiesOfType(StandardEntityURN.CIVILIAN)){
			Civilian c = (Civilian)next;
			
			if(c.getPosition().equals(owner.getID())){
				
				
				return false;
			}
		}
		
		return true;
	}
}
