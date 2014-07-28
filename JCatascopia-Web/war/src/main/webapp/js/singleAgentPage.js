/*
*Copyright 2014, Laboratory of Internet Computing (LInC), Department of Computer Science, University of Cyprus
*
*For any information relevant to JCatascopia Monitoring System,
*please contact Demetris Trihinas, trihinas{at}cs.ucy.ac.cy
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
$(document).ready(function(){
	window.params = getURLParams();
	
	getAvailableMetrics();
	
	window.metricsInfo = {};
	window.metricList = [];
	window.graphs = {};
	
	document.addEventListener ("scroll", function() {
		var d_h = parseInt($(document).height());
		var statusbar = document.getElementById("statusbar");
		if((document.body.scrollTop) + $(window).height() + $("footer").height() >= d_h) {
			statusbar.style.display = "none";
			$(".fixed").removeClass("active");
			$(".secondary").addClass("active");
		}
		else {
			statusbar.style.display = "";
			$(".fixed").addClass("active");
			$(".secondary").removeClass("active");
		}
	});
	
	var mytitle = null;
	if (window.params.agentName != null)
		mytitle = window.params.agentName;
	else 
		mytitle = window.params.agentIP;
	$("#agentTitle").append("Agent: "+ mytitle);
});

/*
 * getting a list of all available metrics for this agent
 */
function getAvailableMetrics() {
	$.ajax({ 
		type: "get",
		url: "restAPI/agents/"+window.params.agentID+"/availableMetrics",
		beforeSend: function(req) {
			req.setRequestHeader("Accept", "application/json");
		},
		contentType: "application/json",
		dataType: "json",
		success: populateMetrics
	});
}

/*
 * delegate method for getAvailableMetrics()
 */
function populateMetrics(data) {
	var staticmetrics = [window.params.agentID + ":memTotal", window.params.agentID + ":diskTotal"];
	for(var i in data.metrics) {
		var metric = data.metrics[i];
		if(metric.type == "STRING" || metric.group == "StaticInfo") {
			staticmetrics.push(metric.metricID);
			continue;
		}
		
		window.metricsInfo[metric.metricID] = metric;
		if(!document.getElementById(metric.group)) {
			$("#left_col").append("<div class=\"group\" id=\""+metric.group+"\"></div>");
			$("#"+metric.group).append("<div class=\"group_title\">"+metric.group+"</div>");
		}
		if(!document.getElementById(metric.name)) {
			$("#"+metric.group).append("<div class=\"metric\"><input type=\"checkbox\" value=\""+metric.metricID+"\"/><span>"+metric.name+"</span></div>");
		}
	}
	
	//set static metrics info
	if(staticmetrics.length > 0) {
		$.ajax({
			type: "post",
			url: "restAPI/metrics/",
			beforeSend: function(req) {
				req.setRequestHeader("Accept", "application/json");
			},
			contentType: "text/plain",
			data: staticmetrics + "",
			success: function(data) {
				console.log(data);
				for(var s in data.metrics) {
					var id = data.metrics[s].metricID.split(':')[1];
					var units = window.metricsInfo[window.params.agentID+":"+id]!=null ? window.metricsInfo[window.params.agentID+":"+id].units : "";
					$("#"+id).append("<span class=\"staticinfo_value\">"+data.metrics[s].value+" "+units+"</span>");
				}
			}
		});
	}
	
	
	$("input[type=checkbox]").change(function(e) {
		if($(this).attr("checked")) {
			window.metricList.push($(this).val());
			var id = $(this).val().split(':')[1];
			
			if(!document.getElementById(id)) {
				$("#right_col").append("<div class='container' id='"+id+"' align='center'>"+
										"<div class='header' id='"+id+"Header'><span class='expand'>-</span>"+id+"</div>"+
										"<div class='wrapper' id='"+id+"Wrap'>"+
										"<div align='right'><span>TimeRange: </span>" +
										"<select class='timerange' id='"+$(this).val()+"'>" +
										"<option value='0'>Disabled</option>" +
										"<option value='60'>1 minute</option>" +
										"<option value='120'>2 minutes</option>" +
										"<option value='300'>5 minutes</option>" +
										"<option value='600'>10 minutes</option>" +
										"<option value='1800'>30 minutes</option>" +
										"<option value='3600'>1 hour</option>" +
										"<option value='7200'>2 hours</option>" +
										"<option value='18000'>5 hours</option>" +
										"<option value='36000'>10 hours</option>" +
										"<option value='86400'>1 day</option>" +
										"</select>" +
										"</div>"+
										"<canvas width='800' height='250' id='"+id+"Graph'>[No canvas supported]</canvas>"+
										"</div>"+
										"</div>");
				// first chart drawing
				var mname = $(this).val();

				var m = new Metric(window.metricsInfo[mname].name, window.metricsInfo[mname].units);
				var g = new Graph(m, window.metricsInfo[mname].name+"Graph");
				g.drawGraph();
				window.graphs[window.metricsInfo[mname].name] = g;
				getMetricUpdates([$(this).val()]);
				
				// collapse/expand
				addExpandCollapseFunctionalityTo("#"+id+"Header","Header","Wrap");
				
				// timerange selection
				$("#"+id+"Wrap select.timerange").change(function() {
					
					if( parseInt($(this).val()) != 0) {
						var ind = window.metricList.indexOf($(this).attr("id"));
						window.metricList.splice(ind,1);
						
						$.ajax({ 
							type: "get",
							url: "restAPI/metrics/"+$(this).attr("id")+"?interval="+$(this).val(),
							beforeSend: function(req) {
								req.setRequestHeader("Accept", "application/json");
							},
							contentType: "application/json",
							dataType: "json",
							success: function(data){ 
								console.log(data);
								var gid = data.metricID.split(':')[1];
								window.graphs[gid].data = [];
								window.graphs[gid].timestampObj = new Timestamp();
								if(data.values.length == 0) {
									window.graphs[gid].newValue(null,null);
									window.graphs[gid].newValue(null,null);
								}
								else if(data.values.length > 20) {
									var diff = parseInt(data.values.length / 20);
									for(var i = 0; i < data.values.length; i += diff) {
										window.graphs[gid].newValue(parseFloat(data.values[i].value), data.values[i].timestamp);
									}
								}
								else {
									for(var i in data.values) {
										window.graphs[gid].newValue(parseFloat(data.values[i].value), data.values[i].timestamp);
									}
								}
							},
						});
					}
					else {
						var gid = $(this).attr("id").split(':')[1];
						window.metricList.push($(this).attr("id"));
						window.graphs[gid].timestampObj = new Timestamp();
						window.graphs[gid].data = [];
						window.graphs[gid].newValue(null,null);
						window.graphs[gid].newValue(null,null);
					}
				});
				
			}
		}
		else {
			var ind = window.metricList.indexOf($(this).val());
			window.metricList.splice(ind,1);
			var id = $(this).val().split(':')[1];
			if(document.getElementById(id)) {
				$("#"+id).remove();
			}
			window.graphs[id] = null;
		}
		console.log(window.metricList);
	});

	/**canast02**/
	window.metricsInterval = window.setInterval(function() {getMetricUpdates(window.metricList)},15000);
	/****/
}

/**canast02**/
function intervalChanged(interval) {
	clearInterval(window.metricsInterval);
	window.metricsInterval = window.setInterval(function() {getMetricUpdates(window.metricList)},interval);
}
/****/

/*
 * Request updates for the registed metrics
 */
function getMetricUpdates(list) {
	if(list.length > 0) {
		$.ajax({
			type: "post",
			url: "restAPI/metrics/",
			beforeSend: function(req) {
				req.setRequestHeader("Accept", "application/json");
			},
			contentType: "text/plain",
			data: "metrics=" + list + "",
			success: updateGraphs
		});
	}
}

/*
 * updates the charts
 */
function updateGraphs(data) {
	console.log(data);

	for(var i in data.metrics) {
		var metric = data.metrics[i];
		var name = window.metricsInfo[metric.metricID].name;
		if(!window.graphs[name]) {
			var m = new Metric(name, window.metricsInfo[metric.metricID].units);
			var g = new Graph(m, name+"Graph");
			g.drawGraph();
			window.graphs[name] = g;
		}
		
		window.graphs[name].newValue(parseFloat(metric.value), metric.timestamp);
	}
}
