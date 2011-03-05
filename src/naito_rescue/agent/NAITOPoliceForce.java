package naito_rescue.agent;

import java.util.*;
import java.io.*;

import rescuecore2.worldmodel.*;
import rescuecore2.standard.messages.*;
import rescuecore2.messages.*;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.*;
import rescuecore2.misc.geometry.*;
import naito_rescue.*;
import naito_rescue.agent.*;
import naito_rescue.object.*;
import naito_rescue.router.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;
import naito_rescue.message.manager.*;

/**
*  啓開隊だよ
*
*/
public class NAITOPoliceForce extends NAITOHumanoidAgent<PoliceForce>
{
	
	@Override
	public String toString(){
		return "NAITOPoliceForce." + me().getID() + "";
	}

	@Override
	protected void postConnect(){
        super.postConnect();
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);
		if (time < config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
			return;
		}
		//logger.info("\n");
		//logger.info("##########    NAITOPoliceForce.think();    ##########");
		//logger.info("Current Area = " + getLocation());
		//logger.info("CurrentTaskList = " + currentTaskList);
		//logger.debug("Preferred Task = " + currentTaskList.peek());
		
		//こいつは遠い所から行かせた方がいいんじゃないか
		
		// 自分のいる場所から隣接エリアへ通行不可となっている閉塞を啓開する
		// (本当はPure Task-Jobでやりたい)
		
		Area location = (Area)(getLocation());
		Blockade target = getTargetBlockade(location, maxRepairDistance / 2);
		if(target != null){
		}
		//addTaskByMessage();
		removeFinishedTask();
		updateTaskPriority();
		
		if(currentTaskList == null || currentTaskList.isEmpty()){
			move(randomWalk());
			return;
		}
		currentTask = currentTaskList.peek();
		//if(currentTask != null)
		//logger.info("currentTask = " + currentTask);
		
		//ここの構造を直したい
		Job currentJob = currentTask.currentJob();
		if(currentJob != null){
			//logger.info("currentJob = " + currentJob);
			currentJob.act();
		}
		else{
			//一番優先度の高いMoveTaskを持ってきて実行，など．
		}
		
	}
	protected void removeFinishedTask(){
		//logger.info("** removeFinishedTask(); **");
		for(Iterator<Task> it = currentTaskList.iterator();it.hasNext();){
			Task t = it.next();
			if(t.isFinished()){
				//logger.debug("Remove finished task: " + t);
				it.remove();
			}
		}
		//logger.info("** removeFinishedTask(); end **");
	}
	protected void addTaskByMessage(){
		//logger.info("@@@@ NAITOPoliceForce.addTaskByMessage(); @@@@");
		for(NAITOMessage m : receivedNow){
			//logger.debug("Process Message: " + m);
			if(m instanceof BlockedRoadMessage){
				BlockedRoadMessage brm = (BlockedRoadMessage)m;
				//logger.debug("One BlockedRoadMessage has received. ");
				List<EntityID> ids = brm.getIDs();
				for(EntityID id : ids){
					NAITOArea nArea = allNAITOAreas.get(id);
					if(nArea != null){
						//logger.info("Add ClearPathTask(" + nArea + ").");
						addTaskIfNew(new ClearPathTask(this, nArea));
					}
				}
			}else if(m instanceof HelpMeInBlockadeMessage){
			}
		}
		//logger.info("@@@@ NAITOPoliceForce.addTaskByMessage(); end. @@@@");
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
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }
    
    public Blockade getTargetBlockade(Area area, int maxDistance) {
        //logger.debug("Looking for nearest blockade in " + area);
        //logger.info("NAITOPoliceForce.getTargetBlockade(" + area + ", " + maxDistance + ")");
		if (!area.isBlockadesDefined()) {
            //Logger.debug("Blockades undefined");
			//logger.info("!area.isBlockadesDefined(); ==> return null;");
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        // Find the first blockade that is in range.
        int x = me().getX();
        int y = me().getY();
        for (EntityID next : ids) {
            Blockade b = (Blockade)model.getEntity(next);
            double d = findDistanceTo(b, x, y);
            //logger.debug("Distance to " + b + " = " + d);
            if (maxDistance < 0 || d < maxDistance) {
                //logger.debug("In range");
				//logger.info("There is blockade.");
				//logger.debug("" + b);
                return b;
            }
        }
        //logger.info("No blockades in range");
        return null;
    }	

}
