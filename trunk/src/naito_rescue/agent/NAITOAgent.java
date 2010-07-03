package naito_rescue.agent;

import rescuecore2.misc.*;
import rescuecore2.components.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import java.util.*;

import rescuecore2.standard.components.StandardAgent;
import sample.AbstractSampleAgent;

import rescuecore2.messages.*;

import naito_rescue.*;
import naito_rescue.agent.*;
import naito_rescue.task.*;
import naito_rescue.task.job.*;
import naito_rescue.message.*;
import naito_rescue.message.manager.*;

public abstract class NAITOAgent<E extends StandardEntity> extends StandardAgent<E>
{
	protected int                 time;
	protected ChangeSet           changed;
	protected MySearch            search;
	protected Collection<StandardEntity> allBuildings;
	protected Collection<StandardEntity> allRoads;
	protected Collection<StandardEntity> firebrigades;
	protected Collection<StandardEntity> policeforces;
	protected Collection<StandardEntity> ambulanceteams;
	
	//FB, PF, ATのリスト
	//IDの昇順に並べる
	protected ArrayList<FireBrigade>      fbList;
	protected ArrayList<PoliceForce>      pfList;
	protected ArrayList<AmbulanceTeam>    atList;
	protected ArrayList<Human>            allAgentsList; //ただしCivilianは除く
	protected int                         fbSize, pfSize, atSize, allAgentsSize;
	
	protected Collection<StandardEntity> allRefuges;
	protected Collection<StandardEntity> firestation;
	protected Collection<StandardEntity> policeoffice;
	protected Collection<StandardEntity> ambulancecenter;
	protected Collection<StandardEntity> civilians;
	
	protected MyLogger            logger;
	protected AgentMessageManager msgManager;
	
	protected static final EntityTools.IDComparator ID_COMP = new EntityTools.IDComparator();
	
	@Override
    protected void postConnect() {
    	logger = new MyLogger(this, false);
    	msgManager = new AgentMessageManager(this);
    	search = new MySearch(model, this);
		 /**
		  * 各種建物, エージェントに関する情報を収集する
		  */
		 allBuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		 allRoads = model.getEntitiesOfType(StandardEntityURN.ROAD);
		 firebrigades = model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE);
		 policeforces = model.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);
		 ambulanceteams = model.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM);
		 civilians = model.getEntitiesOfType(StandardEntityURN.CIVILIAN);
		 
		 fbSize = pfSize = atSize = allAgentsSize = 0;
		 fbList = new ArrayList<FireBrigade>();
		 pfList = new ArrayList<PoliceForce>();
		 atList = new ArrayList<AmbulanceTeam>();
		 allAgentsList = new ArrayList<Human>();
		
		 for(StandardEntity fb : firebrigades){
		 	fbList.add((FireBrigade)fb);
		 	allAgentsList.add((Human)fb);
		 }
		 for(StandardEntity pf : policeforces){
		 	pfList.add((PoliceForce)pf);
		 	allAgentsList.add((Human)pf);
		 }
		 for(StandardEntity at : ambulanceteams){
		 	atList.add((AmbulanceTeam)at);
		 	allAgentsList.add((Human)at);
		 }
		 fbSize = fbList.size();
		 pfSize = pfList.size();
		 atSize = atList.size();
		 allAgentsSize = allAgentsList.size();
		 
		 //IDの昇順に並べる
		 Collections.sort(fbList,        ID_COMP);
		 Collections.sort(pfList,        ID_COMP);
		 Collections.sort(atList,        ID_COMP);
		 Collections.sort(allAgentsList, ID_COMP);
		 
		 //センターの情報を収集
		 allRefuges = model.getEntitiesOfType(StandardEntityURN.REFUGE);
		 firestation = model.getEntitiesOfType(StandardEntityURN.FIRE_STATION);
		 policeoffice = model.getEntitiesOfType(StandardEntityURN.POLICE_OFFICE);
		 ambulancecenter = model.getEntitiesOfType(StandardEntityURN.AMBULANCE_CENTRE);
    }
    
    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard){
    	this.time = time;
		this.changed = changed;
    }
    
//***** ゲッタとセッタ *****
	public StandardEntity getLocation(){
		return location();
	}

	public MyLogger getLogger(){
		return logger;
	}
	public StandardWorldModel getWorldModel(){
		return model;
	}
	public MySearch getSearch(){
		return search;
	}
	public int getTime(){
		return time;
	}
	public E getMe(){
		return me();
	}
	
//***** メソッドのラッパー群 *****//
	public void move(StandardEntity target){
		//ダイクストラ法の経路探索が実装できるまで，breadthFirstSearchを使う
		List<EntityID> path = search.breadthFirstSearch(getLocation(), target);
		if(path != null){
			logger.info("path = " + path);
			move(path);
		}else{
		}
	}
	public void move(StandardEntity target, int x, int y){
		List<EntityID> path = search.breadthFirstSearch(getLocation(), target);
		if(path != null){
			logger.info("path = " + path + ", (x, y) = (" + x + ", " + y + ")");
			move(path, x, y);
		}else{
			
			
			
		}
	}
	public void move(List<EntityID> path){
		logger.info("path = " + path);
		sendMove(time, path);
	}

	public void move(List<EntityID> path, int x, int y){
		logger.info("path = " + path + ", (x, y) = (" + x + ", " + y + ")");
		sendMove(time, path, x, y);
	}
	public void extinguish(EntityID target, int water){
		logger.info("extinguish(" + target + ", " + water + ");");
		sendExtinguish(time, target, water);
	}
	public void clear(EntityID target){
		logger.info("clear(" + target + ");");
		sendClear(time, target);
	}
	public void load(EntityID target){
		logger.info("load(" + target + ");");
		sendLoad(time, target);
	}
	public void unload(){
		logger.info("unload();");
		sendUnload(time);
	}
	public void rescue(EntityID target){
		logger.info("rescue(" + target + ");");
		sendRescue(time, target);
	}
	public void rest(){
		logger.info("rest();");
		sendRest(time);
	}
	public void speak(int channel, byte[] data){
		logger.info("speak(" + channel + ", data);");
		sendSpeak(time, channel, data);
	}
	public void subscribe(int... channels){
		logger.info("subscribe(" + channels + ");");
		sendSubscribe(time, channels);
	}
	public void say(byte[] data){
		logger.info("say(data);");
		
//		if(useSpeak){
//			
//			sendSpeak(time, 0, data);
//		}else{
//			
//			sendSay(time, data);
//		}
//		
		sendSpeak(time, 0, data);
	}
	public void tell(byte[] data){
		logger.info("tell(data);");
		sendTell(time, data);
	}
	
	public Collection<StandardEntity> getRefugesAsCollection(){
		return allRefuges;
	}
	public List<Refuge> getRefugesAsList(){
		return getRefuges();
	}
}
