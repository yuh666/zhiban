#!/bin/bash

STUDENTS_ARRAY=("韩扬,于昊" "武兴,泽鑫" "若望,袁斐" "明华,玉娟,兴杰")
DINGDING_TOKENS=("aaf8b34eb97350d8f423ab70a2177b4262df56949ebc9a7de5a12d517f14dd90")

today=$(date '+%Y年%m月%d日@%A')
week=$(date '+%W')
index=$(($week % ${#STUDENTS_ARRAY[@]}))
students="${STUDENTS_ARRAY[$index]}"

msg="值班信息:今天是$today,本周值班同学是:$students."

for token in ${DINGDING_TOKENS[@]}
do
    for (( i=1; i<=2; i++ ))
    do
        curl 'https://oapi.dingtalk.com/robot/send?access_token='$token -H 'Content-Type: application/json' -d '{
                "msgtype": "text",
                "text": {
                    "content":"'$msg'"
                }
            }'
    done
done
