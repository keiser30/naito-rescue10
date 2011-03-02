#!/bin/sh

if [ -d $1 ] ; then
	FILES=`find $1 -name "*.java"`
	for FILE in $FILES
	do
		sed -i 's/\/\+logger\./logger\./g' $FILE
	done
fi
