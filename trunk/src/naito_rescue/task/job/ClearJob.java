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
	List<EntityID> blockades;
	Area     target_road;
	int      maxDistance;
	MyLogger logger;

	public ClearJob(NAITOHumanoidAgent owner, StandardWorldModel world, Blockade target, Area target_road, int maxDistance){
		super(owner, world);
		this.target = target;
		this.target_road = target_road;
		this.maxDistance = maxDistance;
		this.logger = owner.getLogger();
	}
	public ClearJob(NAITOHumanoidAgent owner, StandardWorldModel world, Area target_road, int maxDistance){
		this(owner, world, null, target_road, maxDistance);
	}
	@Override
	public void doJob(){
		logger.info("ClearJob.doJob();");
		
		if(owner.getLocation().getID().getValue() == target_road.getID().getValue()){
			//自分のいる場所とその近傍について，啓開範囲内にある閉塞を啓開する
			logger.info("In target road (" + target_road + ")");
			target = getTargetBlockade();
			if(target != null){
				logger.info("clear(" + target + ")");
				owner.clear(target.getID());
			}else{
				//閉塞がなかったら...?
				logger.info("There are not blockade in target_road(" + target_road + ") ");
				if(target_road.isBlockadesDefined()){
					List<EntityID> blockades = target_road.getBlockades();
				}else{
					//ここにパスが移ると言うことは，
					//そもそもClearJob.isFinished() == trueでなければならないはず
				}
			}
		}else{
			logger.info("この文に制御が移っているのなら，");
			logger.info("MoveToClearPointJobのisFinishedがトチ狂ってる");
		}
	}

    private Blockade getTargetBlockade() {
		logger.info("ClearJob.getTargetBlockade()");
        //logger.debug("Looking for target blockade");
        Area location = (Area)owner.getLocation();
        //logger.debug("Looking in current location");
        Blockade result = getTargetBlockade(location, maxDistance);
        if (result != null) {
            logger.info("There is blockade in this.location");
			logger.debug("" + result);
			return result;
        }
        //logger.debug("Looking in neighbouring locations");
        for (EntityID next : location.getNeighbours()) {
            location = (Area)world.getEntity(next);
            result = getTargetBlockade(location, maxDistance);
            if (result != null) {
                logger.info("There is blockade in this.location.getNeighbours();");
				logger.debug("" + result);
				return result;
            }
        }
		logger.debug("return null.");
        return null;
    }

    private Blockade getTargetBlockade(Area area, int maxDistance) {
		logger.info("ClearJob.getTargetBlockade(" + area + ", " + maxDistance + ")");
        if (!area.isBlockadesDefined()) {
            logger.info("!area.isBlockadesDefined(); ==> return null.");
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        // Find the first blockade that is in range.
        int x = ((Human)owner.getMe()).getX();
        int y = ((Human)owner.getMe()).getY();
        for (EntityID next : ids) {
            Blockade b = (Blockade)world.getEntity(next);
            double d = owner.findDistanceTo(b, x, y);
            if (maxDistance < 0 || d < maxDistance) {
				logger.info("There is blockade.");
				logger.debug("" + b);
                return b;
            }
        }
        logger.debug("No blockades in range");
        return null;
    }
	@Override 
	protected boolean isFinished(NAITOHumanoidAgent owner, StandardWorldModel world){
		logger.info("ClearJob.isFinished();");
		if(target_road != null){
			logger.debug("target_road.isBlockadesDefined() = " + target_road.isBlockadesDefined());
			return !(target_road.isBlockadesDefined());
		}else if(owner.getLocation() instanceof Area){
			List<EntityID> neighbours = ((Area)(owner.getLocation())).getNeighbours();
			for(EntityID neighbour : neighbours){
				Area location = (Area)(world.getEntity(neighbour));
				if(location.isBlockadesDefined()){
					logger.debug("" + location + " isBlockadeDefined()!");
					return false;
				}	
			}
			if(((Area)(owner.getLocation())).isBlockadesDefined()){
				logger.debug("(owner.getLocation()) " + owner.getLocation() + " isBlockadeDefined()!");
				return false;
			}
			return true;
		}
		return false;
	}
}
