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
			return -1;
		}
	}
	
	public void writeInt32(int n, byte[] raw, int from){
		raw[from    ] = (byte)((n >> 24) & 0xFF);
		raw[from + 1] = (byte)((n >> 16) & 0xFF);
		raw[from + 2] = (byte)((n >>  8) & 0xFF);
		raw[from + 3] = (byte)((n >>  0) & 0xFF);
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
