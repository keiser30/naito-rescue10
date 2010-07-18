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

	private static final int           CROWLABLE_NUM = 5;
//	protected ArrayList<Task>           currentTaskList;
	protected ArrayList<Building>       crowlingBuildings;
	protected ArrayList<Human>          teamMembers;
	
	protected boolean                   isLeader;
	protected boolean                   isMember;
	protected boolean                   isOnTeam;

	protected int                       MY_MESSAGE_ADDRESS_TYPE;
	
	@Override
    protected void postConnect() {
		 super.postConnect();
		 
		 if(this instanceof NAITOFireBrigade){
		 	MY_MESSAGE_ADDRESS_TYPE = ADDR_FB;
		 }else if(this instanceof NAITOPoliceForce){
		 	MY_MESSAGE_ADDRESS_TYPE = ADDR_PF;
		 }else if(this instanceof NAITOAmbulanceTeam){
		 	MY_MESSAGE_ADDRESS_TYPE = ADDR_AT;
		 }else{
		 	MY_MESSAGE_ADDRESS_TYPE = ADDR_UNKNOWN;
		 }

		 crowlingBuildings = new ArrayList<Building>();
		 teamMembers = new ArrayList<Human>();
		 
		
/*
		 //チーム分け
		 isLeader = isMember = isOnTeam = false;
		 createCrowlingTeam();
		 isOnTeam = isLeader || isMember;
		 
		 if(isMember && !crowlingBuildings.isEmpty()){
		 	logger.info("Crowling. \n" + crowlingBuildings);
		 	for(Building b : crowlingBuildings){
		 		currentTaskList.add(new MoveTask(this, model, (Area)b));
		 	}
		 }
*/
	}
	
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);
		
		logger.setTime(time);
		if (time < config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
			return;
		}

		//currentTaskListに関する処理
		//currentTaskが終了していたら，そいつをリストから削除する
		removeFinishedTask();
		
		//自分の今いる場所に閉塞がある場合
		//情報をPFに送る
		reportBlockedRoadInLocation();
		
		//閉塞を報告してから3ターンたってもまだ啓開されていない
		//かつ，自分がまだ閉塞のそばにいる
		// -> 自分が閉塞の中に詰まっている可能性が高いので，再度報告する
		reportBlockadeAboutSelf();

		//自分の視界に燃えている建物がある場合
		//とりあえずその情報をFBに送りつける
		reportBurningBuildingInView();

		//自分の視界にある建物の中に市民がいる場合
		//とりあえずその情報をATに送りつける
		reportCivilianInView();
	}

	//メッセージの取得
	public List<naito_rescue.message.Message> receiveMessage(AKSpeak speak){
		if(speak.getContent() == null || speak.getContent().length <= 0){
			return null;
		}
		
		List<naito_rescue.message.Message> msgList = msgManager.receiveMessage(speak);
		if(msgList == null){
			return null;
		}
		
		for(naito_rescue.message.Message mes : msgList){
			if(mes.getAddrAgent() != me().getID().getValue() && mes.getAddrType() != MY_MESSAGE_ADDRESS_TYPE){
				//自分(もしくは自分と同種別のエージェント)に対するメッセージでなかったら無視する
				msgList.remove(mes);
			}
		}
		
		return msgList;
	}
//---------- report関連 ----------
	//過去に閉塞を報告したエリアについて，
	//. 自分がまだそのエリアにいて，
	//. まだ閉塞が啓開されておらず，
	//. 閉塞を報告してから2ターン以上経過している
	//場合に，再度PFに対して閉塞を報告する.
	//(自分が閉塞に詰まって動けなくなっている可能性が高い)
	private void reportBlockadeAboutSelf(){
		if(!(this instanceof NAITOPoliceForce)){
			for(Area reported : reportedBlockedRoad.keySet()){
				if(getLocation().getID().getValue() == reported.getID().getValue() &&
				   reported.isBlockadesDefined() &&
				   !(reported.getBlockades().isEmpty()) &&
				   (this.time - reportedBlockedRoad.get(reported)) >= 2){
					
					logger.info("Help me. reported = " + reported);
					ClearMessage clear_msg = msgManager.createClearMessage(-1, ADDR_PF, false, getLocation().getID());
					msgManager.sendMessage(clear_msg);
					reportedBlockedRoad.put(reported, this.time);
					
				}
			}
		}
	}
	private void reportCivilianInView(){
		List<Civilian> civilians = getViewCivilians();
		for(Civilian c : civilians){
			StandardEntity civilian_location = c.getPosition(model);
			//道路を突っ走ってる市民に対してLoadを実行しようとするとコケる気がする...
			if(civilian_location instanceof Building && !reportedVictimInBuilding.contains((Building)civilian_location)){
				logger.info("Report victim. victim = " + c + ", location = " + civilian_location);
				RescueMessage rescue_msg = msgManager.createRescueMessage(-1, ADDR_AT, false, civilian_location.getID());
				msgManager.sendMessage(rescue_msg);
				reportedVictimInBuilding.add((Building)civilian_location);
			}
		}
	}
	private void reportBurningBuildingInView(){
		List<Building> view_buildings = getViewBuildings();
		for(Building b : view_buildings){
			if(b.isOnFire() && !reportedBurningBuilding.contains(b)){
				logger.info("Report Burning Building. building = " + b);
				StandardEntityConstants.Fieryness fieryness = b.getFierynessEnum();
				ExtinguishMessage ex_msg = msgManager.createExtinguishMessage(-1, ADDR_FB, false, b.getID(), (b.isGroundAreaDefined()?b.getGroundArea():1000));
				msgManager.sendMessage(ex_msg);
				reportedBurningBuilding.add(b);
			}
		}
	}
	private void reportBlockedRoadInLocation(){
		if(!(this instanceof NAITOPoliceForce)){
			StandardEntity location = getLocation();
			if(location instanceof Area && ((Area)location).isBlockadesDefined() && !((Area)location).getBlockades().isEmpty()){
				// 閉塞が発生しているRoadのIDを送りつける
				//  -> 閉塞の発見と啓開は，このメッセージを受け取った啓開隊に任せる
				if( !(reportedBlockedRoad.containsKey( (Area)location )) ){
					logger.info("Report Blockade. blocked road = " + location);
					ClearMessage clear_msg = msgManager.createClearMessage(-1, ADDR_PF, false, getLocation().getID());
					msgManager.sendMessage(clear_msg);
					
					reportedBlockedRoad.put((Area)location, time);
				}
			}
		}
	}
//---------- //report関連 ----------

	private void removeFinishedTask(){
		if(currentTask != null && currentTask.isFinished()){
			
			logger.info("Remove currentTask. currentTask = " + currentTask);
			currentTaskList.remove(currentTask);
			currentTask = null;
		}
	}

//---------- 視界情報の取得関連 ----------
	public List<Building> getViewBuildings(){
		
		ArrayList<Building> buildings = new ArrayList<Building>();
		StandardEntity entity = null;
		for(EntityID id : this.changed.getChangedEntities()){
			entity = model.getEntity(id);
			if(entity instanceof Building){
				
				buildings.add((Building)entity);
			}
		}
		return buildings;
	}
    public List<Civilian> getViewCivilians(){
    	
    	List<Civilian> civilians = new ArrayList<Civilian>();
    	StandardEntity entity;
    	for(EntityID next : this.changed.getChangedEntities()){
    		entity = model.getEntity(next);
    		if(entity instanceof Civilian){
    			
    			civilians.add((Civilian)entity);
    		}
    	}
    	//追加
    	for(StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)){
    		Civilian civilian = (Civilian)next;
    		if(model.getEntity(civilian.getPosition()) instanceof Building){
    			civilians.add(civilian);
    		}
    	}
    	return civilians;
    }
//---------- //視界情報の取得関連 ----------

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
		//どの種類のエージェントが探訪を担当するか決定する
		int max = maxInt(atSize, pfSize, fbSize);
		
		/*
		if(atSize == max && atSize > CROWLABLE_NUM){
			//AmbulanceTeamが探訪する
			
			if(this instanceof NAITOAmbulanceTeam){
				
				if(atList.get(0).getID().getValue() == me().getID().getValue()){
					
					isLeader = true;
				}else{
					
					isMember = true;
				}
				teamMembers.addAll(atList);
				decideCrowlingBuildings();
			}
		}else if(pfSize == max && pfSize > CROWLABLE_NUM){
		*/
			//各種エージェントが，それぞれチームを作って回る
			/*
			if(this instanceof NAITOPoliceForce){
				if(pfList.get(0).getID().getValue() == me().getID().getValue()){
					isLeader = true;
				}else{
					isMember = true;
				}
				teamMembers.addAll(pfList);
				decideCrowlingBuildings();
			}
			*/
			if(this instanceof NAITOFireBrigade){
				if(pfList.get(0).getID().getValue() == me().getID().getValue()){
					isLeader = true;
				}else{
					isMember = true;
				}
				teamMembers.addAll(fbList);
				decideCrowlingBuildings();
			}
			if(this instanceof NAITOAmbulanceTeam){
				if(pfList.get(0).getID().getValue() == me().getID().getValue()){
					isLeader = true;
				}else{
					isMember = true;
				}
				teamMembers.addAll(atList);
				decideCrowlingBuildings();
			}
		/*
		}else if(fbSize == max && fbSize > CROWLABLE_NUM){
			//FireBrigadeが探訪する
			
			if(this instanceof NAITOFireBrigade){
				
				if(fbList.get(0).getID().getValue() == me().getID().getValue()){
					
					isLeader = true;
				}else{
					
					isMember = true;
				}
				teamMembers.addAll(fbList);
				decideCrowlingBuildings();
			}
		}else{
			//全員で探訪する
			
			
			if(allAgentsList.get(0).getID().getValue() == me().getID().getValue()){
				
				isLeader = true;
			}else{
				
				isMember = true;
			}
			teamMembers.addAll(allAgentsList);
			decideCrowlingBuildings();
			//}
			
	*/
	}
	//探訪する建物を決定する
	//(多分ここの計算はものすごい時間を食う)
	private void decideCrowlingBuildings(){
		int    roleID = teamMembers.indexOf(me());
		int    separateBlock = 1;
		double crowlingWidth, crowlingHeight, crowlingX, crowlingY;
		
		//DisasterSpaceをいくつのブロックに分割するかを決める
		for(; (separateBlock * separateBlock) < teamMembers.size();separateBlock++);
		
		separateBlock--;
		if(separateBlock < 1) separateBlock = 1;

		//roleIDの正規化...roleID=[0 ... pow(separateBlock)-1]になるように
		while(roleID >= (separateBlock * separateBlock)) roleID -= separateBlock;
		
		//どっからどこまでのBuildingを探訪するか決定する
		crowlingWidth  = (w_maxX - w_minX) / separateBlock;
		crowlingHeight = (w_maxY - w_minY) / separateBlock;
		crowlingX      = w_minX + w_width * (roleID % separateBlock);
		crowlingY      = w_minY + w_height * (roleID / separateBlock);
		
		Building b = null;
		StringBuffer pretty_crowling = new StringBuffer();
		pretty_crowling.append("\n");
		for(StandardEntity building : allBuildings){
			b = (Building)building;
			if(b.getX() > crowlingX && b.getX() <= (crowlingX + w_width) &&
			   b.getY() > crowlingY && b.getY() <= (crowlingY + w_height)){
				crowlingBuildings.add(b);
			}
		}
		logger.info(pretty_crowling.toString());
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
		

		if(currentTaskList.isEmpty()){
			//初期タスクの設定がここになる
			/*
			
			for(StandardEntity entity : allBuildings){
				currentTaskList.add(new MoveTask(this, model, (Area)entity));
			}
			*/
			return null;
		}
		int maxRank = Integer.MIN_VALUE;
		Task resultTask = currentTaskList.get(0); //念のため初めのタスクをいれておく
		Task tempTask;
		for(int i = 0;i < currentTaskList.size();i++){
			tempTask = currentTaskList.get(i);
			
			if(tempTask.getRank() > maxRank){
				maxRank = tempTask.getRank();
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
    
//---------- 閉塞の取得関連 ----------
    
    //行く手を遮る閉塞を得る
    public Blockade getBlockadeOnPath(List<EntityID> path){
    	logger.info("path = " + path);
		for(EntityID next : path){
			Area area = (Area)(model.getEntity(next));
			Blockade result = getTargetBlockade(area, maxRepairDistance);
			if(result != null){
				logger.info("Find blockade. blockade = " + result
				            + ", at " + area
				            + "(ID=" + area.getID().getValue() + ")");
				return result;
			}
		}
		//自分の身の回りについてみる
		return getTargetBlockade();
    }
    //自分が今いる場所から啓開可能な閉塞を返す
	public Blockade getTargetBlockade(){
		
		int maxDistance = maxRepairDistance;
		//自分のいる場所に着いて閉塞を得る
		Area location = (Area)getLocation();
		Blockade blockade = getTargetBlockade(location, maxDistance);
		if(blockade != null){
			logger.info("Find blockade. blockade = " + blockade + "at " + location);
			return blockade;
		}
        //自分のいる場所の近傍について閉塞を得る
        for (EntityID next : location.getNeighbours()) {
            location = (Area)(model.getEntity(next));
            blockade = getTargetBlockade(location, maxDistance);
            if (blockade != null) {
            	logger.info("Find blockade. blockade = " + blockade + "at " + location);
                return blockade;
            }
        }
		logger.info("There's no blockade.");
        return null;
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
	
    public Blockade getTargetBlockade(Area area, int maxDistance) {
        
		if (!area.isBlockadesDefined()) {
			logger.info(area + " is not defined blockade.");
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        // Find the first blockade that is in range.
        int x = getX();
        int y = getY();
        for (EntityID next : ids) {
            Blockade b = (Blockade)model.getEntity(next);
            double d = findDistanceTo(b, x, y);
            if (maxDistance < 0 || d < maxDistance) {
            	logger.info("Find blockade. blockade = " + b);
                return b;
            }
        }
        
        return null;
    }
//---------- //閉塞の取得関連 ----------
	//SampleAmbulanceTeamから移植
    public StandardEntity someoneOnBoard() {
        for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            if (((Human)next).getPosition().equals(getID())) {
                return next;
            }
        }
        return null;
    }
}
