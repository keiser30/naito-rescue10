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

import static naito_rescue.debug.DebugUtil.*;

public abstract class NAITOArea
{
	protected Area   object;
	protected int    reportBlockadeTime = -1;
	protected int    visitedTime = -1;
	protected double estimate = Double.MAX_VALUE;
	
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
	public void setEstimateCost(double cost){
		this.estimate = cost;
	}
	public double getEstimateCost(){
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
	public int getX(){
		return object.getX();
	}
	public int getY(){
		return object.getY();
	}
	
	@Override
	public boolean equals(Object other){
		//p("NAITOArea.equals();");
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
	
	@Override
	public String toString(){
		return "NAITOArea(" + object.getID().getValue() + ")";
	}
}
