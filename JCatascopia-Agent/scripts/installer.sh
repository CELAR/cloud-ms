#!/bin/bash

NAME="${pom.name}"
SRC="JCatascopiaAgentDir"
DEST="/usr/local/bin/"

cp -r $SRC $DEST
cp $NAME /etc/init.d
chmod +x /etc/init.d/$NAME

DISTRO=$(eval cat /etc/*release)
if [[ "$DISTRO" == *Ubuntu* ]]; then
        echo "distro in use is Ubuntu"
        update-rc.d $NAME defaults
fi
if [[ "$DISTRO" == *CentOS* ]]; then
        echo "distro in use is CentOS"
        chkconfig --add $NAME
        chkconfig $NAME on
fi
if [[ "$DISTRO" == *openSUSE* ]]; then
        echo "distro in use is openSUSE"
        insserv /etc/init.d/$NAME
fi
echo "JCatascopia-Agent installed..."
