#!/bin/bash

JAR="${project.build.finalName}.jar"
JCATASCOPIA_SERVER_HOME="/usr/local/bin/JCatascopiaServerDir"
java -jar $JCATASCOPIA_SERVER_HOME/$JAR  $JCATASCOPIA_SERVER_HOME & 
