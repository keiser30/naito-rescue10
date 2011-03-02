package naito_rescue.object;

import java.util.*;

import sample.*;
import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import rescuecore2.misc.geometry.*;

import naito_rescue.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;
import naito_rescue.message.manager.*;

public abstract class NAITOArea
{
	protected Area object;
	protected int  reportBlockadeTime = -1;
	protected int  visitedTime = -1;
	protected int  estimate = Integer.MAX_VALUE;
	
	public NAITOArea(Area object){
		this.object = object;
	}
	public void setReportBlockadeTime(int t){
		this.reportBlockadeTime = t;
	}
	public int getReportBlockadeTime(){
		return this.reportBlockadeTime;
	}
	public boolean hasReportedBlockade(){
		//reportblockadeTime == -1なら未報告
		return this.reportBlockadeTime != -1;
	}
	public void setVisitedTime(int t){
		this.visitedTime = t;
	}
	public int getVisitedTime(){
		return this.visitedTime;
	}
	public boolean hasVisited(){
		return this.visitedTime != -1;
	}
	public void setEstimateCost(int cost){
		this.estimate = cost;
	}
	public int getEstimateCost(){
		return estimate;
	}
	public boolean isBlockadesDefined(){
		return object.isBlockadesDefined();
	}
	public List<EntityID> getBlockades(){
		return object.getBlockades();
	}
	public EntityID getID(){
		return object.getID();
	}
	public Area getStandardArea(){
		return object;
	}
	
	@Override
	public boolean equals(Object other){
		if(!(other instanceof NAITOArea)){
			return false;
		}
		NAITOArea otherArea = (NAITOArea)other;
		if(this.getID().getValue() == otherArea.getID().getValue()){
			return true;
		}else{
			return false;
		}
	}
}
