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
