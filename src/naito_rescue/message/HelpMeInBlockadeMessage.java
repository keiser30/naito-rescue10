package naito_rescue.message;

import java.io.*;
import java.util.*;

import static naito_rescue.debug.DebugUtil.*;
import naito_rescue.message.*;
import naito_rescue.message.manager.*;
import naito_rescue.message.stream.*;
import naito_rescue.agent.*;

import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import rescuecore2.misc.geometry.*;

public class HelpMeInBlockadeMessage extends NAITOBaseMessage
{
	// 閉じ込められてる閉塞がある道路のID
	private EntityID road_id = null;
	
	public HelpMeInBlockadeMessage(){
		this.type = MessageType.HELP_BLOCK;
	}
	public HelpMeInBlockadeMessage(EntityID id){
		this();
		setRoadID(id);
	}
	public HelpMeInBlockadeMessage(int roadID){
		this();
		setRoadID(new EntityID(roadID));
	}
	
	@Override
	protected void decodeMessage(RawDataInputStream in){
		try{
			int id = in.readInt();
			road_id = new EntityID(id);
		}catch(Exception e){e.printStackTrace();}
	}
	@Override
	protected RawDataOutputStream encodeMessage(){
		RawDataOutputStream output = new RawDataOutputStream();
		try{
			////p("書き込むID = " + road_id.getValue());
			output.writeInt(road_id.getValue());
			return output;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	
	public void setRoadID(EntityID id){
		this.road_id = id;
	}
	public EntityID getRoadID(){
		return this.road_id;
	}
	
	@Override
	public String toString(){
		return "HelpMeInBlockadeMessage(" + road_id + "):" + this.id + "";
	}
}
