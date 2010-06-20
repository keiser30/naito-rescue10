package naito_rescue.agent;

import java.util.*;

import sample.*;
import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import rescuecore2.misc.geometry.*;

import naito_rescue.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;
import naito_rescue.message.manager.*;


public abstract class NAITOHumanoidAgent<E extends StandardEntity> extends NAITOAgent<E> implements MessageConstants
{
    private static final String SAY_COMMUNICATION_MODEL = "kernel.standard.StandardCommunicationModel";
    private static final String SPEAK_COMMUNICATION_MODEL = "kernel.standard.ChannelCommunicationModel";
	private static final int            CROWLABLE_NUM = 5;
	protected boolean                   useSpeak;
	protected ArrayList<Task>           currentTaskList;
	protected ArrayList<Building>       crowlingBuildings;
	protected ArrayList<Human>          teamMembers;
	protected Task                      currentTask;
	protected Job                       currentJob;
	protected boolean                   centerLess;
	protected boolean                   fireStationLess;
	protected boolean                   ambulanceCenterLess;
	protected boolean                   policeOfficeLess;
	protected boolean                   ignoreBroadcast;
	
	protected boolean                   isLeader;
	protected boolean                   isMember;
	protected boolean                   isOnTeam;

	protected java.awt.geom.Rectangle2D world_rect;
	protected double                    minX;
	protected double                    minY;
	protected double                    maxX;
	protected double                    maxY;
	protected double                    w_width;
	protected double                    w_height;
	protected ArrayList<EntityID>       reportedBlockedRoad; //閉塞があることを送信済みの道路IDリスト
	
	@Override
    protected void postConnect() {
		 super.postConnect();
		 
		 useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
		 currentTaskList = new ArrayList<Task>();
		 crowlingBuildings = new ArrayList<Building>();
		 teamMembers = new ArrayList<Human>();

		 //センターレスに関する設定
		 fireStationLess = firestation.isEmpty();
		 policeOfficeLess = policeoffice.isEmpty();
		 ambulanceCenterLess = ambulancecenter.isEmpty();
		 centerLess = fireStationLess && policeOfficeLess && ambulanceCenterLess;
		 
		 reportedBlockedRoad = new ArrayList<EntityID>();

		 world_rect = model.getBounds();
		 minX = world_rect.getX();
		 minY = world_rect.getY();
		 maxX = minX + world_rect.getWidth();
		 maxY = minY + world_rect.getHeight();
		 w_width = world_rect.getWidth();
		 w_height = world_rect.getHeight();
		 
		 logger.info(this.toString() + " start!");
		 logger.debug("useSpeak            = " + useSpeak);
		 logger.debug("fireStationLess     = " + fireStationLess);
		 logger.debug("policeOfficeLess    = " + policeOfficeLess);
		 logger.debug("ambulanceCenterLess = " + ambulanceCenterLess);
		 logger.debug("    => centerLess   = " + centerLess);
		 
		 //チーム分け
		 isLeader = isMember = isOnTeam = false;
		 createCrowlingTeam();
		 isOnTeam = isLeader || isMember;
		 
		 logger.debug("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓");
		 logger.debug("createCrowlingTeam() is end.");
		 logger.debug("isLeader = " + isLeader);
		 logger.debug("isMember = " + isMember);
		 logger.debug("   |___ isOnTeam = " + isOnTeam);
		 logger.debug("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓");
		 
		 if(isMember && !crowlingBuildings.isEmpty()){
		 	logger.info("isMember && crowlingBuildings.isNotEmpty() ==> 建物探訪タスクをaddする");
		 	for(Building b : crowlingBuildings){
		 		logger.debug("廻る建物は(crowlingBuilding) => " + b);
		 		currentTaskList.add(new MoveTask(this, model, (Area)b));
		 	}
		 }
	}
	
	int ii = 0, jj = 0;
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);

		if (time < config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
			logger.info("まだですよ: " + time);
			return;
		}

		logger.info("****************************************");
		logger.info("");
		logger.info("**********____" + time + "____**********");
		logger.info("**  NAITOHumanoidAgent.think();");
		logger.info("**  location    = " + getLocation());
		logger.info("**  currentTask = " + currentTask);
		if(currentTask != null)
			logger.info("**  currentTask.jobs[] = " + currentTask.getJobs());
		logger.info("Entities in view range:");
		for(EntityID id : changed.getChangedEntities()){
			StandardEntity entity = model.getEntity(id);
			logger.debug("entity => " + entity);
		}

		//currentTaskListに関する処理
		//currentTaskが終了していたら，そいつをリストから削除する
		logger.info("1. //////// currentTaskが終了していたら，そいつをリストから削除する ////////");
		if(currentTask != null && currentTask.isFinished()){
			logger.debug("**  currentTaskList.remove(" + currentTask + ")");
			logger.debug("**  ==> currentTaskList = " + currentTaskList);
			currentTaskList.remove(currentTask);
			currentTask = null;
		}
		logger.info("//////// /////////////////////////////////////////////////////// //////// 1.");
			
		StandardEntity location = getLocation();
		//啓開の発見検証用コード
		if(location instanceof Area){
			logger.info("2. ////////// 閉塞の発見検証用コード //////////");
			if(((Area)location).isBlockadesDefined()){
				logger.info("//  There is blockade...?");
				if(!((Area)location).getBlockades().isEmpty()){
					logger.info("//  There is blockade!");
					logger.debug("//  blockades = " + ((Area)location).getBlockades());
				}else{
					logger.info("//  location.isBlockadesDefined()...but, getBlockades() is empty.");
					logger.debug("//  blockades = " + ((Area)location).getBlockades());
				}
			}else{
				logger.info("//  There is no blockade.");
			}
			logger.info("////////// ////////////////////// ////////// 2.");
		}
		//自分の今いる場所に閉塞がある場合
		logger.info("3. //////// 自分の身の回りにある閉塞の発見 ////////");
		if(location instanceof Area && ((Area)location).isBlockadesDefined() && !((Area)location).getBlockades().isEmpty()){
			// 閉塞が発生しているRoadのIDを送りつける
			//  -> 閉塞の発見と啓開は，このメッセージを受け取った啓開隊に任せる
			if(!(this instanceof NAITOPoliceForce)/* && !(reportedBlockedRoad.contains(location.getID())) */){
				logger.info("There is blockade => createClearMessage();");
				ClearMessage clear_msg = msgManager.createClearMessage(-1, ADDR_PF, false, getLocation().getID());
				msgManager.sendMessage(clear_msg);
				logger.debug("Find blockade (" + getLocation() + ")");
				logger.debug("Sending ClearMessage...");
				reportedBlockedRoad.add(location.getID());
			}
		}
		logger.info("//////// ////////////////////////////// //////// 3.");

		//自分の視界に燃えている建物がある場合
		//とりあえずその情報をFBに送りつける
		logger.info("4. //////// 視界にある建物の処理(1) ////////");
		if(!(this instanceof NAITOFireBrigade)){
			List<Building> view_buildings = getViewBuildings();
			for(Building b : view_buildings){
				if(b.isOnFire()){
					logger.info("There is burning building => createExtinguishMessage();");
					StandardEntityConstants.Fieryness fieryness = b.getFierynessEnum();
					ExtinguishMessage ex_msg = msgManager.createExtinguishMessage(-1, ADDR_FB, false, b.getID(), (b.isGroundAreaDefined()?b.getGroundArea():1000));
					msgManager.sendMessage(ex_msg);
				}
			}
		}
		logger.info("//////// /////////////////////// //////// 4.");

		//自分の視界にある建物の中に市民がいる場合
		//とりあえずその情報をATに送りつける
		logger.info("5. //////// 視界にある建物の処理(2) ////////");
		if(!(this instanceof NAITOAmbulanceTeam)){
			List<Civilian> civilians = getViewCivilians();
			for(Civilian c : civilians){
				StandardEntity civilian_location = c.getPosition(model);
				//道路を突っ走ってる市民に対してLoadを実行しようとするとコケる気がする...
				if(civilian_location instanceof Building){
					logger.info("There is victim => createRescueMessage();");
					RescueMessage rescue_msg = msgManager.createRescueMessage(-1, ADDR_AT, false, civilian_location.getID());
					msgManager.sendMessage(rescue_msg);
				}
			}
		}
		logger.info("//////// /////////////////////// //////// 5.");
	}


	public List<Building> getViewBuildings(){
		logger.info("getViewBuildings();");
		ArrayList<Building> buildings = new ArrayList<Building>();
		StandardEntity entity = null;
		for(EntityID id : this.changed.getChangedEntities()){
			entity = model.getEntity(id);
			if(entity instanceof Building){
				logger.debug("getViewBuildings() => " + entity);
				buildings.add((Building)entity);
			}
		}
		return buildings;
	}
    public List<Civilian> getViewCivilians(){
    	logger.info("getViewCivilians();");
    	List<Civilian> civilians = new ArrayList<Civilian>();
    	StandardEntity entity;
    	for(EntityID next : this.changed.getChangedEntities()){
    		entity = model.getEntity(next);
    		if(entity instanceof Civilian){
    			logger.debug("getViewCivilian() => " + entity);
    			civilians.add((Civilian)entity);
    		}
    	}
    	return civilians;
    }
	public void addTaskIfNew(Task tt){
		if(tt instanceof ExtinguishTask){
			ExtinguishTask et = (ExtinguishTask)tt;
			for(Task t : currentTaskList){
				if(t instanceof ExtinguishTask){
					Building b = ((ExtinguishTask)t).getTarget();
					if(b.getID().getValue() == et.getTarget().getID().getValue()){
						return;
					}
				}
			}
			currentTaskList.add(et);
		}else if(tt instanceof ClearTask){
			ClearTask ct = (ClearTask)tt;
			for(Task t : currentTaskList){
				if(t instanceof ClearTask){
					Area target = ((ClearTask)t).getTarget();
					if(target.getID().getValue() == ct.getTarget().getID().getValue())
						return;
				}
			}
			currentTaskList.add(ct);
		}
	}
	private int maxInt(Integer... nums){
		int max = 0;
		for(int i = 0;i < nums.length;i++){
			if(nums[i] > max){
				max = nums[i];
			}
		}
		return max;
	}
	//建物探訪をするチームを作成する
	public void createCrowlingTeam(){
		logger.info("=======================================");
		logger.info("createCrowlingTeam();");
		logger.info("CLOWLABLE_NUM = " + CROWLABLE_NUM);
		logger.info("atSize        = " + atSize);
		logger.info("pfSize        = " + pfSize);
		logger.info("fbSize        = " + fbSize);
		//どの種類のエージェントが探訪を担当するか決定する
		int max = maxInt(atSize, pfSize, fbSize);
		logger.info("max           = " + max);
		logger.info("どっちにしても全員で廻る(RoboCup2010)");
		/*
		if(atSize == max && atSize > CROWLABLE_NUM){
			//AmbulanceTeamが探訪する
			logger.info("担当はAmbulanceTeam (atSize = " + atSize + ")");
			if(this instanceof NAITOAmbulanceTeam){
				logger.info(this + " in crowling Team.");
				if(atList.get(0).getID().getValue() == me().getID().getValue()){
					logger.info("------> " + this + " is Leader!");
					isLeader = true;
				}else{
					logger.info("------> " + this + " is Member.");
					isMember = true;
				}
				teamMembers.addAll(atList);
				decideCrowlingBuildings();
			}
		}else if(pfSize == max && pfSize > CROWLABLE_NUM){
			//PoliceForceが探訪する
			logger.info("担当はPoliceForce (pfSize = " + pfSize + ")");
			if(this instanceof NAITOPoliceForce){
				logger.info(this + " in crowling Team.");
				if(pfList.get(0).getID().getValue() == me().getID().getValue()){
					logger.info("------> " + this + " is Leader!");
					isLeader = true;
				}else{
					logger.info("------> " + this + " is Member.");
					isMember = true;
				}
				teamMembers.addAll(pfList);
				decideCrowlingBuildings();
			}
		}else if(fbSize == max && fbSize > CROWLABLE_NUM){
			//FireBrigadeが探訪する
			logger.info("担当はFireBrigade (fbSize = " + fbSize + ")");
			if(this instanceof NAITOFireBrigade){
				logger.info(this + " in crowling Team.");
				if(fbList.get(0).getID().getValue() == me().getID().getValue()){
					logger.info("------> " + this + " is Leader!");
					isLeader = true;
				}else{
					logger.info("------> " + this + " is Member.");
					isMember = true;
				}
				teamMembers.addAll(fbList);
				decideCrowlingBuildings();
			}
		}else{
		*/
		// <-
		//全員で探訪する
		logger.info("みんなで仲良く廻りましょう (allAgentsList.size() = " + allAgentsList.size() + ")");
		logger.info(this + " in crowling Team.");
		if(allAgentsList.get(0).getID().getValue() == me().getID().getValue()){
			logger.info("------> " + this + " is Leader!");
			isLeader = true;
		}else{
			logger.info("------> " + this + " is Member.");
			isMember = true;
		}
		teamMembers.addAll(allAgentsList);
		decideCrowlingBuildings();
		//}
		logger.info("=======================================");
	}
	//探訪する建物を決定する
	//(多分ここの計算はものすごい時間を食う)
	private void decideCrowlingBuildings(){
		logger.info("|```````````````````````````````````|");
		logger.info(this + ".decideCrowlingBuildings();");
		logger.info("allBuildings.size() = " + allBuildings.size());
		
		int roleID = teamMembers.indexOf(me());
		int separateBlock = 1;
		
		logger.debug("minX     = " + minX);
		logger.debug("minY     = " + minY);
		logger.debug("maxX     = " + maxX);
		logger.debug("maxY     = " + maxY);
		logger.debug("w_width  = " + w_width);
		logger.debug("w_height = " + w_height);
		logger.debug("roleID   = " + roleID);
		//DisasterSpaceをいくつのブロックに分割するかを決める
		for(; (separateBlock * separateBlock) < teamMembers.size();separateBlock++);
		
		separateBlock--;
		if(separateBlock < 1) separateBlock = 1;

		//roleIDの正規化...roleID=[0 ... pow(separateBlock)-1]になるように
		while(roleID >= (separateBlock * separateBlock)) roleID -= separateBlock;
		
		logger.debug("separateBlock = " + separateBlock);
		
		//どっからどこまでのBuildingを探訪するか決定する
		double width  = (maxX - minX) / separateBlock;
		double height = (maxY - minY) / separateBlock;
		double x      = minX + width * (roleID % separateBlock);
		double y      = minY + height * (roleID / separateBlock);
		
		logger.debug("//---------- 範囲 ----------//");
		logger.debug("x      = " + x);
		logger.debug("y      = " + y);
		logger.debug("width  = " + width);
		logger.debug("height = " + height);
		logger.debug("//--------------------------//");
		Building b = null;
		for(StandardEntity building : allBuildings){
			b = (Building)building;
			logger.debug("//----------------------//");
			logger.debug(b + ":");
			logger.debug("b.getX() = " + b.getX());
			logger.debug("b.getY() = " + b.getY());
			logger.debug("//----------------------//");
			if(b.getX() > x && b.getX() <= (x + width) &&
			   b.getY() > y && b.getY() <= (y + height)){
			   	logger.debug("In range:");
			   	logger.debug("minX = " + x + ", minY = " + y);
			   	logger.trace("b.x = " + b.getX());
			   	logger.trace("b.y = " + b.getY());
			   	logger.debug("maxX = " + (x + width) + ", maxY = " + (y + height));
				crowlingBuildings.add(b);
			}
		}
		logger.info("crowlingBuildings = " + crowlingBuildings);
		logger.info("|___________________________________|");
	}
	
	public abstract void taskRankUpdate();
	
	public Task action(){
		logger.info("action();");
		if(currentTask != null && !currentTask.isFinished()){
			logger.info(" => currentTask!=null && !currentTask.isFinished() => return currentTask;");
			return currentTask;
		}else{
			logger.info(" => taskRankUpdate(); => return getHighestRankRask();");
			taskRankUpdate();
			return getHighestRankTask();
		}
	/*
		taskRankUpdate();
		return getHighestRankTask();
	*/
	}
	
	/**
	*  currentTaskListを降順ソートして1番目の要素を返す
	*  (Taskのランク値の大きい方が優先される)
	*/
	protected static Comparator<Task> task_comp = new Comparator<Task>(){
		public int compare(Task t1, Task t2){
			return t2.getRank() - t1.getRank();
		}
	};
	public Task getHighestRankTask(){
		logger.info("getHighestRankTask();");

		if(currentTaskList.isEmpty()){
			//初期タスクの設定がここになる
			logger.info("NOT REACHED.");
			for(StandardEntity entity : allBuildings){
				currentTaskList.add(new MoveTask(this, model, (Area)entity));
			}
		}
		int maxRank = Integer.MIN_VALUE;
		Task resultTask = currentTaskList.get(0); //念のため初めのタスクをいれておく
		Task tempTask;
		for(int i = 0;i < currentTaskList.size();i++){
			tempTask = currentTaskList.get(i);
			logger.debug("task(" + i + ").getRank() => " + tempTask.getRank());
			if(tempTask.getRank() > maxRank){
				resultTask = tempTask;
			}
		}
		return resultTask;
	}
	
	@Override
	public String toString(){
		return "NAITOHumanoidAgent: " + me().getID();
	}

	public int getX(){
		return ((Human)me()).getX();
	}
	public int getY(){
		return ((Human)me()).getY();
	}

    protected List<EntityID> randomWalk() {
		int RANDOM_WALK_LENGTH = 50;
        List<EntityID> result = new ArrayList<EntityID>(RANDOM_WALK_LENGTH);
        Set<StandardEntity> seen = new HashSet<StandardEntity>();
        StandardEntity current = location();
        for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
            result.add(current.getID());
            seen.add(current);
            List<StandardEntity> neighbours = new ArrayList<StandardEntity>(search.findNeighbours(current));
            Collections.shuffle(neighbours, random);
            boolean found = false;
            for (StandardEntity next : neighbours) {
                if (seen.contains(next)) {
                    continue;
                }   
                current = next;
                found = true;
                break;
            }   
            if (!found) {
                // We reached a dead-end.
                break;
            }   
        }   
        return result;
    }
    public int findDistanceTo(Blockade b, int x, int y) {
        List<Line2D> lines = GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
        double best = Double.MAX_VALUE;
        Point2D origin = new Point2D(x, y); 
        for (Line2D next : lines) {
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
            double d = GeometryTools2D.getDistance(origin, closest);
            if (d < best) {
                best = d;
            }   

        }
        return (int)best;
	}

	
	//SampleAmbulanceTeamから移植
    public StandardEntity someoneOnBoard() {
		logger.info("NAITOHumanoidAgent.someonwOnBoard();");
        for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            if (((Human)next).getPosition().equals(getID())) {
                logger.debug(next + " is on board");
                return next;
            }
        }
        return null;
    }
}

