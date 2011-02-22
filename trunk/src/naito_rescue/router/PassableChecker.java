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
		////logger.debug("*** PassableChecker.isPassableFromAgentToNeighbour() ***");
		Area from = (Area)(owner.getLocation());
		//logger.debug("Checking From = " + from + " To Neighbour = " + to + " .");
		if(!from.getNeighbours().contains(to.getID())){
			//logger.debug(to + " is not neighbour of \"from\":" + from);
			////logger.debug("return false;");
			////logger.debug("*** PassableChecker.isPassableFromAgentToNeighbour() end. ***");
			return false;
		}
		
		if(!from.isBlockadesDefined()){
			//logger.debug("\"from\"(" + from + ") is not defined blockade.");
			////logger.debug("return true;");
			////logger.debug("*** PassableChecker.isPassableFromAgentToNeighbour() end. ***");
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
		
		////logger.debug("edgeTo.getEndX()   = " + edgeTo.getEndX());
		////logger.debug("edgeTo.getStartX() = " + edgeTo.getStartX());
		////logger.debug("edgeTo.getEndY()   = " + edgeTo.getEndY());
		////logger.debug("edgeTo.getStartY() = " + edgeTo.getStartY());
		////logger.trace("endX        = " + endX);
		////logger.trace("endY        = " + endY);
		Line2D line = new Line2D(new Point2D(startX, startY), new Point2D(endX, endY));
		
		Point2D intersectionPoint;
		for(Blockade blockade : blockades){
			List<Line2D> bLines = getBlockadeLine2Ds(blockade);
			for(Line2D bLine : bLines){
				intersectionPoint = GeometryTools2D.getSegmentIntersectionPoint(line, bLine);
				if(intersectionPoint != null){
					//logger.debug("!!!!! Crossed Blockade (in isPassableFromAgentToNeighbour()) !!!!!");
					//logger.debug("From     = " + from);
					//logger.debug("To       = " + to);
					//logger.debug("Blockade = " + blockade);
					////logger.trace("Line (" + line.getOrigin() + ")=>(" + line.getEndPoint() + ") intersect Blockade.");
					////logger.trace("bLine = (" + bLine.getOrigin() + ")=>(" + bLine.getEndPoint() + ")");
					////logger.debug("Edge = (" + edgeTo.getStartX() + "," + edgeTo.getStartY() + ")=>("
					//	 + edgeTo.getEndX() + "," + edgeTo.getEndY() + ")");
					////logger.debug("Edge Center = (" + endX + "," + endY + ")");
					////logger.debug("Intersect Point = (" + intersectionPoint + ")");
					////logger.debug("return false;");
					////logger.debug("*** PassableChecker.isPassableFromAgentToNeighbour() end. ***");
					return false;
				}
			}
		}
		////logger.debug("No blockades crossed.");
		////logger.debug("*** PassableChecker.isPassableFromAgentToNeighbour() end. ***");
		return true;
	}
	//pathで示される経路が通行可能かどうか
	public boolean isPassable(List<EntityID> path){
		//logger.debug("*** PassableChecker.isPassable() ***");
		//logger.debug("Checking EntityIDs Path = [" + path + "]");
		
		Area first = (Area)(model.getEntity(path.get(0)));
		////logger.debug("First, Checking Blockade in Agent's Location.");
		if(!isPassableFromAgentToNeighbour(first)){
			//logger.debug("Agent crossed blockade in Agent Location(" + first + ").");
			//logger.debug("return false;");
			//logger.debug("*** PassableChecker.isPassable() end. ***");
			return false;
		}
		////logger.debug("Then, Checking Blockade in Moving Path.");
		for(int i = 0;i < path.size()-1; i++){
			Area from = (Area)(model.getEntity(path.get(i)));
			Area to   = (Area)(model.getEntity(path.get(i+1)));
			//logger.debug("Current Area = [" + from + "], Next Area = [" + to + "].");
			if(!isPassableTo(from, to)){
				//logger.debug("Cannot pass from = " + from + ", to = " + to + " . return false;");
				//logger.debug("*** PassableChecker.isPassable() end. ***");
				return false;
			}
		}
		//logger.debug("*** PassableChecker.isPassable() end. ***");
		return true;
	}
	//これが作りたくて半年間も唸っていたのか
	private boolean isPassableTo(Area from, Area to){
		////logger.debug("*** isPassableTo(" + from + ", " + to + "); ***");
		List<Blockade> blockades = new ArrayList<Blockade>();
		
		if(!from.getNeighbours().contains(to.getID())){
			//logger.debug(to + " is not neighbour of " + from + ". return false;");
			////logger.debug("*** isPassableTo(" + from + ", " + to + "); end. ***");
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
					////logger.debug("return false;");
					////logger.debug("*** isPassableTo(" + from + ", " + to + "); end. ***");
					return false;
				}
			}
		}
		////logger.debug("return true");
		////logger.debug("*** isPassableTo(" + from + ", " + to + "); end. ***");
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
