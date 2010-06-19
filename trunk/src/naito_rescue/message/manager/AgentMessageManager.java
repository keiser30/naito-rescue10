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
		
		logger.info("AgentMessageManager.receiveMessage();");
		byte[] rawdata = speak.getContent();
		List<naito_rescue.message.Message> result = new ArrayList<naito_rescue.message.Message>();
		//List<naito_rescue.message.Message> result = null;
		
		logger.info("Extracting Message from received data...");
		logger.debug("AKSpeak.getContent().size() = " + rawdata.length);
		
		//ノイズ対策
		if(rawdata.length <= 0){
			logger.info("rawdata.length = 0 => return null;");
			return null;
		}
		int hoge = 0;
		int idx = 0;
		while(true){
			try{
				naito_rescue.message.Message mes = null;
				logger.info((++hoge) + "個目: ");
				//readInt32するときは，「4バイト以上ストリームから読み込み可能」
				//であることを保証してから読み込んだ方が良い by Ogawa
				
				//ヘッダの抜き出し
				int msg_type        = utils.readInt8(rawdata, idx);        idx += 1;

				if(msg_type == TYPE_NULL){
					logger.info("receiveMessage() reached TYPE_NULL");
					logger.debug("Returning Message List = " + result);
					return result;
				}

				//ヘッダの抜き出し(続き)
				int msgId          = utils.readInt32(rawdata, idx);        idx += 4;
				int addrAgent      = utils.readInt16(rawdata, idx);        idx += 2;
				int addrType       = utils.readInt8(rawdata, idx);         idx += 1;
				boolean broadcast  = (utils.readInt8(rawdata, idx) > 0);   idx += 1;
				int ttl            = utils.readInt8(rawdata, idx);         idx += 1;
				int datasize       = utils.readInt8(rawdata, idx);         idx += 1;
				
				logger.info("Extracting Header...");
				logger.debug("msg_type    = " + msg_type);
				logger.debug("msgId       = " + msgId);
				logger.debug("addrAgent   = " + addrAgent);
				logger.debug("addrType    = " + addrType);
				logger.debug("broadcast   = " + broadcast);
				logger.debug("ttl         = " + ttl);
				logger.debug("datasize    = " + datasize);
				
				int target_id_num = utils.readInt32(rawdata, idx); idx += 4;//datasizeを鮮やかに無視
				EntityID target = new EntityID(target_id_num);

				//メッセージ本体の抜き出し
				switch(msg_type){
					case TYPE_FIRE:
						int size = utils.readInt32(rawdata, idx);  idx += 4;
						logger.debug("(ExtinguishMessage)target_id_num = " + target_id_num);
						logger.debug("(ExtinguishMessage)size          = " + size);
						mes = new ExtinguishMessage(msgId, addrAgent, addrType, broadcast, target, size);
						break;
					case TYPE_RESCUE:
						mes = new RescueMessage(msgId, addrAgent, addrType, broadcast, target);
						break;
					case TYPE_CLEAR:
						mes = new ClearMessage(msgId, addrAgent, addrType, broadcast, target);
						logger.debug("Now received message:\n    ====> " + mes.toString());
						break;
					default:
						logger.info("Unknown type: " + msg_type);
				}
				if(mes != null){
					logger.info("Extract one message.");
					logger.info("Message = " + mes);
					received.add(mes);
					result.add(mes);
				}else{
					//エラーは吐くけど何もしない
					logger.info("mes == null");
					logger.info("Perhaps, There is Unknown Message Type...");
				}
			}catch(ArrayIndexOutOfBoundsException aioobe){
				//不正に読み出したら問答無用でnullを返す
				logger.info("ArrayIndexOutOfBoundsException!!!!!");
				return null;
			}catch(Exception e){
				System.err.println("Exception in AgentMessageManager!");
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage(naito_rescue.message.Message mes_){
		logger.info("AgentMessageManager.sendMessage();");
		logger.info("sendMessageList.add(" + mes_ + ")");
		sendMessageList.add(mes_);
		
		boolean isNowSendCycle = (owner.getTime() % 1) == 0;
		if(isNowSendCycle && !sendMessageList.isEmpty()){
			logger.info("isNowSendCycle!");
			logger.debug("==> time = " + owner.getTime());
			byte[] rawdata;
			int    rawdata_size = 0;
			int    remove_idx = 0;
			
			for(int i = 0;i < sendMessageList.size();i++){
				//rawdata_size += HEADER_SIZE + sendMessageList.get(remove_idx).getSize();
				int thisSize = HEADER_SIZE + sendMessageList.get(i).getSize();
				if(rawdata_size+thisSize < 61){
					remove_idx++;
					rawdata_size += thisSize;
					logger.debug("remove_idx   = " + remove_idx);
					logger.debug("rawdata_size = " + rawdata_size);
				}else{
					break;
				}
			}
			
			rawdata = new byte[rawdata_size+4]; //rawdataを確保
			int idx = 0;
			
			//送信処理
			logger.info("先頭" + remove_idx + "個のメッセージを送信します．");
			for(int i = 0;i < remove_idx;i++){
				logger.info((i+1) + "個目: ");
				naito_rescue.message.Message mes = sendMessageList.removeFirst();

				//ヘッダの書き込み
				idx = utils.writeHeader(rawdata, mes.getType(), mes.getID(), mes.getAddrAgent(), mes.getAddrType(), mes.isBroadcast(), -1, mes.getSize());
				//本体の書き込み
				switch(mes.getType()){
					case TYPE_FIRE:
						logger.debug("TYPE_FIRE message will be created.");
						ExtinguishMessage ems = (ExtinguishMessage)mes;
						utils.writeInt32(ems.getTarget().getValue(), rawdata, idx); idx += 4;
						utils.writeInt32(ems.getTargetSize(), rawdata, idx); idx += 4;
						//logger.trace("target ID   = " + utils.readInt32(rawdata, 28));
						//logger.trace("target size = " + utils.readInt32(rawdata, 32));
						break;
					case TYPE_RESCUE:
						logger.debug("TYPE_RESCUE message will be created.");
						RescueMessage rm = (RescueMessage)mes;
						utils.writeInt32(rm.getTarget().getValue(), rawdata, idx); idx += 4;
						break;
					case TYPE_CLEAR:
						logger.debug("TYPE_CLEAR message will be created.");
						ClearMessage cm = (ClearMessage)mes;
						logger.debug("Now written message: :");
						logger.debug(" ===> " + cm.toString());
						utils.writeInt32(cm.getTarget().getValue(), rawdata, idx); idx += 4;
						break;
					default:
						logger.debug("Writing Unknown message type: " + mes.getType());
						return;
				}//end switch
				mes.setSendTime(owner.getTime());
				sended.add(mes);
			}//end for
			utils.writeInt8(TYPE_NULL, rawdata, idx);
			logger.info("Now send rawdata...");
			logger.debug("==> rawdata.length = " + rawdata.length);
			owner.speak(channel, rawdata);
			logger.info("Sending Message End.");
			logger.debug("Now(After sending messages) sendMessageList.size() = " + sendMessageList.size());
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
		ClearMessage result = new ClearMessage(createMsgID(), addrAgent, addrType, broadcast, target);
		create_mes_count++;
		return result;
	}
	
}
