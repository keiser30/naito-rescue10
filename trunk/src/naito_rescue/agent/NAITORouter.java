package naito_rescue.agent;

import rescuecore2.misc.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.entities.*;

import naito_rescue.*;
import java.util.*;

public final class NAITORouter{
	private StandardWorldModel world;
	private NAITOAgent         owner;
	private MyLogger           logger;
	
	ArrayList<Area> OPEN, CLOSED;
	HashMap<Area, Integer> estimates;

    public NAITORouter(NAITOAgent owner) {
		this.owner = owner;
		this.world = owner.getWorldModel();
		this.logger = this.owner.getLogger();
		
		OPEN = new ArrayList<Area>();
		CLOSED = new ArrayList<Area>();
		estimates = new HashMap<Area, Integer>();
	}

    public List<EntityID> breadthFirstSearch(StandardEntity start, StandardEntity... goals) {
        return breadthFirstSearch(start, Arrays.asList(goals));
    }


	// A*
	public List<EntityID> AStar(Area from, Area to){
		//初期化
		OPEN.clear();
		CLOSED.clear();
		estimates.clear();
		
		return null; //for compile.
	}
	public int estimateCost(Area from, Area to, Area current){
		int g = 0, h = 0; //f(current) = g(current) + h(current);
		
		if(from.getID().getValue() == current.getID().getValue()){
			//g = 0なので、hの見積り(currentからtoへのユークリッド距離)を返す
			return euclidDistance(current, to);
		}
		
		//1. gの見積り　... fromからcurrentまでの距離の見積り
		//1.1 fromから次のAreaまでの距離の見積り
		//1.2 fromの次からcurrentまでの距離の見積り
		
		return -1; //for compile.
		
	}
	private int euclidDistance(Area a, Area b){
		//aからbまでのユークリッド距離を返す
		double startX = a.getX();
		double startY = a.getY();
		double toX = b.getX();
		double toY = b.getY();
		
		double disX = toX - startX;
		double disY = toY - startY;
		return (int) Math.sqrt((disX*disX) + (disY*disY));
	}
	public boolean isPassable(Area previous, Area current, Area next){
		return false; //for compile.
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
                result.add(world.getEntity(next));
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
