package naito_rescue.message.stream;

import java.util.*;
import java.io.*;

public class RawDataOutputStream
{
	ByteArrayOutputStream baos;
	DataOutputStream output;
	
	public RawDataOutputStream()
	{
		baos = new ByteArrayOutputStream();
		output = new DataOutputStream(baos);
	}
	
	public void writeInt(int i) throws IOException{
		output.writeInt(i);
	}
	public void writeByte(byte b) throws IOException{
		output.writeByte(b);
	}
	public void writeShort(short s) throws IOException{
		output.writeShort(s);
	}
	public void writeChar(char c) throws IOException{
		output.writeChar(c);
	}
	public void append(RawDataOutputStream other) throws IOException{
		byte[] otherData = other.toByteArray();
		output.write(otherData, 0, otherData.length);
	}
	public void write(byte[] array, int offset, int len) throws IOException{
		output.write(array, offset, len);
	}
	public void write(byte[] array) throws IOException{
		output.write(array, 0, array.length);
	}
	public byte[] toByteArray(){
		return baos.toByteArray();
	}
	public int length(){
		return baos.toByteArray().length;
	}
}
