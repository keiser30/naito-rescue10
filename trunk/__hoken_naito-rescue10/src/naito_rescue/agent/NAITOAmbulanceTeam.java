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
    
	volatile boolean once = true; //for DEBUG;
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
		// DEBUG: シミュレーション初期に市民情報がとれるかどうかテストする
		if(once){
			logger.debug("DEBUG: シミュレーション初期における市民情報の取得");
			Collection<StandardEntity> civilians = model.getEntitiesOfType(StandardEntityURN.CIVILIAN);
			if(civilians != null && civilians.size() != 0){
				logger.debug("情報を取得できました!");
				logger.debug("情報の総数 = " + civilians.size());
				String prefix = "";
				int i = 0;
				for(StandardEntity entity : civilians){
					Civilian civilian = (Civilian)entity;
					prefix ="(" + (i++) + ")";
					logger.debug(prefix + "civilian: " + civilian);
					logger.trace(prefix + "civilian's location:   " + model.getEntity(civilian.getPosition()));
					logger.trace(prefix + "civilian's HP:         " + civilian.getHP());
					logger.trace(prefix + "civilian's damage:     " + civilian.getDamage());
					logger.trace(prefix + "civilian's buriedness: " + civilian.getBuriedness());
				}
			}else{		
				logger.debug("情報を取得できません");
				if(civilians == null)
					logger.debug("Because of: List<StandardEntity> civilians is null.");
				else if(civilians.size() == 0)
					logger.debug("Becuase of: List<StandardEntity> civilians size() == 0");
				else
					logger.debug("Becuase of: UNKNOWN");
			}

			String prefix = null;
			int i;
			Collection<StandardEntity> firebrigades = model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE);
			Collection<StandardEntity> policeforces = model.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);
			Collection<StandardEntity> ambulanceteams = model.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM);
			
			logger.debug("DEBUG: FireBrigadeの情報を収集...");
			if(firebrigades != null && firebrigades.size() != 0){
				i = 0;
				for(StandardEntity entity : firebrigades){
					FireBrigade fb = (FireBrigade)entity;
					prefix = "("+(i++)+")";
					logger.debug(prefix + "FB: " + fb);
					logger.debug(prefix + "FB's location: " + model.getEntity(fb.getPosition()));
				}
			}else{
				logger.debug("情報を収集できません");
				if(firebrigades == null)
					logger.debug("Because of: Collection(FB) == null");
				else if(firebrigades.size() == 0)
					logger.debug("Because of: Collection(FB).size() == 0");
				else
					logger.debug("Because of: UNKNOWN");
			}
			logger.debug("DEBUG: PoliceForceの情報を収集...");
			if(policeforces != null && policeforces.size() != 0){
				i = 0;
				for(StandardEntity entity : policeforces){
					PoliceForce pf = (PoliceForce)entity;
					prefix = "("+(i++)+")";
					logger.debug(prefix+"PF: " + pf);
					logger.debug(prefix+"PF's location: " + model.getEntity(pf.getPosition()));
				}
			}else{
				logger.debug("情報を収集できません");
				if(policeforces == null)
					logger.debug("Because of: Collection(PF) == null");
				else if(policeforces.size() == 0)
					logger.debug("Because of: Collection(PF).size() == 0");
				else
					logger.debug("Because of: UNKNOWN");
			}
			if(allRefuges != null && allRefuges.size() != 0){
			}else{
				if(allRefuges.size() == 0) logger.debug("避難所がありません");
			}
			once = false;
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
