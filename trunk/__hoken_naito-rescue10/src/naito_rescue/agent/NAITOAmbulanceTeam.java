package naito_rescue.agent;

import java.util.*;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.*;

public class NAITOAmbulanceTeam extends NAITOHumanoidAgent<AmbulanceTeam>
{
	private Collection<StandardEntity> unexploredBuildings;

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
           		 // Subscribe to channel 1
				logger.info("sendSubscribe(" + time + ", 1");
           	 	sendSubscribe(time, 1);
			}else{
				return;
			}
        }
        for (Command next : heard) {
//            Logger.debug("Heard " + next);
        }
        

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
                    Logger.info("Moving to refuge");
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
		        else {
		            // Try to move to the target
		            List<EntityID> path = search.breadthFirstSearch(location(), next.getPosition(model));
		            if (path != null) {
		                logger.info("Moving to target. path = " + path);
		                //sendMove(time, path);
						move(path);
		                return;
		            }
		        }
        	}
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
                List<EntityID> path = search.breadthFirstSearch(location(), next.getPosition(model));
                if (path != null) {
                    logger.info("Moving to target. path = " + path);
                    //sendMove(time, path);
					move(path);
                    return;
                }
            }
        }
        // Nothing to do
        List<EntityID> path = search.breadthFirstSearch(location(), unexploredBuildings);
        if (path != null) {
            logger.info("Searching buildings");
            //sendMove(time, path);
			move(path);
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