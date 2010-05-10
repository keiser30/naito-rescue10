package naito_rescue.message;

public interface MessageConstants
{
	public static final int TYPE_FIRE       = 0x00000001;
	public static final int TYPE_RESCUE     = 0x00000002;
	public static final int TYPE_CLEAR = 0x00000003;
	
	public static final int ADDR_FB = 0x00000101;
	public static final int ADDR_PF = 0x00000102;
	public static final int ADDR_AT = 0x00000103;
	
	
	public static final int HEADER_ELEM_SIZE = 4; //メッセージヘッダの要素のサイズ
	public static final int HEADER_ELEM_NUM  = 7; //メッセージヘッダの要素数
	public static final int HEADER_SIZE = HEADER_ELEM_SIZE * HEADER_ELEM_NUM;
}
