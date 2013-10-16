#!/bin/bash

JAR="JCatascopia-Agent-0.0.1-SNAPSHOT.jar"
JCATASCOPIA_AGENT_HOME="/usr/local/bin/JCatascopiaAgentDir"
java -jar $JCATASCOPIA_AGENT_HOME/$JAR  $JCATASCOPIA_AGENT_HOME & 
