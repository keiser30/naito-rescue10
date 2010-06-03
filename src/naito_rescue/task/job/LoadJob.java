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
		logger.info("LoadJob.doJob();");
		if(civilian != null){
			logger.debug("civilian != null");
			logger.debug("今まさに運搬中です");
			return;
		}
		//Collection<StandardEntity> civilians = world.getEntitiesOfType(StandardEntityURN.CIVILIAN);
		List<Civilian> civilians = owner.getViewCivilians();
		logger.debug("civilians = " + civilians);
		if(civilians.isEmpty()){
			//視界範囲に市民エージェントがいない
			logger.debug("LoadJob.illegal: 中に誰もいませんよ");
			logger.debug("illegal = true");
			illegal = true;
			return;
		}
		//for(StandardEntity next : civilians){
		for(Civilian next : civilians){
			//Civilian c = (Civilian)next;
			Civilian c = next;
			logger.debug("c.getBuriedness() = " + c.getBuriedness());
			if(c.getBuriedness() <= 0){
				logger.debug("Decide target civilian = " + civilian);
				logger.debug("    |____ load(" + c.getID() + ")");
				civilian = c;
				owner.load(c.getID());
				return;
			}
		}
		//パスがここにたどり着いたとき...
		//   |___ buriednes() == 0な市民がいなかった
		logger.debug("LoadJob.illegal: 視界範囲にいる市民すべては埋没していない");
		logger.debug("illegal = true");
		illegal = true;
	}

	@Override 
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		logger.info("LoadJob.isFinished();");
		if(illegal){
			logger.debug("illegal");
			return true;
		}
		if(civilian == null){
			logger.debug("civilian is null.");
			logger.debug("    |____ return false;");
			return false;
		}else{
			logger.debug("civilian != null");
			logger.debug("    |____ 今市民を乗せました.");
			return true;
		}
/*
		StandardEntity location = owner.getLocation();
		logger.debug("owner.getLocation() = " + location);
		for(StandardEntity next : world.getEntitiesOfType(StandardEntityURN.CIVILIAN)){
			Civilian c = (Civilian)next;
			logger.debug("c.location = " + c.getPosition());
			if(c.getPosition().equals(owner.getID())){
				logger.debug("c.getPosition().equals(location.getID())");
				logger.debug("   |____ return true;");
				return true;
			}
		}
		logger.debug("All civilian is not on AT.");
		logger.debug("    |____ return false;");
		return false;
*/
	}
}
