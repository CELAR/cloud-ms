#!/bin/bash

JAR="${project.build.finalName}.jar"
JCATASCOPIA_SERVER_HOME="/usr/local/bin/JCatascopiaServerDir"
JCATASCOPIA_LOCK="/var/lock/JCatascopia-Server-lock"
java -jar $JCATASCOPIA_SERVER_HOME/$JAR  $JCATASCOPIA_SERVER_HOME $JCATASCOPIA_LOCK & 
