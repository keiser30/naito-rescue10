package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

//RescueJobの次に呼ばれる...
//   |___ buriedness() == 0な市民がいるはず
public class LoadJob extends Job
{
	Building target;
	Civilian civilian;

	public LoadJob(NAITOHumanoidAgent owner, StandardWorldModel world, Building target){
		super(owner, world);
		this.target = target;
		civilian = null;
	}
	@Override 
	public void doJob(){
		
		if(civilian != null){
			
			
			return;
		}
		//Collection<StandardEntity> civilians = world.getEntitiesOfType(StandardEntityURN.CIVILIAN);
		List<Civilian> civilians = owner.getViewCivilians();
		
		if(civilians.isEmpty()){
			//視界範囲に市民エージェントがいない
			
			
			illegal = true;
			return;
		}
		//for(StandardEntity next : civilians){
		for(Civilian next : civilians){
			//Civilian c = (Civilian)next;
			Civilian c = next;
			
			if(c.getBuriedness() <= 0){
				
				
				civilian = c;
				owner.load(c.getID());
				return;
			}
		}
		//パスがここにたどり着いたとき...
		//   |___ buriednes() == 0な市民がいなかった
		
		
		illegal = true;
	}

	@Override 
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		
		if(illegal == true){
			
			return true;
		}
		if(civilian == null){
			
			
			return false;
		}else{
			
			
			return true;
		}
/*
		StandardEntity location = owner.getLocation();
		
		for(StandardEntity next : world.getEntitiesOfType(StandardEntityURN.CIVILIAN)){
			Civilian c = (Civilian)next;
			
			if(c.getPosition().equals(owner.getID())){
				
				
				return true;
			}
		}
		
		
		return false;
*/
	}
}
