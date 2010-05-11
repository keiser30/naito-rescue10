package naito_rescue.message;

import sample.*;
import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;

public class RescueMessage extends naito_rescue.message.Message
{
	private EntityID rescueTarget;

	public RescueMessage(int id, int addrAgent, int addrType, boolean broadcast, EntityID target){
		super(MessageConstants.TYPE_RESCUE, id, addrAgent, addrType, broadcast);
		this.rescueTarget = target;
	}
	
	public void setTarget(EntityID target){ this.rescueTarget = target; }
	public EntityID getTarget()           { return this.rescueTarget;  }
	
	@Override
	public int getSize(){
		return 4; //int1つ分
	}
}
