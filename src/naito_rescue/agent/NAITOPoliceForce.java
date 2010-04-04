package naito_rescue.agent;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Area;

public class NAITOPoliceForce extends NAITOHumanoidAgent<PoliceForce>
{
    private static final String DISTANCE_KEY = "clear.repair.distance";

	private int distance;

	@Override
	public String toString(){
		return "NAITOPoliceForce: " + me().getID() + "";
	}

	@Override
	protected void postConnect(){
        super.postConnect();
		model.indexClass(StandardEntityURN.ROAD);
		distance = config.getIntValue(DISTANCE_KEY);
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time,changed,heard);
	}

    @Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }  
}
