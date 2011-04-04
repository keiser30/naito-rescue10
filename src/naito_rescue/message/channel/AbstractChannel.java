package naito_rescue.message.channel;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import static naito_rescue.debug.DebugUtil.*;
import naito_rescue.message.*;
import naito_rescue.message.converter.*;
import naito_rescue.message.converter.compressor.*;
import naito_rescue.message.stream.*;
import naito_rescue.agent.*;
import naito_rescue.*;

import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;

public abstract class AbstractChannel
{
	private int channelNum = -1;
	private int channelLim = 0;
	
	public AbstractChannel(int num){
		this.channelNum = num;
	}
	public int getChannelNum(){
		return channelNum;
	}
	public void setChannelLim(int lim){
		this.channelLim = lim;
	}
	public int getChannelLim(){
		return channelLim;
	}
}
