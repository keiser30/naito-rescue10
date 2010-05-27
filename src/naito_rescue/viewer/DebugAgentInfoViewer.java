package naito_rescue.viewer;

import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.standard.entities.*;
import rescuecore2.messages.control.*;
import rescuecore2.messages.components.*;
import rescuecore2.standard.messages.*;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;


public class DebugAgentInfoViewer extends JPanel
{
	Human                agent = null;
//	JPanel               leftPanel, rightPanel;
	JTable               leftTable, rightTable;
	JScrollPane          leftScrollPane, rightScrollPane;
	AgentTableModel      agent_model;
	CommandTableModel    command_model;
	java.util.List<Command>        commands = null;
	
	static HashMap<Class, String> cmd2str = new HashMap<Class, String>();
	static{
		cmd2str.put(AKMove.class, "Move");
		cmd2str.put(AKExtinguish.class, "Extinguish");
		cmd2str.put(AKClear.class, "Clear");
		cmd2str.put(AKLoad.class, "Load");
		cmd2str.put(AKUnload.class, "Unload");
		cmd2str.put(AKRescue.class, "Rescue");
		cmd2str.put(AKSubscribe.class, "Subscribe");
		cmd2str.put(AKSpeak.class, "Speak");
		cmd2str.put(AKRest.class, "Rest");
	}
	
	public DebugAgentInfoViewer(){
		super();
		this.setLayout(new GridLayout(1,2));
		
//		leftPanel = new JPanel();
//		rightPanel = new JPanel();
		
		leftTable = new JTable();
		rightTable = new JTable();
		
		agent_model = new AgentTableModel();
		leftTable.setModel(agent_model);
		command_model = new CommandTableModel();
		rightTable.setModel(command_model);
		//leftPanel.setPreferredSize(new Dimension(250,250));
		//rightPanel.setPreferredSize(new Dimension(250,250));
		//leftPanel.setBorder(BorderFactory.createTitledBorder("基本的な情報"));
		//leftPanel.add(leftTable);
		//rightPanel.add(rightTable);
		//rightPanel.setBorder(BorderFactory.createTitledBorder("今何してる?"));
		leftScrollPane = new JScrollPane(leftTable);
		rightScrollPane = new JScrollPane(rightTable);
		leftScrollPane.setPreferredSize(new Dimension(250,250));
		leftScrollPane.setBorder(BorderFactory.createTitledBorder("基本的な情報"));
		rightScrollPane.setPreferredSize(new Dimension(250,250));
		rightScrollPane.setBorder(BorderFactory.createTitledBorder("今何してる?"));

		this.add(leftScrollPane);
		this.add(rightScrollPane);
	}
	
	public void setTargetAgent(Human agent){
		this.agent = agent;
		agent_model.fire();		
	}
	public void setCommandList(java.util.List<Command> commands){
		command_model.setCommands(commands);
		command_model.fire();
	}
	
	private class CommandTableModel extends AbstractTableModel{
		
		java.util.List<Command> commands;
		Command targetCommand;
		
		public void setCommands(java.util.List<Command> commands){
			this.commands = commands;
		}
		public void fire(){
			extractTargetCommand();
			fireTableStructureChanged();
			fireTableDataChanged();
		}
		private void extractTargetCommand(){
			for(Command command : commands){
				EntityID agentID = command.getAgentID();
				if(agent != null && agent.getID().getValue() == agentID.getValue()){
					targetCommand = command;
					break;
				}
			}
		}
		@Override
		public int getRowCount(){
			return 5;
		}
		@Override
		public int getColumnCount(){
			return 2;
		}
		@Override
		public Object getValueAt(int row, int col){
			if(targetCommand != null){
				switch(col){
					case 0:
						switch(row){
							case 0:
								return "Action:";
							case 1:
								if(targetCommand instanceof AKSubscribe || targetCommand instanceof AKSpeak){
									return "channel:";
								}else{
									return "target:";
								}
							case 2:
								if(targetCommand instanceof AKMove){
									return "path:";
								}else if(targetCommand instanceof AKExtinguish){
									return "power:";
								}else{
									return "";
								}
							case 3: /* FALL THROUGH */
							case 4:
							default:
								return "";
						}
					case 1:
						switch(row){
							case 0: //Action name
								return cmd2str.get(targetCommand.getClass());
							case 1: //Action target
								if(targetCommand instanceof AKMove){
									java.util.List<EntityID> path = ((AKMove)targetCommand).getPath();
									return path.get(path.size()-1);
								}else if(targetCommand instanceof AKExtinguish){
									return ((AKExtinguish)targetCommand).getTarget();
								}else if(targetCommand instanceof AKClear){
									return ((AKClear)targetCommand).getTarget();
								}else if(targetCommand instanceof AKLoad){
									return ((AKLoad)targetCommand).getTarget();
								}else if(targetCommand instanceof AKRescue){
									return ((AKRescue)targetCommand).getTarget();
								}else if(targetCommand instanceof AKSubscribe){
									return ((AKSubscribe)targetCommand).getChannels();
								}else if(targetCommand instanceof AKSpeak){
									return ((AKSpeak)targetCommand).getChannel() + "";
								}
							case 2: //appendix1
								if(targetCommand instanceof AKMove){
									return ((AKMove)targetCommand).getPath();
								}else if(targetCommand instanceof AKExtinguish){
									return ((AKExtinguish)targetCommand).getWater() + "";
								}
							case 3: /* FALL THROUGH */
							case 4:
							default:
								return "hoge";
						}//end inner-switch
				}//end outer-switch
			}else{
				return "NoCommand";
			}//end if
			return "";
		}
        @Override
        public String getColumnName(int col) {
            switch (col) {
            case 0:
                return "Property";
            case 1:
                return "Value";
            default:
                throw new IllegalArgumentException("Invalid column: " + col);
            }
        }
	}
	
    private class AgentTableModel extends AbstractTableModel {

        public void fire() {
            fireTableStructureChanged();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return 6;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int row, int col) {
        	if(agent != null){
		        switch (col) {
		        case 0:
		        	switch(row){
		        		case 0:
		        			return "ID";
		        		case 1:
		        			return "Type";
		        		case 2:
		        			return "HP";
		        		case 3:
		        			return "Stamina";
		        		case 4:
		        			if(agent instanceof FireBrigade){
		        				return "Water";
		        			}
		        		case 5:
		        		default:
		        			return "";
		        	}
		        case 1:
		        	switch(row){
		        		case 0:
		        			return agent.getID().getValue() + "";
		        		case 1:
							if(agent instanceof FireBrigade){
								return "FireBrigade";
							}else if(agent instanceof PoliceForce){
								return "PoliceForce";
							}else if(agent instanceof AmbulanceTeam){
								return "AmbulanceTeam";
							}else if(agent instanceof Civilian){
								return "Civilian";
							}else{
								return "UNKNOWN";
							}
		        		case 2:
		        			return agent.getHP() + "";
		        		case 3:
		        			return agent.getStamina() + "";
		        		case 4:
		        			if(agent instanceof FireBrigade){
		        				return ((FireBrigade)agent).getWater() + "";
		        			}
		        		case 5:
		        		default:
		        			return "";
		        	}
		        default:
		            throw new IllegalArgumentException("Invalid column: " + col);
		        }
            }else{
            	return "";
            }
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
            case 0:
                return "Property";
            case 1:
                return "Value";
            default:
                throw new IllegalArgumentException("Invalid column: " + col);
            }
        }
    }	
}
