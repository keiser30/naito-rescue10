package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

//TODO: 閉塞周りの回避行動を積みたい．

public class MoveJob extends Job
{
	Area target;
	int x = -1;
	int y = -1;
	boolean Illegal = false;

	public MoveJob(NAITOHumanoidAgent owner, StandardWorldModel world, Area target, int x, int y){
		super(owner, world);
		this.target = target;
		this.x = x;
		this.y = y;
	}
	public MoveJob(NAITOHumanoidAgent owner, StandardWorldModel world, Area target){
		this(owner, world, target, -1, -1);
	}
	
	public Area getTarget(){ return target; }
	
	@Override
	public void doJob(){
		List<EntityID> path = owner.getSearch().breadthFirstSearch(owner.getLocation(), Collections.singleton(target));
		if(path != null){
			Blockade blockade = owner.getBlockadeOnPath(path);
			if(owner instanceof NAITOPoliceForce && blockade != null){
				owner.clear(blockade.getID());
				return;
			}
			if(x == -1 || y == -1){
				owner.move(path);
			}else{
				owner.move(path, x, y);
			}
		}else{
			owner.getLogger().info("MoveJob().doJob() target (" + target + ") is not adjucent to " + owner.getLocation());
		}
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		int location_id = owner.getLocation().getID().getValue();
		int target_id = target.getID().getValue();

		if(illegal == true) return true;
		if(location_id == target_id){
			return true;
		}else{
			return false;
		}
	}
}