package naito_rescue.router;

import rescuecore2.misc.*;
import rescuecore2.misc.geometry.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.entities.*;

import naito_rescue.*;
import naito_rescue.agent.*;
import naito_rescue.object.*;
import java.util.*;

import static naito_rescue.debug.DebugUtil.*;

public final class NAITORouter{
	private StandardWorldModel model;
	private NAITOAgent         owner;
	private MyLogger           logger;
	
	//ArrayList<Area> OPEN, CLOSED;
	//HashMap<Area, Integer> estimates;
	//HashMap<Area, Area>    ancestors;

	PassableChecker checker;
	
    public NAITORouter(NAITOAgent owner) {
		this.owner = owner;
		this.model = owner.getWorldModel();
		this.logger = this.owner.getLogger();
		
		//OPEN = new ArrayList<Area>();
		//CLOSED = new ArrayList<Area>();
		//estimates = new HashMap<Area, Integer>();
		//ancestors = new HashMap<Area, Area>();
		
		checker = new PassableChecker(owner);
	}

    public List<EntityID> breadthFirstSearch(StandardEntity start, StandardEntity... goals) {
        return breadthFirstSearch(start, Arrays.asList(goals));
    }
    public List<EntityID> getRoute(StandardEntity goal){
    	return breadthFirstSearch(owner.getLocation(), goal);
    }

	public List<EntityID> AStar(StandardEntity start, StandardEntity goal){
		String context = start + ", " + goal + "";
		logger.info("///// NAITORouter.AStar(" + context + ") /////");
		
		List<NAITOArea> OPEN   = new ArrayList<NAITOArea>();
		List<NAITOArea> CLOSED = new ArrayList<NAITOArea>();
		Map<Area, Area> ancestors = new HashMap<Area, Area>();
		
		Area startArea = null, goalArea = null;
		if(start instanceof Area && goal instanceof Area){
			startArea = (Area)(start);
			goalArea = (Area)(goal);
			int cost = estimateCost(ancestors, startArea, startArea, goalArea);
			
			NAITOArea area = (NAITOArea)(owner.allNAITOAreas.get(start.getID()));
			area.setEstimateCost(cost);
			OPEN.add(area);
		}else{
			logger.info("start (" + start + ") or goal (" + goal + ") is null. return false;");
			logger.info("///// NAITORouter.AStar(" + context + ") end. /////");
			return null;
		}
		
		boolean found = false;
		ancestors.put(startArea, startArea);
		do{
			NAITOArea nCurrent = removeMinimumCostArea(OPEN);//OPENから，コストの見積りが最小のエリアを取り出す
			if(nCurrent == null)
				break; //そんなもんはないのでループ終了
			if(nCurrent.getID().getValue() == goal.getID().getValue()){
				// Goal.
				found = true;
				break;
			}
			List<Area> areas = getNeighbours(nCurrent.getStandardArea());
			for(Area area : areas){
				int newCost = estimateCost(ancestors, startArea, area, goalArea); //コストの見積り計算
				NAITOArea nArea = (NAITOArea)(owner.allNAITOAreas.get(area.getID()));
				if(!OPEN.contains(nArea) && !CLOSED.contains(nArea)){
					OPEN.add(nArea);
					nArea.setEstimateCost(newCost);
					ancestors.put(nArea.getStandardArea(), nCurrent.getStandardArea());
				}else{
					int oldCost = nArea.getEstimateCost();
					if(newCost < oldCost){
						nArea.setEstimateCost(newCost);
						ancestors.remove(nArea.getStandardArea());
						ancestors.put(nArea.getStandardArea(), nCurrent.getStandardArea());
						if(CLOSED.contains(nArea)){
							CLOSED.remove(nArea);
							OPEN.add(nArea);
						}
					}
				}
			}
		}while(!found && !OPEN.isEmpty());
		if(!found){
			return null;
		}
		//closedをリストにして返す
	}
	
	private NAITOArea removeMinimumCostArea(List<NAITOArea> open){
		int minCost = Integer.MAX_VALUE;
		int index = -1;
		for(int i = 0;i < open.size();i++){
			NAITOArea area = open.get(i);
			if(area.getEstimateCost() < minCost){
				minCost = area.getEstimateCost();
				index = i;
			}
		}
		if(index == -1)
			return null;
		return open.remove(index);
	}
	private int estimateCost(Map<Area, Area> ancestors, Area start, Area position, Area goal){
		if(ancestors.isEmpty()){
			int startX = start.getX();
			int startY = start.getY();
			int endX   = goal.getX();
			int endY   = goal.getY();
			return euclidDistance(startX, startY, endX, endY);
		}
		Area current = position;
		Area previous = null;
		int estimateG = 0;
		do{
			previous = current;
			current = ancestors.get(current);
			estimateG += estimateGValue(previous, current);
		}while(current.getID().getValue() != start.getID().getValue());
		
		int startX = position.getX();
		int startY = position.getY();
		int endX   = goal.getX();
		int endY   = goal.getY();
		int estimateH = euclidDistance(startX, startY, endX, endY);
		
		int result = estimateG + estimateH;
		return result;
	}
	private int estimateGValue(Area previous, Area current){
		if(!previous.getNeighbours().contains(current.getID())){
			return -1;
		}
		
		int cost = 0;
		
		Edge edgeTo = current.getEdgeTo(previous.getID());
		int  startX = current.getX();
		int  startY = current.getY();
		int  edgeX  = (edgeTo.getEndX() + edgeTo.getStartX()) / 2;
		int  edgeY  = (edgeTo.getEndY() + edgeTo.getStartY()) / 2;
		
		cost = euclidDistance(startX, startY, edgeX, edgeY);
		
		int endX = previous.getX();
		int endY = previous.getY();
		
		cost += euclidDistance(edgeX, edgeY, endX, endY);
		return cost;
	}
	
	private int euclidDistance(int startX, int startY, int endX, int endY){
		int dX = endX - startX;
		int dY = endY - startY;
		
		return (int) Math.sqrt((dX * dX) + (dY * dY));
	}
	private List<Area> getNeighbours(Area area){
		List<Area> result = new ArrayList<Area>();
		for(EntityID nID : area.getNeighbours()){
			Area a = (Area)(model.getEntity(nID));
			result.add(a);
		}
		return result;
	}
    /**
	   経路の幅優先探索
       Do a breadth first search from one location to the closest (in terms of number of nodes) of a set of goals.
       @param start The location we start at.
       @param goals The set of possible goals.
       @return The path from start to one of the goals, or null if no path can be found.
    */
    public List<EntityID> breadthFirstSearch(StandardEntity start, Collection<? extends StandardEntity> goals) {
        List<StandardEntity> open = new LinkedList<StandardEntity>();
        Map<StandardEntity, StandardEntity> ancestors = new HashMap<StandardEntity, StandardEntity>();
        open.add(start);
        StandardEntity next = null;
        boolean found = false;
        ancestors.put(start, start);
        do {
            next = open.remove(0);
            if (isGoal(next, goals)) {
                found = true;
                break;
            }
            Collection<StandardEntity> neighbours = findNeighbours(next);
            if (neighbours.isEmpty()) {
                continue;
            }
            for (StandardEntity neighbour : neighbours) {
                if (isGoal(neighbour, goals)) {
                    ancestors.put(neighbour, next);
                    next = neighbour;
                    found = true;
                    break;
                }
                else {                                                                           
                    if (!ancestors.containsKey(neighbour) && !(neighbour instanceof Building)){ 
                       //&& neighbour instanceof Road && !((Road)neighbour).isBlockadesDefined()) {
                        open.add(neighbour);
                        ancestors.put(neighbour, next);
                    }
                }
            }
        } while (!found && !open.isEmpty());
        if (!found) {
            // No path
            return null;
        }
        // Walk back from goal to start
        StandardEntity current = next;
        List<EntityID> path = new LinkedList<EntityID>();
        //        Logger.debug("Building path");
        //        Logger.debug("Goal found: " + current);
        do {
            path.add(0, current.getID());
            current = ancestors.get(current);
            //            Logger.debug("Parent node: " + current);
            if (current == null) {
                throw new RuntimeException("Found a node with no ancestor! Something is broken.");
            }
        } while (current != start);
        //        Logger.debug("Final path: " + path);
        return path;
    }

    /**
       Get the neighbours of an entity.
       @param e The entity to look up.
       @return All neighbours of that entity.
    */
    public Collection<StandardEntity> findNeighbours(StandardEntity e) {
        Collection<StandardEntity> result = new ArrayList<StandardEntity>();
        if (e instanceof Area) {
            Area a = (Area)e;
            for (EntityID next : a.getNeighbours()) {
                result.add(model.getEntity(next));
            }
        }
        return result;
    }

    private boolean isGoal(StandardEntity e, Collection<? extends StandardEntity> test) {
        for (StandardEntity next : test) {
            if (next.getID().equals(e.getID())) {
                return true;
            }
        }
        return false;
    }
	public String hogeString(){
		return owner.toString();
	}
}
