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
<meta charset="US-ASCII">
<!-- scripts -->
<script type="text/javascript" src="js/jquery.min.1.8.js"></script>
<script type="text/javascript" src="js/jquery.dropdown.js"></script>
<script type="text/javascript" src="js/utilities.js"></script>
<script type="text/javascript" src="js/singleSubPage.js"></script>
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
<link rel="stylesheet" style="text/css" href="css/style.css" />
<link rel="stylesheet" style="text/css" href="css/menu.css" />
<link rel="stylesheet" style="text/css" href="css/jquery.dropdown.css" />
<link rel="stylesheet" style="text/css" href="css/singleAgentPage.css" />
<link rel="stylesheet" style="text/css" href="css/singleSubPage.css" />
<title>CELAR - Subscriptions</title>
</head>
<body>
	<!-- header and menu -->

	<div class="column" id="left_col">
		<div id="subscribedAgents">
			<!-- javascript generated -->
		</div>
		<div id="unsubscribedAgents">
			<!-- javascript generated -->
		</div>
	</div>
	<div class="column" id="right_col">
		<div class="infocontainer" align="center">
			<div class='header'>System Information</div>
			<div class='infowrapper' align="left">
				<table class="staticinfo_table">
					<tr>
						<td class="staticinfo" id="subName"><span
							class="staticinfo_label">Subscription Name</span></td>
						<td class="staticinfo" id="group"><span
							class="staticinfo_label">Group</span></td>
					</tr>
					<tr>
						<td class="staticinfo" id="func"><span
							class="staticinfo_label">Function</span></td>
						<td class="staticinfo" id="metricType"><span
							class="staticinfo_label">Metric Type</span></td>
					</tr>
					<tr>
						<td class="staticinfo" id="period"><span
							class="staticinfo_label">Period</span></td>
						<td class="staticinfo" id="metricUnits"><span
							class="staticinfo_label">Metric Units</span></td>
					</tr>
				</table>
			</div>
		</div>
		<!-- javascript generated -->
		<div class="container" id="subscription" align="center">
			<div class="header" id="subscriptionHeader">
				<span class="expand">-</span>
			</div>
			<div align='right'>
				<span>TimeRange: </span> <select class='timerange'>
					<option value='0'>Disabled</option>
					<option value='60'>1 minute</option>
					<option value='120'>2 minutes</option>
					<option value='300'>5 minutes</option>
					<option value='600'>10 minutes</option>
					<option value='1800'>30 minutes</option>
					<option value='3600'>1 hour</option>
					<option value='7200'>2 hours</option>
					<option value='18000'>5 hours</option>
					<option value='36000'>10 hours</option>
					<option value='86400'>1 day</option>
				</select>
			</div>
			<div class="wrapper" id="subscriptionWrap" style="display: block;">
				<canvas width="800" height="290" id="subscriptionGraph"
					style="cursor: default;">[No canvas supported]</canvas>
			</div>
		</div>
	</div>
</body>
</html>
