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
		Collection<StandardEntity> civilians = world.getEntitiesOfType(StandardEntityURN.CIVILIAN);
		logger.debug("civilians = " + civilians);
		for(StandardEntity next : civilians){
			Civilian c = (Civilian)next;
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
	}

	@Override 
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		logger.info("LoadJob.isFinished();");
		if(civilian == null){
			logger.debug("civilian is null.");
			logger.debug("    |____ return false;");
			return false;
		}
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
	}
}
