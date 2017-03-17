
var col_ind = 0;


function getParam ( sname )
{
	var params = location.search.substr(location.search.indexOf("?")+1);
	var sval = "";
	params = params.split("&");
	// split param and value into individual pieces
	for (var i=0; i<params.length; i++)
	{
		temp = params[i].split("=");
		if ( [temp[0]] == sname ) { sval = temp[1]; }
	}
	return sval;
}

function getRandomColor() {
	var letters = '0123456789ABCDEF'.split('');
	var color = '#'; encoding
	for (var i = 0; i < 6; i++ ) {
		color += letters[Math.floor(Math.random() * 16)];
	}
	return color;
}

function getRandomSoftColor() {
	var color = '#';

	switch(col_ind % 3){

	case 0:
		color = color + "b6cee7"
		break;
	case 1:
		color = color + "b6e6dc"
		break;
	case 2:
		color = color + "99badd"
		break;
	}
	col_ind ++;
	return color;
}

function write_html_multimedia(multimedia_content,phases){

	jQuery.ajaxSetup({async:false});

	var i = 0;

	for(var j=0; j<phases.length; j++){

		$("body").append("<div style=\"background-color:" + getRandomSoftColor() + "\" id=\"" + phases[j]["phase"] + "\"><center><font size=\"10\" color=\"black\">" + phases[j]["phase"] + "</font></center></div>");

		//console.log("we are in phase " + phases[j]["phase"])
		console.log("index: " + i + " multimedia_content length " + multimedia_content.length)

		while(i < multimedia_content.length && multimedia_content[i]["time"] <= phases[j]["end"])
		{

			//console.log("multimedia " + multimedia_content[i]["time"] + " inserted in phase " + phases[j]["start"] + " " + phases[j]["end"])

			var myDate = new Date( multimedia_content[i]["time"]);

			if(multimedia_content[i]["type"] == "text"){

				$.get("multimedia/"+session+"/"+multimedia_content[i]["id"], function(content) {

					$("#"+phases[j]["phase"]).append("<font size=\"4\" color=\"black\"> time: " + myDate.toGMTString() + " </font>");
					$("#"+phases[j]["phase"]).append("<font size=\"4\" color=\"black\">  "+content+"</font>");
					$("#"+phases[j]["phase"]).append("<p></p>");
				}).fail(function(content) {	 //try twice  
					console.log("retry ","multimedia/"+session+"/"+multimedia_content[i]["id"])
					$("#"+phases[j]["phase"]).append("<p ><font size=\"4\" color=\"black\">"+content+"</font></p>");
				});
			}else   
				if(multimedia_content[i]["type"] == "image"){

					if((multimedia_content[i]["view"] == "workspace" && only_workspace == true) || only_workspace == false){

						$("#"+phases[j]["phase"]).append("<font size=\"4\" color=\"black\"> time: " + myDate.toGMTString() + " </font>");
						$("#"+phases[j]["phase"]).append("<img width=\"800\" height=\"600\" src=\"" + multimedia_content[i]["data"] + "\"</img>");
						$("#"+phases[j]["phase"]).append("<p></p>");
					}

				}

			i ++;
		}

	}
}

function create_story(){

	$.getJSON("/pelars/phase/"+session, function(parsed_phases) {

		if(only_workspace == false){

			$.getJSON("/pelars/multimedia/"+session, function(parsed_muiltimedia) {
				parsed_multimedia.sort(function(a,b){return parseDouble(a.time) - parseDouble(b.time);});
				write_html_multimedia(parsed_muiltimedia, parsed_phases);
			});
		}
		else{
			$.getJSON("/pelars/multimedia/"+session+"/image", function(parsed_muiltimedia) {
				parsed_multimedia.sort(function(a,b){return parseDouble(a.time) - parseDouble(b.time);});
				write_html_multimedia(parsed_muiltimedia, parsed_phases);
			});
		}
	});

}

var session = getParam("session")
var only_workspace = false;
only_workspace = Boolean(getParam("table"))
if(session == ""){
	alert("call with session as parameter")
}else{
	create_story();
}



