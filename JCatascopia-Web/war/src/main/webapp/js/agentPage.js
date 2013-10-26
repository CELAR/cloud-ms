$(document).ready(function() {
	addExpandCollapseFunctionalityTo(".header", "Header", "Content");
	addSpinnerToElement("agentsContent");
	window.initialized = false;
//	getAgents();
});

//updates the agents list
function updateContent(json) {
	console.log(json);
	if(!window.initialized) {
		$("#agentsContent").html("");
		for(var i in json.agents) {
			$("#agentsContent").append("<a href=\"singleAgentPage.jsp?agentID="+json.agents[i].agentID+"&agentIP="+json.agents[i].agentIP+"\">" +
					"						<div class=\"instance\" align=\"center\">"+
												"<img src=\"img/vm_run.png\" height=\"100\"><br/><span>"+json.agents[i].agentIP+"</span>" +
											"</div>" +
										"</a>");
		}
		window.initialized = true;
		
		$("#agentsContent .instance")
		.mouseenter(function() {
			$(this).css("background-color","rgba(112,211,250,0.25)");
			$(this).css("border","1px solid blue");
		})
		.mouseleave(function() {
			$(this).css("background-color","");
			$(this).css("border","1px solid white");
		});
	}
}