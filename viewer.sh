#!/bin/sh

AGENT_DIR_BASE="."
AGENT_JAR_FILE="$AGENT_DIR_BASE/naito_rescue.jar"

KERNEL_ADDRESS="localhost"
KERNEL_PORT=7000

CLASSPATH=`find ./jars -name "*.jar" | xargs | sed -e "s/ /:/g"`
CLASSPATH=$CLASSPATH:$AGENT_JAR_FILE

java -Xmx256m -cp $CLASSPATH rescuecore2.LaunchComponents naito_rescue.viewer.NAITOViewer -h $KERNEL_ADDRESS -p $KERNEL_PORT &
