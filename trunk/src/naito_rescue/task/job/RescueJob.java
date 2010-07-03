package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class RescueJob extends Job
{

	Building target;
	Human    civilian;

	public RescueJob(NAITOHumanoidAgent owner, StandardWorldModel world, Building target){
		super(owner, world);
		this.target = target;
		this.civilian = null;
	}

	@Override
	public void doJob()
	{
		
		if(owner.getLocation().getID().getValue() != target.getID().getValue()){
			//本来ここにパスが来ることはありえないが...
			
			
			
			
			owner.move(target);
			return;
		}
		if(civilian != null){
			
			
			owner.rescue(civilian.getID());
			return;
		}
		//視界範囲にいる市民情報を取得
		//Collection<StandardEntity> civilians = world.getEntitiesOfType(StandardEntityURN.CIVILIAN);
		List<Civilian> civilians = owner.getViewCivilians();
		
		
		if(civilians.isEmpty()){
			
			
			illegal = true;
			return;
		}
		//buriedness > 0ならrescue
		
		//for(StandardEntity next : civilians){
		for(Civilian next : civilians){
			
			//if(((Civilian)next).getBuriedness() > 0){
			if(next.getBuriedness() > 0){
				//civilian = (Civilian)next;
				civilian = next;
				owner.rescue(civilian.getID());
				
				
				return;
			}else{
				
			}
		}
		
		
		illegal = true;
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		

		//視界範囲内にいるすべての市民エージェントが埋没していない場合
		//また，視界範囲に市民エージェントがいない場合
		if(illegal){
			
			
			return true;
		}
		if(civilian == null){
			
			
			return false;
		}
		
		return civilian.getBuriedness() <= 0;
	}
}
