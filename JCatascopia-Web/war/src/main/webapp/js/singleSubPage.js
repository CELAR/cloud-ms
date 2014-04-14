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
$(document).ready(function() {
	var params = getURLParams();
	window.subID = params.subID;
	$.ajax({ 
		type: "get",
		url: "restAPI/subscriptions/"+params.subID,
		beforeSend: function(req) {
			req.setRequestHeader("Accept", "application/json");
		},
		contentType: "application/json",
		dataType: "json",
		success: function(data) {
			console.log(data);
			for(var i in data) {
				if(i == "func") {
					$("#"+i).append("<span class=\"staticinfo_value\">"+data[i]+" ("+data["originMetric"]+")</span>");
				}
				else {
					$("#"+i).append("<span class=\"staticinfo_value\">"+data[i]+"</span>");
				}
			}
			$("#subscriptionHeader").append(data["subName"]);
			window.timerangeOn = false;
			initializeGraph(data);
		},
		statusCode: {
			404: function() {
				alert("Could not find a subscription with this id.");
			}
		}
	});

	getSubAgents(params.subID);
	
	addExpandCollapseFunctionalityTo(".header","Header","Wrap");
	
	/**canast02**/
	window.params = params;
	window.subAgentsInterval = window.setInterval(function() {getSubAgents(params.subID);}, 15000);
	/****/
});

/**canast02**/
function intervalChanged(interval) {
	clearInterval(window.subAgentsInterval);
	window.subAgentsInterval = window.setInterval(function() {getSubAgents(window.params.subID);}, interval);
	clearInterval(window.graphInterval);
	window.graphInterval = window.setInterval(function(){getUpdates(window.data.metricID);}, interval);
}

/****/

//initializes the chart
function initializeGraph(data) {
	var metric = new Metric(data["func"]+":"+data["originMetric"], data.metricUnits);
//	var metric = new Metric(data.subName, data.metricUnits);
	var graph = new Graph(metric, "subscriptionGraph");
	graph.drawGraph();
	window.graph = graph;
	
	getUpdates(data.metricID);
	
	/**canast02**/
	window.data = data;
	window.graphInterval = window.setInterval(function(){getUpdates(data.metricID);}, 15000);
	/****/
	
	$("select.timerange").change(function() {
		if($(this).val() == 0) {
			console.log("disabled timerange");
			window.timerangeOn = false;
			window.graph.timestampObj = new Timestamp();
			window.graph.data = [];
			window.graph.newValue(null,null);
			window.graph.newValue(null,null);
			getUpdates(data.metricID);
		}
		else {
			console.log("enabled timerange")
			window.timerangeOn = true;
			
			$.ajax({ 
				type: "get",
				url: "restAPI/metrics/"+data.metricID+"?interval="+$(this).val(),
				beforeSend: function(req) {
					req.setRequestHeader("Accept", "application/json");
				},
				contentType: "application/json",
				dataType: "json",
				success: function(data){ 
					console.log(data);
					window.graph.timestampObj = new Timestamp();
					window.graph.data = [];
					if(data.values.length == 0) {
						window.graph.newValue(null,null);
						window.graph.newValue(null,null);
					}
					else if(data.values.length > 20) {
						var diff = parseInt(data.values.length / 20);
						console.log(diff);
						for(var i = 0; i < data.values.length; i += diff) {
							window.graph.newValue(parseFloat(data.values[i].value), data.values[i].timestamp);
						}
					}
					else {
						for(var i in data.values) {
							window.graph.newValue(parseFloat(data.values[i].value), data.values[i].timestamp);
						}
					}
				},
			});
		}
	});
}

//ajax request for receiving metric updates
function getUpdates(metricID) {
	if(!window.timerangeOn)
		$.ajax({
			type: "post",
			url: "restAPI/metrics/",
			beforeSend: function(req) {
				req.setRequestHeader("Accept", "application/json");
			},
			contentType: "text/plain",
			data: metricID,
			success: updateGraph
		});

}

//updates the chart
function updateGraph(data) {
	console.log(data);
	window.graph.newValue(parseFloat(data.metrics[0].value), data.metrics[0].timestamp);
}

//updates the agents dropdown menu
function updateContent(json) {
	var unsub = $("#unsubscribedAgents").html("");
	for(var i in json.agents) {
		if(json.agents[i].status == "UP") {
			if(!document.getElementById(json.agents[i].agentID)) {
				unsub.append(getAgentDiv("add", json.agents[i].agentID, json.agents[i].agentIP));
				$("#"+json.agents[i].agentID+" .add").click(function() {
					var agentID = $(this).parent().attr("id");
					addAgent(window.subID, agentID);
				});
			}
		}
	}
}

//lists the subscription agents
function getSubAgents(subID) {
	$.ajax({ 
		type: "get",
		url: "restAPI/subscriptions/"+subID+"/agents",
		beforeSend: function(req) {
			req.setRequestHeader("Accept", "application/json");
		},
		contentType: "application/json",
		dataType: "json",
		success: function(data) {
			console.log(data);
			var div = $("#subscribedAgents").html("");
			for(var i in data.agents) {
				if(document.getElementById(data.agents[i].agentID)) {
					$("#"+data.agents[i].agentID).remove();
				}
				div.append(getAgentDiv("remove", data.agents[i].agentID, data.agents[i].agentIP));
				$("#"+data.agents[i].agentID+" .remove").click(function(){
					var agentID = $(this).parent().attr("id");
					removeAgent(subID, agentID);
				});
			}
		}
	});
}

//returns the agent id with the specified type, id and label
function getAgentDiv(type, id, label) {
	return 	"<div class=\"agent\" id='"+id+"'>"+
				"<div class=\"container\">"+
					"<div class=\"label\">"+label+"</div>"+
				"</div>"+
				"<div class=\""+type+"\">"+
					"<div>"+(type=="remove" ? "-" : "+")+"</div>"+
				"</div>"+
			"</div>"
}

//moves the agent div to subscribed agents
function addAgent(subID, agentID) {
	$.ajax({
		type: "get",
		url: "restAPI/subscriptions/"+subID+"/agent/"+agentID+"?action=addAgent",
		beforeSend: function(req) {
			req.setRequestHeader("Accept", "application/json");
		},
		contentType: "application/json",
		dataType: "json",
		success: function(data) {
			console.log(data.status);
			var div = getAgentDiv("remove", agentID, $("#"+agentID+" .container .label").text());
			$("#"+agentID).remove();
			$("#subscribedAgents").append(div);
			$("#"+agentID).click(function() {
				removeAgent(subID, agentID);
			});
		}
	});
}

//moves the agent div to unsubscribed agents
function removeAgent(subID, agentID) {
	$.ajax({
		type: "get",
		url: "restAPI/subscriptions/"+subID+"/agent/"+agentID+"?action=removeAgent",
		beforeSend: function(req) {
			req.setRequestHeader("Accept", "application/json");
		},
		contentType: "application/json",
		dataType: "json",
		success: function(data) {
			console.log(data.status);
			$("#unsubscribedAgents").append(getAgentDiv("add", agentID, $("#"+agentID+" .container .label").text()));
			$("#"+agentID).remove();
			$("#"+agentID).click(function() {
				addAgent(subID, agentID);
			});
		}
	});
}
