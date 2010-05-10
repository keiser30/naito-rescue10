package naito_rescue.message;

import sample.*;
import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;

public class ExtinguishMessage extends naito_rescue.message.Message
{
	private EntityID extinguishTarget;
	private int      targetSize = 0;
	
	public ExtinguishMessage(int id, int addrAgent, int addrType, boolean broadcast, EntityID target, int size){
		super(MessageConstants.TYPE_FIRE, id, addrAgent, addrType, broadcast);
		this.extinguishTarget = target;
		this.targetSize = size;
	}
	
	public void setTarget(EntityID target){ this.extinguishTarget = target; }
	public EntityID getTarget()           { return this.extinguishTarget;  }
	public int getTargetSize(){ return this.targetSize; }
	
	@Override
	public int getSize(){
		return 8; //int2つ分
	}
}
