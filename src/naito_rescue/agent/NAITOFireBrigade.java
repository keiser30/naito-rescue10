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

/* 平沢進のCDはやく届かないかなー */
public class NAITOFireBrigade extends NAITOHumanoidAgent<FireBrigade>
{
	boolean debug = true;
	
	private ArrayList<Building> burningHearBuildings;
	
	private boolean isOverrideVoice;
	private boolean isOverrideNear;
	
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        
		if(crowlingBuildings.isEmpty()){
			//自分がisMemberなのにも関わらずcrowlingBuidlingが空っぽ...
			//かわいそうなので声データ優先にします
			logger.info("Kawaisou => true");
			isOverrideVoice = true;
		}else if(fbList.size() < 5){
			isOverrideVoice = (fbList.indexOf(me()) % 3) < 2;
		}else{
			//全体の3/4が声データ優先
			isOverrideVoice = (fbList.indexOf(me()) % 4) < 3;
		}
		isOverrideNear = (fbList.indexOf(me()) % 2) == 0;
		burningHearBuildings = new ArrayList<Building>();
	}

	public String toString(){
		return "NAITOFireBrigade." + me().getID() + "";
	}

	protected void think(int time, ChangeSet changed, Collection<Command> heard){

		super.think(time, changed, heard);
		if(time < 3){
			return;
		}
		
		//シンガポールのときに使ったものから引っぺがして修正
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
				
				//ノイズ対策
				if(msgList == null){
					logger.debug("msgList == null (maybe )");
					continue;
				}
				logger.info("Extracting messages size = " + msgList.size());
				for(naito_rescue.message.Message message : msgList){
					if(message.getAddrAgent() != me().getID().getValue() && message.getAddrType() != ADDR_FB){
						logger.info("Ignore message.");
						continue; //自分(もしくは自分と同じ種別のエージェント)宛のメッセージでなかったら無視
					}

					if(message.getType() == TYPE_FIRE){
						logger.info("TYPE_FIRE messsage has received.");
						EntityID target_id = ((ExtinguishMessage)message).getTarget();
						StandardEntity target = model.getEntity(target_id);
						logger.info("currentTaskList.add(ExtinguishTask(" + target + ")");
						//currentTaskList.add(new ExtinguishTask(this, model, (Building)target, maxPower, maxExtinguishDistance));
						burningHearBuildings.add((Building)target);
					}
				}//end inner-for.
			}
		}//end outer-for.
        // Are we currently filling with water?
        if (me.isWaterDefined() && me.getWater() < (maxWater*0.4) && location() instanceof Refuge) {
            logger.info("Filling with water at " + location());
            rest();
            return;
        }
        // Are we out of water?
        if ((me.isWaterDefined() && me.getWater() == 0) ||
            (me.getHP() < (me.getStamina() / 55555) )) {
            // Head for a refuge
            List<EntityID> path = search.breadthFirstSearch(getLocation(), allRefuges);
            if (path != null) {
                logger.info("Moving to refuge");
                move(path);
                return;
            }
            else {
                logger.debug("Couldn't plan a path to a refuge.");
                path = randomWalk();
                logger.info("Moving randomly");
                move(path);
                return;
            }
        }
		//自分の視界範囲にある延焼中のビルディングを処理
		List<Building> burningBuildings = getViewBuildings();
		for(Building b : burningBuildings){
			if(b.isOnFire() && model.getDistance(me.getPosition(), b.getID()) <= maxExtinguishDistance){
				extinguish(b.getID(), maxExtinguishPower);
				if(burningHearBuildings.contains(b)) burningHearBuildings.remove(b);
				if(!crowlingBuildings.isEmpty() && crowlingBuildings.contains(b)){
					crowlingBuildings.remove(b);
				}
			}
		}
		//聴覚情報からの処理
		if(!burningHearBuildings.isEmpty()){
			for(Building b : burningHearBuildings){
				if(model.getDistance(me.getPosition(), b.getID()) <= maxExtinguishDistance){
					extinguish(b.getID(), maxExtinguishPower);
					burningHearBuildings.remove(b);
					if(!crowlingBuildings.isEmpty() && crowlingBuildings.contains(b)){
						crowlingBuildings.remove(b);
					}
				}else{
					move(b);
				}
			}
		}
		
		//チーム分けしてビルディングを回る
		if(!crowlingBuildings.isEmpty()){
			List<EntityID> path = search.breadthFirstSearch(getLocation(), crowlingBuildings.get(0));
			if(path != null) search.isPassable(path); //test
			move(crowlingBuildings.get(0));
		}
	}
	
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum(){
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
	}

}
