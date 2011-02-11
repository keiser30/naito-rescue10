package naito_rescue.message;

public enum MessageType
{
	NULL((byte)0x00), BLOCKED((byte)0x01),
	FIRE((byte)0x02), CIVILIAN((byte)0x03),
	HELP((byte)0x04), ACK((byte)0x20),
	ERROR((byte)0x7F);
	
	private byte type;
	
	//ちょっと無理やりなコンストラクタ
	MessageType(byte b){
		type = b;
	}
	public byte type(){
		return type;	
	}
	public static MessageType from(byte header)
	{
		for(MessageType t : MessageType.values()){
			if(header == t.type()) return t;
		}
		return MessageType.ERROR; // Invalid header .
	}
}
