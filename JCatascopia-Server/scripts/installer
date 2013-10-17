#!/bin/bash

NAME="${pom.name}"
SRC="JCatascopiaServerDir"
DEST="/usr/local/bin/"

cp -r $SRC $DEST
cp $NAME /etc/init.d
chmod +x /etc/init.d/$NAME
update-rc.d $NAME defaults 
