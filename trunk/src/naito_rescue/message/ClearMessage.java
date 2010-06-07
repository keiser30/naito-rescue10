package naito_rescue.message;

import sample.*;
import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;

import java.io.*;
import java.util.*;

public class ClearMessage extends naito_rescue.message.Message
{
	private EntityID clearTarget;
	
	public ClearMessage(int id, int addrAgent, int addrType, boolean broadcast, EntityID target){
		super(MessageConstants.TYPE_CLEAR, id, addrAgent, addrType, broadcast);
		this.clearTarget = target;
	}
	
	public void setTarget(EntityID target){ this.clearTarget = target; }
	public EntityID getTarget()           { return this.clearTarget;  }
	
	@Override
	public int getSize(){
		return 4; //int1つ分
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("ClearMessage: \n");
		sb.append("    |____ addrAgent = " + addressAgent + "\n");
		sb.append("    |____ addrType  = " + addressType + "\n");
		sb.append("    |____ broadcast = " + broadcast + "\n");
		sb.append("    |____ target    = " + clearTarget + "\n");

		return sb.toString();

	}
}
