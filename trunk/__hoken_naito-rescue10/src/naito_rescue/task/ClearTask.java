package naito_rescue.task;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import java.util.*;

import java.io.*;

/** 契約!
*   コンストラクタの引数に渡されるRoad targetについて
*   !(target.isBlockadeDefined()) は認めない
*/
public class ClearTask extends Task
{
	//移動先
	Road target;
	List<EntityID> blockades;
	int maxDistance;

	public ClearTask(NAITOHumanoidAgent owner, StandardWorldModel world, Road target, int distance){
		super(owner, world);
		this.target = target;
		this.maxDistance = distance;

		this.blockades = target.getBlockades();
	}

	@Override
	public ArrayList<Job> createJobList(){
		ArrayList<Job> jobs = new ArrayList<Job>();
		//jobs.add(new MoveToExtinguishPointJob(owner, world, target, distance));
		jobs.add(new MoveToClearPointJob(owner, world, blockades, maxDistance));
		jobs.add(new ClearJob(owner, world, maxDistance));
		return jobs;
	}

	@Override
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		owner.getLogger().info("ClearTask.isFinished();");
		owner.getLogger().info("=====> return " + (target.isBlockadesDefined()?"false":"true"));
		return !(target.isBlockadesDefined());
	}
	public Road getTargetRoad(){ return target; }
}
