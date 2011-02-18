package naito_rescue.task.job;

import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;
import naito_rescue.task.job.*;
import naito_rescue.agent.*;
import naito_rescue.*;
import java.util.*;
import java.io.*;

public abstract class Job
{
	protected NAITOHumanoidAgent owner;
	protected StandardWorldModel model;
	protected MyLogger           logger;
	
	public Job(NAITOHumanoidAgent owner){
		this.owner = owner;
		this.model = owner.getWorldModel();
		this.logger = owner.getLogger();
	}
	
	public abstract boolean isFinished();
	public abstract void    act();
}
