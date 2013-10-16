#!/bin/bash

JAR="${project.build.finalName}.jar"
JCATASCOPIA_AGENT_HOME="/usr/local/bin/JCatascopiaAgentDir"
java -jar $JCATASCOPIA_AGENT_HOME/$JAR  $JCATASCOPIA_AGENT_HOME & 
