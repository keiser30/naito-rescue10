package naito_rescue.message;

public interface MessageConstants
{
	public static final int TYPE_NULL       = 0x00;
	public static final int TYPE_FIRE       = 0x01;
	public static final int TYPE_RESCUE     = 0x02;
	public static final int TYPE_CLEAR      = 0x03;
	
	public static final int ADDR_FB = 0x00000011;
	public static final int ADDR_PF = 0x00000012;
	public static final int ADDR_AT = 0x00000013;
	public static final int ADDR_UNKNOWN = 0x00000020;
	
	public static final int HEADER_TYPE_SIZE = 1; //メッセージヘッダの「メッセージの種類」は1バイト
	public static final int HEADER_MSG_ID_SIZE = 4;
	public static final int HEADER_ADDR_AGENT_SIZE = 2;
	//public static final int HEADER_PADDING_SIZE = 1; //AddrType + isBroadcastを合わせて1バイト
	public static final int HEADER_ADDR_TYPE_SIZE = 1;
	public static final int HEADER_ISBROADCAST_SIZE = 1;
	public static final int HEADER_TTL_SIZE = 1;
	public static final int HEADER_DATASIZE_SIZE = 1;
	public static final int HEADER_SIZE = HEADER_TYPE_SIZE +
	                                      HEADER_MSG_ID_SIZE +
										  HEADER_ADDR_AGENT_SIZE +
										  HEADER_ADDR_TYPE_SIZE +
										  HEADER_ISBROADCAST_SIZE +
										  HEADER_TTL_SIZE +
										  HEADER_DATASIZE_SIZE;
}
