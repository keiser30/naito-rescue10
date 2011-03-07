#!/bin/sh

# RESCUE_HOME="/home/boss/rescue/rescue-nightly/0629"
# RESCUE_HOME="/home/boss/rescue/rescue-competition2010"
RESCUE_HOME="/home/boss/rescue/rescue-server1.0_for_test"
RCRS_BOOT_DIR=$RESCUE_HOME"/boot"
MAP=$RESCUE_HOME"/maps/gml/legacy/Kobe_astar1"

# ./cleanLogs.sh

make clean;make

# xterm -e "cd $RCRS_BOOT_DIR; ./start.sh -m $MAP" &
xterm -e "cd $RCRS_BOOT_DIR; ./start2.sh -m $MAP" &

sleep 10

./go.sh &
./viewer.sh --viewer.maximise=true
