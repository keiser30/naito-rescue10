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

public class NAITOPoliceForce extends NAITOHumanoidAgent<PoliceForce> implements MessageConstants
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
		
		logger.info("NAITOPoliceForce.hearing...");

		for(Command next : heard){
			logger.debug("heard->next = " + next);
			if(next instanceof AKSpeak){
				/**
				*  無線or声データの処理
				*/
				logger.info("Receive AKSpeak.");
				AKSpeak speak = (AKSpeak)next;
				List<naito_rescue.message.Message> msgList = msgManager.receiveMessage(speak);
				logger.info("Extracting messages size = " + msgList.size());
				for(naito_rescue.message.Message message : msgList){
					if(message.getAddrAgent() != me().getID().getValue() && message.getAddrType() != ADDR_PF){
						logger.info("Ignore message.");
						continue; //自分(もしくは自分と同じ種別のエージェント)宛のメッセージでなかったら無視
					}

					if(message.getType() == TYPE_CLEAR){
						logger.info("TYPE_CLEAR messsage has received.");
						EntityID target_id = ((ClearMessage)message).getTarget();
						StandardEntity target = model.getEntity(target_id);
						logger.info("PF.currentTaskList.add(ClearTask(" + target + ")");
						currentTaskList.add(new ClearTask(this, model, (Area)target, distance));
					}
				}
			}
		}

		//自分が閉塞の近くにいたら，そいつを啓開するタスクを追加する
        List<Road> targets = getBlockedRoadsInView(changed);
		if(targets != null && !targets.isEmpty()){
			for(Road r : targets){
				currentTaskList.add(new ClearTask(this, model, (Area)r, distance));
			}
		}
        
		currentTask = action();
		currentJob = currentTask.currentJob();
		logger.info("currentTask = " + currentTask);
		logger.info("currentJob  = " + currentJob);
		if(currentJob != null)
			currentJob.doJob();
		else
			logger.debug("currentJob is null.");
	}
	
	@Override
	public void taskRankUpdate(){
		logger.info("PoliceForce.taskRankUpdate();");
		int distance;
		int rank;
		double width = (w_width > w_height? w_width : w_height);
		
		for(Task t : currentTaskList){
			//ClearTask: 割り当て10000...5000
			if(t instanceof ClearTask){
				logger.info("taskRankUpdate=>ClearTask");
				distance = model.getDistance(getLocation(), ((ClearTask)t).getTarget());
				rank = basicRankAssign(10000, 5000, distance, width);
				logger.info("t.setRank(" + rank + ");");
				t.setRank(rank);
			}
			//MoveTask:
			else if(t instanceof MoveTask){
				logger.info("taskRankUpdate=>MoveTask");
				distance = model.getDistance(getLocation(), ((MoveTask)t).getTarget());
				if(isOnTeam){
					//割り当て9000...5000
					logger.debug("taskRankUpdate=>MoveTask=>isOnTeam");
					rank = basicRankAssign(9000, 5000, distance, width);
				}else{
					//割り当て4000...1000(default)
					logger.debug("taskRankUpdate=>MoveTask=>!isOnTeam");
					rank = basicRankAssign(4000, 1000, distance, width);
				}
				logger.info("t.setRank(" + rank + ");");
				t.setRank(rank);
			}
			/*
			//RestTask:
			else if(t instanceof RestTask){
				logger.info("taskRankUpdate=>RestTask");
				logger.info("t.setRank(Integer.MAX_VALUE);");
				t.setRank(Integer.MAX_VALUE);
			}
			*/
		}
	}
	
	//ClearTask, MoveTaskなどにおいて，自分から対象までの距離を元にしたタスク優先度の割り当てをおこなう
	//(距離が遠くなるほど優先度は低くなる)
	private int basicRankAssign(int maxRank, int minRank, int distance, double world_width){
		logger.debug("basicRankAssign();");
		logger.debug("maxRank  = " + maxRank);
		logger.debug("minRank  = " + minRank);
		logger.debug("distance = " + distance);
		
		int rank = maxRank;
		logger.trace("distance = " + distance);
		if(distance > 0){
			int increment = (int)((maxRank - minRank) * (distance / world_width));
			if(increment > minRank){
				increment = minRank;
			}
			logger.trace("increment = " + increment);
			rank = maxRank - increment;
		}
		logger.debug("rank = " + rank);
		return rank;
	}
	
    @Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    } 
/*
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
*/
	private List<Road> getBlockedRoadsInView(ChangeSet changed){
		logger.info("getBlockedRoadsInView();");
		ArrayList<Road> result = new ArrayList<Road>();
		for(EntityID id : changed.getChangedEntities()){
			StandardEntity entity = model.getEntity(id);
			if(entity instanceof Road){
				Road r = (Road)entity;
				if(r.isBlockadesDefined() && !r.getBlockades().isEmpty()){
					logger.debug("Blocked Road => " + r);
					result.add(r);
				}
			}
		}
		if(result.isEmpty()){
			logger.info("getBlockedRoadsInView(); => There is no blocked road.");
		}
		return result;
	}
    public Blockade getTargetBlockade() {
       // logger.debug("Looking for target blockade");
	   	logger.info("NAITOPoliceForce.getTargetBlockade();");
        Area location = (Area)location();
        //logger.debug("Looking in current location");
        Blockade result = getTargetBlockade(location, distance);
        if (result != null) {
			logger.info("There is blockade in this.location;");
			logger.debug("" + result);
            return result;
        }
        //logger.debug("Looking in neighbouring locations");
        for (EntityID next : location.getNeighbours()) {
            location = (Area)model.getEntity(next);
            result = getTargetBlockade(location, distance);
            if (result != null) {
				logger.info("There is blockade in this.location.getNeighbours();");
				logger.debug("" + result);
                return result;
            }
        }
		logger.info("There is not blockade. return null;");
        return null;
    }

    public Blockade getTargetBlockade(Area area, int maxDistance) {
        //logger.debug("Looking for nearest blockade in " + area);
        logger.info("NAITOPoliceForce.getTargetBlockade(" + area + ", " + maxDistance + ")");
		if (!area.isBlockadesDefined()) {
            //Logger.debug("Blockades undefined");
			logger.info("!area.isBlockadesDefined(); ==> return null;");
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        // Find the first blockade that is in range.
        int x = me().getX();
        int y = me().getY();
        for (EntityID next : ids) {
            Blockade b = (Blockade)model.getEntity(next);
            double d = findDistanceTo(b, x, y);
            //logger.debug("Distance to " + b + " = " + d);
            if (maxDistance < 0 || d < maxDistance) {
                //logger.debug("In range");
				logger.info("There is blockade.");
				logger.debug("" + b);
                return b;
            }
        }
        logger.info("No blockades in range");
        return null;
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