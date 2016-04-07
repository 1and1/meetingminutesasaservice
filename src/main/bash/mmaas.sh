#! /bin/bash

# (c) 2013 S. Fuhrmann, 1&1 Internet AG

SEP=":"

if [ $OSTYPE = "cygwin" ]; then
	SEP=";"
fi

BASE=$(dirname $0)/..
for i in $BASE/lib/*; do
	CP=$CP$SEP$i
done

java $JAVA_OPTS -Dlog4j.configuration=file:$BASE/config/log4j.properties -cp $CP de.einsundeins.meetingminutesasaservice.App $@
