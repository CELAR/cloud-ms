#!/bin/bash

JAR="${project.build.finalName}.jar"
JCATASCOPIA_AGENT_HOME="/usr/local/bin/JCatascopiaAgentDir"
JCATASCOPIA_LOCK="/var/lock/JCatascopia-Agent-lock"
java -jar $JCATASCOPIA_AGENT_HOME/$JAR  $JCATASCOPIA_AGENT_HOME $JCATASCOPIA_LOCK & 
