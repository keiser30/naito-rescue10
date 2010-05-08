package naito_rescue.agent;

import java.util.*;

import rescuecore2.worldmodel.*;
import rescuecore2.standard.messages.*;
import rescuecore2.messages.*;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.*;

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
    	
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard){
		super.think(time, changed, heard);

		logger.info("NAITOAmbulanceTeam.think();");
        if (time <= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
			if(time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)){
           		 // Subscribe to channel 3
				logger.info("sendSubscribe(" + time + ", 1");
           	 	sendSubscribe(time, 3);
			}else{
				return;
			}
        }

		
		if(target_building != null && getLocation().getID().getValue() == target_building.getID().getValue()){
			visitedBuildings.add(target_building);
		}
		//ボイスデータの処理
        for (Command next : heard) {
			byte[] rawdata = null;
			if(next instanceof AKSpeak || next instanceof AKTell || next instanceof AKSay){
				logger.debug("AKSpeak(or AKTell or AKSay) data received.");
				if(next instanceof AKSpeak){
					rawdata = ((AKSpeak)next).getContent();
				}else if(next instanceof AKTell){
					rawdata = ((AKTell)next).getContent();
				}else if(next instanceof AKSay){
					rawdata = ((AKSay)next).getContent();
				}else{
					break;
				}
				if(rawdata != null){
					String str_data = null;
					logger.debug("Extracting voice data...");
					try{
						str_data = new String(rawdata, "UTF-8");
						logger.trace("str_data = " + str_data);
					}catch(Exception e){
						logger.info("Exception in Extracting voice data.");
						logger.trace("continue for-loop.");
						continue;
					}
					logger.debug("Extracting visited building id...");
					EntityID visitedID = extractVisitedBuildingID(str_data);
					if(visitedID == null) continue;
					visitedBuildings.add(model.getEntity(visitedID));
				}
			}
        }
        
		updateTargetBuildings();
        updateUnexploredBuildings(changed);
        // Am I transporting a civilian to a refuge?
        if (someoneOnBoard()) {
            // Am I at a refuge?
            if (location() instanceof Refuge) {
                // Unload!
                Logger.info("Unloading");
                sendUnload(time);
                return;
            }
            else {
                // Move to a refuge
                List<EntityID> path = search.breadthFirstSearch(location(), getRefuges());
                if (path != null) {
                    logger.info("Moving to refuge");
                    //sendMove(time, path);
					move(path);
                    return;
                }
                // What do I do now? Might as well carry on and see if we can dig someone else out.
                logger.debug("Failed to plan path to refuge");
            }
        }
        //視界に市民がいるときの処理
        List<Civilian> view_civs = getViewCivilians(changed);
        if(view_civs != null && view_civs.size() != 0){
        	logger.trace("Civilians are in my view.");
        	for(Civilian next : view_civs){
		        if (next.getPosition().equals(location().getID())) {
		            // Targets in the same place might need rescueing or loading
		            if (next.getBuriedness() == 0 && !(location() instanceof Refuge)) {
		                // Load
		                logger.info("Loading " + next);
		                sendLoad(time, next.getID());
		                return;
		            }
		            if (next.getBuriedness() > 0) {
		                // Rescue
		                logger.info("Rescueing " + next);
		                sendRescue(time, next.getID());
		                return;
		            }
		        }
		        /*
		        else {
		            // Try to move to the target
		            List<EntityID> path = search.breadthFirstSearch(location(), next.getPosition(model));
		            if (path != null && next.getPosition(model) instanceof Building) { //
		                logger.info("Moving to target. path = " + path);
		                sendMove(time, path);
						//move(path);
		                return;
		            }
		        }*/
        	}
        }
        
        //建物探訪
        logger.debug("建物探訪");
        target_building = getTargetBuilding();
        logger.debug("target_building = " + target_building);
        List<EntityID> target_building_path = search.breadthFirstSearch(getLocation(), target_building);
        if(target_building_path != null){
        	logger.debug("target_building_path = " + target_building_path);
        	try{
        		speak(3, ("VISITED_"+target_building.getID().getValue()).getBytes("UTF-8"));
        	}catch(Exception e){}
        	//sendMove(time, target_building_path);
        	move(target_building_path);
        	return;
        }else{
        	logger.debug("target_building_path is null!");
        }
        
        // Go through targets (sorted by distance) and check for things we can do
        for (Human next : getTargets()) { //getTargetsは大体がnull.もしくはサイズ0.
            if (next.getPosition().equals(location().getID())) {
                // Targets in the same place might need rescueing or loading
                if ((next instanceof Civilian) && next.getBuriedness() == 0 && !(location() instanceof Refuge)) {
                    // Load
                    logger.info("Loading " + next);
                    sendLoad(time, next.getID());
                    return;
                }
                if (next.getBuriedness() > 0) {
                    // Rescue
                    logger.info("Rescueing " + next);
                    sendRescue(time, next.getID());
                    return;
                }
            }
            else {
                // Try to move to the target
                List<EntityID> target_human_path = search.breadthFirstSearch(location(), next.getPosition(model));
                if (target_human_path != null && next.getPosition(model) instanceof Building) {
                    logger.info("Moving to target. path = " + target_human_path);
                    //sendMove(time, path);
					move(target_human_path);
                    return;
                }
            }
        }
        // Nothing to do
        List<EntityID> target__path = search.breadthFirstSearch(location(), unexploredBuildings);
        if (target__path != null) {
            logger.info("Searching buildings");
            //sendMove(time, path);
			move(target__path);
            return;
        }else{
        	logger.info("Cannot Searching Buildings.");
        }
        logger.info("Moving randomly");
        //sendMove(time, randomWalk());
		move(randomWalk());
	}
	public void taskRankUpdate(){
	}
	protected EntityID extractVisitedBuildingID(String str){
		EntityID result;
		if(str.startsWith("VISITED_")){
			int id = Integer.parseInt(str.substring(8));
			result = new EntityID(id);
			if(model.getEntity(result) instanceof Building){
				logger.debug("Visited Building ID is found. id = " + id);
				return result;
			}else{
				logger.debug("Visited Building ID is not found. id = " + id);
				return null;
			}
		}else{
			logger.debug("str_data is not started from 'VISITED_'");
			return null;
		}
	}
	protected StandardEntity getTargetBuilding(){
		logger.debug("getTargetBuilding();");
		StandardEntity result = null;
		if(team == 0){
			int distance = Integer.MAX_VALUE;
			result = null;
			for(StandardEntity target : targetBuildings){
				int dist_temp = model.getDistance(getLocation(), target);
				if(dist_temp < distance){
					distance = dist_temp;
					result = target;
				}
			}
		}else{
			int distance = 0;
			result = null;
			for(StandardEntity target : targetBuildings){
				int dist_temp = model.getDistance(getLocation(), target);
				if(dist_temp > distance){
					distance = dist_temp;
					result = target;
				}
			}
		}
		logger.debug("result = " + result);
		return result;
	}
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
	}
    private boolean someoneOnBoard() {
        for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
            if (((Human)next).getPosition().equals(getID())) {
                Logger.debug(next + " is on board");
                return true;
            }
        }
        return false;
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
//                && (h.getBuriedness() > 0 || h.getDamage() > 0)) {
				&& h.getBuriedness() > 0){
                logger.debug("targets.add(" + h + ")");
                targets.add(h);
            }
        }
        Collections.sort(targets, new DistanceSorter(location(), model));
        return targets;
    }
	private void updateTargetBuildings(){
		logger.debug("updateTargetBuildings();");
		logger.debug("visitedBuildings = " + visitedBuildings);
		for(StandardEntity visited : visitedBuildings){
			targetBuildings.remove(visited);
		}
	}
    private void updateUnexploredBuildings(ChangeSet changed) {
    	logger.trace("called updateUnexploredBuildings();");
        for (EntityID next : changed.getChangedEntities()) {
            StandardEntity e = model.getEntity(next);
            if (e != null) {
                unexploredBuildings.remove(e);
            }
        }
    }
    public List<Civilian> getViewCivilians(ChangeSet changed){
    	List<Civilian> civilians = new ArrayList<Civilian>();
    	StandardEntity entity;
    	for(EntityID next : changed.getChangedEntities()){
    		entity = model.getEntity(next);
    		logger.debug("getViewCivilians() next = " + entity);
    		if(entity instanceof Civilian) civilians.add((Civilian)entity);
    	}
    	return civilians;
    }
}
