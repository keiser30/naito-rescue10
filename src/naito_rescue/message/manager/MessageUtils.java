package naito_rescue.message.manager;

import java.io.*;
import java.util.*;

public class MessageUtils
{
	private ByteArrayOutputStream baos;
	private DataOutputStream      dos;
	private ByteArrayInputStream  bis;
	private DataInputStream       dis;
	
	private int header_size;
	private boolean debug = false;

	public int writeHeader(byte[] rawdata,  int     type, 
	                       int    mesID,    int     addrAgent, 
						   int    addrType, boolean isBroadcast, 
						   int    ttl,      int     datasize){
		int idx = 0;
		writeInt8(type,              rawdata, idx); idx += 1;
		writeInt32(mesID,            rawdata, idx); idx += 4;
		writeInt16(addrAgent,        rawdata, idx); idx += 2;
		writeInt8(addrType,          rawdata, idx); idx += 1;
		writeInt8((isBroadcast?1:0), rawdata, idx); idx += 1;
		writeInt8(ttl,               rawdata, idx); idx += 1;
		writeInt8(datasize,          rawdata, idx); idx += 1;

		if(debug){
			int idx_ = 0;
			System.out.println();
			System.out.println("********************************************");
			System.out.println("type = " + type);
			System.out.println("Read type = " + readInt8(rawdata, idx_)); idx_ += 1;
			System.out.println("mesID = " + mesID);
			System.out.println("Read mesID = " + readInt32(rawdata, idx_)); idx_ += 4;
			System.out.println("**addrAgent = " + addrAgent);
			System.out.println("**Read addrAgent = " + readInt16(rawdata, idx_)); idx_ += 2;
			System.out.println("addrType = " + addrType);
			System.out.println("Read addrType = " + readInt8(rawdata, idx_)); idx_ += 1;
			System.out.println("isBroadcast = " + isBroadcast);
			System.out.println("Read isBroadcast = " + readInt8(rawdata, idx_)); idx_ += 1;
			System.out.println("ttl = " + ttl);
			System.out.println("Read ttl = " + readInt8(rawdata, idx_)); idx_ += 1;
			System.out.println("datasize = " + datasize);
			System.out.println("Read datasize = " + readInt8(rawdata, idx_));
		}
		return idx;
	}
	public void setHeaderSize(int size){
		this.header_size = size;
	}
	
	public int readInt32(byte[] raw, int from){
		bis = new ByteArrayInputStream(raw);
		dis = new DataInputStream(bis);
		
		try{
			dis.skipBytes(from);
			return dis.readInt();
		}catch(Exception e){
			System.err.println("Exception in readInt32();");
			e.printStackTrace();
			return -1;
		}
	}
	public int readInt8(byte[] raw, int from){
		bis = new ByteArrayInputStream(raw);
		dis = new DataInputStream(bis);

		try{
			dis.skipBytes(from);
			byte b = dis.readByte();
			if(debug){
				System.err.println("readInt8();");
				System.err.println("\t return = " + b);
				System.err.println("\t return(byte) = " + (int)b);
			}
			return b;
		}catch(Exception e){
			System.err.println("Exception in readInt8();");
			e.printStackTrace();
			return -1;
		}
	}
	public int readInt16(byte[] raw, int from){
		bis = new ByteArrayInputStream(raw);
		dis = new DataInputStream(bis);

		try{
			dis.skipBytes(from);
			short s = dis.readShort();
			if(debug){
				System.err.println("readInt16();");
				System.err.println("\t return = " + s);
				System.err.println("\t return(short) = " + (int)s);
			}
			return s;
		}catch(Exception e){
			System.err.println("Exception in readInt16();");
			e.printStackTrace();
			return -1;
		}
	}
	public void writeInt32(int n, byte[] raw, int from){
		raw[from    ] = (byte)((n >> 24) & 0xFF);
		raw[from + 1] = (byte)((n >> 16) & 0xFF);
		raw[from + 2] = (byte)((n >>  8) & 0xFF);
		raw[from + 3] = (byte)((n >>  0) & 0xFF);
	}
	public void writeInt16(int n, byte[] raw, int from){
		raw[from    ] = (byte)((n >> 8) & 0xFF);
		raw[from + 1] = (byte)((n >> 0) & 0xFF);
		if(debug){
			System.out.println("writeInt16(" + n + ")");
			System.err.println("\t raw[from]  = " + raw[from]);
			System.err.println("\t raw[from+1]= " + raw[from+1]);
			System.err.println("\t " + (readInt16(raw, from)));
		}
	}
	public void writeInt8(int n, byte[] raw, int from){
		raw[from] = (byte)((n >> 0) & 0xFF);
		if(debug){
			System.err.println("writeInt8(" + n + ")");
			System.err.println("\t raw[from] = " + raw[from]);
			System.err.println("\t " + (readInt8(raw, from)));
		}
	}
	public byte[] readBytes(byte[] raw, int from, int size){
		byte[] ret = new byte[size];
		bis = new ByteArrayInputStream(raw);
		
		try{
			bis.read(ret, from, size);
			return ret;
		}catch(Exception e){
			return null;
		}
	}
}
