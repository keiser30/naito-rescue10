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

	protected boolean useSpeak;
	protected int     time;
	protected List<Task> currentTaskList = new ArrayList<Task>();
	protected Task    currentTask;
	protected Job     currentJob;
	protected MyLogger logger;

	protected Set<StandardEntity> visited = new HashSet<StandardEntity>();
	protected SampleSearch search;

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		this.time = time;
		if(search == null){
			search = new SampleSearch(model);
		}
		if(currentTask != null && !currentTask.isFinished()){
		}
	}
	@Override
    protected void postConnect() {
		 super.postConnect();
		 //search = new SampleSearch(model);
		 useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
		 logger = new MyLogger(this, true);
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
	public void move(Area target){
		List<EntityID> path = new LinkedList<EntityID>();
		path.add(target.getID());
		move(path);
	}
	public void move(List<EntityID> target){
		sendMove(time, target);
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

}

