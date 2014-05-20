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
<!-- javascript -->
<script type="text/javascript" src="js/jquery.min.1.8.js"></script>
<script type="text/javascript" src="js/jquery.dropdown.js"></script>
<script type="text/javascript" src="js/spin.js"></script>
<script type="text/javascript" src="js/utilities.js"></script>
<script type="text/javascript" src="js/subPage.js"></script>
<!-- stylesheets -->
<link rel="stylesheet" style="text/css" href="css/jquery.dropdown.css" />
<link rel="stylesheet" style="text/css" href="css/menu.css" />
<link rel="stylesheet" style="text/css" href="css/style.css" />
<link rel="stylesheet" style="text/css" href="css/subPage.css" />
<title>Subscriptions</title>
</head>
<body>
	<!-- header and menu -->

	<section class="block border" id="subList">
		<div class="header" align="center" id="subListHeader">
			<span class="expand">-</span>My Subscriptions
		</div>
		<div id="subListContent" class="content">
			<!-- javascript generated -->
		</div>
		<div
			style="padding-left: 11px; margin-top: -5px; margin-bottom: 10px;">
			<input type="button" value="Remove Checked" id="removeBtn" />
		</div>
	</section>

	<section class="block border" id="createView">
		<div class="header" align="center" id="createViewHeader">
			<span class="expand">+</span>Create New Subscription
		</div>
		<div id="overlay">
			<span style="display: inline-block; height: 50%; width: 1px;"></span><img
				src="img/loader.gif" />
		</div>
		<div class="content hidden" id="createViewContent">
			<table>
				<tr>
					<th>Subscription Name:</th>
					<td><input type="text" size="100"
						placeholder="Enter a name for your subscription" id="subNameTxt"></td>
				</tr>
				<tr>
					<th>Metric Name:</th>
					<td><select id="metricName_select">
							<option value="-1">Select a metric</option>
							<optgroup label="Available Metrics">
								<!-- javascript generated -->
							</optgroup>
					</select> <span id="metricRealType"></span><span id="metricRealUnits"></span>
					</td>
				</tr>
				<tr>
					<th>Metric Type:</th>
					<td><select id="metricType_select">
							<option>DOUBLE</option>
							<option>INTEGER</option>
					</select></td>
				</tr>
				<tr>
					<th>Metric Units:</th>
					<td><input type="text" size="25"
						placeholder="Enter the metric's units" id="metricUnitsTxt"></td>
				</tr>
				<tr>
					<th>Metric Group:</th>
					<td><input type="text" size="25"
						placeholder="Enter the metric's group" id="metricGroupTxt"></td>
				</tr>
				<tr>
					<th>Select agents:</th>
					<td><select multiple="multiple" id="agents_select">
							<!-- javascript generated -->
					</select></td>
				</tr>
				<tr>
					<th>Grouping Function:</th>
					<td><select id="func_select">
							<option value="-1">Select a function</option>
							<optgroup label="Available functions">
								<option value="SUM">SUM</option>
								<option value="AVG">AVG</option>
								<option value="MIN">MIN</option>
								<option value="MAX">MAX</option>
							</optgroup>
					</select></td>
				</tr>
				<tr>
					<th>Update Period:</th>
					<td><input type="text" size="25"
						placeholder="Interval between updates" id="periodTxt"
						onkeyup="numericFilter(this);"></td>
				</tr>
				<tr>
					<td colspan="2" align="right"><input type="submit"
						value="Submit" id="submitBtn" /></td>
				</tr>
			</table>
		</div>
	</section>
</body>
</html>
