package naito_rescue.task.job;

import naito_rescue.*;
import naito_rescue.agent.*;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;

import java.util.*;
import java.io.*;

public class MoveToClearPointJob extends Job
{

	List<EntityID> blockades;
	int            maxDistance;
	MyLogger       logger;

	public MoveToClearPointJob(NAITOHumanoidAgent owner, StandardWorldModel world, List<EntityID> blockades, int distance){
		super(owner, world);
		this.logger = owner.getLogger();
		this.blockades = blockades;
		this.maxDistance = distance;
	}

	public void doJob(){
		logger.info("MoveToClearPointJob.doJob();");
		logger.info("blockades = " + blockades);

		//閉塞への経路の中で，最短のものを選択肢手移動する
		int distance = Integer.MAX_VALUE;
		List<EntityID> path = null;
		logger.trace("Entering for loop...");
		for(EntityID blockade : blockades){
			logger.trace("now blockade = " + blockade);
			int dist_temp;
			List<EntityID> path_temp = owner.getSearch().breadthFirstSearch(owner.getLocation(), world.getEntity(blockade)); //TODO:
			dist_temp = world.getDistance(owner.getLocation().getID(), blockade);
			logger.trace("path_temp = " + path_temp);
			logger.trace("dist_temp = " + dist_temp);
			if(path != null && dist_temp < distance){
				distance = dist_temp;
				path = path_temp;
			}
		}
		if(path != null){
			logger.info("Path to Blockade is find.");
			logger.debug("path = " + path);
			logger.debug("distance = " + distance);
			owner.move(path);
			logger.info("MoveToClearPointJob.doJob() DONE!");
		}else{
			logger.info("Path to Blockade is not find.");
		}
	}

	public boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		for(EntityID blockade : blockades){
			int distance = world.getDistance(owner.getLocation(), world.getEntity(blockade));
			if(distance <= maxDistance) return true;
		}
		return false;
	}
}
