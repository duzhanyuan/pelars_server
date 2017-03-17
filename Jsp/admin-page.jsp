<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="icon" type="image/png" href="pelarslogo.png" sizes="16x16">
<title>Administrator page</title>
<script type="text/javascript"
	src="jquery/dist/jquery.min.js"></script>
<script src="cookie-handler.js" type="text/javascript"></script>
<link rel="stylesheet" type="text/css" href="style.css">
</head>
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
		   	bo = TokenService.isValid(token, request.getRemoteAddr());
			if(bo){
				validity = "valid";
			}
			Token tok = new Token(token);
			ip = tok.getIp();
			role = tok.getRole();
	    }
	}
}
if(token == null || bo == false || !role.equals("administrator")) response.sendRedirect("unauthorized.jsp?p-url=admin-page.jsp");
%>

<script type="text/javascript">

	$(document).ready(function() {
		$.getJSON(  "user",
					function(json){
						var tr = "";
						for(var i = 0; i < json.length; i++){
							tr = tr.concat("<tr>")
							tr = tr.concat("<td>"
									+ json[i].email
									+ "</td>");
							tr = tr.concat("<td>"
									+ json[i].role
									+ "</td>");
					
							tr = tr
									.concat("<td><form onsubmit=\"return changeRole(this,"
											+ (json[i].identifier)
											+ ");\">\
							<select name=\"roles\"><option value=\"student\">student</option><option value=\"researcher\">researcher</option><option value=\"administrator\">\
							administrator</option><option value=\"researcher\">teacher</option><option value=\"viewer\">viewer</option>" + 
							"</select><input type=\"submit\" value=\"Submit\"></form></td>");
							tr = tr
									.concat("<td><button onclick=\"return deleteUser(this,"
											+ (json[i].identifier)
											+ ");\">\
									Delete user</button></td>");
							tr = tr
									.concat("<td><button onclick=\"return deleteUserSessions(this,"
											+ (json[i].identifier)
											+ ");\">\
									Delete all sessions</button></td>");
							tr = tr.concat("</tr>");
						}
						document.getElementById("table").innerHTML = tr;
						loadScroll();
					}
		);
});

	function changeRole(form, id){
		var myurl = "user/".concat(id);
		var sub_data = { "role" : form.roles.value};

		jQuery.ajax({
			timeout : 5000,
			type : "POST",
			url : myurl,
			//ALWAYS befor sending JSON data! 
			data : JSON.stringify(sub_data),
			dataType : "json",
			contentType : "application/json; charset=utf-8",
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

	function deleteUser(form, id){
		var ask = confirm("Are you sure to delete this user?");
		if(ask){
			var myurl = "user/".concat(id);
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

		function deleteUserSessions(form, id){
		var sub_data = { "delete_sessions" : "true"};
		var ask = confirm("Are you sure to delete all the sessions associated to this user?");
		if(ask){
			alert("deleting sessions...");
			var myurl = "user/".concat(id);
			jQuery.ajax({
				timeout : 500000,
				type : "POST",
				url : myurl,
				//ALWAYS before sending JSON data! 
				data : JSON.stringify(sub_data),
				dataType : "json",
				contentType : "application/json; charset=utf-8",
				success : function(jqXHR, status) {
					alert("sessions deleted");
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
</script>
<body onunload="saveScroll()">
	<table style="width: 100%" id="table">
	</table>
</body>
</html>