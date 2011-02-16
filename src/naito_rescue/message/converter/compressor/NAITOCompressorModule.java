package naito_rescue.message.converter.compressor;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import static naito_rescue.debug.DebugUtil.*;
import naito_rescue.message.*;
import naito_rescue.message.converter.*;
import naito_rescue.message.stream.*;
import naito_rescue.agent.*;

import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import rescuecore2.misc.geometry.*;

public class NAITOCompressorModule implements ICompressorModule
{
	public RawDataInputStream decompress(byte[] compress){
		try{
			Inflater decomp = new Inflater();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			decomp.setInput(compress);
			byte[] buf = new byte[1024];
			while(!decomp.finished()){
				int len = decomp.inflate(buf);
				baos.write(buf, 0, len);
			}
			
			RawDataInputStream result = new RawDataInputStream(baos.toByteArray());
			
			p("解凍前の配列長 = " + compress.length);
			p("解凍後の配列長 = " + result.getRawData().length);
			return result;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	public byte[] compress(RawDataOutputStream stream){
		try{
			if(stream.length() == 0) return null;
			Deflater comp = new Deflater();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			byte[] rawdata = stream.toByteArray();
			
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
