#!/bin/sh

AGENT_DIR_BASE="."
AGENT_JAR_FILE="$AGENT_DIR_BASE/naito_rescue.jar"

FIREBRIGADE="naito_rescue.agent.NAITOFireBrigade*n"
AMBULANCETEAM="naito_rescue.agent.NAITOAmbulanceTeam*n"
POLICEFORCE="naito_rescue.agent.NAITOPoliceForce*n"

KERNEL_ADDRESS="localhost"
KERNEL_PORT=7000

KERNEL_DIR_BASE="/home/robocup/rescue/rescue-nightly/0422"

#CLASSPATH=`find $KERNEL_DIR_BASE/jars -name "*.jar" | xargs | sed -e "s/ /:/g"`
#CLASSPATH=$CLASSPATH:`find $KERNEL_DIR_BASE/lib -name "*.jar" | xargs | sed -e "s/ /:/g"`
#CLASSPATH=$CLASSPATH:$AGENT_JAR_FILE

CLASSPATH=`find ./jars -name "*.jar" | xargs | sed -e "s/ /:/g"`
CLASSPATH=$CLASSPATH:$AGENT_JAR_FILE

#echo ":::::::::::::::::::::::::::::::::::::::\n"
#echo $AGENT_JAR_FILE
#echo ":::::::::::::::::::::::::::::::::::::::\n"
# echo $CLASSPATH
# echo ":::\n"
# echo 

# echo "java -Xmx800m -Xmn512m -cp $CLASSPATH rescuecore2.LaunchComponents $FIREBRIGADE $AMBULANCETEAM $POLICEFORCE -h $KERNEL_ADDRESS -p $KERNEL_PORT " 
#java -Xmx512m -Xmn256m -cp $CLASSPATH rescuecore2.LaunchComponents $FIREBRIGADE -h $KERNEL_ADDRESS -p $KERNEL_PORT & 
#java -Xmx512m -Xmn256m -cp $CLASSPATH rescuecore2.LaunchComponents $AMBULANCETEAM -h $KERNEL_ADDRESS -p $KERNEL_PORT &  
# java -Xmx512m -Xmn256m -cp $CLASSPATH rescuecore2.LaunchComponents $POLICEFORCE -h $KERNEL_ADDRESS -p $KERNEL_PORT &
# java -Xmx800m -Xmn512m -cp $CLASSPATH sample.LaunchSampleAgents -h $KERNEL_ADDRESS -p $KERNEL_PORT  
java -Xmx800m -Xmn256m -cp $CLASSPATH naito_rescue.LaunchAgents -h $KERNEL_ADDRESS -p $KERNEL_PORT
echo "NAITO-Rescue started."
