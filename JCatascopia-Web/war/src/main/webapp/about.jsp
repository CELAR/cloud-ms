<!--
Copyright 2014, Laboratory of Internet Computing (LInC), Department of Computer Science, University of Cyprus

For any information relevant to JCatascopia Monitoring System,
please contact Demetris Trihinas, trihinas{at}cs.ucy.ac.cy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<%@ page language="java" contentType="text/html; charset=US-ASCII"
	pageEncoding="US-ASCII"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<!-- scripts -->
<script type="text/javascript" src="js/jquery.min.1.8.js"></script>
<script type="text/javascript" src="js/jquery.dropdown.js"></script>
<script type="text/javascript" src="js/utilities.js"></script>
<!-- stylesheets -->
<link rel="stylesheet" style="text/css" href="css/menu.css" />
<link rel="stylesheet" style="text/css" href="css/style.css" />
<link rel="stylesheet" style="text/css" href="css/jquery.dropdown.css" />
<style>
section {
	margin-bottom: 35px;
}

span.title {
	font-size: 20px;
	text-decoration: underline;
	font-weight: bold;
}

.reference {
	margin: 10px 0px 0px 20px;
	font-size: 15px;
}

span a:link,span a:visited,span a:hover,span a:active {
	color: black;
	text-decoration: underline;
}
</style>
<title>About JCatascopia</title>
</head>
<body style="height: 100%;">
	<section>
		<span class="title">JCatascopia Monitoring System utilizes</span>
		<div class="reference">
			<a href="http://www.rgraph.net" target="_blank"> <img
				alt="RGraph" src="img/rgraph_logo.png"> <img alt="RGraph"
				src="img/rgraph.png">
			</a> <br /> <a href="http://zeromq.org/" target="_blank"> <img
				alt="ZMQ" src="img/zmq.jpg">
			</a> <br /> <a href="http://mysql.com/" target="_blank"> <img
				alt="MySQL" src="img/mysql.jpg">
			</a> <br /> <a href="http://jersey.java.net" target="_blank"> <img
				alt="Jersey" src="img/jersey_logo.png">
			</a>
		</div>
	</section>
	<section>
		<span class="title">Developer:</span>
		<div class="reference">
			<span style="font-weight: bold;">Demetris Trihinas</span><br> <span
				style="font-style: italic;">Researcher @ Computer Science
				Department</span><br> <span style="font-style: italic;">University
				of Cyprus</span><br> <span style="font-style: italic;">trihinas@cs.ucy.ac.cy</span><br>
			<span style="font-style: italic;"><a
				href="http://linc.ucy.ac.cy/" target="_blank">http://linc.ucy.ac.cy/</a></span>
		</div>
		<br /> <span class="title">Web Interface</span>
		<div class="reference">
			<span style="font-weight: bold;">Chrysovalantis Anastasiou</span><br>
			<span style="font-style: italic;">B.Sc Student @ Computer
				Science Department</span><br> <span style="font-style: italic;">University
				of Cyprus</span><br> <span style="font-style: italic;">canast02@cs.ucy.ac.cy</span><br>
			<span style="font-style: italic;"><a
				href="http://linc.ucy.ac.cy/" target="_blank">http://linc.ucy.ac.cy/</a></span>
		</div>
	</section>
</body>
</html>
