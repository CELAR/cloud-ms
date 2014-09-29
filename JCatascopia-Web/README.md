JCatascopia-Web
====================================

Prerequisites
---------------

- **Software Prerequisites** 

The JCatascopia-Server core requires Java to be installed. Recommended versions are 1.6.x and 1.7.x. Furthermore, if the JCatascopia-Web requires a web container e.g. [Apache Tomcat](http://tomcat.apache.org/). Recommended version are Tomcat 6.X.X and 7.X.X.

- **Network Prerequisites** 

JCatascopia-Server uses for the scope of the [CELAR](http://celarcloud.eu/) project TCP as its default distribution network protocol. Ports 4242, 4243 and 4245 must be made available plus the web container port (e.g. default tomcat port 8080)

Licence
---------------
The complete source code of the JCatascopia Monitoring System is open-source and available to the community under the [Apache 2.0 licence](http://www.apache.org/licenses/LICENSE-2.0.html)

Getting Started
---------------
JCatascopia-Server is distributed and packaged in RPM and War. The default packaging for the scope of the CELAR project is RPM.

- Add CELAR repository to yum configuration:

```shell
cat > /etc/yum.repos.d/celar.repo <<EOF
[CELAR-snapshots]
name=CELAR-snapshots
baseurl=http://snf-175960.vm.okeanos.grnet.gr/nexus/content/repositories/snapshots
enabled=1
protect=0
gpgcheck=0
metadata_expire=30s
autorefresh=1
type=rpm-md
EOF
```

- Download the LATEST version of the JCatascopia-Web from the CELAR artifact repository (Note: Tomcat should be located in /usr/share/tomcat):

```shell
yum install -y JCatascopia-Web
```

- An exemplary deployment script to automatically download and configure JCatascopia-Web can be found [here](https://github.com/CELAR/celar-deployment/blob/master/orchestrator/jcatascopia-server.sh) with example how to install Cassandra DB as well

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

