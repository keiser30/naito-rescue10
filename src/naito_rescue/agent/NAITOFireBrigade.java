package naito_rescue.agent;

import java.util.*;
import java.io.*;
import rescuecore2.worldmodel.*;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.*;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;

import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;


public class NAITOFireBrigade extends NAITOHumanoidAgent<FireBrigade>
{
	boolean debug = true;
	
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
	}

	public String toString(){
		return "NAITOFireBrigade." + me().getID() + "";
	}

	protected void think(int time, ChangeSet changed, Collection<Command> heard){

		super.think(time, changed, heard);
		if(time < 3){
			return;
		}
		if(debug){
			//AStarのテスト(denen_test12_1)
			logger.setContext("think");
			logger.info("********** TEST AStar! yeah! Good for you!!! **********");
			Area from = (Area)getLocation();
			Area to   = (Area)model.getEntity(new EntityID(593));
			logger.info("Path from = " + from);
			logger.info("Path to   = " + to);
			List<EntityID> path = search.AStar(from, to);
			logger.unsetContext();
		
			debug = false;
		}
	}
	
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
	}

}
