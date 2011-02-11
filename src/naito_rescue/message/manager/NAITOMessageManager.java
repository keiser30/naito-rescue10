package naito_rescue.message.manager;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import static naito_rescue.debug.DebugUtil.*;
import naito_rescue.message.*;
import naito_rescue.message.communication.*;
import naito_rescue.message.converter.*;
import naito_rescue.message.intermediate.*;
import naito_rescue.message.stream.*;
import naito_rescue.agent.*;

import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import rescuecore2.misc.geometry.*;

public class NAITOMessageManager
{
	private NAITOHumanoidAgent owner;
	private List<? extends NAITOMessage> receivedHistory;
	private List<? extends NAITOMessage> sendedHistory;
	private IMessageConverterModule converter;
	
	public NAITOMessageManager(NAITOHumanoidAgent owner){
		this.owner = owner;
		this.receivedHistory = new ArrayList<NAITOMessage>();
		this.sendedHistory = new ArrayList<NAITOMessage>();
		
		converter = new NAITOMessageConverter();
	}
	
	public List<? extends NAITOMessage> receiveMessages(AKSpeak speak){
		List<? extends NAITOMessage> decoded = converter.decodeMessages(speak);
		return decoded;
	}
	public void sendMessages(List<? extends NAITOMessage> list, int ch){
		byte[] encoded = converter.encodeMessages(list);
		owner.speak(ch, encoded);
	} 

}
