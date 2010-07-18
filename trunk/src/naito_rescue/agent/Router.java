package naito_rescue.agent;

import rescuecore2.misc.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.entities.*;

import naito_rescue.*;
import java.util.*;

public interface Router
{
	public List<EntityID> getRoute(StandardEntity from, StandardEntity to);
}