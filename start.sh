#!/bin/sh

# AGENT_DIR_BASE=""
# AGENT_DIR_FILE="$(AGENT_DIR_BASE)/naito_rescue.jar"

FIREBRIGADE="naito_rescue.agent.NAITOFireBrigade*n"
AMBULANCETEAM="naito_rescue.agent.NAITOAmbulanceTeam*n"
POLICEFORCE="naito_rescue.agent.NAITOPoliceForce*n"

KERNEL_ADDRESS="localhost"
KERNEL_PORT=7000

KERNEL_DIR_BASE="/home/robocup/rescue/rescue-nightly/0407"

CLASSPATH=`find $KERNEL_DIR_BASE/jars -name "*.jar" | xargs | sed -e "s/ /:/g"`
CLASSPATH=$CLASSPATH:`find $KERNEL_DIR_BASE/lib -name "*.jar" | xargs | sed -e "s/ /:/g"`

# echo $CLASSPATH
# echo ":::\n"
# echo 

echo "java -Xmx2048m -Xmn1024m -cp $CLASSPATH rescuecore2.LaunchComponents $FIREBRIGADE $AMBULANCETEAM $POLICEFORCE -h $KERNEL_ADDRESS -p $KERNEL_PORT " 
java -cp $CLASSPATH rescuecore2.LaunchComponents $FIREBRIGADE $AMBULANCETEAM $POLICEFORCE -h $KERNEL_ADDRESS -p $KERNEL_PORT  
echo "Done:java -cp $CLASSPATH rescuecore2.LaunchComponents $FIREBRIGADE $AMBULANCETEAM $POLICEFORCE -h $KERNEL_ADDRESS -p $KERNEL_PORT " 
