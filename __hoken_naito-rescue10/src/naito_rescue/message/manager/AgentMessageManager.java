package naito_rescue.message.manager;

import rescuecore2.messages.*;
import rescuecore2.standard.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import naito_rescue.*;
import naito_rescue.agent.*;
import naito_rescue.message.*;

import java.util.*;

public class AgentMessageManager implements MessageConstants
{
	private NAITOHumanoidAgent owner;
	private MessageUtils       utils = new MessageUtils();
	private ArrayList<naito_rescue.message.Message> received = new ArrayList<naito_rescue.message.Message>();
	private ArrayList<naito_rescue.message.Message> sended = new ArrayList<naito_rescue.message.Message>();
	private MyLogger logger;
	private int create_mes_count;
	
	public AgentMessageManager(NAITOHumanoidAgent owner){
		if(owner == null) return;
		this.owner = owner;
		this.logger = owner.getLogger();
		this.mes_count = 0;
	}
	
	public naito_rescue.message.Message receiveMessage(AKSpeak speak){
		byte[] rawdata = speak.getContent();
		naito_rescue.message.Message result = null;
		
		int idx = 0;
		int type          = utils.readInt32(rawdata, idx);       idx += 4;
		int msgId         = utils.readInt32(rawdata, idx);       idx += 4;
		int addrAgent     = utils.readInt32(rawdata, idx);       idx += 4;
		int addrType      = utils.readInt32(rawdata, idx);       idx += 4;
		boolean broadcast = (utils.readInt32(rawdata, idx) > 0); idx += 4;
		int ttl           = utils.readInt32(rawdata, idx);       idx += 4;
		int datasize      = utils.readInt32(rawdata, idx);       idx += 4;
		
		int target_id_num = utils.readInt32(rawdata, idx); idx += 4;//datasizeを鮮やかに無視
		EntityID target = new EntityID(target_id_num);
		switch(type){
			case TYPE_FIRE:
				int size = utils.readInt32(rawdata, idx); idx += 4;
				result = new ExtinguishMessage(msgId, addrAgent, addrType, broadcast, target, size);
				break;
			case TYPE_RESCUE:
				result = new RescueMessage(msgId, addrAgent, addrType, broadcast, target);
				break;
			case TYPE_CLEAR:
				result = new ClearMessage(msgId, addrAgent, addrType, broadcast, target);
				break;
			default:
				logger.info("Unknown type: " + type);
		}
		if(result != null){
			received.add(result);
		}else{
			return null;
		}
		return result;
	/*	
		logger.println("receiveMessage();");
		logger.println("type = " + type);
		logger.println("msgId = " + msgId);
		logger.println("addrAgent = " + addrAgent);
		logger.println("addrType = " + addrType);
		logger.println("broadcast = " + broadcast);
		logger.println("ttl = " + ttl);
		logger.println("datasize = " + datasize);
	*/
	}
	
	public void sendMessage(naito_rescue.message.Message mes){
		byte[] rawdata new byte[HEADER_SIZE + mes.getSize()];
		
		int idx = 0;
		//ヘッダの書き込み
		utils.writeInt32(mes.getType(), rawdata,      idx); idx += 4;
		utils.writeInt32(mes.getID(), rawdata,        idx); idx += 4;
		utils.writeInt32(mes.getAddrAgent(), rawdata, idx); idx += 4;
		utils.writeInt32(mes.getAddrType(), rawdata,  idx); idx += 4;
		if(mes.isBroadcast()) utils.writeInt32( 1, rawdata, idx); idx += 4;
		else                  utisl.writeInt32(-1, rawdata, idx); idx += 4;
		utils.writeInt32(0xDEAD, rawdata,             idx); idx += 4;//ttlにはダミーの値を書き込んでおく
		utils.writeInt32(mes.getSize(), rawdata,      idx); idx += 4;

		//本体の書き込み
		switch(mes.getType()){
			case TYPE_FIRE:
				ExtinguishMessage ems = (ExtinguishMessage)mes;
				utils.writeInt32(ems.getTarget().getValue(), rawdata, idx); idx += 4;
				utils.writeInt32(ems.getTargetSize(), rawdata, idx);
				break;
			case TYPE_RESCUE:
				RescueMessage rm = (RescueMessage)mes;
				utils.writeInt32(rm.getTarget().getValue(), rawdata, idx);
				break;
			case TYPE_CLEAR:
				ClearMessage cm = (ClearMessage)mes;
				utils.writeInt32(cm.getTarget().getValue(), rawdata, idx);
				break;
			default:
				logger.info("Writing Unknown message type: " + mes.getType());
				return;
		}
		mes.setSendTime(owner.getTime());
		sended.add(mes);
		owner.say(rawdata);
	}
	private int getDigit(int n){
		int digit = 1;
		while(n / 10 > 0){
			digit++;
			n /= 10;
		}
		return digit;
	}
	private int createMsgID(){
		return owner.getID().getValue() * (int)(Math.pow(10, getDigit(send_mes_count))) + create_mes_count;
	}
	public RescueMessage createRescueMessage(int addrAgent, int addrType, boolean broadcast, EntityID target){
		RescueMessage result = new RescueMessage(createMsgID(), addrAgent, addrType, broadcast, target);
		create_mes_count++;
		return result;
	}
	public ExtinguishMessage createExtinguishMessage(int addrAgent, int addrType, boolean broadcast, EntityID target, int size){
		ExtinguishMessage result = new ExtinguishMessage(createMsgID(), addrAgent, addrType, broadcast, target, size);
		create_mes_count++;
		return result;
	}
	public ClearMessage createClearMessage(int addrAgent, int addrType, boolean broadcast, EntityID target){
		ClearMessgae result = new ClearMessage(createMsgID(), addrAgent, addrType, broadcast, target);
		create_mes_count++;
		return result;
	}
	
}
