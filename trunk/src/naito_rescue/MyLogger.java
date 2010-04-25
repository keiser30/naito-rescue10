package naito_rescue;

import rescuecore2.standard.entities.*;
import naito_rescue.agent.*;
import java.io.*;

public class MyLogger
{
	File        logfile;
	PrintStream logger;
	
	int         loglevel = 1; //0なら何もしない

	public MyLogger(NAITOHumanoidAgent owner, boolean isStdout){
		if(loglevel == 0) return;

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

	}

	public void println(String str){
		if(loglevel == 0) return;

		logger.println(str);
	}
	public void flush(){
		if(loglevel == 0) return;
		logger.flush();
	}
}
