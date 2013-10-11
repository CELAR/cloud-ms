#!/bin/bash
# Grabs and kill a process from the pidlist that has the word JCatascopia-Agent

#pid=`ps aux | grep JCatascopia-Agent | awk '{print $2}' | head -n 1`
pid=`ps aux | grep JCatascopia-Agent | awk '{print $2}'`
kill -9 $pid  > /dev/null 2>&1 &
