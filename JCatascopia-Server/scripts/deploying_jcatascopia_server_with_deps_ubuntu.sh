#!/bin/bash

CELAR_REPO=http://snf-175960.vm.okeanos.grnet.gr
JC_SERVER_VERSION=LATEST
JC_SERVER_ARTIFACT=JCatascopia-Server
JC_SERVER_TYPE=tar.gz
JC_GROUP=eu.celarcloud.cloud-ms

JC_WEB_VERSION=LATEST
JC_WEB_ARTIFACT=JCatascopia-Web-war
JC_WEB_TYPE=war

TOMCAT_VERSION=7.0.55
TOMCAT_DIR=/usr/share

eval "sed -i 's/127.0.0.1.*localhost.*/127.0.0.1 localhost $HOSTNAME/g' /etc/hosts"

apt-get update

#download and install java
apt-get install -y openjdk-7-jre-headless

#download, install and configure cassandra
apt-get install -y curl
echo "deb http://debian.datastax.com/community stable main" | sudo tee -a /etc/apt/sources.list.d/cassandra.sources.list
curl -L http://debian.datastax.com/debian/repo_key | sudo apt-key add -
apt-get update
apt-get install libjna-java
apt-get install -y dsc22
pkill java
rm -rf /var/lib/cassandra/*

#download and install JCatascopia-Server...
URL="$CELAR_REPO/nexus/service/local/artifact/maven/redirect?r=snapshots&g=$JC_GROUP&a=$JC_SERVER_ARTIFACT&v=$JC_SERVER_VERSION&p=$JC_SERVER_TYPE"
wget -O JCatascopia-Server.tar.gz $URL
tar xvfz JCatascopia-Server.tar.gz
cd JCatascopia-Server-*
./installer.sh
#celar hack
mv -f JCatascopia-Server-CELAR /etc/init.d/JCatascopia-Server
cd ..

#download, install and parameterize tomcat...
if [ ! -d apache-tomcat-$TOMCAT_VERSION ]; then
  wget http://archive.apache.org/dist/tomcat/tomcat-7/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz
  tar xvfz apache-tomcat-$TOMCAT_VERSION.tar.gz -C $TOMCAT_DIR/
  mv $TOMCAT_DIR/apache-tomcat-$TOMCAT_VERSION $TOMCAT_DIR/tomcat/
fi

#download and install JCatascopia-Web...
URL="$CELAR_REPO/nexus/service/local/artifact/maven/redirect?r=snapshots&g=$JC_GROUP&a=$JC_WEB_ARTIFACT&v=$JC_WEB_VERSION&p=$JC_WEB_TYPE"
wget -O JCatascopia-Web.war $URL
cp JCatascopia-Web.war $TOMCAT_DIR/tomcat/webapps/
