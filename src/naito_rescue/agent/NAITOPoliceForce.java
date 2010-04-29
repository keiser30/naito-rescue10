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
//	private PrintWriter logger;

	private Collection<StandardEntity> allRoads;
	// private String LOGFILENAME = LOGFILENAME_BASE + (++pflog_num) + ".log";

	@Override
	public String toString(){
		return "NAITOPoliceForce." + me().getID() + "";
	}

	@Override
	protected void postConnect(){
        super.postConnect();
		model.indexClass(StandardEntityURN.ROAD);
		distance = config.getIntValue(DISTANCE_KEY);
		
		allRoads = model.getEntitiesOfType(StandardEntityURN.ROAD);
	}

	static boolean once = true;
	List<EntityID> path;
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time,changed,heard);

		logger.info("********** " + time + " **********");
		logger.info("NAITOPoliceForce.think();");
		//logger.debug("ignoring agents command time = " + config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY));
		//logger.debug("location = " + getLocation());
		
		if (time <= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
		//	logger.info("まだコマンドを送っちゃダメです");
			logger.debug("(time = " + time + ")");
			return;
		}
		Road far_road = null;
/*
		if(once){
			// MySearchのテスト
			int distance = 0;
			for(StandardEntity road : allRoads){
				int dist_temp = model.getDistance(getLocation(), road);
				if(distance < dist_temp){
					distance = dist_temp;
					logger.debug("distance = " + distance);
					logger.debug("<---- ID = " + road.getID().getValue());
					far_road = (Road)road;
				}
			}
			if(far_road == null){
				logger.info("far_road is null.");
				logger.info("return think();");
				once = false;
				return;
			}
			logger.debug("far_road.id = " + far_road.getID().getValue());
			logger.debug("finding path from " + getLocation() + ", to " + far_road);
			path = search.getRoute(getLocation().getID(), far_road.getID());
			logger.debug("path = " + path);
			once = false;
			move(path);
		}
		//move(path);
*/
	}
	public void taskRankUpdate(){
	}
    @Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }  
}
