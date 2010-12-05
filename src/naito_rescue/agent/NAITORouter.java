package naito_rescue.agent;

import rescuecore2.misc.*;
import rescuecore2.misc.geometry.*;
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
	HashMap<Area, Area>    ancestors;

    public NAITORouter(NAITOAgent owner) {
		this.owner = owner;
		this.world = owner.getWorldModel();
		this.logger = this.owner.getLogger();
		
		OPEN = new ArrayList<Area>();
		CLOSED = new ArrayList<Area>();
		estimates = new HashMap<Area, Integer>();
		ancestors = new HashMap<Area, Area>();
	}

    public List<EntityID> breadthFirstSearch(StandardEntity start, StandardEntity... goals) {
        return breadthFirstSearch(start, Arrays.asList(goals));
    }
	
	//isPassable => delegete
	public boolean isPassable(List<EntityID> path){
		logger.setContext("isPassable()");
		if(path.size() == 2){
			logger.trace("path.size() = 2 (" + path + ")");
			logger.debug("Delegate to isPassableFromCenterToNext()");
			logger.unsetContext();
			return isPassableFromCenterToNext(path);
		}else if(path.size() > 2){
			logger.trace("path.size() > 2 (" + path + ")");
			logger.debug("Delegate to isPassableAll()");
			logger.unsetContext();
			return isPassableAll(path);
		}else{
			//error.
			logger.info("path.size() != 2 && path.size() <= 2 ... error.");
			return true;
		}
	}
	
	private boolean isPassableArea(Line2D passLine, Area area){
		logger.setContext("isPassableArea()");
		
		List<EntityID> blockades = area.getBlockades();
		if(blockades == null || blockades.size() == 0){
			logger.info("No Blockade in " + area + "");
			logger.unsetContext();
			return true;
		}
		
		//全閉塞についてのLine2Dを取得
		List<Line2D> blockadeLines = new ArrayList<Line2D>();
		for(EntityID bID : blockades){
			Blockade b = (Blockade) (world.getEntity(bID));
			int[] apexes = b.getApexes();
			for(int i = 0; i < apexes.length-3; i += 2){
				Point2D first  = new Point2D(apexes[i], apexes[i+1]);
				Point2D second = new Point2D(apexes[i+2], apexes[i+3]);
				blockadeLines.add(new Line2D(first, second));
				logger.debug("blockadesLines.add(" + first + " => " + second + "");
			}
			Point2D last = new Point2D(apexes[apexes.length-2], apexes[apexes.length-1]);
			Point2D first = new Point2D(apexes[0], apexes[1]);
			logger.debug("blockadesLines.add(" + last + " => " + first + "");
			blockadeLines.add(new Line2D(last, first));
			
		}
		for(Line2D line : blockadeLines){
			if(GeometryTools2D.getSegmentIntersectionPoint(line, passLine) != null){
				logger.debug("blockade line = " + line);
				logger.debug("passLine      = " + passLine);
				logger.debug("=> return false;");
				logger.unsetContext();
				return false;
			}else{
				logger.debug("getSegmentIntersectionPoint() == null.ok.");
			}
		}
		logger.debug("return true;");
		logger.unsetContext();
		return true;
	}
	private boolean isPassableFromCenterToNext(List<EntityID> path){
		logger.setContext("isPassableFromCenterToNext");
		Area from = (Area) (world.getEntity(path.get(0)));
		Area to   = (Area) (world.getEntity(path.get(1)));
		
		int fromX = from.getX();
		int fromY = from.getY();
		
		Edge adjacentEdge = from.getEdgeTo(to.getID());
		int toX = ( adjacentEdge.getEndX() - adjacentEdge.getStartX() ) / 2;
		int toY = ( adjacentEdge.getEndY() - adjacentEdge.getStartY() ) / 2;
		
		Line2D passLine = new Line2D(new Point2D(fromX, fromY), new Point2D(toX, toY));

		
		logger.unsetContext();
		return isPassableArea(passLine, from);
	}
	private boolean isPassableAll(List<EntityID> path){
		logger.setContext("isPassableAll()");
		if(!isPassableFromCenterToNext(path)){
			logger.debug("return false;");
			logger.unsetContext();
			return false;
		}
		//2番目の要素から見ていく
		logger.debug("Entering for loop...");
		for(int i = 1; i < path.size()-1; i++){
			Area currentArea = (Area)(world.getEntity(path.get(i)));
			Area previousArea = (Area)(world.getEntity(path.get(i-1)));
			Area nextArea = (Area)(world.getEntity(path.get(i+1)));
			logger.debug("currentArea  = " + currentArea);
			logger.debug("previousArea = " + previousArea);
			logger.debug("nextArea     = " + nextArea);
			
			Edge previousEdge = currentArea.getEdgeTo(previousArea.getID());
			Edge nextEdge = currentArea.getEdgeTo(nextArea.getID());
			
			int fromX = ( previousEdge.getEndX() - previousEdge.getStartX() ) / 2;
			int fromY = ( previousEdge.getEndY() - previousEdge.getStartY() ) / 2;
			int toX = ( nextEdge.getEndX() - nextEdge.getStartX() ) / 2;
			int toY = ( nextEdge.getEndY() - nextEdge.getStartY() ) / 2;
			
			Line2D passLine = new Line2D(new Point2D(fromX, fromY),
			                             new Point2D(toX, toY));
			logger.debug("passLine = " + passLine);
			if(!isPassableArea(passLine, currentArea)){
				logger.debug("return false(in for loop)");
				logger.unsetContext();
				return false;
			}
		}
		logger.debug("Exiting for loop.");
		logger.debug("return true;");
		logger.unsetContext();
		return true;
	}
	// A*
	public List<EntityID> AStar(Area from, Area to){
		logger.setContext("AStar");
		logger.info("********** AStar **********");
		
		//リストの初期化
		OPEN.clear(); logger.debug("OPEN.clear();");
		CLOSED.clear(); logger.debug("CLOSED.clear();");
		estimates.clear(); logger.debug("estimates.clear();");
		
		OPEN.add(from); logger.debug("OPEN.add(" + from + ");");
		int result = estimateCost(from, to, (Area)(owner.getLocation()));
		logger.info("estimateCost(from, to, current) = " + result + " (... first time)");
		estimates.put(from, new Integer(0));
		
		//BIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIG EYE!!!!!!!!
		Area minCostArea = null;
		logger.setContext("AStar=>enter_while_loop");
		while(!estimates.isEmpty() && (minCostArea = getMinCostArea()) != null){
			logger.info("********** while loop **********");
			logger.info("** minCostArea = " + minCostArea + ", cost = " + estimates.get(minCostArea) + ".");
			logger.debug("** OPEN   = " + OPEN);
			logger.debug("** CLOSED = " + CLOSED);
			logger.trace("** estimates = " + estimates);
			estimates.remove(minCostArea);
			logger.trace("** after remove estimates = " + estimates);
			
			if(minCostArea.getID().getValue() == to.getID().getValue()){
				//find path.
				logger.info("++++++++ Find Path!! ++++++++");
				CLOSED.add(minCostArea);
				//ancestors.put(minCostArea, to);
				
				List<EntityID> results = closed2idList(to);
				for(EntityID hoge : results){
					logger.info("++ " + hoge + ", estimates(" + hoge + ").get = " + estimates.get(hoge));
				}
				
				logger.unsetContext();
				return results;
			}
			//for debug.
			if(OPEN.contains(minCostArea)){
				logger.debug("OPEN.contains(" + minCostArea + ")");
			}else{
				logger.debug("! OPEN.contains(" + minCostArea + ")");
			}
			//end for debug.
			
			//OPEN.remove(minCostArea);
			//OPEN.clear();
			CLOSED.add(minCostArea);
			logger.debug("CLOSED = " + CLOSED.toString());
			
			List<EntityID> neighbours = minCostArea.getNeighbours();
			logger.info("Checking minCostArea.neighbours...");
			if(neighbours != null){
				for(EntityID id : neighbours){
					StandardEntity entity = world.getEntity(id);
					if(entity instanceof Area){
						Area neighbour = (Area)entity;
						logger.info("Candidate: " + neighbour);
						int currentCost = estimateCost(from, to, neighbour);
					//	int g = g.get(currentArea) + distance(currentArea/*minCostArea*/ , neighbour); //distance = neighbour's distance.
					//	int h = h(neighbour); //euclid distance.
					//	if(!OPEN.containsValue(neighbour) || g.get(neighbour) > g ){
					//		g.put(neighbour, g);
					//		OPEN.removeByValue(neighbour);
					//		OPEN.put(h + g, neighbour);
					//	}
					// //xxまでの部分がゴソッといらなくなる
						logger.debug("currentCost = " + currentCost);
						//if(CLOSED.contains(neighbour)){
						if(OPEN.contains(neighbour) || CLOSED.contains(neighbour)){
							logger.debug("CLOSED.contains(" + neighbour + ")");
							if(! estimates.containsKey(neighbour)){
								//Bad.
								logger.info("estimatesがneighbourをkeyとして含んでいない");
								continue;
							}
							int previousCost = estimates.get(neighbour);
							logger.debug("previousCost = " + previousCost);
							if(previousCost > currentCost){
								logger.info("Exchange CLOSED and OPEN. area = " + neighbour);
								logger.debug("previousCost(" + previousCost + ") > currentCost(" + currentCost + ")");
								estimates.remove(neighbour);
								estimates.put(neighbour, new Integer(currentCost));
								ancestors.remove(neighbour);
								ancestors.put(neighbour, minCostArea);
								System.out.println("++  edit "+neighbour + ";;;" + minCostArea);
								if(CLOSED.contains(neighbour)){
									CLOSED.remove(neighbour);
									OPEN.add(neighbour);
								}
								break; //exit for loop.
							}
						}else{
							logger.debug("OPEN.add(" + neighbour + ")");
							logger.debug("estimates.put(" + neighbour + ", " + currentCost);
							//CLOSED.remove(neighbour);
							OPEN.add(neighbour);
							estimates.put(neighbour, new Integer(currentCost));
							ancestors.put(neighbour, minCostArea);
							System.out.println("++  add "+neighbour + ";;;" + minCostArea);
						} //xx
					}
				}
			}
		}
		logger.unsetContext();
		logger.unsetContext();
		logger.info("return null; ... path is not found.");
		return null;
	}
	//estimatesリストからコスト最小のAreaを返す
	private Area getMinCostArea(){
		logger.setContext("getMinCostArea()");
		Area result = null;
		int minCost = Integer.MAX_VALUE;
		for(Area area : OPEN){
			logger.info("Candidate: " + area);
			if(!estimates.containsKey(area)){
				logger.info("あり得ないことだとは思いますがgagaga");
				continue;
			}
			int cost = estimates.get(area);
			logger.debug("area = " + area.getID() + ", cost = " + cost + " (minCost = " + minCost + ")");
			if(cost < minCost){
				logger.debug("Exchange area.");
				minCost = cost;
				result = area;
				logger.debug("minCost = " + minCost + ", result = " + result);
			}
		}
		if(result == null){
			//Bad.
			logger.info("resultがnull estimates.keySet()がnullを返してたりしてね");
			logger.unsetContext();
			return null;
		}
		logger.info("return " + result);
		logger.unsetContext();
		return result;
	}
	
	public List<EntityID> closed2idList(Area goal){
		logger.setContext("closed2idList()");
		if(CLOSED == null || CLOSED.isEmpty()){
			//Bad.
			logger.info("CLOSED == null || CLOSED.isEmpty() => return null;");
			logger.info("うそーん");
			logger.unsetContext();
			return null;
		}
		ArrayList<EntityID> result = new ArrayList<EntityID>();
		int hoge = 0;System.out.println("++      foal"+goal);
		System.out.println(ancestors.containsKey(goal)+" +++ " + ancestors);
		for(Area ret = goal; ret != null ;ret = ancestors.get(ret)){
			result.add(ret.getID());
			//System.out.println((hoge++) + ";;;" + ret);
		}
		Collections.reverse(result);
		logger.info("return " + result);
		logger.unsetContext();
		return result;
		//return null; //for compile.
	}
	public int estimateCost(Area from, Area to, Area current){
		logger.setContext("estimateCost(" + from.getID().getValue() + ", " + to.getID().getValue() + ", " + current.getID().getValue() + ")");
		logger.info("estimateCost();");
		int g = 0, h = 0; //f(current) = g(current) + h(current);
		
		if(CLOSED.isEmpty()){
			logger.info("CLOSED.isEmpty();");
			if(from.getID().getValue() != current.getID().getValue()){
				//Bad.
				logger.info("AStar開始時点の呼び出しのはずがそうではなかったようだ");
				logger.debug("from    = " + from);
				logger.debug("current = " + current);
				logger.unsetContext();
				return -1;
			}
			
			int result = euclidDistance(from, to);
			logger.debug("CLOSED.isEmpty() -> return " + result + "");
			logger.unsetContext();
			return euclidDistance(from, to);
		}
		//CLOSEDを順番にたどってg値の見積りを行う
		logger.info("CLOSED tekuteku.");
		Area lastArea = CLOSED.get(CLOSED.size()-1);
		logger.debug("lastArea = " + lastArea);
		for(int i = 0;CLOSED.get(i).getID().getValue() != lastArea.getID().getValue();i++){
			Area currentArea = CLOSED.get(i);
			Area nextArea = CLOSED.get(i+1);
			
			logger.debug("currentArea = " + currentArea);
			logger.debug("nextArea = " + nextArea);
			//for debug.
			Edge centerToAdj = currentArea.getEdgeTo(nextArea.getID());
			if(centerToAdj == null){
				//Bad.
				logger.info("隣接してないよぶわあぁーか (c)みつどもえ");
			}
			//end for debug.
			int distance = adjacentDistance(currentArea, nextArea);
			logger.debug("distance = " + distance);
			g += distance;
			logger.trace("iii = " + i);
		}
		//for debug.
		boolean success = false;
		List<EntityID> neighbours = lastArea.getNeighbours();
		for(EntityID id : neighbours){
			StandardEntity entity = world.getEntity(id);
			logger.debug("lastArea.neighbour = " + entity);
			if(entity.getID().getValue() == current.getID().getValue()){
				logger.info("Estimate \" g\" is SUCCESS!!!!!!!!!!!!!(多分)");
				success = true;
			}
		}
		if(success){
			int lastAreaToCurrent = adjacentDistance(lastArea, current);
			g += lastAreaToCurrent;
		}else{
			logger.info("lastAreaとcurrentが隣接していない");
			logger.debug("lastArea = " + lastArea + ", current = " + current);
		}
		//end for debug.
		// h値の見積り
		h = euclidDistance(current, to);
		
		logger.info("h = " + h + ", g = " + g + " => return " + (g + h));
		logger.unsetContext();
		return g + h;
		//return -1; //for compile.
		
	}
	
	//adjacentDistance(Area, Area);
	//a1の中点 -> a1とa2をつなぐエッジ -> a2の中点までの距離を返す
	//(つまりエージェントがa1からa2まで通る軌跡の距離)
	//a1とa2は隣接していなければならない
	private int adjacentDistance(Area a1, Area a2){
		logger.setContext("adjacentDistance()");
		logger.debug("a1 = " + a1 + ", a2 = " + a2);
		Edge centerToAdj = a1.getEdgeTo(a2.getID());
		
		if(centerToAdj == null){
			logger.info(a1 + "と" + a2 + "は隣接していません");
			return 1;
		}
		logger.debug("Adjacent Edge: " + centerToAdj);
		
		int adjCenterX = (centerToAdj.getEndX() + centerToAdj.getStartX()) / 2;
		int adjCenterY = (centerToAdj.getEndY() + centerToAdj.getStartY()) / 2;
		Line2D fromCenterToEdge = new Line2D(a1.getX(), a1.getY(), adjCenterX, adjCenterY);
		Line2D fromEdgeToNextArea = new Line2D(adjCenterX, adjCenterY, a2.getX(), a2.getY());
		
		logger.debug("Adjacent Edge center X = " + adjCenterX);
		logger.debug("Adjacent Edge center Y = " + adjCenterY);
		logger.debug("Area a1's center X = " + a1.getX());
		logger.debug("Area a1's center Y = " + a1.getY());
		logger.debug("Area a2's center X = " + a2.getX());
		logger.debug("Area a2's center Y = " + a2.getY());
		logger.debug("a1's center to adjacent edge: " + fromCenterToEdge);
		logger.debug("adjacent edge to a2's center: " + fromEdgeToNextArea);
		
		int d1 = euclidDistance(fromCenterToEdge);
		int d2 = euclidDistance(fromEdgeToNextArea);
		
		logger.debug("distance from a1's center to edge = " + d1);
		logger.debug("distance from edge to a2's center = " + d2);
		logger.info("return " + (d1+d2));
		logger.unsetContext();
		return d1 + d2;
	}
	private int euclidDistance(Line2D line){
		logger.setContext("euclidDistance(Line2D)");
		int startX = (int) line.getOrigin().getX();
		int startY = (int) line.getOrigin().getY();
		int endX = (int) line.getEndPoint().getX();
		int endY = (int) line.getEndPoint().getY();
		logger.debug("line = " + line);
		logger.debug("line start X = " + startX);
		logger.debug("line start Y = " + startY);
		logger.debug("line end   X = " + endX);
		logger.debug("line end   Y = " + endY);
		
		int dx = endX - startX;
		int dy = endY - startY;
		logger.debug("difference of X = " + dx);
		logger.debug("difference of Y = " + dy);
		
		int result = (int) Math.sqrt((dx*dx) + (dy*dy));
		logger.info("return " + result);
		logger.unsetContext();
		return result;
	}
	private int euclidDistance(Area a, Area b){
		//aからbまでのユークリッド距離を返す
		logger.setContext("euclidDistance(Area, Area)");
		logger.debug("euclidDistance(" + a.getID().getValue() + ", " + b.getID().getValue() + ")");
		double startX = a.getX();
		double startY = a.getY();
		double toX = b.getX();
		double toY = b.getY();
		
		double disX = toX - startX;
		double disY = toY - startY;
		logger.trace("(" + startX + ", " + startY + ") => (" + toX + ", " + toY + ") (" + disX + ":" + disY + ")");
		
		int result = (int) Math.sqrt((disX*disX) + (disY*disY));
		logger.info("return " + result);
		logger.unsetContext();
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
