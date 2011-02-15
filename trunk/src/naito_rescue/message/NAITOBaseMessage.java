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

public abstract class NAITOBaseMessage extends NAITOMessage
{
	protected int id = -1;
	protected short sendTime = -1;
	
	public RawDataOutputStream encode(){
		RawDataOutputStream stream = new RawDataOutputStream();
		try{
			stream.writeInt(this.id);
			stream.writeShort(this.sendTime);
			
			RawDataOutputStream raw = encodeMessage();
			stream.append(raw);
			return stream;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	public void decode(RawDataInputStream in){
		try{
			this.id = in.readInt();
			this.sendTime = in.readShort();
			decodeMessage(in);
		}catch(Exception e){e.printStackTrace();}
	}
	
	protected abstract RawDataOutputStream encodeMessage();
	protected abstract void decodeMessage(RawDataInputStream in);
	 
	// ゲッタとかセッタとか	
	public void setID(int id){ this.id = id; }
	public void setTime(int time){ this.sendTime = (short)time; }
	public int getID(){ return id; }
	public int getTime(){ return sendTime; }
}
