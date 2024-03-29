package naito_rescue.router;

import java.util.*;
import java.io.*;

import rescuecore2.misc.*;
import rescuecore2.misc.geometry.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.entities.*;

import naito_rescue.*;
import naito_rescue.agent.*;
import naito_rescue.object.*;
import naito_rescue.router.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;
import naito_rescue.message.manager.*;

public class PassableChecker
{
	NAITOAgent         owner;
	StandardWorldModel model;
	MyLogger           logger;
	
	public PassableChecker(NAITOAgent owner){
		this.owner = owner;
		this.model = owner.getWorldModel();
		this.logger = owner.getLogger();
	}
	//超大雑把な経路の長さを返す
	public double approxLengthOfPath(List<EntityID> path){
		EntityID preID = owner.getLocation().getID();
		double result = 0.0;
		for(EntityID id : path){
			StandardEntity previous = model.getEntity(preID);
			StandardEntity current = model.getEntity(id);
			
			result += model.getDistance(previous, current);
			previous = current;
		}
		return result;
	}
	//ちょっと厳密に経路の長さを返す
	public double lengthOfPath(List<EntityID> path){
		//logger.info("");
		if(path == null || path.size() == 0){
			return 0;
		}
		else if(path.size() == 1){
			Area current = (Area)(owner.getLocation());
			Area neighbour = (Area)(model.getEntity(path.get(0)));
			if( !current.getNeighbours().contains(neighbour.getID()) ){
				//logger.info("Agent Location(" + current.getID().getValue() + ")'s neighbour is not contains path.get(0)=" + neighbour.getID().getValue() + ", return 0;");
				return 0;
			}
		}
		else if(path.size() >= 2){
			//pathは非破壊で検査しなければならないことに注意
			double length = 0;
			//エージェントのいる所 => パスの0番目に至るエッジの中点まで
			Area location = (Area)(owner.getLocation());
			Area neighbour = (Area)(model.getEntity(path.get(0)));
			if( !location.getNeighbours().contains(neighbour.getID()) ){
				//logger.info("Agent Location(" + location.getID().getValue() + ")'s neighbour is not contains path.get(0)=" + neighbour.getID().getValue() + ", return 0;");
				return 0;
			}
			Edge edgeTo = location.getEdgeTo(neighbour.getID());
			if(edgeTo == null){
				//logger.info("Edge is null; return 0;");
				return 0;
			}
			int neighbourX = (edgeTo.getEndX() + edgeTo.getStartX()) / 2;
			int neighbourY = (edgeTo.getEndY() + edgeTo.getStartY()) / 2;
			length += euclidDistance(owner.getX(), owner.getY(), neighbourX, neighbourY);
			
			//1番目からpath.size()-1番目まで
			List<EntityID> pathForCheck = new ArrayList<EntityID>();
			pathForCheck.addAll(path);
			pathForCheck.add(0, owner.getLocation().getID());
			for(int i = 1; i < path.size()-1; i++){
				Area previous = (Area)(model.getEntity( path.get(i-1) ));
				Area current  = (Area)(model.getEntity( path.get(i)   ));
				Area next     = (Area)(model.getEntity( path.get(i+1) ));
				
				Edge toPre = current.getEdgeTo(previous.getID());
				Edge toNext = current.getEdgeTo(next.getID());
				if(toPre == null || toNext == null){
					//logger.info("Edge is null(in 1 to path.size() -1); return 0;");
					return 0;
				}
				int startX = (toPre.getEndX() + toPre.getStartX()) / 2;
				int startY = (toPre.getEndY() + toPre.getStartY()) / 2;
				int endX = (toNext.getEndX() + toNext.getStartX()) / 2;
				int endY = (toNext.getEndY() + toNext.getStartY()) / 2;
				length += euclidDistance(startX, startY, endX, endY);
			}
			
			//path.size()-2 からpath.size()-1へ
			Area previous = (Area)(model.getEntity( path.get(path.size()-2) ));
			Area last = (Area)(model.getEntity( path.get(path.size()-1) ));
			Edge toPre = last.getEdgeTo(previous.getID());
			if(toPre == null){
				//logger.info("Edge is null(in last); return 0;");
				return 0;
			}
			int startX = (toPre.getEndX() + toPre.getStartX()) / 2;
			int startY = (toPre.getEndY() + toPre.getStartY()) / 2;
			int endX = last.getX();
			int endY = last.getY();
			length += euclidDistance(startX, startY, endX, endY);
			
			return length;
		}
		//logger.info("Monsters A Go Go.");
		return 0;
	}
	private double euclidDistance(int startX, int startY, int endX, int endY){
		int dX = endX - startX;
		int dY = endY - startY;
		
		//logger.info("dx = " + dX + ", dy = " + dY + "=> startX = " + startX + ", startY = " + startY + ", endX = " + endX + ", endY = " + endY + " => result = " + (int) Math.hypot(dX, dY) + " <= dx = " + dX + ", dy = " + dY);
		//return (int) Math.sqrt((dX * dX) + (dY * dY));
		return (int) Math.hypot(dX, dY);
	}
	//pathで示される経路が通行可能かどうか
	public boolean isPassable(List<EntityID> path){
		String context = path.toString();
		//logger.info("((((== PassableChecker.isPassable(" + context + ") ==))))");
		
		if(path == null || path.size() == 0){
			//logger.info("path == null OR path.size() == 0; return false;");
			//logger.info("((((== PassableChecker.isPassable(" + context + ") end. ==))))");
			return false;
		}
		else if(path.size() == 1){
			Area neighbour = (Area)(model.getEntity(path.get(0)));
			boolean result = isPassableFromAgentToNeighbour(neighbour);
			//logger.info("path().size == 1; return isPassableFromAgent(location=" + owner.getLocation().getID() + ")ToNeighbour(" + path.get(0) + "); => return " + result);
			//logger.info("((((== PassableChecker.isPassable(" + context + ") end. ==))))");
			return result;
		}
		else if(path.size() >= 2){
			//logger.info("Now, First Checking isPassableFromAgentToNeighbour(" + path.get(0) + ");");
			Area neighbour = (Area)(model.getEntity(path.get(0)));
			if(!isPassableFromAgentToNeighbour(neighbour)){
				//logger.info("!isPassableFromAgent(" + owner.getLocation().getID() + ")ToNeighbour(" + path.get(0) + "); return false;");
				//logger.info("((((== PassableChecker.isPassable(" + context + ") end. ==))))");
				return false;
			}
			
			//logger.info("Then, Add EntityID(" + owner.getLocation().getID().getValue() + ") to path.");
			path.add(0, owner.getLocation().getID());
			//logger.info("Current Path = " + path);
			
			//logger.info("Loop Checking path Previous To Next Through CurrentArea.");
			for(int i = 1; i < path.size()-1; i++){
				Area previous = (Area)(model.getEntity( path.get(i-1) ));
				Area current  = (Area)(model.getEntity( path.get(i)   ));
				Area next     = (Area)(model.getEntity( path.get(i+1) ));
				//logger.info("Previous Area = " + previous);
				//logger.info("Current  Area = " + current);
				//logger.info("Next     Area = " + next);
				if(!isPassableFromPreviousToNext(previous, current, next)){
					//logger.info("!isPassableFromPreviousToNext(" + previous + "," + current + "," + next + "); return false;");
					//logger.info("((((== PassableChecker.isPassable(" + context + ") end. ==))))");
					return false;
				}
			}
			//logger.info("At last, Checking Path Last and previous Last.");
			Area previous = (Area)(model.getEntity( path.get(path.size()-2) ));
			Area last     = (Area)(model.getEntity( path.get(path.size()-1) ));
			//logger.info("Previous(Last) = " + previous);
			//logger.info("Last           = " + last);
			//logger.info("Call isPassableFromEdgeToNextCenter();");
			boolean result = isPassableFromEdgeToNextCenter(previous, last);
			//logger.info("After Call isPassableFromEdgeToNextCenter(" + previous + ", " + last + "); result = " + result + ", return result;");
			//logger.info("((((== PassableChecker.isPassable(" + context + ") end. ==))))");
			return result;
		}
		//logger.info("Monster A Go Go.");
		return false;
	}
	
	private List<Blockade> getBlockadesList(Area area){
		List<Blockade> result = new ArrayList<Blockade>();
		
		if(!area.isBlockadesDefined() ||
		    area.getBlockades() == null ||
		    area.getBlockades().size() == 0){
			return result; //あれ，これっているのか?
		}
		for(EntityID bID : area.getBlockades()){
			Blockade blockade = (Blockade)(model.getEntity(bID));
			result.add(blockade);
		}
		//logger.info("getBlockadesList(" + area + ") ==> return result = [ " + result + " ]");
		return result;
	}
	//GeometryTools2Dの中にありそう
	private Line2D getLineOfPositionPair(int startX, int startY, int endX, int endY){
		Point2D pos1 = new Point2D(startX, startY);
		Point2D pos2 = new Point2D(endX, endY);
		
		return new Line2D(pos1, pos2);
	}
	private boolean isPassableFromAgentToNeighbour(Area neighbour){
		//logger.info("++++ isPassableFromAgentToNeighbour(" + neighbour + ") ++++");
		
		Area current = (Area)(owner.getLocation());
		List<Blockade> blockades = new ArrayList<Blockade>();
		//logger.info("Current Location = " + current);
		
		blockades.addAll(getBlockadesList(current));
		blockades.addAll(getBlockadesList(neighbour));
		//logger.info("Blockades(In isPassableFromAgentToNeighbour()) = [ " + blockades + " ]");
		
		Edge edgeTo = current.getEdgeTo(neighbour.getID());
		if(edgeTo == null){
			//logger.info("edgeTo is null. return false;");
			//logger.info("++++ isPassableFromAgentToNeighbour(" + neighbour + ") end. ++++");
			return false;
		}
		int edgeCenterX = (edgeTo.getEndX() + edgeTo.getStartX()) / 2;
		int edgeCenterY = (edgeTo.getEndY() + edgeTo.getStartY()) / 2;
		Line2D fromAgentToEdge = getLineOfPositionPair(owner.getX(), owner.getY(),
		                                               edgeCenterX,  edgeCenterY);
		Line2D fromEdgeToCenter = getLineOfPositionPair(edgeCenterX, edgeCenterY,
		                                                neighbour.getX(), neighbour.getY());

		Point2D intersectionPoint;		                                                
		for(Blockade blockade : blockades){
			List<Line2D> bLines = getBlockadeLine2Ds(blockade);
			for(Line2D bLine : bLines){
				intersectionPoint = GeometryTools2D.getSegmentIntersectionPoint(fromAgentToEdge, bLine);
				if(intersectionPoint != null){
					//logger.info("!!!!! Crossed Blockade (in isPassableFromAgentToNeighbour, fromAgentToEdge, CurrentArea = [ " + current + " ], Neighbour = [ " + neighbour + " ]) !!!!!");
					//logger.info("fromAgentToEdge = (" + fromAgentToEdge.getOrigin() + ")=>(" + fromAgentToEdge.getEndPoint() + ").");
					//logger.info("Intersect Blockade Line = (" + bLine.getOrigin() + ")=>(" + bLine.getEndPoint() + ")");
					//logger.info("Blockade = " + blockade);
					//logger.info("++++ isPassableFromAgentToNeighbour(" + neighbour + ") end. ++++");
					return false;
				}
				intersectionPoint = GeometryTools2D.getSegmentIntersectionPoint(fromEdgeToCenter, bLine);
				if(intersectionPoint != null){
					//logger.info("!!!!! Crossed Blockade (in isPassableFromAgentToNeighbour, fromEdgeToCenter, CurrentArea = [ " + current + " ], Neighbour = [ " + neighbour + " ]) !!!!!");
					//logger.info("fromEdgeToCenter = (" + fromEdgeToCenter.getOrigin() + ")=>(" + fromEdgeToCenter.getEndPoint() + ").");
					//logger.info("Intersect Blockade Line = (" + bLine.getOrigin() + ")=>(" + bLine.getEndPoint() + ")");
					//logger.info("Blockade = " + blockade);
					//logger.info("++++ isPassableFromAgentToNeighbour(" + neighbour + ") end. ++++");
					return false;
				}
			}
		}
		//logger.info("No Blockades Crossed(in isPassableFromAgentToNeighbour())");
		//logger.info("++++ isPassableFromAgentToNeighbour(" + neighbour + ") end. ++++");
		return true;
	}
	private boolean isPassableFromPreviousToNext(Area previous, Area current, Area next){
		String context = "[" + previous + "], [" + current + "], [" + next + "]";
		//logger.info("----- isPassableFromPreviousToNext(" + context + ") -----");
		
		List<Blockade> currentBlockades = getBlockadesList(current);
		if(currentBlockades.isEmpty()){
			//logger.info("There's No Blockade in CurrentArea(" + current + "). Path Through OK. return true;");
			//logger.info("----- isPassableFromPreviousToNext(" + context + ") end. -----");
			return true;
		}
		
		Edge toPrevious = current.getEdgeTo(previous.getID());
		Edge toNext     = current.getEdgeTo(next.getID());
		if(toPrevious == null || toNext == null){
			//logger.info("toPrevious == null || toNext == null => return false;");
			//logger.info("----- isPassableFromPreviousToNext(" + context + ") end. -----");
			return false;
		}
		int prevEdgeX = (toPrevious.getEndX() + toPrevious.getStartX()) / 2;
		int prevEdgeY = (toPrevious.getEndY() + toPrevious.getStartY()) / 2;
		int nextEdgeX = (toNext.getEndX() + toNext.getStartX()) / 2;
		int nextEdgeY = (toNext.getEndY() + toNext.getStartY()) / 2;
		//logger.info("Line: (" + prevEdgeX + ", " + prevEdgeY + ")=>(" + nextEdgeX + ", " + nextEdgeY + ")");
		
		Line2D line = getLineOfPositionPair(prevEdgeX, prevEdgeY, nextEdgeX, nextEdgeY);
		Point2D intersectionPoint;
		for(Blockade blockade : currentBlockades){
			List<Line2D> bLines = getBlockadeLine2Ds(blockade);
			for(Line2D bLine : bLines){
				intersectionPoint = GeometryTools2D.getSegmentIntersectionPoint(line, bLine);
				if(intersectionPoint != null){
					//logger.info("!!!!! Crossed Blockade (in isPassableFromPreviousToNext, line, PreviousArea = [ " + previous + " ], CurrentArea = [ " + current + " ], NextArea = [ " + next + " ]) !!!!!");
					//logger.info("line = (" + line.getOrigin() + ")=>(" + line.getEndPoint() + ").");
					//logger.info("Intersect Blockade Line = (" + bLine.getOrigin() + ")=>(" + bLine.getEndPoint() + ")");
					//logger.info("Blockade = " + blockade);
					//logger.info("----- isPassableFromPreviousToNext(" + context + ") end. -----");
					return false;
				}
			}
		}
		//logger.info("No Blockades Crossed(in isPassableFromPreviousToNext()).");
		//logger.info("----- isPassableFromPreviousToNext(" + context + ") end. -----");
		return true;
	}
	private boolean isPassableFromEdgeToNextCenter(Area previous, Area last){
		String context = "[" + previous + "], [" + last + "]";
		//logger.info("+=+=+=+= isPassableFromEdgeToNextCenter(" + context + ") +=+=+=+=");
		
		List<Blockade> blockades = new ArrayList<Blockade>();
		blockades.addAll(getBlockadesList(previous));
		blockades.addAll(getBlockadesList(last));
		
		Edge toPrevious = last.getEdgeTo(previous.getID());
		if(toPrevious == null){
			//logger.info("toPrevious == null; => return false;");
			//logger.info("+=+=+=+= isPassableFromEdgeToNextCenter(" + context + ") end. +=+=+=+=");
			return false;
		}
		int edgeCenterX = (toPrevious.getEndX() + toPrevious.getStartX()) / 2;
		int edgeCenterY = (toPrevious.getEndY() + toPrevious.getStartY()) / 2;
		Line2D line = getLineOfPositionPair(edgeCenterX, edgeCenterY, last.getX(), last.getY());
		
		Point2D intersectionPoint;
		for(Blockade blockade : blockades){
			List<Line2D> bLines = getBlockadeLine2Ds(blockade);
			for(Line2D bLine : bLines){
				intersectionPoint = GeometryTools2D.getSegmentIntersectionPoint(line, bLine);
				if(intersectionPoint != null){
					//logger.info("!!!!! Crossed Blockade (in isPassableFromEdgeToNextCenter, line, PreviousArea = [ " + previous + " ], LastArea = [ " + last + " ]) !!!!!");
					//logger.info("line = (" + line.getOrigin() + ")=>(" + line.getEndPoint() + ").");
					//logger.info("Intersect Blockade Line = (" + bLine.getOrigin() + ")=>(" + bLine.getEndPoint() + ")");
					//logger.info("Blockade = " + blockade);
					//logger.info("+=+=+=+= isPassableFromEdgeToNextCenter(" + context + ") end. +=+=+=+=");
					return false;
				}
			}
		}
		//logger.info("No Blockades Crossed(in isPassableFromEdgeToNextCenter()).");
		//logger.info("+=+=+=+= isPassableFromEdgeToNextCenter(" + context + ") end. +=+=+=+=");
		return true;
	}
	//これが作りたくて半年間も唸っていたのか
	private boolean isPassableTo(Area from, Area to){
		//logger.debug("*** isPassableTo(" + from + ", " + to + "); ***");
		List<Blockade> blockades = new ArrayList<Blockade>();
		
		if(!from.getNeighbours().contains(to.getID())){
			//logger.debug(to + " is not neighbour of " + from + ". return false;");
			//logger.debug("*** isPassableTo(" + from + ", " + to + "); end. ***");
			return false;
		}
		
		//閉塞の取得
		if(!from.isBlockadesDefined() && from.getBlockades() != null){
			for(EntityID bID : from.getBlockades()){
				blockades.add((Blockade)(model.getEntity(bID)));
			}
		}
		if(!to.isBlockadesDefined() && to.getBlockades() != null){
			for(EntityID bID : to.getBlockades()){
				blockades.add((Blockade)(model.getEntity(bID)));
			}
		}
		
		//fromから，toへつながるエッジの真ん中に対して辺を引く
		//これが，エージェントがfromからtoへ移動するときの軌跡になる
		Edge edgeTo = from.getEdgeTo(to.getID());
		double startX = from.getX();
		double startY = from.getY();
		double endX = (edgeTo.getEndX() + edgeTo.getStartX()) / 2;
		double endY = (edgeTo.getEndY() + edgeTo.getStartY()) / 2;

		Line2D line = new Line2D(new Point2D(startX, startY),
		                         new Point2D(endX, endY));
		
		// fromとtoから取得した閉塞に対して
		// 閉塞の各辺とエージェントの軌跡が交差すれば移動不可能．
		for(Blockade blockade : blockades){
			List<Line2D> blockadeLines = getBlockadeLine2Ds(blockade);
			for(Line2D bLine : blockadeLines){
				if(GeometryTools2D.getSegmentIntersectionPoint(line, bLine) != null){
					//logger.debug("!!!!! Crossed Blockade (in isPassableTo) !!!!!");
					//logger.debug("From     = " + from);
					//logger.debug("To       = " + to);
					//logger.debug("Blockade = " + blockade);
					//logger.debug("return false;");
					//logger.debug("*** isPassableTo(" + from + ", " + to + "); end. ***");
					return false;
				}
			}
		}
		//logger.debug("return true");
		//logger.debug("*** isPassableTo(" + from + ", " + to + "); end. ***");
		return true;
	}
	private List<Line2D> getBlockadeLine2Ds(Blockade blockade){
		List<Line2D> lines = new ArrayList<Line2D>();
		int[] apexes = blockade.getApexes();
		
		for(int i = 0; i < apexes.length-3; i += 2){
			Point2D first  = new Point2D(apexes[i], apexes[i+1]);
			Point2D second = new Point2D(apexes[i+2], apexes[i+3]);
			lines.add(new Line2D(first, second)); 
		}
		lines.add(new Line2D(new Point2D(apexes[apexes.length-2], apexes[apexes.length-1]),
		                     new Point2D(apexes[0], apexes[1])) );
		return lines;
	}
}
