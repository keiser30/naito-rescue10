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
	private NAITOAgent owner;
	private MessageUtils       utils = new MessageUtils();
	private ArrayList<naito_rescue.message.Message> received = new ArrayList<naito_rescue.message.Message>();
	private ArrayList<naito_rescue.message.Message> sended = new ArrayList<naito_rescue.message.Message>();
	private LinkedList<naito_rescue.message.Message> sendMessageList = new LinkedList<naito_rescue.message.Message>();
	private MyLogger logger;
	private int create_mes_count;
	private int channel = 1;

	public AgentMessageManager(NAITOAgent owner){
		if(owner == null) return;
		this.owner = owner;
		this.logger = owner.getLogger();
		this.create_mes_count = 0;
	}
	
	public List<naito_rescue.message.Message> receiveMessage(AKSpeak speak){
		byte[] rawdata = speak.getContent();
		List<naito_rescue.message.Message> result = new ArrayList<naito_rescue.message.Message>();

		//ノイズ対策
		if(rawdata == null || rawdata.length < HEADER_SIZE){
			
			return null;
		}
		int hoge = 0;
		int idx = 0;
		
		while(true){
			try{
				naito_rescue.message.Message mes = null;
				
				//readInt32するときは，「4バイト以上ストリームから読み込み可能」
				//であることを保証してから読み込んだ方が良い by Ogawa
				
				//ヘッダの抜き出し
				int msg_type        = utils.readInt8(rawdata, idx);        idx += 1;

				if(msg_type == TYPE_NULL){
					return result;
				}

				//ヘッダの抜き出し(続き)
				int msgId          = utils.readInt32(rawdata, idx);        idx += 4;
				int addrAgent      = utils.readInt16(rawdata, idx);        idx += 2;
				int addrType       = utils.readInt8(rawdata, idx);         idx += 1;
				boolean broadcast  = (utils.readInt8(rawdata, idx) > 0);   idx += 1;
				int ttl            = utils.readInt8(rawdata, idx);         idx += 1;
				int datasize       = utils.readInt8(rawdata, idx);         idx += 1;

				int target_id_num = utils.readInt32(rawdata, idx); idx += 4;//datasizeを鮮やかに無視
				EntityID target = new EntityID(target_id_num);

				//メッセージ本体の抜き出し
				switch(msg_type){
					case TYPE_FIRE:
						int size = utils.readInt32(rawdata, idx);  idx += 4;
						
						
						mes = new ExtinguishMessage(msgId, addrAgent, addrType, broadcast, target, size);
						break;
					case TYPE_RESCUE:
						mes = new RescueMessage(msgId, addrAgent, addrType, broadcast, target);
						break;
					case TYPE_CLEAR:
						mes = new ClearMessage(msgId, addrAgent, addrType, broadcast, target);
						
						break;
					default:
						
				}
				if(mes != null){
					received.add(mes);
					result.add(mes);
				}else{
					//エラーは吐くけど何もしない
				}
			}catch(ArrayIndexOutOfBoundsException aioobe){
				//不正に読み出したら問答無用でnullを返す
				
				return null;
			}catch(Exception e){
				System.err.println("Exception in AgentMessageManager!");
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage(naito_rescue.message.Message mes_){
		
		
		sendMessageList.add(mes_);
		
		boolean isNowSendCycle = (owner.getTime() % 1) == 0;
		if(isNowSendCycle && !sendMessageList.isEmpty()){
			
			
			byte[] rawdata;
			int    rawdata_size = 0;
			int    remove_idx = 0;
			
			for(int i = 0;i < sendMessageList.size();i++){
				//rawdata_size += HEADER_SIZE + sendMessageList.get(remove_idx).getSize();
				int thisSize = HEADER_SIZE + sendMessageList.get(i).getSize();
				if(rawdata_size+thisSize < 61){
					remove_idx++;
					rawdata_size += thisSize;
				}else{
					break;
				}
			}
			
			rawdata = new byte[rawdata_size+4]; //rawdataを確保
			int idx = 0;
			
			//送信処理
			
			for(int i = 0;i < remove_idx;i++){
				
				naito_rescue.message.Message mes = sendMessageList.removeFirst();

				//ヘッダの書き込み
				idx = utils.writeHeader(rawdata, mes.getType(), mes.getID(), mes.getAddrAgent(), mes.getAddrType(), mes.isBroadcast(), -1, mes.getSize());
				//本体の書き込み
				switch(mes.getType()){
					case TYPE_FIRE:
						
						ExtinguishMessage ems = (ExtinguishMessage)mes;
						utils.writeInt32(ems.getTarget().getValue(), rawdata, idx); idx += 4;
						utils.writeInt32(ems.getTargetSize(), rawdata, idx); idx += 4;
						//
						//
						break;
					case TYPE_RESCUE:
						
						RescueMessage rm = (RescueMessage)mes;
						utils.writeInt32(rm.getTarget().getValue(), rawdata, idx); idx += 4;
						break;
					case TYPE_CLEAR:
						
						ClearMessage cm = (ClearMessage)mes;
						utils.writeInt32(cm.getTarget().getValue(), rawdata, idx); idx += 4;
						break;
					default:
						
						return;
				}//end switch
				mes.setSendTime(owner.getTime());
				sended.add(mes);
			}//end for
			utils.writeInt8(TYPE_NULL, rawdata, idx);
			owner.speak(channel, rawdata);
			
			
		}//end if
	}
	public void setChannel(int n){
		this.channel = n;
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
		return owner.getID().getValue() * (int)(Math.pow(10, getDigit(create_mes_count))) + create_mes_count;
	}
	public RescueMessage createRescueMessage(int addrAgent, int addrType, boolean broadcast, EntityID target){
		RescueMessage result = new RescueMessage(createMsgID(), addrAgent, TYPE_RESCUE, broadcast, target);
		create_mes_count++;
		return result;
	}
	public ExtinguishMessage createExtinguishMessage(int addrAgent, int addrType, boolean broadcast, EntityID target, int size){
		ExtinguishMessage result = new ExtinguishMessage(createMsgID(), addrAgent, TYPE_FIRE, broadcast, target, size);
		create_mes_count++;
		return result;
	}
	public ClearMessage createClearMessage(int addrAgent, int addrType, boolean broadcast, EntityID target){
		ClearMessage result = new ClearMessage(createMsgID(), addrAgent, TYPE_CLEAR, broadcast, target);
		create_mes_count++;
		return result;
	}
	
}
