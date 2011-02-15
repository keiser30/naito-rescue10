#!/bin/bash

if [ -d $1 ] ; then
	find $1 -name "*.java" | xargs gedit &
fi
