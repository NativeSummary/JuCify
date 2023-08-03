#!/bin/bash

source /root/jucify_venv/bin/activate
cd $(dirname $0)/scripts

TIMEOUT=${JUCIFY_TIMEOUT:-0}
echo Jucify total timeout: $TIMEOUT
/usr/bin/time -v /usr/bin/timeout --kill-after=60s $TIMEOUT ./main.sh "$@"

# ./main.sh -f /benchApps/getter_imei.apk -t -p /platforms
# must use full path
# result files: APK_NAME/ APK_NAME_result/ APK_NAME.native.log APK_NAME.flow.log
