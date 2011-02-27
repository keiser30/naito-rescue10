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

public class NAITOBuilding extends NAITOArea
{
	int      reportFireTime = -1;
	public NAITOBuilding(Building building){
		super(building);
	}
	public void setReportFireTime(int time){
		this.reportFireTime = time;
	}
	public int getReportFireTime(){
		return reportFireTime;
	}
	public boolean hasReportedFire(){
		return reportFireTime != -1;
	}
	public Building getStandardBuilding(){
		return (Building)object;
	}
}
