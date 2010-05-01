package naito_rescue.agent;

import java.util.List;
import java.util.Collection;
import java.util.EnumSet;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;

import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Area;

import rescuecore2.log.Logger;

/**
   A sample civilian agent.
 */
public class NAITOCivilian extends NAITOHumanoidAgent<Civilian> {
    private static final double DEFAULT_HELP_PROBABILITY = 0.1;
    private static final double DEFAULT_OUCH_PROBABILITY = 0.1;
    private static final int DEFAULT_CONSCIOUS_THRESHOLD = 2500;

    private static final String HELP_PROBABILITY_KEY = "civilian.help.probability";
    private static final String OUCH_PROBABILITY_KEY = "civilian.ouch.probability";
    private static final String CONSCIOUS_THRESHOLD_KEY = "civilian.conscious.threshold";

    private static final String OUCH = "Ouch";
    private static final String HELP = "Help";

    private double helpProbability;
    private double ouchProbability;
    private int consciousThreshold;

    @Override
    public String toString() {
        return "Sample civilian";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.REFUGE);
        helpProbability = config.getFloatValue(HELP_PROBABILITY_KEY, DEFAULT_HELP_PROBABILITY);
        ouchProbability = config.getFloatValue(OUCH_PROBABILITY_KEY, DEFAULT_OUCH_PROBABILITY);
        consciousThreshold = config.getIntValue(CONSCIOUS_THRESHOLD_KEY, DEFAULT_CONSCIOUS_THRESHOLD);
        Logger.info("Civilian " + getID() + " connected");
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        // If we're not hurt or buried run for a refuge!
        Civilian me = me();
        int damage = me.isDamageDefined() ? me.getDamage() : 0;
        int hp = me.isHPDefined() ? me.getHP() : 0;
        int buriedness = me.isBuriednessDefined() ? me.getBuriedness() : 0;
        if (hp <= 0 || hp < consciousThreshold) {
            // Unconscious (or dead): do nothing
            Logger.info("Unconcious or dead");
            sendRest(time);
            return;
        }
        if (damage > 0 && random.nextDouble() < ouchProbability) {
            Logger.info("Shouting in pain");
            say(OUCH, time);
        }
        if (buriedness > 0 && random.nextDouble() < helpProbability) {
            Logger.info("Calling for help");
            say(HELP, time);
        }
        if (damage == 0 && buriedness == 0 && (location() instanceof Area)) {
            // Run for the refuge
            List<EntityID> path = search.breadthFirstSearch(location(), getRefuges());
            if (path != null) {
                Logger.info("Heading for a refuge");
                sendMove(time, path);
                return;
            }
            else {
                Logger.info("Moving randomly");
                sendMove(time, randomWalk());
                return;
            }
        }
        Logger.info("Not moving");
        sendRest(time);
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.CIVILIAN);
    }

    private void say(String message, int time) {
        try {
            if (useSpeak) {
                sendSpeak(time, 0, message.getBytes("UTF-8"));
            }
            else {
                sendSay(time, message.getBytes("UTF-8"));
            }
        }
        catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("This should not have happened!", e);
        }
    }
	public void taskRankUpdate(){}
}
