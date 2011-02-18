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

public class NAITORoad
{
	private Road road;
	private int  reportBlockadeTime = -1;
	
	public NAITORoad(Road road){
		this.road = road;
	}
	public void setReportBlockadeTime(int time){
		this.reportBlockadeTime = time;
	}
	public int getReportBlockadeTime(){
		return reportBlockadeTime;
	}
	public boolean hasReportedBlockade(){
		return reportBlockadeTime != -1;
	}
	public Road getStandardRoad(){
		return road;
	}
}
