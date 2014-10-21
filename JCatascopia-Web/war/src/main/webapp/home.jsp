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
<script type="text/javascript" src="js/home.js"></script>
<script type="text/javascript" src="js/graph.js"></script>
<script type="text/javascript" src="js/spin.js"></script>
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
<link rel="stylesheet" style="text/css" href="css/home.css" />
<link rel="stylesheet" style="text/css" href="css/menu.css" />
<link rel="stylesheet" style="text/css" href="css/style.css" />
<link rel="stylesheet" style="text/css" href="css/jquery.dropdown.css" />
<title>CELAR - HomePage</title>
</head>
<body>
	<!-- header and menu -->
	<div class="wrapper">
	
		<div class="block" align="center" style="margin: 0 auto;width:97.5%;margin-bottom:10px;">
			<div class="header" id="elasticityHeader">
				<span class="expand">-</span>Number of Running Agents in Deployment
			</div>	
			<div class="content" align="center" id="elasticityContent" style="height:100%;">
				<div class="graphContainer" align="center">
					<canvas width="850" height="300" id="runningVMsGraph">[No canvas supported]</canvas>
				</div>
			</div>
		</div>	
	
		<div class="column" id="left_col" align="center">
			<div class="block">
				<div class="header" id="runningVMsHeader">
					<span class="expand">-</span>Running VMs
				</div>
				<div class="content" align="center" id="runningVMsContent">
					<!-- javascript generated -->
					<div id="spinner1" style="margin-top: 45px;"></div>
				</div>
			</div>
			
			<div class="block">
				<div class="header" id="stoppedVMsHeader">
					<span class="expand">+</span>VMs Down
				</div>
				<div class="content" align="center" id="stoppedVMsContent"
					style="display: none;">
					<!-- javascript generated -->
					<div id="spinner2" style="margin-top: 45px;"></div>
				</div>
			</div>		
		</div>
		
		<div class="column" id="right_col" align="center">
			<div class="block">
				<div class="header"><span class="expand">-</span>Console</div>
				<div class="content" id="consoleContent" align="center">
					<div id="consoleDiv" style="text-align:left;font-size:medium;padding:3px;overflow: auto;"></div>
				</div>
			</div>
			
			<div class="block">
				<div class="header" id="subscriptionsHeader">
					<span class="expand">+</span>Metric Subscriptions
				</div>
				<div class="content" align="center" id="subscriptionsContent" style="display: none;">
					<!-- javascript generated -->
					<div id="spinner3" style="margin-top: 45px;"></div>
				</div>
			</div>
		</div>
		
		
		<div class="block" align="center" style="margin: 0 auto;width:97.5%;">
			<div class="header" id="clusterviewHeader">
				<span class="expand">-</span>Cluster View
			</div>	
			<div class="content" align="center" id="clusterviewContent" style="height:100%;max-height:650px;padding-bottom:10px;display:none;">
				<div id="clusterWT">
					<!-- javascript generated -->
				</div>
				<div id="clusterND">
					<h2 style="text-align:left;border-bottom:2px solid #ddd;padding:2px 10px 0px 10px;width:95%;">Not Defined</h2>
					<div id="cvNotDefined" style="text-align:left;width:95%;overflow:auto;padding:5px 10px 5px 10px; border: 1px solid #ddd;">
						<!-- javascript generated -->
					</div>	
				</div>
			</div>
		</div>	
		
			
	</div>
</body>
</html>
