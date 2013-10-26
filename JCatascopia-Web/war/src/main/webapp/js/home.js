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
		if(json.agents[i].status == "UP") {
			agentsUP++;
			panel = $("#runningVMsContent");
			var link = "singleAgentPage.jsp?agentID="+json.agents[i].agentID+"&agentIP="+json.agents[i].agentIP;
			panel.append("<a href=\""+link+"\"><div class=\"instance\">"+
						"<img alt=\"vm\" src=\"img/vm_run.png\" width=\"64\" height=\"64\"><br />"+
						"<span>"+json.agents[i].agentIP+"</span>"+
				 		"</div></a>");
		}
		else if(json.agents[i].status == "DOWN") {
			panel = $("#stoppedVMsContent");
			panel.append("<div class=\"instance\">"+
						"<img alt=\"vm\" src=\"img/vm_down.png\" width=\"64\" height=\"64\"><br />"+
						"<span>"+json.agents[i].agentIP+"</span>"+
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
	
	window.graph.newValue(agentsUP,null);
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