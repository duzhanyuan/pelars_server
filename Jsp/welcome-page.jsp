<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="icon" type="image/png" href="pelarslogo.png" sizes="16x16">
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<link rel="stylesheet" type="text/css" href="style.css">
<title>Login Success Page</title>

	<script type="text/javascript"
	src="jquery/dist/jquery.min.js"></script>
	
	
</head>
<%@ page import="authorization.TokenService" %>
<%@ page import="authorization.Token" %>
<%@ page import="pelarsServer.User" %>
<%@ page import="servlets.Util" %>
<%
String s_token = null;
Cookie[] cookies = request.getCookies();
String s_validity = "not valid";
String role = "";
Long s_id = null;
boolean validity = false;

if(cookies != null){
	for(Cookie cookie : cookies){
	    if(cookie.getName().equals("token")) {
		    s_token = cookie.getValue();
		    validity = TokenService.isValid(s_token, request.getRemoteAddr());
			if(validity){s_validity = "valid"; }
			Token token = new Token(s_token);
			role = token.getRole();
	    }
	/*    if(cookie.getName().equals("user_id")) {
		    s_id = cookie.getValue();
	    } */
	}
	
	
}
if(s_token == null || validity == false) {
	response.sendRedirect("unauthorized.jsp?p-url=welcome-page.jsp");
}

else{

User u = Util.getUser(request);
s_id = u.getId();
}
%>

<script type="text/javascript">

var user_id = <%=s_id%>;

	$(document).ready(function() {
		$.getJSON("/pelars/user/" + <%=s_id%>,
					function(json){
						var tr = "";
					
							tr = tr.concat("<p> Welcome "
									+ json.name
									+ "</p>");
							tr = tr.concat("<p>Country: "
									+ json.namespace
									+ "</p>");
							tr = tr.concat("<p>Affiliation: "
									+ json.affiliation
									+ "</p>");
							tr = tr.concat("<p>Email: "
									+ json.email
									+ "</p>");
							tr = tr.concat("<p>Role: "
									+ json.role
									+ "</p>");
							tr = tr.concat("<button type=\"button\" onclick=\"personalChange()\"> Change your personal details </button>");
							tr = tr.concat("<button type=\"button\" onclick=\"viewSessions()\"> Edit your sessions </button>");	
							tr = tr.concat("<button type=\"button\" onclick=\"showToken()\"> Show your token </button>");	
						
						document.getElementById("welcome").innerHTML = tr;
						document.getElementById("logout").innerHTML = "<form action='LogoutServlet' method='post'> \
						<input type='submit' value='Logout' >\
						</form>";
					}
		);
});
	
	function personalChange(){
		window.open("change-personal.html");
		return false;
	}
	
	function viewSessions(){
		window.location.href = "admin-session.jsp";
		return false;
	}
	
	function showToken(){
		window.location.href = "login-success.jsp";
		return false;
	}
	
</script>

<body>
<div id = welcome class="container"></div>
<div id = logout></div>

</body>
</html>