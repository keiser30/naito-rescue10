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
	}


	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    } 

	private List<Road> getBlockedRoads(ChangeSet changed){
		
		ArrayList<Road> result = new ArrayList<Road>();
		//まず視界情報について検査
		for(EntityID id : changed.getChangedEntities()){
			StandardEntity entity = model.getEntity(id);
			if(entity instanceof Road){
				Road r = (Road)entity;
				if(r.isBlockadesDefined() && !r.getBlockades().isEmpty()){
					
					result.add(r);
				}
			}
		}
		//次にallRoadsについて検査
		for(StandardEntity road : allRoads){
			if(road instanceof Road){
				Road r = (Road)road;
				if(r.isBlockadesDefined() && !r.getBlockades().isEmpty()){
					result.add(r);
				}
			}
		}
		if(result.isEmpty()){
			
		}
		return result;
	}

}
