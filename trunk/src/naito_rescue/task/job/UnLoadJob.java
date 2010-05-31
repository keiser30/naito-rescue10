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
		//避難所までいくとか何とか追加しないとダメな姫ガス
		//owner.unload();
		if(owner.getLocation() instanceof Refuge){
			Collection<StandardEntity> civilians = world.getEntitiesOfType(StandardEntityURN.CIVILIAN);
			logger.debug("civilians = " + civilians);
			for(StandardEntity next : civilians){
				Civilian c = (Civilian)next;
				logger.debug("c.getBuriedness() = " + c.getBuriedness());
				if(c.getPosition().equals(owner.getID())){
					logger.debug("今運ばれている市民エージェントは = " + c);
					logger.debug("    |____ unload(" + c.getID() + ")");
					owner.unload();
					return;
				}
			}
			logger.debug("市民がどこかで取りこぼされている...?");
		}
		logger.debug("まだ避難所へ到達していない");
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel model){
		if(!(owner.getLocation() instanceof Refuge)){
			logger.debug("owner.location is not Refuge.");
			logger.debug("   |____ return false;");
			return false;
		}
		
		for(StandardEntity next : world.getEntitiesOfType(StandardEntityURN.CIVILIAN)){
			Civilian c = (Civilian)next;
			logger.debug("c.location = " + c.getPosition());
			if(c.getPosition().equals(owner.getID())){
				logger.debug("c.getPosition().equals(location.getID())");
				logger.debug("   |____ return false(まだ市民を運搬中);");
				return false;
			}
		}
		logger.debug("すべての市民はATによって運搬されていない(英語の直訳みたいな文)");
		return true;
	}
}
