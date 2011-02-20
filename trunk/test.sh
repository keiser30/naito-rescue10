#!/bin/sh

RESCUE_HOME="/home/boss/rescue/rescue-competition2010"
RCRS_BOOT_DIR=$RESCUE_HOME"/boot"
MAP=$RESCUE_HOME"/maps/gml/test"

./cleanLogs.sh

make clean;make

xterm -e "cd $RCRS_BOOT_DIR; ./start.sh -m $MAP" &

sleep 10

./go.sh
