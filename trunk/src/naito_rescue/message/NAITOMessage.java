package naito_rescue.message;

import java.io.*;
import java.util.*;

import static naito_rescue.debug.DebugUtil.*;
import naito_rescue.message.*;
import naito_rescue.message.manager.*;
import naito_rescue.message.stream.*;
import naito_rescue.agent.*;

public abstract class NAITOMessage
{
	protected MessageType type = MessageType.NULL;
	
	public abstract RawDataOutputStream encode();
	public abstract void decode(RawDataInputStream in);
	
	public MessageType getType(){
		return type;
	}
}
