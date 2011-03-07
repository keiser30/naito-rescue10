#!/bin/sh

if [ -d $1 ] ; then
	FILES=`find $1 -name "*.java"`
	for FILE in $FILES
	do
		sed -i 's/\([[:blank:]]\)logger\./\1\/\/logger\./g' $FILE
	done
fi

if [ -f $1 ] ; then
	sed -i 's/\([[:blank:]]\)logger\./\1\/\/logger\./g' $1
fi
