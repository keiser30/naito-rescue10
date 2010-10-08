package naito_rescue;

import rescuecore2.standard.entities.*;
import naito_rescue.agent.*;
import java.io.*;
import java.util.*;
import java.text.*;

public class MyLogger
{
	File                logfile;
	PrintStream         logger;
	Throwable           debugInfo;
	StackTraceElement[] element;
	Stack<String>       contextStack;
	int                 time;

/**
*  LOGLEVEL(上から下に行くほどレベルが下がる)
*    ...
*    info    ... 1番大雑把な情報
*    debug   ... デバッグ用に変数をチェックしたりしなかったり用
*    trace   ... メソッドの中身を上から下にトレースする用
*
*/
	final int INFO = 100;
	final int DEBUG = 90;
	final int TRACE = 85;
	final int TOP = INFO+1;
	final int NOTHING = INFO+1;

	Date now = new Date();
	SimpleDateFormat date = new SimpleDateFormat("yyyyMMddkkmmss");
	String prefix;
	static HashMap<Class, String> prefix_map = new HashMap<Class, String>();
	static{
		prefix_map.put(NAITOFireBrigade.class, "FB");
		prefix_map.put(NAITOAmbulanceTeam.class, "AT");
		prefix_map.put(NAITOPoliceForce.class, "PF");
	}
	String      context = "";
	
	int         loglevel = TRACE; //NOTHINGなら何もしない

	public MyLogger(NAITOAgent owner, boolean isStdout){
		if(loglevel >= NOTHING) return;

		try{
			if(isStdout){
				logger = System.out;
			}else{
				logfile = new File("./logs/" + date.format(now) + "__" + owner.toString() + ".log");
				logger = new PrintStream(new FileOutputStream(logfile));
			}
		}catch(IOException ioe){
			System.err.println("*************************************************");
			System.err.println("*         IOException: public MyLogger()        *");
			System.err.println("*************************************************");
			System.exit(-1);
		}
		
		// "Type(ID)"
		prefix = "";
		contextStack = new Stack<String>();
		String type = prefix_map.get(owner.getClass());
		if(type == null){
			prefix += "UNKNOWN";
		}else{
			prefix += type;
		}
		prefix += "(" + owner.getID().getValue() + ")";
		time = 0;
	}
	public void setTime(int t){
		this.time = t;
	}
	public void setContext(String str){
		// "[str]"
		contextStack.push("::" + str);
		this.context = contextStack.peek(); //not remove.
	}
	public void unsetContext(){
		if(!contextStack.isEmpty()) contextStack.pop(); //remove
		if(contextStack.isEmpty()){
			this.context = "";
		}else{
			this.context = contextStack.peek(); //not remove.
		}
	}
	public void unsetAllContext(){
		if(!contextStack.isEmpty()) contextStack.removeAllElements();
		this.context = "";
	}
	public void info(String str){
		if(loglevel >= NOTHING) return;
		println("--> "+str);
	}
	public void debug(String str){
		//if(loglevel > DEBUG) return;
		println("----> "+str);
	}
	public void trace(String str){
		if(loglevel > TRACE) return;
		println("--------> "+str);
	}

	 
	private void println(String str){
		if(loglevel >= NOTHING) return;
		logger.println(time+":"+"["+prefix+context+"]"+str);
	}
	public void flush(){
		if(loglevel >= NOTHING) return;
		logger.flush();
	}
}
