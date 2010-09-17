package naito_rescue.message;

import naito_rescue.agent.*;

public abstract class Message
{
	protected int msgID; //メッセージID
	protected int msgType; //メッセージタイプ
	protected int addressAgent; //エージェントIDを指定した宛先
	protected int addressType; //エージェントタイプを指定した宛先
	protected boolean broadcast; //このメッセージをブロードキャストするか否か(未使用)
	protected int sendTime;
	protected int from; //送信元のエージェントID
	
	public Message(int type, int id, int addrAgent, int addrType, boolean broadcasts){
		this.msgType = type;
		this.msgID = id;
		this.addressAgent = addrAgent;
		this.addressType = addrType;
		this.broadcast = broadcast;
	}
	
	public void setBroadcast(boolean b){ this.broadcast = b;    }
	public void setAddrAgent(int a)    { this.addressAgent = a; }
	public void setAddrType(int a)     { this.addressType = a;  }
	public void setSendTime(int t)     { this.sendTime = t; }
	
	public int getType()        { return msgType;      }
	public int getID()          { return msgID;        }
	public int getAddrAgent()   { return addressAgent; }
	public int getAddrType()    { return addressType;  }
	public int getSendTime()    { return sendTime;     }
	public boolean isBroadcast(){ return broadcast;    }
	
	public abstract int getSize();
}
