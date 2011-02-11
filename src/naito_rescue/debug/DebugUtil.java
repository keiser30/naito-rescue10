package naito_rescue.debug;
// デバッグ用出力，その他
// static importして使うと便利
public class DebugUtil
{
	/*
	*  デバッグ出力
	*/
	public static void p(String s){
		System.out.println("====>" + s);
	}
	public static void n(){
		System.out.println();
	}
	public static void nn(){
		System.out.println("\n");
	}
	// byte[] 配列のデバッグ出力
	public static void pbarray(byte[] b){
		p(":配列:");
		for(int i = 0;i < b.length;i++){
			System.out.printf("%x", b[i]); System.out.print(":");
		}
		n();
	}
	
}
