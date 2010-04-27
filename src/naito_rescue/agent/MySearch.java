package naito_rescue.agent;

import rescuecore2.worldmodel.*;
import rescuecore2.standard.entities.*;

import naito_rescue.*;
import java.util.*;

public final class MySearch {
    private StandardWorldModel world;
	private NAITOHumanoidAgent owner;
	private MyLogger           logger;
	
	private int                        initCost[][];
	private int                        cost[][];
	private Collection<StandardEntity> allRoads;
	private Collection<StandardEntity> allBuildings;
	private int                        numRoads;


    /**
       Construct a new MySearch.
       @param world The world model to search.
     */
    public MySearch(StandardWorldModel world, NAITOHumanoidAgent owner) {
        this.world = world;
		this.owner = owner;
		this.logger = owner.getLogger();
		this.allRoads = this.world.getEntitiesOfType(StandardEntityURN.ROAD);
		this.allBuildings = this.world.getEntitiesOfType(StandardEntityURN.BUILDING);
    	this.numRoads = allRoads.size();

		this.initCost = new int[this.numRoads][this.numRoads];
		this.cost = new int[this.numRoads][this.numRoads];
		
		costInit();
	}

	private void costInit(){
		for(int i = 0;i < numRoads;i++){
			Arrays.fill(initCost[i], 0);
			initCost[i][i] = -1;
		}

		// 隣接する道路と道路について，
		// それらを結ぶ長さを初期コストとする．
		// (コスト配列の添字はID)
		Collection<StandardEntity> neighbours;
		Road from;
		int fromIdx, toIdx;
		for(Iterator<StandardEntity> it = allRoads.iterator();it.hasNext();){
			from = (Road)it.next();
			fromIdx = from.getID().getValue();
			neighbours = findNeighbours(from);
			for(StandardEntity neighbour : neighbours){
				if(neighbour instanceof Road){
					toIdx = ((Road)neighbour).getID().getValue();
					initCost[fromIdx][toIdx] = world.getDistance(from.getID(), neighbour.getID());
					initCost[toIdx][fromIdx] = world.getDistance(from.getID(), neighbour.getID());
				}
			}
		}
		
		for(int i = 0;i < numRoads;i++){
			for(int j = 0;j < numRoads;j++){
				cost[i][j] = initCost[i][j];
			}
		}
	}


	public List<EntityID> getRoute(EntityID fromID, EntityID toID){
		
		for(int i = 0;i < numRoads;i++){
			for(int j = 0;j < numRoads;j++){
				cost[i][j] = initCost[i][j];
			}
		}

		boolean     doneArea[] = new boolean[numRoads];
		EntityID    prevArea[] = new EntityID[numRoads];
		EntityID    areaIdx;

		areaIdx = fromID;
		doneArea[fromID.getValue()] = true;
		prevArea[fromID.getValue()] = fromID;
		while(areaIdx.getValue() != toID.getValue()){
			int minCost = Integer.MAX_VALUE;
			EntityID nextIdx;
			EntityID prevIdx;

			for(int i = 0;i < numRoads;i++){
				if(doneArea[i]){
					for(int j = 0;j < numRoads;j++){
						if(doneArea[j]) continue;
						int c = cost[i][j];
						if(minCost > c && c > 0){
							prevIdx = i;
							nextIdx = j;
							minCost  = c;
						}
					}
				}
			}//end for.

			prevArea[nextIdx.getValue()] = prevIdx;
			if(nextIdx.getValue() == toID().getValue()){
				break;
			}
			myCost = cost[prevIdx.getValue()][nextIdx.getValue()];
			for(int i = 0;i < numRoads.size();i++){
				if(cost[nextIdx.getValue()][i] > 0){
					cost[nextIdx.getValue()][i] += myCost;
				}
			}
			doneArea[nextIdx.getValue()] = true;
			areaIdx = nextIdx;
		}//end while.
		

		LinkedList<EntityID> ll = new LinkedList<EntityID>();
		ll.add(toID);
		EntityID no = toID;
		while(prevArea[no] != fromID){
			ll.add(prevArea[no]);
			no = prevArea[no];
		}
		ll.add(fromID);
		return ll;
	}


    /**
       Do a breadth first search from one location to the closest (in terms of number of nodes) of a set of goals.
       @param start The location we start at.
       @param goals The set of possible goals.
       @return The path from start to one of the goals, or null if no path can be found.
    */
    public List<EntityID> breadthFirstSearch(StandardEntity start, StandardEntity... goals) {
        return breadthFirstSearch(start, Arrays.asList(goals));
    }

	/**
		breadthFirstSearchの改良
		@return The path from start to one of the goals, or null if no path can be found.
	*/
/*
	public List<EntityID> getRoute(StandardEntity start, Collection<? extends StandardEntity> goals){

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
                    if (!ancestors.containsKey(neighbour) && !(neighbour instanceof Building)) {
                        open.add(neighbour);
                        ancestors.put(neighbour, next);
                    }
                }
            }
        } while (!found && !open.isEmpty());
	}
*/
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
                    if (!ancestors.containsKey(neighbour) && !(neighbour instanceof Building)) {
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
