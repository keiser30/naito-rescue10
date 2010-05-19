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
		
		blockades = target.getBlockades();
	}

	public void doJob(){
		logger.info("////////// MoveToClearPointJob.doJob(); //////////");
		//logger.info("blockades = " + blockades);

		//閉塞への経路の中で，最短のものを選択肢手移動する
		List<EntityID> path = owner.getSearch().breadthFirstSearch(owner.getLocation(), target);
		Blockade target_blockade = getTargetBlockade();
		if(target_blockade == null){
			logger.debug("target_blockade is null.");
			logger.debug("return");
			return;
		}

		if(path != null){
			logger.info("Path to Blockade is find.");
			logger.debug("path = " + path + ", x = " + target_blockade.getX() + ", " + target_blockade.getY());
			owner.move(path, target_blockade.getX(), target_blockade.getY());
			logger.info("move();");
		}else{
			logger.info("Path to Blockade is not find.");
		}
	}
	//SamplePoliceForceから移植, ちょっと改造
	private Blockade getTargetBlockade(){
		logger.info("getTargetBlockade();");
        if (!target.isBlockadesDefined()) {
        	logger.debug("Target area is not defined Blockade.");
            return null;
        }
        int x = owner.getX();
        int y = owner.getY();
        double distance = Double.MAX_VALUE;
        Blockade result = null;
        for (EntityID next : blockades) {
            Blockade b = (Blockade)world.getEntity(next);
            double d = findDistanceTo(b, x, y);
            logger.debug("Distance to " + b + " = " + d);
            if (d < distance) {
                logger.debug("In range");
                logger.debug("==> range       = " + d);
                logger.debug("==> maxDistance = " + distance);
                distance = d;
                result = b;
            }
        }
        logger.debug("return blockade = " + result);
        return result;
	}
	//SamplePoliceForceから移植
    private int findDistanceTo(Blockade b, int x, int y) {
    	logger.info("findDistanceTo(" + b + ", " + x + ", " + y + ")");
        //logger.debug("Finding distance to " + b + " from " + x + ", " + y);
        List<Line2D> lines = GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
        double best = Double.MAX_VALUE;
        Point2D origin = new Point2D(x, y);
        for (Line2D next : lines) {
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
            double d = GeometryTools2D.getDistance(origin, closest);
            //logger.debug("Next line: " + next + ", closest point: " + closest + ", distance: " + d);
            if (d < best) {
                best = d;
                //logger.debug("New best distance");
            }
        }
        logger.info("returning best = " + (int)best);
        return (int)best;
	}
	public boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		//if(blockades == null || blockades.isEmpty()) return true;
		logger.info("////////// MoveToClearPointJob.isFinished(); ///////////");
		/*
		for(EntityID blockade : blockades){
			logger.debug("blockade: " + blockade);
			int distance = world.getDistance(owner.getLocation(), world.getEntity(blockade));
			logger.debug("distance    = " + distance);
			logger.debug("maxDistance = " + maxDistance);
			if(distance < maxDistance) return true;
		}
		*/
		StandardEntity location = owner.getLocation();
		logger.debug("location = " + location);
		logger.debug("target   = " + target);
		if(location.getID().getValue() == target.getID().getValue()){
			logger.debug("return true;");
			return true;
		}
		logger.debug("return false;");
		return false;
	}
}
