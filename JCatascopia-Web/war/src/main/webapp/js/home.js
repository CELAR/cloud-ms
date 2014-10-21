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
	addSpinnerToElement("spinner1");
	addSpinnerToElement("spinner2");
	addSpinnerToElement("spinner3");
	
	addExpandCollapseFunctionalityTo(".header","Header","Content");
	
	var metric = new Metric("Running","VMs");
	var graph = new Graph(metric, "runningVMsGraph");
	graph.newVal = 0;
	graph.drawGraph();
	window.graph = graph;
	
	getSubscriptions();
	/**canast02**/
	window.subInterval = window.setInterval(getSubscriptions, 15000);
	/****/
	
	window.agentMap = {};
	window.agentMapDOWN = {};
	
	$("#consoleDiv").bind("consoleEvt", 
			function(e,par){
				var msg = par.Param1;
				
				var len=$("#consoleDiv").children().length;
				if (len > 20)
					$('#consoleDiv').find("div:lt(2)").html("");	
				
				$("#consoleDiv").append("<div id=\"console_row\" style=\"padding:3px;\"> &gt; " + msg +"</div>");
				$("#consoleDiv").animate({ scrollTop: $("#consoleDiv").prop("scrollHeight") - $('#consoleDiv').height() }, 1000);
			}
		);	
	
	clusterGetAgents();
	window.clusterInterval = window.setInterval(clusterGetAgents, 15000);
	$("#clusterND").hide();
});

/**canast02**/
function intervalChanged(interval) {
	console.log(window.subInterval + " " + interval);
	clearInterval(window.subInterval);
	window.subInterval = window.setInterval(getSubscriptions, interval);
}
/****/

/*
 * delegate method for getAgents()
 */
function updateContent(json){
	console.log(json);

	var agentsUP = 0;
	var panel;
	$("#runningVMsContent").html("");
	$("#stoppedVMsContent").html("");
	for(var i in json.agents) {
		
		var txt = null;
		var link = "singleAgentPage.jsp?agentID="+json.agents[i].agentID+"&agentIP="+json.agents[i].agentIP;
		var agentName = json.agents[i].agentName;
		if ( agentName != null){
			txt = agentName;
			link += "&agentName="+agentName;
		}
		else
			txt = json.agents[i].agentIP;
		
		if(json.agents[i].status == "UP") {
			agentsUP++;
			panel = $("#runningVMsContent");
			panel.append("<a href=\""+link+"\"><div class=\"instance\">"+
						"<img alt=\"vm\" src=\"img/vm_run.png\" width=\"64\" height=\"64\"><br />"+
						"<span>"+txt+"</span>"+
				 		"</div></a>");
		}
		else if(json.agents[i].status == "DOWN") {
			panel = $("#stoppedVMsContent");
			panel.append("<div class=\"instance\">"+
						"<img alt=\"vm\" src=\"img/vm_down.png\" width=\"64\" height=\"64\"><br />"+
						"<span>"+txt+"</span>"+
				 		"</div>");
		}
	}
	$("#runningVMsContent .instance")
	.mouseenter(function() {
		$(this).css("background-color","rgba(112,211,250,0.25)");
		$(this).css("border","1px solid blue");
	})
	.mouseleave(function() {
		$(this).css("background-color","");
		$(this).css("border","1px solid white");
	});
	
	window.graph.newValue(agentsUP,clock());
}

/*
 * delegate method for getSubscriptions()
 */
function populateSubscriptions(json) {
	console.log(json);
	var panel = $("#subscriptionsContent").html("");
	for(var i in json.subs) {
		var link = "singleSubPage.jsp?subID="+json.subs[i].subID;
		panel.append("<a href=\""+link+"\"><div class=\"instance\" style=\"padding: 2px 0px;\">"+
				"<img alt=\"vm\" src=\"img/sub.png\" width=\"64\" height=\"64\"><br />"+
				"<span>"+json.subs[i].subName+"</span>"+
		 		"</div></a>");
	}
	$("#subscriptionsContent .instance")
	.mouseenter(function() {
		$(this).css("background-color","rgba(112,211,250,0.25)");
		$(this).css("border","1px solid blue");
	})
	.mouseleave(function() {
		$(this).css("background-color","");
		$(this).css("border","1px solid white");
	});
}

function clusterView(json){
	console.log(JSON.stringify(json));
	$("#clusterWT").html("");
	$("#cvNotDefined").html("");
	$("#clusterND").hide();
	
	var clusters = {};
	
	for(var i in json.agents){
		var txt = null;
		var link = "singleAgentPage.jsp?agentID="+json.agents[i].agentID+"&agentIP="+json.agents[i].agentIP;
		var agentName = json.agents[i].agentName;
		if (agentName != null){
			txt = agentName;
			link += "&agentName="+agentName;
		}
		else
			txt = json.agents[i].agentIP;
		
		if (json.agents[i].status == "UP") {
			
			var tags = json.agents[i].tags;
			if (tags != null){
				//for now only take the first tag
				tags = tags.split(",")[0];
				if (!(tags in clusters)){
					clusters[tags] = tags;
					$("#clusterWT").append("<h2 style=\"text-align:left;border-bottom:2px solid #ddd;padding:2px 10px 0px 10px;width:95%;\">"+"Cluster: "+tags+"</h2><div id=\"cv"+tags+"\" style=\"text-align:left;width:95%;overflow:auto;padding:5px 10px 5px 10px; border: 1px solid #ddd;\"></div>");
				}
			}
			else{
				$("#clusterND").show();
				tags = "NotDefined";
			}
			panel = $("#cv"+tags);
			panel.append("<a href=\""+link+"\"><div class=\"instance\" style=\"width:12%\">"+
						"<img alt=\"vm\" src=\"img/vm_run.png\" width=\"64\" height=\"64\"><br />"+
						"<span>"+txt+"</span>"+
				 		"</div></a>");
			
			var k = json.agents[i].agentID;
			var v = json.agents[i].agentIP;
			if (!(k in window.agentMap)){
				window.agentMap[k] = v;
				if (k in window.agentMapDOWN)
					msg = "<b>AGENT RECONNECTED</b> with ";
				else
					msg = "<b>AGENT ADDED</b> with ";
				if (txt != v && txt != null)
					msg += " name: "+txt;
				msg += " ip: "+v+", id: "+k;
				consoleView("["+clock()+"]" + " " + msg);
			}
		}
		else if (json.agents[i].status == "DOWN") {
			var k = json.agents[i].agentID;
			var v = json.agents[i].agentIP;
			if (k in window.agentMap){
				delete window.agentMap[k];
				window.agentMapDOWN[k] = v;
				msg = "<b>AGENT DOWN</b> with ";
				if (agentName != v && agentName != null)
					msg += "name: "+agentName;
				msg += " ip: "+v+", id: "+k;
				consoleView("["+clock()+"]" + " " + msg);
			}
		}
		else{
			var k = json.agents[i].agentID;
			var v = json.agents[i].agentIP;
			if (k in window.agentMapDOWN){
				delete window.agentMapDOWN[k];
				msg = "<b>AGENT REMOVED</b> with ";
				msg += "ip: "+v+", id: "+k;
				consoleView("["+clock()+"]" + " " + msg);
			}
		}
	}
}

function clusterGetAgents(){
	$.ajax({ 
		type: "get",
		url: "restAPI/agents?applicationID=1",
		dataType: "json",
		success: clusterView,
		statusCode:{}
	});
}


function consoleView(msg){
	$("#consoleDiv").trigger("consoleEvt",{Param1:msg});
}
