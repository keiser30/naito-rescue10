package naito_rescue.message.converter;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import static naito_rescue.debug.DebugUtil.*;
import naito_rescue.message.*;
import naito_rescue.message.communication.*;
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

public class NAITOMessageConverterModule implements IMessageConverter
{
	/**
	*  decodeMessages();
	*  AKSpeak.getContent();の中身から
	*  受信したメッセージの列を復号する
	*/
	public List<NAITOMessage> decodeMessages(AKSpeak speak){
		List<NAITOMessage> list = new ArrayList<NAITOMessage>();
		
		byte[] rawdata = speak.getContent();
		if(rawdata == null || rawdata.length <= 0){
			return null;
		}
		byte[] decompressedData = decompress(rawdata); //解凍
		RawDataInputStream stream = new RawDataInputStream(decompressedData);
		
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
			case HELP:
				m = new HelpMeInBlockadeMessage();
				break;
			case ACK:
				p("まだ実装してません(ｷﾘｯ");
				break;
			default:
				p("Invalid Message Type: " + type);
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
				p("m == null => return list;");
				return list;
			}
		}// end while
		return list;
	}
	// zip圧縮された通信データを解凍して返す
	private static byte[] decompress(byte[] compress){
		try{
			Inflater decomp = new Inflater();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			decomp.setInput(compress);
			byte[] buf = new byte[1024];
			while(!decomp.finished()){
				int len = decomp.inflate(buf);
				baos.write(buf, 0, len);
			}
			
			byte[] result = baos.toByteArray();
			
			p("解凍前の配列長 = " + compress.length);
			p("解凍後の配列長 = " + result.length);
			return result;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	/**
	*  encodeMessages();
	*  メッセージのリストから
	*  byte型配列にエンコードする
	*/
	public byte[] encodeMessages(List<? extends NAITOMessage> list){
		RawDataOutputStream stream = new RawDataOutputStream();
		try{
			for(NAITOMessage m : list){
				// メッセージのタイプを書き込む(1byte)
				stream.writeByte(m.getType().type());
				
				RawDataOutputStream rawdata = m.encode();
				stream.append(rawdata);
			}
			// 末尾に、MessageType.NULLを書き込む
			stream.writeByte(MessageType.NULL.type());
			byte[] compress = compress(stream.toByteArray()); //圧縮
			//p("return;");
			return compress;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	private static byte[] compress(byte[] rawdata){
		try{
			Deflater comp = new Deflater();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			comp.setLevel(Deflater.BEST_COMPRESSION);
			comp.setInput(rawdata);
			comp.finish();
			
			byte[] buf = new byte[1024];
			while(!comp.finished()){
				int len = comp.deflate(buf);
				baos.write(buf, 0, len);
			}
			
			byte[] result = baos.toByteArray();
			
			p("圧縮前の配列長 = " + rawdata.length);
			p("圧縮後の配列長 = " + result.length);
			return result;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
}
