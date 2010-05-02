package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.*;
import naito_rescue.task.*;
import naito_rescue.agent.*;

import java.util.*;
import java.io.*;

/**
 * 道路啓開Job
 */
public class ClearJob extends Job
{
	Blockade target = null;
	Road     target_road;
	int      maxDistance;
	MyLogger logger;

	public ClearJob(NAITOHumanoidAgent owner, StandardWorldModel world, Blockade target, Road target_road, int maxDistance){
		super(owner, world);
		this.target = target;
		this.target_road = target_road;

		this.logger = owner.getLogger();
	}
	public ClearJob(NAITOHumanoidAgent owner, StandardWorldModel world, int maxDistance){
		this(owner, world, null, null, maxDistance);
	}
	@Override
	public void doJob(){
		logger.info("ClearJob.doJob();");
		int x = ((Human)(owner.getMe())).getX();
		int y = ((Human)(owner.getMe())).getY();
		if(target != null){
			logger.debug("owner.clear(" + target + ")");
			owner.clear(target.getID());
		}else if(owner.getLocation() instanceof Area){
			// target == nullの時
			// ownerが閉塞につかまってるとして，owner近辺を啓開する
			// まずowner.getLocation()が閉塞だったらそこを啓開する
			List<EntityID> ids = ((Area)(owner.getLocation())).getBlockades();
			if(ids != null){
				logger.info("Owner's location is defined blockades.");
				for (EntityID next : ids) {
					Blockade b = (Blockade)world.getEntity(next);
					double d = owner.findDistanceTo(b, x, y);
					owner.getLogger().debug("Distance to " + b + " = " + d);
					if (maxDistance < 0 || d < maxDistance) {
						owner.getLogger().debug("In range");
						//啓開
						logger.debug("(Location) owner.clear(" + b + ")");
						owner.clear(b.getID());
						return;
					}
				}
			}else{
				// owner.getLocation()がふつーの道だったら，その近辺を探索して啓開する
				List<EntityID> neighbours = ((Area)(owner.getLocation())).getNeighbours(); //ownerの近辺にあるオブジェクト群のIDを取得
				for(EntityID neighbour : neighbours){
					Area location = (Area)(owner.getWorldModel().getEntity(neighbour));
					if(location.isBlockadesDefined()){
						List<EntityID> b = location.getBlockades();
						for(EntityID blockade : b){
							double d = owner.findDistanceTo((Blockade)(world.getEntity(blockade)), x, y);
							if(maxDistance < 0 || d < maxDistance){
								//啓開
								logger.debug("(Neighbour) owner.clear(" + blockade + ")");
								owner.clear(blockade);
								return;
							}
						}
					}
				}
				//ここにパスが来るケース:
				//  . 啓開はあったが，啓開可能距離範囲内に存在しなかった
				//  . そもそも啓開がなかった
				return;
			}
		}else if(target != null){
			logger.debug("target != null");
			if(target_road.isBlockadesDefined()){
				List<EntityID> ids = target_road.getBlockades();
				if(ids != null){
					for (EntityID next : ids) {
						Blockade b = (Blockade)world.getEntity(next);
						double d = owner.findDistanceTo(b, x, y);
						logger.debug("Distance to " + b + " = " + d);
						if (maxDistance < 0 || d < maxDistance) {
							logger.debug("In range");
							//啓開
							logger.debug("(Target) owner.clear(" + b + ")");
							owner.clear(b.getID());
							return;
						}
					}
				}
			}
			logger.debug("target_road is not defined blockade.");
		}else{
			logger.debug("ClearJob: target is null && !owner.getLocation() instanceof Area && target == null");
			return;
		}
	}

	@Override 
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		if(target_road != null){
			return !(target_road.isBlockadesDefined());
		}else if(owner.getLocation() instanceof Area){
			List<EntityID> neighbours = ((Area)(owner.getLocation())).getNeighbours();
			for(EntityID neighbour : neighbours){
				Area location = (Area)(world.getEntity(neighbour));
				if(location.isBlockadesDefined()) return false;
			}
			return true;
		}
		return false;
	}
}
