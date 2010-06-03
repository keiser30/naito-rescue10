package naito_rescue.agent;

import rescuecore2.misc.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.entities.*;

import naito_rescue.*;
import java.util.*;

public final class MySearch {
    private StandardWorldModel world;
	private NAITOAgent         owner;
	private MyLogger           logger;
	
	private int                           initCost[][];
	private int                           cost[][];
	private Collection<StandardEntity>    allRoads;
	private Collection<StandardEntity>    allBuildings;
	private LinkedList<Road>              allRoadsList;
	private LinkedList<Building>          allBuildingsList;
	private int                           numRoads;
	private int                           numBuildings;
	private Hashtable<Integer, Integer>   road2idx;
	private Hashtable<Integer, Integer>   building2idx;

	//private HashTable<IDPair, Integer> roadIDPair2cost;

    /**
       Construct a new MySearch.
       @param world The world model to search.
     */
    public MySearch(StandardWorldModel world, NAITOAgent owner) {
        this.world = world;
		this.owner = owner;
		this.logger = owner.getLogger();
		this.allRoads = this.world.getEntitiesOfType(StandardEntityURN.ROAD);
		this.allBuildings = this.world.getEntitiesOfType(StandardEntityURN.BUILDING);

		allRoadsList = new LinkedList<Road>();
		allBuildingsList = new LinkedList<Building>();
		for(Iterator it = this.allRoads.iterator();it.hasNext();){
			allRoadsList.add((Road)(it.next()));
		}
		for(Iterator it = this.allBuildings.iterator();it.hasNext();){
			allBuildingsList.add((Building)(it.next()));
		}
    	this.numRoads = allRoadsList.size();
		this.numBuildings = allBuildingsList.size();
		this.road2idx = new Hashtable<Integer, Integer>();
		this.building2idx = new Hashtable<Integer, Integer>();
		this.initCost = new int[this.numRoads][this.numRoads];
		this.cost = new int[this.numRoads][this.numRoads];

		for(int i = 0;i < numRoads;i++){
			road2idx.put(allRoadsList.get(i).getID().getValue(), i);
		}
		for(int i = 0;i < numBuildings;i++){
			building2idx.put(allBuildingsList.get(i).getID().getValue(), i);
		}
		logger.info("MySearch is constructed.");
//		costInit();
	}

	private void costInit(){
		logger.info("Mysearch.costInit();");
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
		for(int i = 0;i < numRoads;i++){
			from = allRoadsList.get(i);

			logger.trace("Now processing... "+from);
			fromIdx = from.getID().getValue();
			neighbours = findNeighbours(from);
			for(StandardEntity neighbour : neighbours){
				logger.trace("neighbour = " + neighbour);
				if(neighbour instanceof Road){
					//logger.trace("neighbour = " + neighbour);
					toIdx = ((Road)neighbour).getID().getValue();
					initCost[road2idx.get(fromIdx)][road2idx.get(toIdx)] = world.getDistance(from.getID(), neighbour.getID());
					initCost[road2idx.get(toIdx)][road2idx.get(fromIdx)] = world.getDistance(from.getID(), neighbour.getID());
					logger.trace("initCost["+fromIdx+"]["+toIdx+"] = " + initCost[road2idx.get(fromIdx)][road2idx.get(toIdx)]);
				}
			}
		}
		
		for(int i = 0;i < numRoads;i++){
			for(int j = 0;j < numRoads;j++){
				cost[i][j] = initCost[i][j];
			}
		}

		logger.info("MySearch.costInit(); DONE!");
	}


	public List<EntityID> getRoute(EntityID fromID, EntityID toID){
		logger.info("MySearch.getRoute();");
		for(int i = 0;i < numRoads;i++){
			for(int j = 0;j < numRoads;j++){
				cost[i][j] = initCost[i][j];
			}
		}

		logger.debug("fromID = " + fromID.getValue() + ", toID = " + toID.getValue());
		boolean     doneArea[] = new boolean[numRoads];
		EntityID    prevArea[] = new EntityID[numRoads];
		EntityID    areaIdx;

		areaIdx = fromID;
		doneArea[road2idx.get(fromID.getValue())] = true;
		prevArea[road2idx.get(fromID.getValue())] = fromID;
		logger.trace("Entering while loop");
		//EntityID nextIdx = null, prevIdx = null;
		while(areaIdx.getValue() != toID.getValue()){
			int minCost = Integer.MAX_VALUE;
			EntityID nextIdx = null;
			EntityID prevIdx = null;

			logger.trace("areaIdx = " + areaIdx.getValue());
			for(int i = 0;i < numRoads;i++){
				if(doneArea[i]){
					for(int j = 0;j < numRoads;j++){
						if(doneArea[j]) continue;
						int c = cost[i][j];
						if(minCost > c && c > 0){
							prevIdx = allRoadsList.get(i).getID();
							nextIdx = allRoadsList.get(j).getID();
							minCost = c;
						}
					}
				}
			}
			if(nextIdx == null){
				logger.trace("--------------------- nextIdx is null!");
			}
			/*else{
				logger.debug("^^^^^^^^^^^^^^^^^^^^ unknown is null!");
			}
			logger.debug(" --> nextIdx = " + nextIdx.getValue());
			*/
			prevArea[road2idx.get(nextIdx.getValue())] = prevIdx;
			if(nextIdx.getValue() == toID.getValue()){
				logger.trace("break while loop.");
				logger.trace("nextIdx = " + nextIdx.getValue() + ", toID.getValue() = " + toID.getValue());
				break;
			}
			int myCost = cost[road2idx.get(prevIdx.getValue())][road2idx.get(nextIdx.getValue())];
			int row_idx = road2idx.get(nextIdx.getValue());
			for(int i = 0;i < allRoadsList.size();i++){
				if(cost[row_idx][i] > 0){
					cost[row_idx][i] += myCost;
				}
			}

			doneArea[road2idx.get(nextIdx.getValue())] = true;
			areaIdx = nextIdx;
		}//end while.
		

		LinkedList<EntityID> ll = new LinkedList<EntityID>();
		ll.addFirst(toID);
		logger.trace("ll.addFirst(" + toID.getValue() + ")");
		int no = road2idx.get(toID.getValue());
		while(prevArea[no] != fromID){
			ll.addFirst(prevArea[no]);
			logger.trace("ll.addFirst(" + (prevArea[no]).getValue() + ")");
			no = road2idx.get((prevArea[no]).getValue());
		}
		ll.addFirst(fromID);
		logger.trace("ll.addFirst(" + fromID.getValue() + ")");

		StringBuffer logstr = new StringBuffer();
		for(int i = 0;i < ll.size();i++){
			logstr.append(ll.get(i).getValue()+" -> ");
		}
		logger.debug(logstr.toString());
		logger.info("MySearch.getRoute(); DONE!");
		return ll;
	}

	public boolean isPassable(StandardEntity from, StandardEntity to){
		List<EntityID> path = breadthFirstSearch(from, Collections.singleton(to));
		if(path == null){
			return false;
		}else{
			return true;
		}
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
