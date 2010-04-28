package naito_rescue.agent;

import java.util.*;

import sample.*;
import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;

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

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		this.time = time;
		//logger.info("NAITOHumanoidAgent.think();");
		if(this instanceof NAITOPoliceForce){
			logger.info("NAITOHumanoidAgent.think();");
			logger.debug("startMoveTime = " + startMoveTime);
			logger.debug("time - startMoveTime = " + (time - startMoveTime));
			logger.debug("moveTo = " + moveTo);
			logger.debug("isMovingNow = " + isMovingNow);
		}
		if(isMovingNow && 
		   moveTo != null && 
		   getLocation().getID().getValue() != moveTo.getValue()){
		   		if((time - startMoveTime > 3)){
				   logger.debug("Re-routing to target. location = " + getLocation() + " ,target = " + moveTo);
					move(moveTo); //経路探索を、今いるところからやり直す。
				}else{
					logger.debug("(time - startMoveTime) < 3 ==> now moving to target(" + moveTo + ")");
					return;
				}
		}else{
			isMovingNow = false;
			moveTo = null;
		}
		if(currentTask != null && !currentTask.isFinished()){
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

	//メソッドのラッパー群
	public void move(EntityID target){
		//List<EntityID> path = new LinkedList<EntityID>();
		List<EntityID> path = search.getRoute(getLocation().getID(), target);
		//path.add(target.getID());
		move(path);
	}
	public void move(List<EntityID> target){
		this.isMovingNow = true;
		this.moveTo = target.get(target.size()-1); //最後の要素 = 最終目的地
		sendMove(time, target);
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
	//currentTaskListの優先度を更新
	public abstract void taskRankUpdate();

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

