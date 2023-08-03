#!/bin/bash

docker tag jucify nativesummary/jucify
docker push nativesummary/jucify
docker rmi nativesummary/jucify
