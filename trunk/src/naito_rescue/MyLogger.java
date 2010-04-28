package naito_rescue;

import rescuecore2.standard.entities.*;
import naito_rescue.agent.*;
import java.io.*;
import java.util.*;

public class MyLogger
{
	File        logfile;
	PrintStream logger;

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

	String prefix;
	static HashMap<Class, String> prefix_map = new HashMap<Class, String>();
	static{
		prefix_map.put(NAITOFireBrigade.class, "FB:");
		prefix_map.put(NAITOAmbulanceTeam.class, "AT:");
		prefix_map.put(NAITOPoliceForce.class, "PF:");
	}

	int         loglevel = DEBUG; //NOTHINGなら何もしない

	public MyLogger(NAITOHumanoidAgent owner, boolean isStdout){
		if(loglevel >= NOTHING) return;

		try{
			if(isStdout){
				logger = System.out;
			}else{
				logfile = new File(owner.toString() + ".log");
				logger = new PrintStream(new FileOutputStream(logfile));
			}
		}catch(IOException ioe){
			System.err.println("*************************************************");
			System.err.println("*         IOException: public MyLogger()        *");
			System.err.println("*************************************************");
			System.exit(-1);
		}
		prefix = prefix_map.get(owner.getClass());
		if(prefix == null){
			prefix = "UNKNOWN:";
		}

	}

	public void info(String str){
		if(loglevel >= NOTHING) return;
		println(str);
	}
	public void debug(String str){
		if(loglevel > DEBUG) return;
		println("...."+str);
	}
	public void trace(String str){
		if(loglevel > TRACE) return;
		println("........"+str);
	}

	 
	private void println(String str){
		if(loglevel >= NOTHING) return;

		logger.println(prefix+str);
	}
	public void flush(){
		if(loglevel <= NOTHING) return;
		logger.flush();
	}
}
