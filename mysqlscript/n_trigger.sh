#!/bin/bash
 TOKEN=$(curl --data "user=user&pwd=pswd" http://localhost:8080/pelars/password)
 echo "token=$TOKEN"
 TOKEN=${TOKEN//\{\"token\"\:\"}
 TOKEN=${TOKEN//\"\}}
 #TOKEN=${TOKEN//token}
 echo "token=$TOKEN"
 curl --data "token=$TOKEN" http://localhost:8080/pelars/data/$1
