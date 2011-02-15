package naito_rescue.message.manager;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.apache.commons.lang.RandomStringUtils;

import static naito_rescue.debug.DebugUtil.*;
import naito_rescue.message.*;
import naito_rescue.message.converter.*;
import naito_rescue.message.converter.compressor.*;
import naito_rescue.message.stream.*;
import naito_rescue.agent.*;
import naito_rescue.*;

import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import rescuecore2.misc.geometry.*;

public class NAITOMessageManager
{
	private NAITOAgent owner;
	private final int DEFAULT_CHANNEL_CAPACITY = 255; //まぁ大体は
	private int channelCapacity = DEFAULT_CHANNEL_CAPACITY;
	private final List<NAITOMessage> EMPTY_LIST = new ArrayList<NAITOMessage>();
	private List<NAITOMessage> receivedHistory;
	private List<NAITOMessage> remainder;
	private List<NAITOMessage> sendedHistory;
	private List<NAITOMessage> lazyList;
	private List<NAITOMessage> cannot;
	private IMessageConverterModule converter;
	private ICompressorModule       compressor;
	private MyLogger                logger;
	
	public NAITOMessageManager(NAITOAgent owner){
		this.owner = owner;
		this.remainder = new ArrayList<NAITOMessage>();
		this.receivedHistory = new ArrayList<NAITOMessage>();
		this.sendedHistory = new ArrayList<NAITOMessage>();
		this.lazyList = new ArrayList<NAITOMessage>();
		this.cannot = new ArrayList<NAITOMessage>();
		
		converter = new NAITOMessageConverterModule();
		compressor = new NAITOCompressorModule();
		logger    = owner.getLogger();
	}
	
	public List<? extends NAITOMessage> receiveMessages(AKSpeak speak){
		byte[] compressedRawData = speak.getContent();
		if(compressedRawData == null || compressedRawData.length == 0){
			logger.info("zero message has received. ==> return empty list.");
			return EMPTY_LIST;			
		}
		
		RawDataInputStream stream = compressor.decompress(compressedRawData);
		List<? extends NAITOMessage> decoded = converter.decodeMessages(stream);

		logger.info("NAITOMessageManager.receiveMessages();");
		logger.info("-- received " + decoded.size() + " messages. --");
		for(NAITOMessage m : decoded){
			logger.debug(m.toString());
		}
		logger.info("receiveMessages(); end.");
		return decoded;
	}
    public void sendMessages(List<? extends NAITOMessage> list, int ch){
        for(NAITOMessage m : list){
            // NAITOBaseMessageに属するものでidが未生成のものがあれば
            // idを生成する
            if(m instanceof NAITOBaseMessage){
                NAITOBaseMessage bm = (NAITOBaseMessage)m;
                if(bm.getID() == -1) generateBaseMessageID(bm);
            }
        }
        RawDataOutputStream stream = converter.encodeMessages(list);
        byte[] encoded = compressor.compress(stream);
        
        logger.info("NAITOMessageManager.sendMessages();");
        logger.info("-- sended " + list.size() + " messages. --");
        for(NAITOMessage m : list){
            logger.debug(m.toString());
        }
        logger.debug("encoded array length = " + encoded.length);
        logger.info("sendMessages(); end.");
        
        owner.speak(ch, encoded);
    } 
	public void sendMessage(NAITOMessage msg, int ch){
		sendMessages(Arrays.asList(msg), ch);
	}
	public void accumulateMessages(List<? extends NAITOMessage> list){
		for(NAITOMessage m : list){
			lazyList.add(m);
		}
	}
	public void accumulateMessage(NAITOMessage m){
		accumulateMessages(Arrays.asList(m));
	}
	public void flushAccumulatedMessages(){
		
	}
	private void generateBaseMessageID(NAITOBaseMessage bm){
		// 3桁のランダムな数字列
		String idSeed = RandomStringUtils.random(1, "123456789") + RandomStringUtils.randomNumeric(2);
		//エージェントのIDの数字列と，その下位3桁にランダムな数字列を差し込んだものをIDとする
		bm.setID(owner.getID().getValue() * 1000 + Integer.parseInt(idSeed));
	}

}
