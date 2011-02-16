#!/bin/bash

if [ $# -ne 3 ] ; then
	echo "usage: ./replaceAll.sh <dir> <before> <after>"
	exit
fi
FILES=`find $1 -name "*.java"`
#echo $FILES
for FILE in $FILES
do
	echo $FILE
	sed -i "s/$2/$3/g" $FILE
done
