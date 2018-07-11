#!/bin/bash

source `dirname $0`/javaenv.sh

if [[ $1 = --stream ]]; then
	export option0=$1
	shift
fi

while [[ $1 =~ ^- ]];
do
    if [[ $1 = -type ]]; then
    shift
	export option1=$1
	shift
    fi
    if [[ $1 = -meta ]]; then
    shift
	export option2=$1
	shift
    fi
done


export hdtFile=$1
shift

mvn exec:java -Dexec.mainClass="org.rdfhdt.hdtjena.cmd.HDTSparql" -Dexec.args="$option0 $option1 $option2 $hdtFile '$1'"

exit $?
