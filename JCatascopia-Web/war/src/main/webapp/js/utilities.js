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
//clock on the status bar
var intID = window.setInterval(
					function(){
						if(!document.getElementById("clockid")) window.clearInterval(intID);
						$(".active #clockid").each(function() { $(this).html(clock())}); // clock() in utilities.js
					}, 1000
			);

/*
 * adds the hover effect to the menu
 */
function menuEffects() {
	$(".menu_item")
	.mouseenter(function() {
		$(this).css('background-color','#F2D411');
	})
	.mouseleave(function() {
		$(this).css('background-color','blue');
	});
}

/**canast02**/
//getAgents
window.agentsIntervalId = window.setInterval(getAgents,15000);
/****/

/*
 * this function will make a REST request to get
 * a list of the UP/DOWN agents
 * Any page that calls this function must implement the updateContent function
 */
function getAgents(){
	$.ajax({ 
		type: "get",
		url: "restAPI/agents?applicationID=1",
		dataType: "json",
		success:function(data) {
			var dropdown = $("ul.dropdown-menu").html("");
			for(var i in data.agents) {
				var txt = null;
				var link = "singleAgentPage.jsp?agentID="+data.agents[i].agentID+"&agentIP="+data.agents[i].agentIP;
				var agentName = data.agents[i].agentName;
				if ( agentName != null){
					txt = agentName;
					link += "&agentName="+agentName;
				}
				else
					txt = data.agents[i].agentIP;
				
				if(data.agents[i].status == "UP") {
					dropdown.append("<li><a href=\""+link+"\" class=\"vm\">"+txt+"</a></li>");
				}
			}
			if(typeof updateContent == 'function')
				updateContent(data);
		},
		statusCode:{}
	});
}

/*
 * this functions will make a REST request and retrieve
 * the applications subscriptions
 * Any page that calls this function must implement the populateSubscriptions function
 */
function getSubscriptions() {
	$.ajax({ 
		type: "get",
		url: "restAPI/subscriptions",
		dataType: "json",
		success:populateSubscriptions,
		statusCode:{}
	});
}

/*
 * this function returns an array of 
 * the parameters that are contained in a URL address
 */
function getURLParams() {
	var urlArray2={};
	var e,
	a = /\+/g,  // Regex for replacing addition symbol with a space
	r = /([^&=]+)=?([^&]*)/g,
	d = function (s) { return decodeURIComponent(s.replace(a, " ")); },
	q = window.location.search.substring(1);

	while (e = r.exec(q))
		urlArray2[d(e[1])] = d(e[2]);
	return urlArray2;
}

/*
 * this function is used as a clock  
 */
function clock() {
	var now = new Date();
	var minutes=now.getMinutes();
	var seconds=now.getSeconds();
	if (minutes < 10)
		minutes = "0" + minutes;

	if (seconds < 10)
		seconds = "0" + seconds;

	var outStr = now.getHours()+':'+minutes+':'+seconds;
	return outStr;
}

/*
 * allows only numbers in text field
 */
function numericFilter(txb) {
	   txb.value = txb.value.replace(/[^\0-9]/ig, "");
}

/*
 * add a spinner to the element with this id
 */
function addSpinnerToElement(id) {
	// Create the Spinner with options
	var opts = {
		  lines: 15, // The number of lines to draw
		  length: 12, // The length of each line
		  width: 4, // The line thickness
		  radius: 14, // The radius of the inner circle
		  //corners: 1, // Corner roundness (0..1)
		  //rotate: 0, // The rotation offset
		  direction: 1, // 1: clockwise, -1: counterclockwise
		  color: '#000', // #rgb or #rrggbb
		  speed: 1, // Rounds per second
		  trail: 36, // Afterglow percentage
		  shadow: false, // Whether to render a shadow
		  hwaccel: false, // Whether to use hardware acceleration
		  className: 'spinner', // The CSS class to assign to the spinner
		  zIndex: 2e9, // The z-index (defaults to 2000000000)
		  top: 'auto', // Top position relative to parent in px
		  left: 'auto' // Left position relative to parent in px
	};
	new Spinner(opts).spin(document.getElementById(id));
}

/*
 * add the expand and collapse functionality
 */
function addExpandCollapseFunctionalityTo(id,head,content) {
	$(id).click(function() {
		var sec = $(this).attr("id").replace(head,content);
		var cssSel = "#" + $(this).attr("id") + " span";
		$(cssSel).html($(cssSel).html() == "-" ? "+" : "-");
		$("#"+sec).toggle();
	});
}

/*
 * prepare views that are same throughout the whole project
 */
$(document).ready(function() {
	//menu
	$("body").prepend("<section id=\"menu\"></section>");
		//home
		$("#menu").append("<a href=\"home.jsp\"><div class=\"menu_item\">Home</div></a>");
		//agents
		$("#menu").append("<a href=\"agentPage.jsp\"><div class=\"menu_item\" data-dropdown=\"#agents-dropdown\">Agents</div></a>");
			//dropdown
			$("#menu").append("<!-- dropdown content -->"+
								"<div id=\"agents-dropdown\" class=\"dropdown dropdown-tip has-icons\">"+
								    "<ul class=\"dropdown-menu\">"+
										"<!-- javascript generated -->"+
								    "</ul>"+
								"</div><!-- /dropdown content -->");
		//subscriptions
		$("#menu").append("<a href=\"subPage.jsp\"><div class=\"menu_item\">Subscriptions</div></a>");
		
		/**canast02**/
		//sampling interval
		$("#menu").append("<div class=\"menu_item_right\">Sampling Interval(secs): <input type=\"text\" id=\"sampleInterval\" value=\"15\"/><input type=\"submit\" id=\"samplingBtn\" value=\"Go\"/></div>")
		
		$("#samplingBtn").click(function(){
			clearInterval(window.agentsIntervalId);
			var interval = parseInt(document.getElementById("sampleInterval").value) * 1000;
			window.agentsIntervalId = window.setInterval(getAgents, interval);
			
			intervalChanged(interval);
		});
		/****/
	
		/**canast02**/
		//header
		$("body").prepend("<header style=\"height:105px;overflow: hidden;\"><a href=\"home.jsp\"><img src=\"img/jcatascopia_logo1.png\" height=\"60px\" style=\"margin: 0px;\"/></a><img src=\"img/cellar_logo_small_05.png\" height=\"80px\" style=\"margin: 0px;\"/></header>");
		/****/
	
	//footer
	$("body").append("<footer><hr/></footer>");
		//copyright
		$("footer").append("<span style=\"font-style: italic;\">Copyright &copy; CELAR 2013</span>");
		//about
		$("footer").append("<span style=\"position: absolute; right: 5px; bottom: 2px; z-index: 10;\"><a href=\"about.jsp\" target=\"_blank\">About</a></span>");
		//sponsors
		$("footer").append("<div id=\"sponsors\" style=\"position: relative; margin: auto;\"></div>");
			//cofounders
			$("footer #sponsors").append("<div class=\"sponsor_group\" id=\"cofounders\"><div>Co-funded by:</div></div>")
				//european union
				$("footer #sponsors #cofounders").append("<div class=\"sponsor\"><img src=\"img/european_union_logo.png\"/></div>");
				//seventh framework
				$("footer #sponsors #cofounders").append("<div class=\"sponsor\"><img src=\"img/seventh_framework_programme_logo_svg.png\"/></div>");
			//developers
			$("footer #sponsors").append("<div class=\"sponsor_group\" id=\"developers\"><div>Developed by:</div></div>")
				//ucy
				$("footer #sponsors #developers").append("<div class=\"sponsor\"><a href=\"http://www.ucy.ac.cy/\" target=\"_blank\"><img src=\"img/ucy.png\"/></a></div>");

	menuEffects();
	getAgents();
});
