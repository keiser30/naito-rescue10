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
		logger.info("RescueJob.doJob();");
		if(owner.getLocation().getID().getValue() != target.getID().getValue()){
			logger.debug("owner.getLocation() != target");
			logger.debug("owner.getLocation() = " + owner.getLocation());
			logger.debug("             target = " + target);
			logger.debug("|________ move(" + target + ")");
			owner.move(target);
			return;
		}
		if(civilian != null){
			logger.debug("civilian != null ----> " + civilian);
			logger.debug("|________ rescue(" + civilian + ")");
			owner.rescue(civilian.getID());
			return;
		}
		//視界範囲にいる市民情報を取得
		Collection<StandardEntity> civilians = world.getEntitiesOfType(StandardEntityURN.CIVILIAN);
		logger.debug("視界範囲の市民情報を取得");
		logger.debug("civilians = " + civilians);
		if(civilians.isEmpty()){
			logger.debug("中に誰もいませんよ");
			logger.debug("illegal = true");
			illegal = true;
			return;
		}
		//buriedness > 0ならrescue
		logger.trace("Entering for loop....");
		for(StandardEntity next : civilians){
			logger.trace(next + "");
			if(((Civilian)next).getBuriedness() > 0){
				civilian = (Civilian)next;
				owner.rescue(civilian.getID());
				logger.trace("target civilian = " + civilian);
				logger.trace("buriedness      = " + civilian.getBuriedness());
				return;
			}
		}
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		logger.info("RescueJob.isFinished();");
		if(illegal){
			logger.debug("illegal");
			logger.debug("|____ return true;");
			return true;
		}
		if(civilian == null){
			logger.debug("civilian == null");
			logger.debug("    |____ return false;");
			return false;
		}
		logger.debug("civilian.getBuriedness() = " + civilian.getBuriedness());
		return civilian.getBuriedness() <= 0;
	}
}
