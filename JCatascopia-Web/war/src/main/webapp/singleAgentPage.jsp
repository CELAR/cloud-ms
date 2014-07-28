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
<title>AgentPage - <%= request.getParameter("agentIP") %></title>
<!-- javascript -->
<script type="text/javascript" src="js/jquery.min.1.8.js"></script>
<script type="text/javascript" src="js/jquery.dropdown.js"></script>
<script type="text/javascript" src="js/utilities.js"></script>
<script type="text/javascript" src="js/singleAgentPage.js"></script>
<script type="text/javascript" src="js/graph.js"></script>
<script type="text/javascript" src="js/libraries/RGraph.common.core.js"></script>
<script type="text/javascript"
	src="js/libraries/RGraph.common.dynamic.js"></script>
<script type="text/javascript"
	src="js/libraries/RGraph.common.tooltips.js"></script>
<script type="text/javascript"
	src="js/libraries/RGraph.common.effects.js"></script>
<script type="text/javascript" src="js/libraries/RGraph.drawing.rect.js"></script>
<script type="text/javascript"
	src="js/libraries/RGraph.common.context.js"></script>
<script type="text/javascript"
	src="js/libraries/RGraph.common.effects.js"></script>
<script type="text/javascript" src="js/libraries/RGraph.line.js"></script>
<!-- stylesheets -->
<link rel="stylesheet" style="text/css" href="css/singleAgentPage.css" />
<link rel="stylesheet" style="text/css" href="css/menu.css" />
<link rel="stylesheet" style="text/css" href="css/style.css" />
<link rel="stylesheet" style="text/css" href="css/jquery.dropdown.css" />
</head>
<body>
	<div id="container">
		<!-- header and menu -->
		<div class="column" id="left_col">
			<!-- javascript generated -->
		</div>
		<div class="column" id="right_col">
			<div id = "agentTitle" style = "margin:0 auto;width:98%;border-bottom:2px solid #ddd;padding-bottom:5px;margin-bottom:8px;margin-top:-8px;font-weight:bold;font-size:x-large;">
			</div>
			<div class="infocontainer" align="center">
				<div class='header'>System Information</div>
				<div class='infowrapper' align="left">
					<table class="staticinfo_table">
						<tr>
							<td class="staticinfo" id="os"><span
								class="staticinfo_label">Operating System</span></td>
							<td class="staticinfo" id="cpuNum"><span
								class="staticinfo_label">CPU Number</span></td>
						</tr>
						<tr>
							<td class="staticinfo" id="arch"><span
								class="staticinfo_label">Architecture</span></td>
							<td class="staticinfo" id="memTotal"><span
								class="staticinfo_label">Total Memory</span></td>
						</tr>
						<tr>
							<td class="staticinfo" id="btime"><span
								class="staticinfo_label">Boot Time</span></td>
							<td class="staticinfo" id="diskTotal"><span
								class="staticinfo_label">Total Disk</span></td>
						</tr>
					</table>
				</div>
			</div>
			<!-- javascript generated -->
		</div>
	</div>
	<!-- container -->
	<div id="statusbar" class="fixed"
		style="clear: both; background-color: #ddd; bottom: 0px; width: 95%; display: none;">
		<table>
			<tr>
				<td style="font-weight: bold;">AgentID:</td>
				<td><%= request.getParameter("agentID")%></td>
				<td class=".st_sep"></td>
				<td style="font-weight: bold;">AgentIP:</td>
				<td><%= request.getParameter("agentIP")%></td>
				<td class=".st_sep"></td>
				<td style="width: 80%; text-align: right; font-weight: bold;">Time:</td>
				<td id="clockid"></td>
			</tr>
		</table>
	</div>
	<!-- statusbar -->
	<div id="statusbar" class="secondary active"
		style="clear: both; background-color: #ddd; bottom: 0px; width: 100%;">
		<table>
			<tr>
				<td style="font-weight: bold;">AgentID:</td>
				<td><%= request.getParameter("agentID")%></td>
				<td class=".st_sep"></td>
				<td style="font-weight: bold;">AgentIP:</td>
				<td><%= request.getParameter("agentIP")%></td>
				<td class=".st_sep"></td>
				<td style="width: 80%; text-align: right; font-weight: bold;">Time:</td>
				<td id="clockid"></td>
			</tr>
		</table>
	</div>
	<!-- statusbar -->
</body>
</html>
