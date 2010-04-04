package naito_rescue.agent;

//from StandardAgent.java
import rescuecore2.components.AbstractAgent;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.messages.AKRest;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKExtinguish;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKUnload;
import rescuecore2.standard.messages.AKSubscribe;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.standard.messages.AKSay;
import rescuecore2.standard.messages.AKTell;
import java.util.List;
import java.util.ArrayList;
import java.util.EnumSet;

import rescuecore2.standard.components.StandardAgent;
import sample.AbstractSampleAgent;

//from SampleFireBrigade.java
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


// 今の段階ではまだ空クラスだけど気にしない
//public class NAITOAgent<E extends StandardEntity> extends AbstractSampleAgent<E>
public abstract class NAITOAgent<E extends StandardEntity> extends StandardAgent<E>
{
}
