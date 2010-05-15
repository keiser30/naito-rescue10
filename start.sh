#!/bin/sh

AGENT_DIR_BASE="."
AGENT_JAR_FILE="$AGENT_DIR_BASE/naito_rescue.jar"

KERNEL_ADDRESS="localhost"
KERNEL_PORT=7000

CLASSPATH=`find ./jars -name "*.jar" | xargs | sed -e "s/ /:/g"`
CLASSPATH=$CLASSPATH:$AGENT_JAR_FILE

java -Xmx800m -Xmn256m -cp $CLASSPATH naito_rescue.LaunchAgents -h $KERNEL_ADDRESS -p $KERNEL_PORT &

echo "####     ####        ##       ######  ############    ######             "
echo " ###      ##       ## ##        ##         ##        ##    ##            "
echo " ## #     ##      ##   ##       ##         ##       ##      ##           "
echo " ##  #    ##      ##   ##       ##         ##      ##        ##          "
echo " ##   #   ##     ##     ##      ##         ##      ##        ##  ######  "
echo " ##    #  ##     #########      ##         ##      ##        ##          "
echo " ##     # ##    ##       ##     ##         ##       ##      ##           "
echo " ##      ###    ##       ##     ##         ##        ##    ##            "
echo "####     ####  ##         ##  ######       ##         ######             "
echo ""
echo "##########                                                               "
echo " ##       ##                                                             "
echo " ##       ##    #####       ####      #####    ##     ##     #####       "
echo " ##       ##   #     #     ##   ##   #     ##  ##     ##    #     #      "
echo " #########    ##     ##   ##        ##         ##     ##   ##     ##     "
echo " ##  #        #########    #####    ##         ##     ##   #########     "
echo " ##   #       ##                ##  ##         ##     ##   ##            "
echo " ##    #       #         ##     ##   #     ##  ##     ##    #            "
echo "####  ######    ######     #####      #####     ######  #    ######      "
echo ""
echo "                       ##       #######   "
echo "                      ###      ##     ##  "
echo "                     # ##     ##       ## "
echo "                       ##     ##       ## "
echo "                       ##     ##       ## "
echo "                       ##     ##       ## "
echo "                       ##     ##       ## "
echo "                       ##      ##     ##  "
echo "                     ######     #######   "
echo ""                   
echo "    Produced by  Aichi Institute of Technology (Japan)"
echo "                Nagoya Institute of Technology (Japan)"
echo "NAITO-Rescue10 Start!"
