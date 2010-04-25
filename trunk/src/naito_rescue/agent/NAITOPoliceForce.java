package naito_rescue.agent;

import java.util.*;
import java.io.*;

import rescuecore2.worldmodel.*;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.*;

public class NAITOPoliceForce extends NAITOHumanoidAgent<PoliceForce>
{
    private static final String DISTANCE_KEY = "clear.repair.distance";

	private int distance; //閉塞解除が可能な距離...?


	private File pflog;
	private static int pflog_num = 0;
	private FileOutputStream pflog_stream;
	private PrintWriter logger;
	// private String LOGFILENAME = LOGFILENAME_BASE + (++pflog_num) + ".log";

	@Override
	public String toString(){
		return "NAITOPoliceForce: " + me().getID() + "";
	}

	@Override
	protected void postConnect(){
        super.postConnect();
		model.indexClass(StandardEntityURN.ROAD);
		distance = config.getIntValue(DISTANCE_KEY);
		
		try{
			
		}catch(Exception e){
		}
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time,changed,heard);
	}
	public void taskRankUpdate(){
	}
    @Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }  
}
