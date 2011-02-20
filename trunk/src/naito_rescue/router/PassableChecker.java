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
	
	private boolean isPassableFromAgentToNeighbour(Area to){
		logger.info("*** PassableChecker.isPassableFromAgentToNeighbour() ***");
		Area from = (Area)(owner.getLocation());
		logger.info("Checking From = " + from + " To Neighbour = " + to + " .");
		if(!from.getNeighbours().contains(to.getID())){
			logger.info(to + " is not neighbour of \"from\":" + from);
			logger.info("return false;");
			logger.info("*** PassableChecker.isPassableFromAgentToNeighbour() end. ***");
			return false;
		}
		
		if(!from.isBlockadesDefined()){
			logger.info("\"from\"(" + from + ") is not defined blockade.");
			logger.info("return true;");
			logger.info("*** PassableChecker.isPassableFromAgentToNeighbour() end. ***");
			return true;
		}
		
		List<Blockade> blockades = new ArrayList<Blockade>();
		for(EntityID bID : from.getBlockades()){
			Blockade blockade = (Blockade)(model.getEntity(bID));
			blockades.add(blockade);
		}
		Edge edgeTo = from.getEdgeTo(to.getID());
		double startX = owner.getX();
		double startY = owner.getY();
		double endX = (edgeTo.getEndX() + edgeTo.getStartX()) / 2;
		double endY = (edgeTo.getEndY() + edgeTo.getStartY()) / 2;
		
		//logger.debug("edgeTo.getEndX()   = " + edgeTo.getEndX());
		//logger.debug("edgeTo.getStartX() = " + edgeTo.getStartX());
		//logger.debug("edgeTo.getEndY()   = " + edgeTo.getEndY());
		//logger.debug("edgeTo.getStartY() = " + edgeTo.getStartY());
		//logger.trace("endX        = " + endX);
		//logger.trace("endY        = " + endY);
		Line2D line = new Line2D(new Point2D(startX, startY), new Point2D(endX, endY));
		
		Point2D intersectionPoint;
		for(Blockade blockade : blockades){
			List<Line2D> bLines = getBlockadeLine2Ds(blockade);
			for(Line2D bLine : bLines){
				intersectionPoint = GeometryTools2D.getSegmentIntersectionPoint(line, bLine);
				if(intersectionPoint != null){
					logger.info("!!!!! Crossed Blockade (in isPassableFromAgentToNeighbour()) !!!!!");
					logger.info("From     = " + from);
					logger.info("To       = " + to);
					logger.info("Blockade = " + blockade);
					//logger.trace("Line (" + line.getOrigin() + ")=>(" + line.getEndPoint() + ") intersect Blockade.");
					//logger.trace("bLine = (" + bLine.getOrigin() + ")=>(" + bLine.getEndPoint() + ")");
					//logger.debug("Edge = (" + edgeTo.getStartX() + "," + edgeTo.getStartY() + ")=>("
					//	 + edgeTo.getEndX() + "," + edgeTo.getEndY() + ")");
					//logger.debug("Edge Center = (" + endX + "," + endY + ")");
					//logger.debug("Intersect Point = (" + intersectionPoint + ")");
					logger.debug("return false;");
					logger.info("*** PassableChecker.isPassableFromAgentToNeighbour() end. ***");
					return false;
				}
			}
		}
		logger.info("No blockades crossed.");
		logger.info("*** PassableChecker.isPassableFromAgentToNeighbour() end. ***");
		return true;
	}
	//pathで示される経路が通行可能かどうか
	public boolean isPassable(List<EntityID> path){
		logger.info("*** PassableChecker.isPassable() ***");
		logger.info("Checking EntityIDs Path = [" + path + "]");
		
		Area first = (Area)(model.getEntity(path.get(0)));
		logger.info("First, Checking Blockade in Agent's Location.");
		if(!isPassableFromAgentToNeighbour(first)){
			logger.info("Agent crossed blockade in Agent Location(" + first + ").");
			logger.info("return false;");
			logger.info("*** PassableChecker.isPassable() end. ***");
			return false;
		}
		boolean passable = false;
		logger.info("Then, Checking Blockade in Moving Path.");
		for(int i = 0;i < path.size()-1; i++){
			Area from = (Area)(model.getEntity(path.get(i)));
			Area to   = (Area)(model.getEntity(path.get(i+1)));
			logger.info("Current Area = [" + from + "], Next Area = [" + to + "].");
			
			List<Area> passables = passableNeighbours(from);
			logger.info("Passable Neighbours = [" + passables + "]");
			
			passable = false;
			for(Area passableArea : passables){
				if(passableArea.getID().getValue() == to.getID().getValue()){
					passable = true;
					break;
				}
			}
			//passableArea全てを検査してもpassable=falseな場合
			//(即ち経路上のfromからtoへ通り抜けが不可能である場合)
			//経路は通行不可能なのでfalseを返す
			if(!passable){
				logger.info("Cannot passable from [" + from + "] to [" + to + "]. return false;");
				logger.info("*** PassableChecker.isPassable() end. ***");
				return false;
			}
		}
		logger.info("*** PassableChecker.isPassable() end. ***");
		return true;
	}
	public List<Area> passableNeighbours(Area from){
		logger.info("=== PassableChecker.passableNeighbours(); ===");
		List<Area> result = new ArrayList<Area>();
		List<Area> neighbours = new ArrayList<Area>();
		for(EntityID id : from.getNeighbours()){
			Area neighbour = (Area)(model.getEntity(id));
			neighbours.add(neighbour);
		}
		if(neighbours.isEmpty()){
			logger.info("Area from[" + from + "]'s Neighbour is null.");
			logger.info("=== PassableChecker.passableNeighbours(); end. ===");
			return result;
		}
		//logger.debug("Area from[" + from + "]'s Neighbours are = ");
		//logger.debug(neighbours + "");
		
		if(from.isBlockadesDefined() && from.getBlockades() != null){
			List<Blockade> blockades = new ArrayList<Blockade>();
			for(EntityID bID : from.getBlockades()){
				blockades.add((Blockade)(model.getEntity(bID)));
			}
			//logger.trace("Area from's Blockades are = ");
			//logger.trace(blockades + "");
			//from の各neighbourに対して，fromに発生している閉塞によって
			//neighbourへの通行が可能かどうかを調べる
			for(Area neighbour : neighbours){
				logger.debug("Area from = " + from + ", Checking Neighbour: " + neighbour);
				Edge edgeTo = from.getEdgeTo(neighbour.getID());
				if(edgeTo == null){
					continue;
				}
				double startX = from.getX();
				double startY = from.getY();
				double endX = (edgeTo.getEndX() + edgeTo.getStartX()) / 2;
				double endY = (edgeTo.getEndY() + edgeTo.getStartY()) / 2;
				
				//logger.debug("From to Neighbour Line = (" + startX + "," + startY + ")=>(" + endX + "," + endY + ")");
				Line2D line = new Line2D(new Point2D(startX, startY),
				                         new Point2D(endX, endY));
				boolean crossed = false;
				
				checkCrossed:
				for(Blockade blockade : blockades){
					List<Line2D> blockadeLines = getBlockadeLine2Ds(blockade);
					for(Line2D bLine : blockadeLines){
						if(GeometryTools2D.getSegmentIntersectionPoint(line, bLine) != null){
							logger.info("!!!!! Crossed Blockade (in passableNeighbours()) !!!!!");
							logger.info("From     = " + from);
							logger.info("To       = " + neighbour);
							logger.info("Blockade = " + blockade);
							crossed = true; //閉塞と交差
							break checkCrossed; //if(!crossed)の行へ飛ぶ
						}
					}
				}
				if(!crossed) result.add(neighbour);
			}
		}else{
			logger.info("All Neighbours are passable.");
			for(Area neighbour : neighbours){
				result.add(neighbour);
			}
		}
		logger.info("=== PassableChecker.passableNeighbours(); end. ===");
		return result;
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
