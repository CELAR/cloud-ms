#!/bin/bash
# Grabs and kill a process from the pidlist that has the word JCatascopia-Server

pid=`ps aux | grep JCatascopia-Server | awk '{print $2}'`
kill -9 $pid > /dev/null 2>&1 &
