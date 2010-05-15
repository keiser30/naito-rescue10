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
public class NAITOPoliceForce extends NAITOHumanoidAgent<PoliceForce>
{
    private static final String DISTANCE_KEY = "clear.repair.distance";
	private int distance; //閉塞解除が可能な距離...?

	@Override
	public String toString(){
		return "NAITOPoliceForce." + me().getID() + "";
	}

	@Override
	protected void postConnect(){
        super.postConnect();
		distance = config.getIntValue(DISTANCE_KEY);
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);

		logger.info("NAITOPoliceForce.think();");

        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to channel 1
			logger.debug("Subscribe(1);");
            sendSubscribe(time, 1);
        } 
		
		logger.info("NAITOPoliceForce.hearring...");

//		for(Command next : heard){
//			logger.debug("heard->next = " + next);
//			if(next instanceof AKSpeak){
//				/**
//				*  無線or声データの処理
//				*/
//				AKSpeak speak = (AKSpeak)next;
//				
//			}
//		}

/*
		// ボイスデータの処理 
		//  "CLEAR_"で始まるボイスデータを受信したら，
		//  啓開対象となるRoadのIDを抽出して，ClearTaskを
		//  currentTaskListに加える
        for (Command next : heard) {
            //logger.debug("Heard " + next);
			byte[] rawdata = null;
			if(next instanceof AKSpeak || next instanceof AKTell || next instanceof AKSay){
				logger.debug("AKSpeak(or AKTell or AKSay) data received.");
				if(next instanceof AKSpeak){
					rawdata = ((AKSpeak)next).getContent();
				}else if(next instanceof AKTell){
					rawdata = ((AKTell)next).getContent();
				}else if(next instanceof AKSay){
					rawdata = ((AKSay)next).getContent();
				}else{
					break;
				}
			}
			if(rawdata != null){
				String str_data = null;
				logger.debug("Extracting voice data...");
				try{
					str_data = new String(rawdata, "UTF-8");
					logger.trace("str_data = " + str_data);
				}catch(Exception e){
					logger.info("Exception in Extracting voice data.");
					logger.trace("break for-loop.");
					continue;
				}
				if(str_data != null && str_data.startsWith("CLEAR_")){
					logger.debug("CLEAR Message received.");
					int id = Integer.parseInt(str_data.substring(6));
					logger.debug("Clear ID = " + id);
					EntityID clearID = new EntityID(id);
					Road clearRoad = (Road)(model.getEntity(clearID));
					logger.debug("Clear target = " + clearRoad);
					currentTaskList.add(new ClearTask(this, model, clearRoad, distance));
				}
			}
        }
        // Am I near a blockade?
        Blockade target = getTargetBlockade();
        if (target != null) {
            logger.info("Clearing blockade " + target);
            sendSpeak(time, 1, ("Clearing " + target).getBytes());
            sendClear(time, target.getID());
            return;
        }

		//ボイスデータからのClear Taskがたまっていたらそいつを処理しましょう．
		if(currentTaskList != null && currentTaskList.size() > 0){
			logger.info("currentTaskList is not null. extracting currentTask now...");
			taskRankUpdate();
			currentTask = getHighestRankTask();
			logger.debug("currentTask = " + currentTask);
			currentJob = currentTask.currentJob();
			logger.debug("currentJob = " + currentJob);
			logger.info("currentJob.doJob();");
			currentJob.doJob();
		}
        // Plan a path to a blocked area
        List<EntityID> path = search.breadthFirstSearch(location(), getBlockedRoads());
        if (path != null) {
            logger.info("Moving to target");
            Road r = (Road)model.getEntity(path.get(path.size() - 1));
            Blockade b = getTargetBlockade(r, -1);
            //sendMove(time, path, b.getX(), b.getY());
			if(b != null){
				//sendMove(time, path, b.getX(), b.getY());
				move(path, b.getX(), b.getY());
				logger.debug("Path: " + path);
				logger.debug("Target coordinates: " + b.getX() + ", " + b.getY());
			}
            return;
        }
        logger.debug("Couldn't plan a path to a blocked road");
        logger.info("Moving randomly");
        //sendMove(time, randomWalk());
		move(randomWalk());
*/
	}
	
    @Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }  
    private List<Road> getBlockedRoads() {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.ROAD);
        List<Road> result = new ArrayList<Road>();
        for (StandardEntity next : e) {
            Road r = (Road)next;
            if (r.isBlockadesDefined() && !r.getBlockades().isEmpty()) {
                result.add(r);
            }
        }
        return result;
    }
    private Blockade getTargetBlockade() {
        logger.debug("Looking for target blockade");
        Area location = (Area)location();
        logger.debug("Looking in current location");
        Blockade result = getTargetBlockade(location, distance);
        if (result != null) {
            return result;
        }
        logger.debug("Looking in neighbouring locations");
        for (EntityID next : location.getNeighbours()) {
            location = (Area)model.getEntity(next);
            result = getTargetBlockade(location, distance);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private Blockade getTargetBlockade(Area area, int maxDistance) {
        logger.debug("Looking for nearest blockade in " + area);
        if (!area.isBlockadesDefined()) {
            Logger.debug("Blockades undefined");
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        // Find the first blockade that is in range.
        int x = me().getX();
        int y = me().getY();
        for (EntityID next : ids) {
            Blockade b = (Blockade)model.getEntity(next);
            double d = findDistanceTo(b, x, y);
            logger.debug("Distance to " + b + " = " + d);
            if (maxDistance < 0 || d < maxDistance) {
                logger.debug("In range");
                return b;
            }
        }
        logger.debug("No blockades in range");
        return null;
    }

	public void taskRankUpdate(){
		StandardEntity location = getLocation();
		int distance;
		for(Task task : currentTaskList){
			/**
			*  ClearTaskのupdate:
			*  意参るところから距離の近い順にランクを高くする
			*/
			if(task instanceof ClearTask){
				Road target = ((ClearTask)task).getTargetRoad();
				distance = model.getDistance(location, target);
				task.setRank(1000 - distance);
			}
		}
	}
	
	public int getDistance(){
		return distance;
	}
/*
    private int findDistanceTo(Blockade b, int x, int y) {
        Logger.debug("Finding distance to " + b + " from " + x + ", " + y);
        List<Line2D> lines = GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
        double best = Double.MAX_VALUE;
        Point2D origin = new Point2D(x, y);
        for (Line2D next : lines) {
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
            double d = GeometryTools2D.getDistance(origin, closest);
            Logger.debug("Next line: " + next + ", closest point: " + closest + ", distance: " + d);
            if (d < best) {
                best = d;
                Logger.debug("New best distance");
            }

        }
        return (int)best;
    }
*/
    /**
       Get the blockade that is nearest this agent.
       @return The EntityID of the nearest blockade, or null if there are no blockades in the agents current location.
    */
    /*
    public EntityID getNearestBlockade() {
        return getNearestBlockade((Area)location(), me().getX(), me().getY());
    }
    */

    /**
       Get the blockade that is nearest a point.
       @param area The area to check.
       @param x The X coordinate to look up.
       @param y The X coordinate to look up.
       @return The EntityID of the nearest blockade, or null if there are no blockades in this area.
    */
    /*
    public EntityID getNearestBlockade(Area area, int x, int y) {
        double bestDistance = 0;
        EntityID best = null;
        Logger.debug("Finding nearest blockade");
        if (area.isBlockadesDefined()) {
            for (EntityID blockadeID : area.getBlockades()) {
                Logger.debug("Checking " + blockadeID);
                StandardEntity entity = model.getEntity(blockadeID);
                Logger.debug("Found " + entity);
                if (entity == null) {
                    continue;
                }
                Pair<Integer, Integer> location = entity.getLocation(model);
                Logger.debug("Location: " + location);
                if (location == null) {
                    continue;
                }
                double dx = location.first() - x;
                double dy = location.second() - y;
                double distance = Math.hypot(dx, dy);
                if (best == null || distance < bestDistance) {
                    bestDistance = distance;
                    best = entity.getID();
                }
            }
        }
        Logger.debug("Nearest blockade: " + best);
        return best;
    }
    */
}
