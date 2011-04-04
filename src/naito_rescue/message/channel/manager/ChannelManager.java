package naito_rescue.message.channel.manager;

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

public class ChannelManager
{
	NAITOHumanoidAgent owner;
	StandardWorldModel model;
	Config             config;
	
	public ChannelManager(NAITOHumanoidAgent owner, Config config){
		this.owner = owner;
		this.model = owner.getWorldModel();
		this.config = config;
	}
}
