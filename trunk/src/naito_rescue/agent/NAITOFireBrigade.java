package naito_rescue.agent;

import java.util.*;
import java.io.*;
import rescuecore2.worldmodel.*;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.*;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;

import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;


public class NAITOFireBrigade extends NAITOHumanoidAgent<FireBrigade>
{
	boolean debug = true;
	
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
	}

	public String toString(){
		return "NAITOFireBrigade." + me().getID() + "";
	}

	protected void think(int time, ChangeSet changed, Collection<Command> heard){

		super.think(time, changed, heard);
		if(time < 3){
			return;
		}
		//NAITORouterのテスト
		// . isPassable関係
		// .. denen_test10マップ
		// .... Road(401)からRoad(441)まで通行可能かどうか調べる
		StandardEntity location = getLocation();
		if(location.getID().getValue() == 401 && debug){
			List<EntityID> testPath = new ArrayList<EntityID>();
			testPath.add(new EntityID(452));
			testPath.add(new EntityID(441));
			testPath.add(new EntityID(425));
			testPath.add(new EntityID(426));
			testPath.add(new EntityID(427));
			testPath.add(new EntityID(423));
			
			logger.info("TEST OF \"isPassable\".");
			logger.info("Route = 452->441->425->426->427->423");
			
			//基本情報の出力
			logger.info("----- Area's Basic Info. -----");
			logger.setContext("basic_info");
			for(int i = 0; i < testPath.size()-1; i++){
				logger.info("");
				StandardEntity entity = model.getEntity(testPath.get(i));
				logger.info(entity + ":");
				if(! (entity instanceof Area)){
					logger.info(entity + " is not Area.");
					continue;
				}
				
				Area area = (Area)entity;
				logger.debug("Edges = ");
				logger.debug(area.getEdges().toString());
				
				//blockade info.
				logger.info("Blockades = ");
				List<EntityID> blockadesID = area.getBlockades();
				if(blockadesID == null || blockadesID.size() <= 0){
					logger.info(" ... no blockades.");
				}else{
					//logger.info("Blockades = ");
					for(int j = 0; j < blockadesID.size(); j++){
						logger.info(model.getEntity(blockadesID.get(i)).toString());
					}
				}
				
				EntityID nextID = testPath.get(i+1);
				StandardEntity nextEntity = model.getEntity(nextID);
				if(!(nextEntity instanceof Area)){
					logger.info("next " + nextEntity + "is not Area.");
					continue;
				}
				Area nextArea = (Area)nextEntity;
				logger.info("Next area = " + nextArea);
				Edge nextEdge = area.getEdgeTo(nextID);
				
				logger.info(area + " .getEdgeTo(" + nextArea + ") => " + nextEdge);
			}
			logger.unsetContext();
			logger.info("----- //*****// -----");
			
			logger.info("");
			logger.info("----- ----- -----");
			
			//エージェントから次のエリアへの中線
			int fromX = getMe().getX();
			int fromY = getMe().getY();
			Edge firstToNextEdge = ((Area)getLocation()).getEdgeTo(testPath.get(0));
			int toX = (firstToNextEdge.getStartX() + firstToNextEdge.getEndX()) / 2;
			int toY = (firstToNextEdge.getStartY() + firstToNextEdge.getEndY()) / 2;
			logger.info("from = (" + fromX + ", " + fromY + ")");
			logger.debug("Neighbour Edge = " + firstToNextEdge.toString());
			logger.trace("centerPoint? = (" + toX + ", " + toY + ")");
			logger.info("to = (" + toX + ", " + toY + ")");
			Line2D centerLineF2N = new Line2D(new Point2D(fromX, fromY), new Point2D(toX, toY));
			logger.info("CenterLine = " + centerLineF2N.toString());
			
			
			//neighbourエリアから目的地まで各々中線を得てどうのこうの
			logger.setContext("centerLine,Area Info");
			Area previousArea = (Area)(getLocation());
			for(int i = 0;i < testPath.size()-1; i++){
				logger.info("----- +++++ -----");
				Area currentArea = (Area)(model.getEntity(testPath.get(i)));
				Area nextArea    = (Area)(model.getEntity(testPath.get(i+1)));
				Edge fromEdge = currentArea.getEdgeTo(previousArea.getID());
				Edge toEdge = currentArea.getEdgeTo(nextArea.getID());
				
				if(fromEdge == null){
					logger.info(currentArea + " => " + previousArea + "isnt adj.");
				}
				if(toEdge == null){
					logger.info(currentArea + " => " + nextArea + "isnt adj.");
				}
				int fromx = (fromEdge.getStartX() + fromEdge.getEndX()) / 2;
				int fromy = (fromEdge.getStartY() + fromEdge.getEndY()) / 2;
				int tox = (toEdge.getStartX() + toEdge.getEndX()) / 2;
				int toy = (toEdge.getStartY() + toEdge.getEndY()) / 2;
				
				logger.setContext("Area Info.");
				logger.info("currentArea = " + currentArea);
				logger.info("previousArea = " + previousArea);
				logger.info("nextArea = " + nextArea);
				logger.info("previous <- current = " + fromEdge);
				logger.debug("previous -> current = " + previousArea.getEdgeTo(currentArea.getID()));
				logger.info("current -> next = " + toEdge);
				logger.debug("next <- current = " + nextArea.getEdgeTo(currentArea.getID()));
				logger.unsetContext();
				
				Line2D centerLine = new Line2D(new Point2D(fromx, fromy), new Point2D(tox, toy));
				//エッジに関する情報を出力
				logger.setContext("centerLine Info.");
				logger.info("from(x, y) ... " + fromEdge);
				logger.info("to(x, y)   ... " + toEdge);
				logger.info("centerLine = " + centerLine);
				logger.unsetContext();
				previousArea = currentArea;
			}
			//エリア間の中線取得までok
			//閉塞のApexes取得と線分通過の判定
			logger.unsetContext();
			logger.setContext("get Blockade Shape (agent's location)");
			logger.info("");
			logger.unsetContext();
			//まず「エージェント->2つ目のエリア」間
			//. BlockadeごとのApexes取得
			logger.setContext("get Blockade, Apexes(agent)");
			List<EntityID> blockadesID = ((Area)location).getBlockades();
			if(blockadesID == null || blockadesID.size() <= 0){
				logger.info(location + " ... no blockade.");
			}else{
				for(int j = 0; j < blockadesID.size(); j++){
					logger.setContext("get Blockade.");
					logger.info("_____ _____ _____");
					Blockade b = (Blockade)(model.getEntity(blockadesID.get(j)));
					logger.info("get blockade => " + b);
					int[] apexes = b.getApexes();
					StringBuilder apexesStr = new StringBuilder();
					apexesStr.append("[");
					for(int k = 0; k < apexes.length; k++)
						apexesStr.append(apexes[k] + ", ");
					apexesStr.append("]");
					logger.setContext("get Apexes");
					logger.info("Apexes => " + apexesStr.toString());
					
					//線分による表現
					List<Line2D> allBlockadesLines = new ArrayList<Line2D>();
					StringBuilder prettyApexes = new StringBuilder();
					prettyApexes.append("[ ");
					for(int k = 0; k < apexes.length-3; k += 2){
						//Point2D first = new Point2D(apexes[i], apexes[i+1]);
						//Point2D second = new Point2D(apexes[i+2], apexes[i+3]);
						prettyApexes.append("(" + apexes[k] + ", " + apexes[k+1] + ")=>(" + apexes[k+2] + ", " + apexes[k+3] + ")");
						allBlockadesLines.add(new Line2D(new Point2D(apexes[k], apexes[k+1]), new Point2D(apexes[k+2], apexes[k+3])));
					}
					//Point2D last = new Point2D(apexes[apexes.length-2], apexes[apexes.length-1]);
					//Point2D first = new Point2D(apexes[0], apexes[1]);
					prettyApexes.append("(" + apexes[apexes.length-2] + ", " + apexes[apexes.length-1] + ")=>" + "(" + apexes[0] + ", " + apexes[1] + ")");
					prettyApexes.append(" ]");
					allBlockadesLines.add(new Line2D(new Point2D(apexes[apexes.length-2], apexes[apexes.length-1]), new Point2D(apexes[0], apexes[1])));
					logger.info(prettyApexes.toString());
					
					//. 線分通過の判定
					logger.setContext("check blockades line");
					logger.info("----- Start checking -----");
					logger.info("CenterLine = " + centerLineF2N);
					for(Line2D blockadeLine : allBlockadesLines){
						logger.info("Blockade Line = " + blockadeLine);
						if(GeometryTools2D.getSegmentIntersectionPoint(centerLineF2N, blockadeLine) != null){
							logger.info("NG!");
						}else{
							logger.info("OK!");
						}
					}
					logger.info("----- End checking -----");
					logger.unsetContext();
					logger.unsetContext();
					logger.unsetContext();
				}
			}
			
			debug = false;
		}
	}
	
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
	}

}
