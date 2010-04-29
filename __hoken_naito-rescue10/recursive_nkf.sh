#!/bin/bash

find . -name "$1" | xargs nkf $2 --overwrite 
