#!/bin/sh

if [ $1 -d ] ; then
	find $1 -name "*.java" | xargs gedit &
fi
