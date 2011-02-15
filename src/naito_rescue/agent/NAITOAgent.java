package naito_rescue.agent;

import rescuecore2.*;
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

/**
*	NAITOAgent
*		主に情報収集を担当
*/
public abstract class NAITOAgent<E extends StandardEntity> extends StandardAgent<E>
{
	protected int                 time;
	protected ChangeSet           changed;
	//protected MySearch            search;
	protected NAITORouter         search;
	protected E                   me;
	
	//Task-Job関連
	protected ArrayList<Task>            currentTaskList;
	protected Task                       currentTask;
	protected Job                        currentJob;
	
	//建物情報およびエージェント情報
	protected Collection<StandardEntity> allBuildings;
	protected Collection<StandardEntity> allRoads;
	protected Collection<StandardEntity> firebrigades;
	protected Collection<StandardEntity> policeforces;
	protected Collection<StandardEntity> ambulanceteams;
	protected Collection<StandardEntity> civilians;
	
	//コンフィグからとってくる情報のキー(URN)
	private static final String          SAY_COMMUNICATION_MODEL = "kernel.standard.StandardCommunicationModel";
	private static final String          SPEAK_COMMUNICATION_MODEL = "kernel.standard.ChannelCommunicationModel";
	private static final String          VIEW_DISTANCE_KEY = "perception.los.max-distance";
	private static final String          REPAIR_DISTANCE_KEY = "clear.repair.distance";
	private static final String          MAX_WATER_KEY = "fire.tank.maximum";
	private static final String          MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
	private static final String          MAX_POWER_KEY = "fire.extinguish.max-sum";
	
	//コンフィグからとってくる情報
	protected int                       maxRepairDistance;     //閉塞解除可能な距離
	protected int                       viewDistance;          //視界範囲
	protected int                       startActionTime;       //行動を開始できる時刻
	protected int                       maxWater;              //最大給水量(FB)
	protected int                       maxExtinguishDistance; //消火可能範囲
	protected int                       maxExtinguishPower;    //消火水量
	protected boolean                   useSpeak;              //AKSpeakを用いるかどうか
	
	//FB, PF, ATのリスト
	//IDの昇順に並べる
	protected List<FireBrigade>      fbList;
	protected List<PoliceForce>      pfList;
	protected List<AmbulanceTeam>    atList;
	protected List<Human>            allAgentsList; //ただしCivilianは除く
	protected int                    fbSize, pfSize, atSize, allAgentsSize;
	
	//センター群
	protected Collection<StandardEntity> allRefuges;
	protected Collection<StandardEntity> firestation;
	protected Collection<StandardEntity> policeoffice;
	protected Collection<StandardEntity> ambulancecenter;
	
	//センターレスに関する設定
	protected boolean                   centerLess;
	protected boolean                   fireStationLess;
	protected boolean                   ambulanceCenterLess;
	protected boolean                   policeOfficeLess;
	
	//マップの境界情報(原点の(x, y), 及びマップの横幅と縦幅 など)
	protected java.awt.geom.Rectangle2D world_rect;
	protected double                    w_minX;
	protected double                    w_minY;
	protected double                    w_maxX;
	protected double                    w_maxY;
	protected double                    w_width;
	protected double                    w_height;
	
	//レポート関連
	protected Map<Area, Integer>        reportedBlockedRoad;
	protected List<Building>            reportedBurningBuilding;
	protected List<Building>            reportedVictimInBuilding;
	
	//視界情報管理
	ViewInformationManager              viewInfoManager;
	
	//ロガー，メッセージマネージャ
	protected MyLogger             logger;
	protected NAITOMessageManager  msgManager;
	
	
	protected static final EntityTools.IDComparator ID_COMP = new EntityTools.IDComparator();
	
	
	@Override
    protected void postConnect() {
    	logger = new MyLogger(this, false);
    	msgManager = new NAITOMessageManager(this);
    	//search = new MySearch(model, this);
    	search = new NAITORouter(this);
    	
		 /**
		  * 各種建物, エージェントに関する情報を収集する
		  */
		 allBuildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
		 allRoads = model.getEntitiesOfType(StandardEntityURN.ROAD);
		 firebrigades = model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE);
		 policeforces = model.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);
		 ambulanceteams = model.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM);
		 civilians = model.getEntitiesOfType(StandardEntityURN.CIVILIAN);
		 
		 currentTaskList = new ArrayList<Task>();
		 
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
		 
		 //(IDの昇順に並べる)
		 Collections.sort(fbList,        ID_COMP);
		 Collections.sort(pfList,        ID_COMP);
		 Collections.sort(atList,        ID_COMP);
		 Collections.sort(allAgentsList, ID_COMP);
		 
		 //センターの情報を収集
		 allRefuges      = model.getEntitiesOfType(StandardEntityURN.REFUGE);
		 firestation     = model.getEntitiesOfType(StandardEntityURN.FIRE_STATION);
		 policeoffice    = model.getEntitiesOfType(StandardEntityURN.POLICE_OFFICE);
		 ambulancecenter = model.getEntitiesOfType(StandardEntityURN.AMBULANCE_CENTRE);
		 
		 //センターレスに関する設定
		 fireStationLess     = firestation.isEmpty();
		 policeOfficeLess    = policeoffice.isEmpty();
		 ambulanceCenterLess = ambulancecenter.isEmpty();
		 centerLess          = fireStationLess && policeOfficeLess && ambulanceCenterLess;
		 
		 //レポート関連の初期化
		 reportedBlockedRoad      = new HashMap<Area, Integer>();
		 reportedBurningBuilding  = new ArrayList<Building>();
		 reportedVictimInBuilding = new ArrayList<Building>();
		 
		 //コンフィグからのプロパティ設定
		useSpeak              = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
		maxRepairDistance     = config.getIntValue(REPAIR_DISTANCE_KEY);
		viewDistance          = config.getIntValue(VIEW_DISTANCE_KEY, 30000);
		startActionTime       = config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY);
        maxWater              = config.getIntValue(MAX_WATER_KEY);
        maxExtinguishDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxExtinguishPower    = config.getIntValue(MAX_POWER_KEY);
		 
		 //マップの境界情報の取得 ... 後々のdecideCrowlingBuildingで使う
		 world_rect = model.getBounds();
		 w_minX = world_rect.getX();
		 w_minY = world_rect.getY();
		 w_maxX = w_minX + world_rect.getWidth();
		 w_maxY = w_minY + world_rect.getHeight();
		 w_width = world_rect.getWidth();
		 w_height = world_rect.getHeight();
		 
		 //視界情報関連の初期化
		 viewInfoManager = new ViewInformationManager();
    }
    
    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard){
    	this.time = time;
		this.changed = changed;
		this.me = me();
		
		for(Command hear : heard){
			if(hear instanceof AKSpeak){
				logger.info("1 AKSpeak has received.");
				msgManager.receiveMessages((AKSpeak)hear);
			}
		}

		viewInfoManager.update(changed);
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
	/*
	public MySearch getSearch(){
		return search;
	}
	*/
	public NAITORouter getSearch(){
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
		/*
		List<EntityID> path = search.breadthFirstSearch(getLocation(), target);
		if(path != null){
			logger.info("path = " + path);
			move(path);
		}else{
		}
		*/
		//List<EntityID> path = search.AStar((Area)getLocation(), (Area)target);
		List<EntityID> path = search.breadthFirstSearch(getLocation(), target);
		if(path != null){
			move(path);
		}else{
			System.out.println("Path is NULL.");
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
	//全部これに統一した方が楽
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
	
	//視界情報を管理するクラス
	private class ViewInformationManager
	{
		Set<Building>             buildingsInView;
		Set<Road>                 roadsInView;
		Set<Human>                humansInView;
		Set<Blockade>             blockadesInView;
		
		private ViewInformationManager(){
			buildingsInView = new HashSet<Building>();
			roadsInView     = new HashSet<Road>();
			humansInView    = new HashSet<Human>();
			blockadesInView = new HashSet<Blockade>();
		}
		
		public void update(ChangeSet changed){
			//各セットを初期化
			buildingsInView = new HashSet<Building>();
			roadsInView = new HashSet<Road>();
			humansInView = new HashSet<Human>();
			blockadesInView = new HashSet<Blockade>();
			
			for(EntityID change : changed.getChangedEntities()){
				StandardEntity entity = model.getEntity(change);
				
				if     (entity instanceof Building) buildingsInView.add((Building)entity);
				else if(entity instanceof Road)     roadsInView.add((Road)entity);
				else if(entity instanceof Human)    humansInView.add((Human)entity);
				else if(entity instanceof Blockade) blockadesInView.add((Blockade)entity); 
			}
		}
		
		public Set<Building> getBuildingsInView(){ return buildingsInView; }
		public Set<Human>    getHumansInView()   { return humansInView;    }
		public Set<Blockade> getBlockadesInView(){ return blockadesInView; }
		
		public String toString(){
			StringBuffer ret = new StringBuffer();
			ret.append("naito_rescue.agent.NAITOAgent.ViewInformationManager;");
			ret.append("******   View Information ******\n");
			ret.append("    Buildings = [" + buildingsInView + "]\n");
			ret.append("    Humans    = [" + humansInView + "]\n");
			ret.append("    Blockades = [" + blockadesInView + "]\n");
			ret.append("****** //View Information ******\n");
			return ret.toString();
		}
	}
	
}
