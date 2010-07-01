package naito_rescue.task.job;

import naito_rescue.*;
import naito_rescue.agent.*;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.misc.geometry.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;

import java.util.*;
import java.io.*;

public class MoveToClearPointJob extends Job
{

	List<EntityID> blockades;
	Area           target;
	int            maxDistance;

	public MoveToClearPointJob(NAITOHumanoidAgent owner, StandardWorldModel world, Area target, int distance){
		super(owner, world);
		this.target = target;
		this.maxDistance = distance;
		
	}

	public void doJob(){
		logger.info("////////// MoveToClearPointJob.doJob(); //////////");

		//行く手を邪魔する閉塞があったら除去
		Blockade blockade = owner.getBlockadeOnPath();
		if(blockade != null){
			logger.debug("MoveToClearPointJob => clear(" + blockade + ")");
			owner.clear(blockade.getID());
			return;
		}
		//閉塞への経路の中で，最短のものを選択肢手移動する
		List<EntityID> path = owner.getSearch().breadthFirstSearch(owner.getLocation(), target);

		if(path != null){
			logger.info("Path to Blockade is find.");
			logger.debug("path = " + path);
			//owner.move(path, target_blockade.getX(), target_blockade.getY());
			owner.move(path);
			logger.info("doJob(); => move();");
		}else{
			logger.info("Path to Blockade is not find.");
		}
	}

	public boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		//if(blockades == null || blockades.isEmpty()) return true;
		logger.info("////////// MoveToClearPointJob.isFinished(); ///////////");
		StandardEntity location = owner.getLocation();
		logger.debug("location = " + location);
		logger.debug("target   = " + target);
		
		//if(this.blockades == null) illegal = true;
		if(illegal) return true;
		if(location.getID().getValue() == target.getID().getValue()){
			logger.debug("return true;");
			return true;
		}
		logger.debug("return false;");
		return false;
	}
}
