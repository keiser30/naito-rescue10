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

	protected Set<StandardEntity> visited;
	protected MySearch            search;
	protected int                 startMoveTime = 0;
	protected EntityID            moveTo;
	protected boolean             isMovingNow = false;
	protected Collection<StandardEntity> allBuildings;

	
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		this.time = time;

		logger.info("********** " + time + " **********");
		logger.info("NAITOHumanoidAgent.think();");
		logger.debug("isMovingNow = " + isMovingNow);
		logger.debug("moveTo = " + moveTo);
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
		}
			
		StandardEntity location = getLocation();
		//自分の今いる場所に閉塞がある場合
		if(location instanceof Area && ((Area)location).isBlockadesDefined() && !((Area)location).getBlockades().isEmpty()){
				//閉塞が定義してあるRoadのIDを送る -> 閉塞の発見と啓開は，このメッセージを受け取ったPoliceForceに任せる
				if(useSpeak){
					speak(1, ("CLEAR_"+location.getID().getValue()).getBytes());
				}else{
					tell(("CLEAR_"+location.getID().getValue()).getBytes());
				}
				
				logger.debug("Find Blockade. speak(or tell, say) 'Please clear " + (Road)location + "'");
		}
		//移動に時間がかかっている場合
		if(isMovingNow && (time - startMoveTime) > 2){
				//閉塞に詰まってないのに時間がかかっている...
				//とりあえず再要求
				List<EntityID> path = null;
				if(location instanceof Road && model.getEntity(moveTo) instanceof Road){
					//ダイクストラ法のテスト
					logger.debug("ReMove.");
					logger.debug("Dijkstra TEST.");
					logger.debug("location = " + location + "\n      target = " + model.getEntity(moveTo));
					path = search.getRoute(location.getID(), moveTo);
				}else{
					logger.debug("breadthFirstSearch();");
					path = search.breadthFirstSearch(getLocation(), model.getEntity(moveTo));
				if(path != null){
					logger.debug("ReMove.");
					move(path);
				}else{
					logger.debug("Can't re-move because path is null. location = " + location + ", target = " + moveTo);
				}
			}
		}
/*		
		logger.info("NAITOHumanoidAgent.think();");
		logger.debug("startMoveTime = " + startMoveTime);
		logger.debug("time - startMoveTime = " + (time - startMoveTime));
		logger.debug("moveTo = " + moveTo);
		logger.debug("isMovingNow = " + isMovingNow);

		visited.add(getLocation());
		
		if(isMovingNow && 
		   moveTo != null && 
		   getLocation().getID().getValue() != moveTo.getValue()){
		   		if((time - startMoveTime > 3)){
				   logger.debug("Re-routing to target. location = " + getLocation() + " ,target = " + moveTo);
				   try{
						move(model.getEntity(moveTo)); //経路探索を、今いるところからやり直す。
					}catch(Exception e){
					
						isMovingNow = false;
						moveTo = null;
						startMoveTime = 0;
						return;
					
					}
				}else{
					logger.debug("(time - startMoveTime) < 3 ==> now moving to target(" + moveTo + ")");
				}
		}else{
			isMovingNow = false;
			moveTo = null;
			startMoveTime = 0;
		}
*/
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
		 visited = new HashSet<StandardEntity>();
		 search = new MySearch(model, this);

		 allBuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
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
		logger.debug("move()");
		EntityID fromID, toID;
		fromID = getLocation().getID();
		toID = target.getID();

		Collection<StandardEntity> fromNeighbours = search.findNeighbours(getLocation());
		Collection<StandardEntity> targetNeighbours = search.findNeighbours(target);
		if(getLocation() instanceof Building){
			logger.debug("from is Building.");
			for(StandardEntity from_neighbour : fromNeighbours){
				if(from_neighbour instanceof Road){
					logger.debug("new From = " + from_neighbour);
					fromID = from_neighbour.getID();
					break;
				}
			}
			if(target instanceof Building){
				logger.debug("targete is Building.");
				for(StandardEntity target_neighbour : targetNeighbours){
					if(target_neighbour instanceof Road){
						logger.debug("new Target = " + target_neighbour);
						toID = target_neighbour.getID();
						break;
					}
				}
				if(toID.getValue() == target.getID().getValue()){
					logger.debug("Target Building is not adjacent to Road.");
					/*isMovingNow = false;
					moveTo = null;
					throw new Exception("Target Building is not adjacent to Road.");
					*/
					return;
				}
			}
		}else if(target instanceof Building){
			logger.debug("target is Building.");
			for(StandardEntity target_neighbour : targetNeighbours){
				if(target_neighbour instanceof Road){
					logger.debug("new Target = " + target_neighbour);
					toID = target_neighbour.getID();
					break;
				}
			}
			if(toID.getValue() == target.getID().getValue()){
				logger.debug("Target Building is not adjacent to Road.");
				/*
				isMovingNow = false;
				moveTo = null;
				throw new Exception("Target Building is not adjacent to Road.");
				*/
				return;
			}
		}

		List<EntityID> path = search.getRoute(fromID, toID);
		if(getLocation() instanceof Building){
			path.add(0, getLocation().getID());
		}
		if(target instanceof Building){
			path.add((path.size()), target.getID());
		}
		logger.debug("path = " + path);
		move(path);
	}
	public void move(List<EntityID> path){
		logger.trace("move(path)");
		this.isMovingNow = true;
		this.moveTo = path.get(path.size()-1); //最後の要素 = 最終目的地
		sendMove(time, path);
		this.startMoveTime = time;
	}
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

