package naito_rescue.agent;

import java.util.*;
import java.io.*;

import rescuecore2.worldmodel.*;
import rescuecore2.standard.messages.*;
import rescuecore2.messages.*;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.*;
import rescuecore2.misc.geometry.*;
import naito_rescue.*;
import naito_rescue.agent.*;
import naito_rescue.object.*;
import naito_rescue.router.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;
import naito_rescue.message.manager.*;

/**
*  啓開隊だよ
*
*/
public class NAITOPoliceForce extends NAITOHumanoidAgent<PoliceForce>
{
	
	@Override
	public String toString(){
		return "NAITOPoliceForce." + me().getID() + "";
	}

	@Override
	protected void postConnect(){
        super.postConnect();
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);
		if (time < config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
			return;
		}
		logger.info("\n");
		logger.info("##########    NAITOFireBrigade.think();    ##########");
		logger.info("Current Area = " + getLocation());
		logger.info("CurrentTaskList = " + currentTaskList);
		logger.debug("Preferred Task = " + currentTaskList.peek());
		
		//こいつは遠い所から行かせた方がいいんじゃないか
		
		// 自分のいる場所から隣接エリアへ通行不可となっている閉塞を啓開する
		// (本当はPure Task-Jobでやりたい)
		Area location = (Area)(getLocation());
		}
	}


	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }
    
    public Blockade getTargetBlockade(Area area, int maxDistance) {
        //logger.debug("Looking for nearest blockade in " + area);
        logger.info("NAITOPoliceForce.getTargetBlockade(" + area + ", " + maxDistance + ")");
		if (!area.isBlockadesDefined()) {
            //Logger.debug("Blockades undefined");
			logger.info("!area.isBlockadesDefined(); ==> return null;");
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        // Find the first blockade that is in range.
        int x = me().getX();
        int y = me().getY();
        for (EntityID next : ids) {
            Blockade b = (Blockade)model.getEntity(next);
            double d = findDistanceTo(b, x, y);
            //logger.debug("Distance to " + b + " = " + d);
            if (maxDistance < 0 || d < maxDistance) {
                //logger.debug("In range");
				logger.info("There is blockade.");
				logger.debug("" + b);
                return b;
            }
        }
        logger.info("No blockades in range");
        return null;
    }	

}
