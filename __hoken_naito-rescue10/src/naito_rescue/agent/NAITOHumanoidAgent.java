package naito_rescue.agent;

import java.util.*;

import sample.*;
import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.misc.geometry.*;

import naito_rescue.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;

public abstract class NAITOHumanoidAgent<E extends StandardEntity> extends NAITOAgent<E>
{
    private static final String SAY_COMMUNICATION_MODEL = "kernel.standard.StandardCommunicationModel";
    private static final String SPEAK_COMMUNICATION_MODEL = "kernel.standard.ChannelCommunicationModel";

	protected boolean             useSpeak;
	protected int                 time;
	protected ArrayList<Task>     currentTaskList;
	protected Task                currentTask;
	protected Job                 currentJob;
	protected MyLogger            logger;

	protected Set<StandardEntity> visitedBuildings;
	protected Set<StandardEntity> targetBuildings;
	protected MySearch            search;
	protected int                 startMoveTime = 0;
	protected List<EntityID>      movePath, movePath_temp;
	protected EntityID            moveTo;
	protected boolean             isMovingNow = false;
	protected Collection<StandardEntity> allBuildings;
	protected Collection<StandardEntity> allRoads;
	protected Collection<StandardEntity> allRefuges;
	protected Collection<StandardEntity> firestation;
	protected Collection<StandardEntity> policeoffice;
	protected Collection<StandardEntity> ambulancecenter;
	
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		this.time = time;

		logger.info("**********____" + time + "____**********");
		logger.info("NAITOHumanoidAgent.think();");
		logger.info("location = " + getLocation());
		logger.debug("isMovingNow = " + isMovingNow);
		logger.debug("moveTo = " + model.getEntity(moveTo));
		logger.debug("time - startMoveTime = " + (time - startMoveTime));
		

		//currentTaskListに関する処理
		//currentTaskが終了していたら，そいつをリストから削除する
		if(currentTask != null && currentTask.isFinished()){
			logger.debug("currentTaskList.remove(" + currentTask + ")");
			currentTaskList.remove(currentTask);
		}
		if(isMovingNow && getLocation().getID().getValue() == moveTo.getValue()){
			logger.debug("Moving end.");
			isMovingNow = false;
			moveTo = null;
			//if(movePath != null) movePath.clear();
		}
			
		StandardEntity location = getLocation();
		//自分の今いる場所に閉塞がある場合
		if(location instanceof Area && ((Area)location).isBlockadesDefined() && !((Area)location).getBlockades().isEmpty()){

			// 閉塞が発生しているRoadのIDを送りつける
			//  -> 閉塞の発見と啓開は，このメッセージを受け取った啓開隊に任せる
			if(!(this instanceof NAITOPoliceForce)){
				if(useSpeak){
					logger.debug("speak clear road.");
					try{
						speak(0, ("CLEAR_"+location.getID().getValue()).getBytes("UTF-8"));
					}catch(Exception e){}
				}else{
					logger.debug("say clear road.");
					try{
						say(("CLEAR_"+location.getID().getValue()).getBytes("UTF-8"));
					}catch(Exception e){}
				}
				logger.debug("Find Blockade. speak(or tell, say) 'Please clear " + (Road)location + "'");
			}
		}

		if(currentTask != null && !currentTask.isFinished()){
			logger.info("NAITOHumanoidAgent.think() ... currentTask != null.");
			logger.info("currentTask = " + currentTask);
			logger.info("currentJob = " + currentJob);
			currentJob = currentTask.currentJob();
			currentJob.doJob();
		}
	}
	@Override
    protected void postConnect() {
		 super.postConnect();
		 //search = new SampleSearch(model);
		 useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
		 logger = new MyLogger(this, false);
		 currentTaskList = new ArrayList<Task>();
		 visitedBuildings = new HashSet<StandardEntity>();
		 targetBuildings = new HashSet<StandardEntity>();
		 movePath = new ArrayList<EntityID>();
		 movePath_temp = new ArrayList<EntityID>();
		 search = new MySearch(model, this);

		 /**
		  * 各種建物に関する情報を収集する
		  */
		 allBuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		 allRoads = model.getEntitiesOfType(StandardEntityURN.ROAD);
		 allRefuges = model.getEntitiesOfType(StandardEntityURN.REFUGE);
		 firestation = model.getEntitiesOfType(StandardEntityURN.FIRE_STATION);
		 policeoffice = model.getEntitiesOfType(StandardEntityURN.POLICE_OFFICE);
		 ambulancecenter = model.getEntitiesOfType(StandardEntityURN.AMBULANCE_CENTRE);
		 targetBuildings.addAll(allBuildings);
	}
	
	public void taskRankUpdate(){
		for(int i = 0;i < currentTaskList.size();i++){
			Task task = currentTaskList.get(i);
			if(task instanceof MoveTask){
				Area target = ((MoveTask)task).getTarget();
				boolean passable = search.isPassable(getLocation(), target);
				if(!passable){
					logger.debug("MoveTask is not passable!! " + target);
					//通行不可能なら、このタスクは諦める
					currentTaskList.remove(i); 
				}
			}
		}
	}
	@Override
	public String toString(){
		return "NAITOHumanoidAgent: " + me().getID();
	}
	public int getTime(){
		return time;
	}

	public StandardEntity getLocation(){
		return location();
	}

	public MyLogger getLogger(){
		return logger;
	}
	public StandardWorldModel getWorldModel(){
		return model;
	}
	public MySearch getSearch(){
		return search;
	}
	public E getMe(){
		return me();
	}

	//メソッドのラッパー群
	public void move(StandardEntity target) throws Exception{
 //ダイクストラ法の経路探索が実装できるまで，breadthFirstSearchを使う
		List<EntityID> path = search.breadthFirstSearch(getLocation(), target);
		if(path != null){
			move(path);
		}else{
			logger.debug("path is null.");
			logger.debug("location = " + getLocation());
			logger.debug("target   = " + target);
		}
	}
	public void move(StandardEntity target, int x, int y){
		List<EntityID> path = search.breadthFirstSearch(getLocation(), target);
		if(path != null){
			move(path);
		}else{
			logger.debug("path is null.");
			logger.debug("location = " + getLocation());
			logger.debug("target   = " + target);
		}
	}
	public void move(List<EntityID> path){
		logger.debug("NAITOHumanoidAgent.move(path);");
		logger.trace("move(path)");
		this.isMovingNow = true;
		this.moveTo = path.get(path.size()-1); //最後の要素 = 最終目的地
		sendMove(time, path);
		this.startMoveTime = time;
	}
	/** 
	*    reMove()
	*    注意!! 原因不明のバグあり．
	*    修正されるまで呼び出してはならない
	*/
	/*private void reMove(){
		logger.debug("NAITOHumanoidAgent.reMove();");
		StandardEntity location = getLocation();
		if(movePath.size() == 0){
			logger.debug("movePath().size == 0");
			return;
		}
		logger.debug("movePath = " + movePath);
		for(int i = 0;i < movePath.size();i++){
			if(movePath.get(i).getValue() == location.getID().getValue()){
				logger.debug("movePath: contains getLocation(): idx = " + i);
				logger.debug("movePath: location = " + location);
				logger.debug("movePath().size = " + movePath.size());
				logger.debug("sendMove(time, movePath.subList(" + (i+1) + ", " + (movePath.size()) + ")");
				sendMove(time, movePath.subList(i+1, movePath.size()));
				return;
			}
		}
		logger.debug("Can't reMove. movePath is not contains getLocation()");
	}*/
	public void move(List<EntityID> path, int x, int y){
		logger.trace("move(path,x,y)");
		this.isMovingNow = true;
		this.moveTo = path.get(path.size() - 1);
		sendMove(time, path, x, y);
		this.startMoveTime = time;
	}
	public void extinguish(EntityID target, int water){
		sendExtinguish(time, target, water);
	}
	public void clear(EntityID target){
		sendClear(time, target);
	}
	public void load(EntityID target){
		sendLoad(time, target);
	}
	public void unload(){
		sendUnload(time);
	}
	public void rescue(EntityID target){
		sendRescue(time, target);
	}
	public void rest(){
		sendRest(time);
	}
	public void speak(int channel, byte[] data){
		sendSpeak(time, channel, data);
	}
	public void subscribe(int... channels){
		sendSubscribe(time, channels);
	}
	public void say(byte[] data){
		sendSay(time, data);
	}
	public void tell(byte[] data){
		sendTell(time, data);
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
        //logger.debug("Finding distance to " + b + " from " + x + ", " + y);
        List<Line2D> lines = GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
        double best = Double.MAX_VALUE;
        Point2D origin = new Point2D(x, y); 
        for (Line2D next : lines) {
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
            double d = GeometryTools2D.getDistance(origin, closest);
            //logger.debug("Next line: " + next + ", closest point: " + closest + ", distance: " + d); 
            if (d < best) {
                best = d;
                //logger.debug("New best distance");
            }   

        }
        return (int)best;
	}
	/**
	*  currentTaskListを降順ソートして1番目の要素を返す
	*  (Taskのランク値の大きい方が優先される)
	*/
	public Task getHighestRankTask(){
		Comparator<Task> task_comp = new Comparator<Task>(){
			public int compare(Task t1, Task t2){
				return t2.getRank() - t1.getRank();
			}
		};

		Collections.sort(currentTaskList, task_comp);
		return currentTaskList.remove(0);
	}
}

