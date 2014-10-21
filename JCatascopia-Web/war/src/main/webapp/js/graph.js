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
function Metric(yaxisTitle, yaxisUnits) {
	this.key = yaxisTitle;
	this.yTitle = yaxisTitle + "(" + yaxisUnits + ")";
}

/**canast02**/
function Tooltip(timestamp,value){
	this.timestamp = timestamp;
	this.value = value;
	
	this.getTooltip = function() {
		if(timestamp != null)
			return "Time: " + timestamp + "<br/>Value: " + value;
		return this.value + "";
	}
}
/****/

function Graph(metric, id) {
	this.id = id;
	this.data = [];
	this.tooltips = [];
	this.tooltips.push("init");
	this.data.push(null);
	this.newVal = 0;
	this.MAX_LENGTH = 39;
	this.timestampObj = new Timestamp(this.MAX_LENGTH+1);
	
	this.graph = null;

	this.getGraph = function(id, data) {
		if (!this.graph) {
			this.graph = new RGraph.Line(id, data)
			.Set('chart.xticks', this.MAX_LENGTH/2 + 1)
			.Set('chart.background.barcolor1','white')
			.Set('chart.background.barcolor2','white')
			.Set('chart.title.xaxis','Time >>>')
			.Set('chart.title.yaxis',metric.yTitle)
			.Set('chart.title.vpos', 0.5)
			.Set('chart.title.yaxis.pos', 0.5)
			.Set('chart.title.xaxis.pos', 0.2)
			.Set('chart.colors', [ 'blue' ])
			.Set('chart.linewidth', 4)
			.Set('chart.yaxispos', 'right')
			.Set('chart.gutter.right', 70)
			.Set('chart.gutter.bottom', 40)
			.Set('chart.tickmarks','circle')
			.Set('filled', true)
			.Set('chart.fillstyle', ['rgba(0,0,255,0.3)'])
			.Set('chart.filled.accumulative', false);
		}
		return this.graph;
	};
	
	this.drawGraph = function () {
		var _RG = RGraph;
		RGraph.Clear(document.getElementById(this.id));
		
		this.getGraph(this.id, this.data);
		this.graph.Draw();
		
		if (this.data.length > this.MAX_LENGTH) {
			this.data = _RG.array_shift(this.data);
		}

		if (ISIE8) {
			alert('[MSIE] Sorry, Internet Explorer 8 is not fast enough to support animated charts');
		} else {
			this.graph.original_data[0] = this.data;
		}
	};
	
	this.newValue = function(value,timestamp) {
		if(this.timestampObj.newTimestamp(timestamp) == true) {
			this.newVal = value;
			this.data.push(this.newVal);
			this.tooltips.push(new Tooltip(timestamp,value).getTooltip());
			if(this.tooltips.length > this.MAX_LENGTH+1) {
				this.tooltips.splice(1,1);
			}
			this.graph.Set('chart.tooltips', this.tooltips);
			/****/
			this.graph.Set('chart.labels', this.timestampObj.timestamps);
			this.drawGraph();
		}
	}
}

function Timestamp(maxlen) {
	this.timestamps = [null];
	this.space = 0;
	this.timestampsNum = 0;
	
	this.newTimestamp = function(timestamp) {
		if(this.timestampsNum > 0 &&
				this.timestamps[this.timestamps.length-1] != null &&
				timestamp == this.timestamps[this.timestamps.length-1]) return false;
		
		if(this.timestampsNum < (maxlen/4) && (this.space >= 3 || this.timestampsNum == 0)) {
			this.timestamps.push(timestamp);
			this.space = 0;
			this.timestampsNum++;
		}
		else {
			this.space++;
			this.timestamps.push(null);
		}

		if(this.timestamps.length > maxlen) {
			if(this.timestamps[1] != null) this.timestampsNum--;
			this.timestamps.splice(1, 1);
		}
		return true;
	}
}
