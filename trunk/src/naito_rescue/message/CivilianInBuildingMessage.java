package naito_rescue.message;

import java.io.*;
import java.util.*;

import static naito_rescue.debug.DebugUtil.*;
import naito_rescue.message.*;
import naito_rescue.manager.*;
import naito_rescue.stream.*;
import naito_rescue.agent.*;

public class CivilianInBuildingMessage extends NAITOBaseMessage
{
	private EntityID[] ids = null;
	
	public CivilianInBuildingMessage(){
		// NAITOMessageに属するメッセージは
		// コンストラクタの中で必ずメッセージタイプを
		// 設定する必要がある
		this.type = MessageType.CIVILIAN;
	}
	public CivilianInBuildingMessage(int[] ids){
		this();
		setIDs(ids);
	}
	public CivilianInBuildingMessage(List<EntityID> list){
		this();
		setIDs(list);
	}
	public CivilianInBuildingMessage(EntityID id){
		this();
		setIDs(Arrays.asList(id));
	}
	public CivilianInBuildingMessage(int buildingID){
		this();
		setIDs(new int[]{buildingID});
	}
	@Override
	protected void decodeMessage(RawDataInputStream in){
		try{
			int size = in.readByte();
			this.ids = new EntityID[size];

			for(int i = 0;i < size;i++){
				int id = in.readInt();
				ids[i] = new EntityID(id);
				p((i+1) + "個目のID = " + ids[i]);
			}
		}catch(Exception e){e.printStackTrace();}
	}
	@Override
	protected RawDataOutputStream encodeMessage(){
		RawDataOutputStream output = new RawDataOutputStream();

		if(ids == null || ids.length == 0){
			return null;
		}
		
		try{
			output.writeByte((byte)ids.length);
			
			for(int i = 0;i < ids.length;i++){
				p("書き込むID = " + ids[i].getValue());
				output.writeInt(ids[i].getValue());
			}
			return output;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}	
	
	public void setIDs(List<EntityID> list){
		this.ids = new EntityID[list.size()];
		for(int i = 0;i < ids.length;i++){
			ids[i] = list.get(i);
		}
	}
	public void setIDs(int[] _ids){
		this.ids = new EntityID[_ids.length];
		for(int i = 0;i < ids.length;i++){
			EntityID id = new EntityID(_ids[i]);
			ids[i] = id;
		}
	}
	public List<EntityID> getIDs(){
		return Arrays.asList(ids);
	}
	
	@Override
	public String toString(){
		StringBuffer str = new StringBuffer();
		str.append("CivilianInBuildingMessage(");
		if(ids != null){
			for(int i = 0;i < ids.length-1;i++){
				str.append(ids[i] + ",");
			}
			str.append(ids[ids.length-1] + "");
		}
		str.append(")");
		return str.toString();
	}

}
