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
		
		//blockades = target.getBlockades();
	}

	public void doJob(){
		logger.info("////////// MoveToClearPointJob.doJob(); //////////");
		//logger.info("blockades = " + blockades);

		//行く手を邪魔する閉塞があったら除去
		Blockade blockade = getBlockadeOnPath();
		if(blockade != null){
			owner.clear(blockade.getID());
		}
		//閉塞への経路の中で，最短のものを選択肢手移動する
		List<EntityID> path = owner.getSearch().breadthFirstSearch(owner.getLocation(), target);

		if(path != null){
			logger.info("Path to Blockade is find.");
			logger.debug("path = " + path);
			//owner.move(path, target_blockade.getX(), target_blockade.getY());
			owner.move(path);
			logger.info("move();");
		}else{
			logger.info("Path to Blockade is not find.");
		}
	}
	//行く手を遮る閉塞を得る
	private Blockade getBlockadeOnPath(){
		Area location = (Area)owner.getLocation();
		Blockade blockade = owner.getTargetBlockade(location, maxDistance);
		if(blockade != null){
			return blockade;
		}
        //logger.debug("Looking in neighbouring locations");
        for (EntityID next : location.getNeighbours()) {
            location = (Area)world.getEntity(next);
            blockade = owner.getTargetBlockade(location, maxDistance);
            if (blockade != null) {
				logger.info("There is blockade in this.location.getNeighbours();");
				logger.debug("" + blockade);
                return blockade;
            }
        }
		logger.info("There is not blockade. return null;");
        return null;
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
