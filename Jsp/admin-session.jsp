<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="icon" type="image/png" href="pelarslogo.png" sizes="16x16">
<link rel="stylesheet" type="text/css" href="datatables/media/css/jquery.dataTables.css ">
<title>Administrator page</title>
<script type="text/javascript"
	src="jquery/dist/jquery.min.js""></script>
	<script type="text/javascript" charset="utf8" src="datatables/media/js/jquery.dataTables.min.js"></script>
<script src="cookie-handler.js" type="text/javascript"></script>
<link rel="stylesheet" type="text/css" href="style.css">
<style>
body {
	
}
</style>

<%@ page import="authorization.TokenService"%>
<%@ page import="authorization.Token"%>
<%
String token = null;
Cookie[] cookies = request.getCookies();
String validity = "not valid";
String role = "";
String ip = "";
boolean bo = false;
if(cookies !=null){
	for(Cookie cookie : cookies){
	    if(cookie.getName().equals("token")) {
		    token = cookie.getValue();
			Token tok = new Token(token);
			role = tok.getRole();
	    }
	}
}

String all = request.getParameter("all");

%>

</head>


<script type="text/javascript">
	var json;
	var newwin;
	var current = 0;
	var u_role = "<%=role%>"
	var p_all = "<%=all%>"
	
	var fetch_url = ""
	
	fetch_url = (p_all == "true") ? "/pelars/session" : "/pelars/goodsession"
	$(document)
			.ready(
					function() {
						$
								.getJSON(
										fetch_url,
										function(json) {
											var tr = "<thead>";
											tr = tr.concat("<tr>")
											tr = tr.concat("<th>" + "Session"
													+ "</th>");
											tr = tr.concat("<th>" + "User"
													+ "</th>");
											tr = tr.concat("<th>"
													+ "Institution" + "</th>");
											tr = tr.concat("<th>" + "Start"
													+ "</th>");
											tr = tr.concat("<th>"
													+ "Duration in seconds"
													+ "</th>");
											tr = tr.concat("<th>"
													+ "Description" + "</th>");
											tr = tr.concat("<th>" + "Score"
													+ "</th>");
											tr = tr.concat("<th>" + "Update"
													+ "</th>");
											tr = tr.concat("<th>" + "View"
													+ "</th>");
											tr = tr.concat("<th>" + "Visualization"
													+ "</th>");
											tr = tr.concat("<th>" + "Story"
													+ "</th>");
											
											if(u_role == "administrator"){
											tr = tr.concat("<th>" + "Delete"
													+ "</th>");
											}
											tr = tr.concat("</tr></thead><tbody>");
											for ( var i = 0; i < json.length; i++) {
												tr = tr.concat("<tr>")
												tr = tr.concat("<td>"
														+ json[i].session
														+ "</td>");
												tr = tr.concat("<td>"
														+ json[i].user
														+ "</td>");
												tr = tr
														.concat("<td>"
																+ json[i].institution_name
																+ "</td>");
											 // The 0 there is the key, which sets the date to the epoch
											var d = new Date(parseInt(json[i].start));
												tr = tr.concat("<td>"
														+ d
														+ "</td>");
												var duration = json[i].duration;
												if (duration == null) {
													duration = "not closed";
												} else {
													duration = Math
															.floor(duration / 1000);
												}

												var desc = "";
												var score = "";
												if (json[i].description == null) {
													desc == "No description";
												} else {
													desc = json[i].description;
												}

												if (json[i].score == null) {
													score == "No score";
												} else {
													score = json[i].score;
												}

												tr = tr.concat("<td>"
														+ duration + "</td>");
												tr = tr.concat("<td>" + desc
														+ "</td>");
												tr = tr.concat("<td>" + score
														+ "</td>");

												tr = tr
														.concat("<td><button onclick=\"return update("
																+ json[i].session
																+ ");\">\
						Change description and score</button></td>");

												tr = tr
														.concat("<td><button onclick=\"return viewData(this,"
																+ (json[i].session)
																+ ");\">\
								View data table</button></td>");
											if((json[i].duration == null) == false){
												tr = tr
														.concat("<td><button onclick=\"return vizData(this,"
																+ (json[i].session)
																+ ");\">\
						Visualize</button></td>");
													}
													else{
														tr = tr.concat("<td></td>");
													}
											if((json[i].duration == null) == false){
												tr = tr
														.concat("<td><button onclick=\"return vizStory(this,"
																+ (json[i].session)
																+ ");\">\
						Story</button></td>");
													}
													else{
														tr = tr.concat("<td></td>");
													}
											
											//the following lines only if admin
											if(u_role == "administrator"){
												tr = tr
														.concat("<td><button onclick=\"return deleteSession(this,"
																+ (json[i].session)
																+ ");\">\
								Delete</button></td>");
											}

												tr = tr.concat("</tr>");
											}
											
											tr = tr.concat("</tbody>");

											if (tr != "")
												document
														.getElementById("table").innerHTML = tr;
											else
												document
														.getElementById("table").innerHTML = "<tr> You have no sessions</tr>";
														
														$("table").dataTable();

											loadScroll();

										});
					});

	function update(i) {

		newwin = window.open("score-description.html", "",
				"width=800, height=600");

		current = i;

		return false;
	}

	function deleteSession(form, id) {
		var ask = confirm("Are you sure to delete this session?");
		if (ask) {
			var myurl = "/pelars/session/".concat(id);
			jQuery.ajax({
				type : "DELETE",
				url : myurl,
				success : function(jqXHR, status) {
					location.reload();
				},
				error : function(xhr, ajaxOptions, thrownError) {
					alert(xhr.status);
					alert(xhr.responseText);
					alert(thrownError);
				}
			});
			return false;
		}
	}

	function viewData(form, id) {
		window.open("/pelars/view.html#/data/".concat(id));
		return false;
	}
	
	function vizData(form, id) {
		window.open("/pelars/widgets.jsp?session=".concat(id));
		return false;
	}

	function vizStory(form, id) {
		window.open("/pelars/story_board.html?session=".concat(id));
		return false;
	}
	
</script>

<body id="body" onunload="saveScroll()">
	<table style="width: 100%" id="table">
	</table>
</body>
</html>