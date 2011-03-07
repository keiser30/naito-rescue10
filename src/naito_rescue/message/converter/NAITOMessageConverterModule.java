package naito_rescue.message.converter;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import static naito_rescue.debug.DebugUtil.*;
import naito_rescue.message.*;
import naito_rescue.message.stream.*;
import naito_rescue.agent.*;

import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import rescuecore2.misc.geometry.*;

public class NAITOMessageConverterModule implements IMessageConverterModule
{
	/**
	*  decodeMessages();
	*  受信したメッセージの列を復号する
	*/
	public List<NAITOMessage> decodeMessages(RawDataInputStream stream){
		List<NAITOMessage> list = new ArrayList<NAITOMessage>();
		//RawDataInputStream stream = new RawDataInputStream(rawdata);
		
		// メッセージタイプがNULL(メッセージ列の末尾)に達するか、
		// ERROR(何らかの原因でメッセージを復号できなかった場合)に
		// 達するまで、復号処理のループを繰り返す.
		// ここで、メッセージ列は以下の順番でbyte型配列にエンコード
		// されている.
		//
		// MessageType1 | Data | MessageType2 | Data | ... 
		// ... | MessageTypeN | Data | MessageType.NULL
		MessageType type = MessageType.NULL;
		try{
			type = MessageType.from(stream.readByte());
		}catch(Exception e){e.printStackTrace();}
		while(type != MessageType.NULL){
		    
			NAITOMessage m = null;
			switch(type){
			case FIRE:
				m = new FireMessage();
				break;
			case BLOCKED:
				m = new BlockedRoadMessage();
				break;
			case CIVILIAN:
				m = new CivilianInBuildingMessage();
				break;
			case HELP_BLOCK:
				m = new HelpMeInBlockadeMessage();
				break;
			case ACK:
				//p("まだ実装してません(ｷﾘｯ");
				break;
			default:
				//p("Invalid Message Type: " + type);
				break;
			}
			if(m != null){
				// ヘッダにより判別された各メッセージの
				// decode()メソッドを呼び、メッセージの
				// 中身を復号させる.
				m.decode(stream);
				list.add(m);
				try{
					type = MessageType.from(stream.readByte());
				}catch(Exception e){e.printStackTrace();}
			}else{
				//p("m == null => return list;");
				return list;
			}
		}// end while
		return list;
	}
	/**
	*  encodeMessages();
	*  メッセージのリストから
	*  RawDataOutputStream型にエンコする
	*/
	public RawDataOutputStream encodeMessages(List<? extends NAITOMessage> list){
		RawDataOutputStream stream = new RawDataOutputStream();
		try{
			for(NAITOMessage m : list){
				// メッセージのタイプを書き込む(1byte)
				stream.writeByte(m.getType().type());
				
				RawDataOutputStream rawdata = m.encode();
				stream.append(rawdata);
			}
			// 末尾に、MessageType.NULLを書き込む
			//stream.writeByte(MessageType.NULL.type());
			// byte[] compress = compress(stream.toByteArray()); //圧縮
			//byte[] compress = stream.toByteArray();
			////p("return;");
			//return compress;
			return stream;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
}
