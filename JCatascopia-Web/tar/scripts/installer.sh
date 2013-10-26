#!/bin/bash

#script to deploy JCatascopia-Web interface to orchestrator

source /etc/profile
WAR=$(find . -name "*.war")

cp $WAR $CATALINA_BASE/webapps/

exit 0
