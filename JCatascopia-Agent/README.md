JCatascopia-Agent
====================================

Prerequisites
---------------

- **Software Prerequisites** 

JCatascopia-Agent only requires Java to be installed. Recommended versions are 1.6.x and 1.7.x

- **Network Prerequisites** 

JCatascopia-Agent uses for the scope of the [CELAR](http://celarcloud.eu/) project TCP as its default distribution network protocol. Ports 4242, 4243 and 4245 must be made available

Licence
---------------
The complete source code of the JCatascopia Monitoring System is open-source and available to the community under the [Apache 2.0 licence](http://www.apache.org/licenses/LICENSE-2.0.html)

Getting Started
---------------

- Download the LATEST version of the JCatascopia-Agent from the CELAR artifact repository:

```shell
### CELAR Repository Parameters
# if CELAR repo not valid please contact us.
# select version to download, for simplicity the lastest version uses the LATEST tag.
# JCatascopia-Agent is distributed as a tarball or Jar
###
CELAR_REPO=http://snf-175960.vm.okeanos.grnet.gr
JC_VERSION=LATEST
JC_ARTIFACT=JCatascopia-Agent
JC_GROUP=eu.celarcloud.cloud-ms
JC_TYPE=tar.gz

URL="$CELAR_REPO/nexus/service/local/artifact/maven/redirect?r=snapshots&g=$JC_GROUP&a=$JC_ARTIFACT&v=$JC_VERSION&p=$JC_TYPE"
wget -O JCatascopia-Agent.tar.gz $URL
tar xvfz JCatascopia-Agent.tar.gz
```

- Configure, via its config file, and install the Monitoring Agent. The Monitoring Agent is preconfigured with default properties, however, users are required to set the IP of the JCatascopia-Server(s) if not at localhost e.g.:

```
SERVER_IP=192.168.0.1
eval "sed -i 's/server_ip=.*/server_ip=$SERVER_IP/g' JCatascopia-Agent-*/JCatascopiaAgentDir/resources/agent.properties"
cd JCatascopia-Agent-*
./installer.sh
```

- Start the JCatascopia-Agent:

```shell
/etc/init.d/JCatascopia-Agent start
```

- An exemplary deployment script to automatically download and configure JCatascopia-Agent can be found [here](https://github.com/CELAR/celar-deployment/blob/master/vm/jcatascopia-agent.sh)

Note
---------------
This version of JCatascopia is compliant for the purposes of the [CELAR](http://celarcloud.eu/) project. For the standalone version of JCatascopia please refer to [http://linc.ucy.ac.cy/CELAR/jcatascopia](http://linc.ucy.ac.cy/CELAR/jcatascopia)

Contact Us
---------------
Please contact Demetris Trihinas trihinas{at}cs.ucy.ac.cy for any issue

Publications
---------------
For any research work in which JCatascopia is used, please cite the following article:

"JCatascopia: Monitoring Elastically Adaptive Applications in the Cloud", D. Trihinas and G. Pallis and M. D. Dikaiakos, "14th IEEE/ACM International Symposium on Cluster, Cloud and Grid Computing" (CCGRID 2014), Chicago, IL, USA 2014
http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=6846458&isnumber=6846423

Website
---------------
[http://linc.ucy.ac.cy/CELAR/jcatascopia](http://linc.ucy.ac.cy/CELAR/jcatascopia)

