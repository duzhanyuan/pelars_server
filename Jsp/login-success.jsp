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
	src="jquery/dist/jquery.min.js""></script>
	<script type="text/javascript"
	src="cookie-handler.js"></script>
	
	
</head>
<body>
<%@ page import="authorization.TokenService" %>
<%@ page import="authorization.Token" %>
<%
String s_token = null;
Cookie[] cookies = request.getCookies();
String s_validity = "not valid";
String role = "";
String ip = "";
boolean validity = false;

/*String user = null;
if(session.getAttribute("user") == null){
    response.sendRedirect("login.html");
} 
else {
    user = (String) session.getAttribute("user");
}*/

if(cookies != null){
	for(Cookie cookie : cookies){
	    if(cookie.getName().equals("token")) {
		    s_token = cookie.getValue();
		    validity = TokenService.isValid(s_token, request.getRemoteAddr());
			if(validity){s_validity = "valid"; }
			Token token = new Token(s_token);
			ip = token.getIp();
			role = token.getRole();
	    }
	}
}
//else{
   // sessionID = session.getId();
//}

if(s_token == null || validity == false) 
	response.sendRedirect("unauthorized.jsp");
%>

<div class="container">
<h3>Your token is: <%=s_token %>, Login successful.</h3>
<p>Your token is <%=s_validity %> </p>
<p>Your role is <%=role %> </p>

<br>
<form action="LogoutServlet" method="post">
<input type="submit" value="Logout" >
</form>
</div>
</body>
</html>
