package naito_rescue.agent;

import java.util.*;
import java.io.*;

import rescuecore2.worldmodel.*;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.*;
import rescuecore2.misc.geometry.*;

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
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to channel 1
            sendSubscribe(time, 1);
        }
        for (Command next : heard) {
            Logger.debug("Heard " + next);
        }
        // Am I near a blockade?
        Blockade target = getTargetBlockade();
        if (target != null) {
            Logger.info("Clearing blockade " + target);
            sendSpeak(time, 1, ("Clearing " + target).getBytes());
            sendClear(time, target.getID());
            return;
        }
        // Plan a path to a blocked area
        List<EntityID> path = search.breadthFirstSearch(location(), getBlockedRoads());
        if (path != null) {
            Logger.info("Moving to target");
            Road r = (Road)model.getEntity(path.get(path.size() - 1));
            Blockade b = getTargetBlockade(r, -1);
            sendMove(time, path, b.getX(), b.getY());
            Logger.debug("Path: " + path);
            Logger.debug("Target coordinates: " + b.getX() + ", " + b.getY());
            return;
        }
        Logger.debug("Couldn't plan a path to a blocked road");
        Logger.info("Moving randomly");
        sendMove(time, randomWalk());
	}
	public void taskRankUpdate(){
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
        Logger.debug("Looking for target blockade");
        Area location = (Area)location();
        Logger.debug("Looking in current location");
        Blockade result = getTargetBlockade(location, distance);
        if (result != null) {
            return result;
        }
        Logger.debug("Looking in neighbouring locations");
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
        Logger.debug("Looking for nearest blockade in " + area);
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
            Logger.debug("Distance to " + b + " = " + d);
            if (maxDistance < 0 || d < maxDistance) {
                Logger.debug("In range");
                return b;
            }
        }
        Logger.debug("No blockades in range");
        return null;
    }

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
