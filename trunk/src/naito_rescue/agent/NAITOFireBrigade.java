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

import naito_rescue.*;
import naito_rescue.agent.*;
import naito_rescue.object.*;
import naito_rescue.router.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;
import naito_rescue.message.manager.*;
import static naito_rescue.debug.DebugUtil.*;

/* Task-Jobを基にした行動設計 */
public class NAITOFireBrigade extends NAITOHumanoidAgent<FireBrigade>
{
	boolean debug = true;
	
	private ArrayList<Building> burningHearBuildings;
	
	private boolean isOverrideVoice;
	private boolean isOverrideNear;
	
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);

	}

	public String toString(){
		return "NAITOFireBrigade." + me().getID() + "";
	}

	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);
		if (time < config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
			return;
		}
		logger.info("\n");
		logger.info("##########    NAITOFireBrigade.think();    ##########");
		logger.info("Current Area = " + getLocation());
		logger.info("CurrentTaskList = " + currentTaskList);
		logger.debug("Preferred Task = " + currentTaskList.peek());
		
		addTaskInExtinguishableRange();
		addTaskByMessage();
		removeFinishedTask();
		updateTaskPriority();
		
		if(currentTaskList.isEmpty()){
			//logger.info("currentTaskList.isEmpty(); ==> randomWalk();");
			move(randomWalk());
			return;
		}
		
		//logger.info("NAITOFireBrigade.currentTaskList = " + currentTaskList);
		//currentTask = currentTaskList.last();
		
		currentTask = currentTaskList.peek();
		if(currentTask != null) logger.info("currentTask = " + currentTask);
		//logger.debug("currentTask.priority = " + currentTask.getPriority());
		//logger.debug("All Tasks priority debug print...");
		StringBuffer sb = new StringBuffer();
		for(Task t : currentTaskList){
			sb.append(t.getPriority() + ", ");
		}
		//logger.debug(sb.toString());
		
		//ここの構造を直したい
		Job currentJob = currentTask.currentJob();
		if(currentJob != null){
			logger.info("currentJob = " + currentJob);
			currentJob.act();
		}
		else{
			//一番優先度の高いMoveTaskを持ってきて実行，など．
		}
	}
	protected void addTaskInExtinguishableRange(){
		//logger.info("** addTaskInExtinguishableRange; **");
		for(StandardEntity b : allBuildings){
			Building building = (Building)b;
			
			int distance = model.getDistance(getLocation(), building);
			if(building.isOnFire()){
				//currentTaskList.add(new ExtinguishTask(this, building));
				addTaskIfNew(new ExtinguishTask(this, building));
				//logger.debug("Add ExtinguishTask In Range.:" + building);
			}
		}
		//logger.info("** addTaskInExtinguishableRange; end **");
	}

	protected void removeFinishedTask(){
		logger.info("** removeFinishedTask(); **");
		for(Iterator<Task> it = currentTaskList.iterator();it.hasNext();){
			Task t = it.next();
			if(t.isFinished()){
				logger.debug("Remove finished task: " + t);
				it.remove();
			}
		}
		logger.info("** removeFinishedTask(); end **");
	}
	protected void addTaskByMessage(){
		//logger.info("** addTaskByMessage(); **");
		for(NAITOMessage m : receivedNow){
			if(m instanceof FireMessage){
				FireMessage fm = (FireMessage)m;
				List<EntityID> ids = fm.getIDs();
				for(EntityID id : ids){
					Building target = (Building)(model.getEntity(id));
					//currentTaskList.add(new ExtinguishTask(this, target));
					addTaskIfNew(new ExtinguishTask(this, target));
					//logger.debug("Add ExtinguishTask By Message.:" + target);
				}
			}
		}
		//logger.info("** addTaskByMessage(); end **");
	}
	protected void updateTaskPriority(){
		Object[] tasks = currentTaskList.toArray();
		currentTaskList.clear();
		for(Object t : tasks){
			Task task = (Task)t;
			task.updatePriority();
			currentTaskList.add(task);
		}
	}
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
	}

}
