#! /bin/bash

$* &
pid=$!
((lim = "${BINARY_TIMEOUT:-1800}"))
while [[ $lim -gt 0 ]] ; do
    sleep 1
    proc=$(ps -ef | awk -v pid=$pid '$2==pid{print}{}')
    ((lim = lim - 1))
    if [[ -z "$proc" ]] ; then
            ((lim = -9))
    fi
done
if [[ $lim -gt -9 ]] ; then
    pkill -P $pid
    kill -9 -$pid
    echo "Process killed because of timout reached (${BINARY_TIMEOUT:-1800}s)"
fi
