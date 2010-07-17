package naito_rescue.agent;

import java.util.*;
import java.io.*;
import rescuecore2.worldmodel.*;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;

import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;


public class NAITOFireBrigade extends NAITOHumanoidAgent<FireBrigade>
{
	private static final String MAX_WATER_KEY = "fire.tank.maximum";
	private static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
	private static final String MAX_POWER_KEY = "fire.extinguish.max-sum";

	private int maxWater;
	private int maxDistance;
	private int maxPower;

	private Building target;
	private boolean isPreferredVoice;
	private boolean        isPreferredNear;
	private FireBrigade me;
	// private Collection<StandardEntity> allBuildings;

    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
	}

	public String toString(){
		return "NAITOFireBrigade." + me().getID() + "";
	}

	protected void think(int time, ChangeSet changed, Collection<Command> heard){

	}
	
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
	}

}
