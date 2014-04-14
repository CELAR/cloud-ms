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
	addSpinnerToElement("subListContent");
	
	getAgents();
	getSubscriptions();
	getMetrics();
	menuEffects();
	
	addExpandCollapseFunctionalityTo(".header", "Header", "Content");
	
	$("#removeBtn").click(removeBtnFunc);
	
	$("#submitBtn").click(function() {
		var valid = true;
		$("#createView input[type=text]").each(function() {
			if($(this).val() == "") {
				valid = false;
				$(this)
				.addClass('invalid')
				.change(function() {
					if($(this).val() != "") $(this).removeClass('invalid');
				});
			}
		});
		$("#createView select").each(function() {
			var selected = $("#"+$(this).attr("id")+" option:selected");
			if(selected.length == 0 || selected.val() == -1) {
				valid = false;
				$(this)
				.addClass('invalid')
				.change(function() {
					if($(this).val() != "") $(this).removeClass('invalid');
				});
			}
		})
		
		if(!valid) {
			console.log("form is not valid");
			return false;
		}
		console.log("form is valid. proceed to submission");
		$("#overlay").show();
		var metric = {};
		metric.name = $("#subNameTxt").val();
		metric.val = $("#func_select").val() + ":" + $("#metricName_select option:selected").text();
		metric.type = $("#metricType_select option:selected").val();
		metric.units = $("#metricUnitsTxt").val();
		metric.group = $("#metricGroupTxt").val();
		metric.period = $("#periodTxt").val();
		var agents = [];
		$("#agents_select option:selected").each(function() {
			agents.push($(this).val());
		});
		metric.agents = agents;
		
		$.ajax({
			type: 'put',
			url:"restAPI/subscriptions/",
			beforeSend: function(req) {
				req.setRequestHeader("Accept", "application/json");
			},
			contentType: "text/plain",
			data: "{\"metric\":"+JSON.stringify(metric)+"}",
			success: function(data) {
				console.log(data);
				if(data.status == "added") {
					alert("Subscription added successfully!");
					var subObj = {
						subName: metric.name,
						subID: data.subID
					};
					$("#subList ul").append(getSubListObj(subObj));
					$("ul li")
					.mouseover(function() {
						$(this).addClass("hovered");
					})
					.mouseout(function() {
						$(this).removeClass("hovered");
					});
					
					$("input[type=text]").each(function() {
						$(this).val("");
					});
					$("#createView select option:selected").each(function() {
						$(this).removeAttr("selected");
					});
					$("#metricRealType").val("");
				}
				else {
					alert("There was an error while adding subscription!");
				}
				$("#overlay").hide();
			},
			statusCode: {
				500: function() {
					alert("Could not add subscription. Try again!");
					$("#overlay").hide();
				}
			},
			error: function() {
				console.log("sdf");
			}
		});
	});
});

function removeBtnFunc(){
	$("#subList input:checked").each(function(index) {
		var id = $(this).attr("id");
		var li = $(this).parent();
		$.ajax({
			type:"delete",
			url:"restAPI/subscriptions/"+id,
			success: function(data) {
				console.log("subscription "+id+" deleted");
				li.remove();
			},
			statusCode: {
				404: function() {
					alert("Could not delete subscription "+id);
					$("#"+id).removeAttr("checked");
				}
			}
		});		
	});
}

//updates the agents list
function updateContent(json) {
	console.log(json);
	if(!window.initialized) {
		for(var i in json.agents) {
				$("#agents_select").append("<option value=\""+json.agents[i].agentID+"\">"+json.agents[i].agentIP+"</option>");
		}
		window.initialized = true;
	}
}

//populates subscriptions list
function populateSubscriptions(data) {
	$("#subListContent").html("<ul></ul>");
	//$("#subListContent").append("<div style=\"padding-left: 11px; margin-top: -5px; margin-bottom: 10px;\"><input type=\"button\" value=\"Remove Checked\" id=\"removeBtn\" /></div>");
	for(var i in data.subs) {
		$("#subList ul").append(getSubListObj(data.subs[i]));
	}
	
	$("ul li")
	.mouseover(function() {
		$(this).addClass("hovered");
	})
	.mouseout(function() {
		$(this).removeClass("hovered");
	});
}

// returns a customized LI that represents a subscription in the list
function getSubListObj(subObj) {
	return 	"<li>"+
				"<input type=\"checkbox\" id=\""+subObj.subID+"\"/>"+
				"<a href=\"singleSubPage.jsp?subID="+subObj.subID+"\"><span class=\"subName\">"+subObj.subName+"</span></a>"+
				"<span class=\"hover_text\">(Click to view subscription details)</span>"+
			"</li>";
}

// retrieves and lists all available metrics
function getMetrics() {
	$.ajax({
		type: 'get',
		url: 'restAPI/metrics/subscriptions',
		beforeSend: function(req) {
			req.setRequestHeader("Accept", "application/json");
		},
		success: function(data) {
			console.log(data);
			for(var i in data.metrics) {
				$("#metricName_select optgroup").append(getMetricObj(data.metrics[i]));
			}
		}
	});
	$("#metricName_select").change(function() {
		var sel = $("#metricName_select option:selected");
		if(sel.val() == -1) {
			$("#metricRealType").text("");
			$("#metricRealUnits").text("");
		}
		else {
			var type = sel.val().split("/")[0];
			var units = sel.val().split("/")[1];
			$("#metricRealType").text("Type: " + type);
			$("#metricRealUnits").text("Units: " + units)
		}
	});
}

// returns a ready metric name option to append
function getMetricObj(metric) {
	return "<option value=\""+metric.type+"/"+metric.units+"\">"+metric.name+"</option>";
}
