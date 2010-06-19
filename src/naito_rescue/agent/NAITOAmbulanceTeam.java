package naito_rescue.agent;

import java.util.*;

import rescuecore2.worldmodel.*;
import rescuecore2.standard.messages.*;
import rescuecore2.messages.*;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.*;

import naito_rescue.*;
import naito_rescue.message.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;

/**
*  救急隊だよ
*
*/
public class NAITOAmbulanceTeam extends NAITOHumanoidAgent<AmbulanceTeam>
{
	private Collection<StandardEntity> unexploredBuildings;
	private StandardEntity target_building;
	private int            team;

	@Override
	protected void postConnect(){
        super.postConnect();
		model.indexClass(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.REFUGE, StandardEntityURN.BUILDING);
		unexploredBuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);

	}
	@Override
	public String toString(){
		return "NAITOAmbulanceTeam." + me().getID() + "";
	}

	boolean once = true;
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);

		logger.info("NAITOAmbulanceTeam.think();");
        if (time <= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			if(time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
           		 // Subscribe to channel 3
				logger.info("sendSubscribe(" + time + ", 1");
           	 	sendSubscribe(time, 1);
			}else{
				return;
			}
        }

		logger.info("NAITOAmbulanceTeam.hearing...");
		for(Command next : heard){
			logger.debug("heard->next = " + next);
			if(next instanceof AKSpeak){
				/**
				*  無線or声データの処理
				*/
				logger.info("Receive AKSpeak.");
				AKSpeak speak = (AKSpeak)next;
				//ノイズ対策
				if(speak.getContent() == null || speak.getContent().length <= 0){
					logger.debug("speak.getContent() => null or length<=0 ");
					continue;
				}
				List<naito_rescue.message.Message> msgList = msgManager.receiveMessage(speak);
				logger.info("Extracting messages size = " + msgList.size());
				for(naito_rescue.message.Message message : msgList){

					if(message.getAddrAgent() != me().getID().getValue() && message.getAddrType() != ADDR_AT){
						logger.info("Ignore message.");
						continue; //自分(もしくは自分と同じ種別のエージェント)宛のメッセージでなかったら無視
					}

					if(message.getType() == TYPE_RESCUE){
						logger.info("TYPE_RESCUE message has received.");
						RescueMessage resc = (RescueMessage)message;
						EntityID target = resc.getTarget();
						StandardEntity target_entity = model.getEntity(target);
						logger.debug("=> target = " + target_entity);
						logger.info("=> currentTaskList.add(new RescueTask(" + target_entity + "));");
						currentTaskList.add(new RescueTask(this, model, (Building)target_entity));
					}
				}
			}
		}

		for(Human hmn : getTargets()){
			if(model.getEntity(hmn.getPosition()) instanceof Building){
				Building target = (Building)(model.getEntity(hmn.getPosition()));
				currentTaskList.add(new RescueTask(this, model, target));
				logger.info("currentTaskList.add(new RescueTask(...));");
			}
		}
/*
		//RescueTaskのテスト
		if(once){
			logger.debug("DEBUG: RescueTask is valid?");
			currentTaskList.add(new RescueTask(this, model, (Building)model.getEntity(new EntityID(254))));
			once = false;
		}
		//TODO: 視界情報にある建物にいる市民の探訪
*/
		currentTask = action();
		currentJob = currentTask.currentJob();
		logger.info("currentTask = " + currentTask);
		logger.info("currentJob  = " + currentJob);
		if(currentJob != null)
			currentJob.doJob();
		else
			logger.debug("currentJob is null.");
			
		move(randomWalk());
	}
	
	@Override
	public void taskRankUpdate(){
		logger.info("AmbulanceTeam.taskRankUpdate();");
		int distance;
		int rank;
		double width = (w_width > w_height? w_width : w_height);
		
		for(Task t : currentTaskList){
			//RescueTask: 割り当て10000...5000
			//実行中のRescueTask(運搬中のもの)には最大値を割り当てる
			if(t instanceof RescueTask){
				logger.info("taskRankUpdate=>RescueTask");
				if(currentTask != null && currentTask.equals(t) && !currentTask.isFinished() && someoneOnBoard() != null){
					logger.debug("RescueTask: agent is moving to refuge now. ただいま運搬中");
					logger.debug("=> t.setRank(Integer.MAX_VALUE);");
					t.setRank(Integer.MAX_VALUE); //何が何でも実行する
					continue;
				}
				distance = model.getDistance(getLocation(), ((RescueTask)t).getTarget());
				rank = basicRankAssign(10000, 5000, distance, width);
				logger.info("t.setRank(" + rank + ");");
				t.setRank(rank);
			}
			//MoveTask:
			else if(t instanceof MoveTask){
				logger.info("taskRankUpdate=>MoveTask");
				Area target = ((MoveTask)t).getTarget();
				if(!search.isPassable(getLocation(), target)){
					//通行不可能な場合，実行しない
					logger.debug("MoveTask=>!isPassable(); => setRank(Integer.MIN_VALUE");
					t.setRank(Integer.MIN_VALUE);
					//currentTaskList.remove(t);
					continue;
				}
				distance = model.getDistance(getLocation(), target);
				if(isOnTeam){
					//割り当て9000...5000
					logger.debug("taskRankUpdate=>MoveTask=>isOnTeam");
					rank = basicRankAssign(9000, 5000, distance, width);
				}else{
					//割り当て4000...1000(default)
					logger.debug("taskRankUpdate=>MoveTask=>!isOnTeam");
					rank = basicRankAssign(4000, 1000, distance, width);
				}
				logger.info("t.setRank(" + rank + ");");
				t.setRank(rank);
			}
			/*
			//RestTask:
			else if(t instanceof RestTask){
				logger.info("taskRankUpdate=>RestTask");
				logger.info("t.setRank(Integer.MAX_VALUE);");
				t.setRank(Integer.MAX_VALUE);
			}
			*/
		}
	}
	
	//RescueTask, MoveTaskなどにおいて，自分から対象までの距離を元にしたタスク優先度の割り当てをおこなう
	//(距離が遠くなるほど優先度は低くなる)
	private int basicRankAssign(int maxRank, int minRank, int distance, double world_width){
		logger.debug("basicRankAssign();");
		logger.debug("maxRank  = " + maxRank);
		logger.debug("minRank  = " + minRank);
		logger.debug("distance = " + distance);
		
		int rank = maxRank;
		logger.trace("distance = " + distance);
		if(distance > 0){
			int increment = (int)((maxRank - minRank) * (distance / world_width));
			if(increment > minRank){
				increment = minRank;
			}
			logger.trace("increment = " + increment);
			rank = maxRank - increment;
		}
		logger.debug("rank = " + rank);
		return rank;
	}

    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}

    private List<Human> getTargets() {
        List<Human> targets = new ArrayList<Human>();
        for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN, StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.AMBULANCE_TEAM)) {
            Human h = (Human)next;
            if (h == me()) {
                continue;
            }
            if (h.isHPDefined()
                && h.isBuriednessDefined()
                && h.isDamageDefined()
                && h.isPositionDefined()
                && h.getHP() > 0
                && (h.getBuriedness() > 0 || h.getDamage() > 0)
				&& h.getBuriedness() > 0){
                logger.debug("targets.add(" + h + ")");
                targets.add(h);
            }
        }
        Collections.sort(targets, new DistanceSorter(location(), model));
        return targets;
    }
/*
	private void updateTargetBuildings(){
		logger.debug("updateTargetBuildings();");
		logger.debug("visitedBuildings = " + visitedBuildings);
		for(StandardEntity visited : visitedBuildings){
			targetBuildings.remove(visited);
		}
	}
*/
    private void updateUnexploredBuildings(ChangeSet changed) {
    	logger.trace("called updateUnexploredBuildings();");
        for (EntityID next : changed.getChangedEntities()) {
            StandardEntity e = model.getEntity(next);
            if (e != null) {
                unexploredBuildings.remove(e);
            }
        }
    }
}
