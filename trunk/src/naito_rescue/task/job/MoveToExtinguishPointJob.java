package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

public class MoveToExtinguishPointJob extends Job
{
	Area target;
	Collection<StandardEntity> extinguishPoints;
	ArrayList<StandardEntity>  extinguishPointsList;
	int  tryCount = 0;
	StandardEntity moveTarget;
	int  maxDistance;
	boolean Illegal;
	//distanceの昇順にソートする
	Comparator<StandardEntity> distanceComparator = new Comparator<StandardEntity>(){
		public int compare(StandardEntity e1, StandardEntity e2){
			int dist1 = owner.getWorldModel().getDistance(owner.getID(), e1.getID());
			int dist2 = owner.getWorldModel().getDistance(owner.getID(), e2.getID());

			return dist1 - dist2;
		}
	};
	public MoveToExtinguishPointJob(NAITOHumanoidAgent owner, StandardWorldModel world, Area target, int distance){
		super(owner, world);
		this.target = target;
		this.maxDistance = distance;
		this.extinguishPointsList = new ArrayList<StandardEntity>();

		extinguishPoints = owner.getWorldModel().getObjectsInRange(target, distance);
		for(StandardEntity extinguishPoint : extinguishPoints){
			extinguishPointsList.add(extinguishPoint);
		}
		Collections.sort(extinguishPointsList, distanceComparator);
		this.tryCount = 0;
	}
	
	public Area getTarget(){ return target; }
	
	@Override
	public void doJob(){
	/*
		tryCount++;
		tryCount = tryCount % extinguishPointsList.size();
		moveTarget = extinguishPointsList.get(tryCount);
	*/
		try{
			List<EntityID> path = owner.getSearch().breadthFirstSearch(owner.getLocation(), extinguishPoints);
			owner.move(moveTarget);
		}catch(Exception e){
			owner.getLogger().info("MoveToExtinguishPointJob: " + e);
	//		doJob(); //再試行
		}
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		logger.info("MoveToExtinguishPointJob.isFinished();");
		logger.debug("distance(owner.getLocation(), target) = " + world.getDistance(owner.getLocation(), target));
		logger.trace("owner.getLocation() = " + owner.getLocation());
		logger.trace("target              = " + target);
		logger.debug("maxDistance = " + maxDistance);
		logger.debug("return " + (world.getDistance(owner.getLocation(), target) < maxDistance ? "true" : "false"));
		return world.getDistance(owner.getLocation(), target) < maxDistance;
	}
}
