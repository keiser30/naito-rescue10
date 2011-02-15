package naito_rescue.message.stream;

import java.util.*;
import java.io.*;

public class RawDataInputStream
{
	byte[] rawdata;
	ByteArrayInputStream bais;
	DataInputStream input;
	
	public RawDataInputStream(byte[] data){
		rawdata = new byte[data.length];
		System.arraycopy(data, 0, rawdata, 0, data.length);
		
		bais = new ByteArrayInputStream(rawdata);
		input = new DataInputStream(bais);
	}
	public int readInt() throws IOException{
		return input.readInt();
	}
	public short readShort() throws IOException{
		return input.readShort();
	}
	public byte readByte() throws IOException{
		return input.readByte();
	}
	public int readUnsignedByte() throws IOException{
		return input.readUnsignedByte();
	}
	public char readChar() throws IOException{
		return input.readChar();
	}
	public byte[] getRawData(){
		return rawdata;
	}
}
