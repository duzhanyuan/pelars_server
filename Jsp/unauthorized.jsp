<%@ page language="java" contentType="text/html; charset=US-ASCII"
	pageEncoding="US-ASCII"%>

<!DOCTYPE html>
<html>
<head>
<link rel="icon" type="image/png" href="pelarslogo.png" sizes="16x16">
<meta charset="US-ASCII">
<title>Unauthorized Access</title>
<% 
String url = request.getParameter("p-url");

Cookie[] cookies = request.getCookies();
Cookie url_cookie = null;

if(cookies != null){
	for(Cookie cookie : cookies){
		if(cookie.getName().equals("p-url")) {
			url = cookie.getValue();
			url_cookie = cookie;
			url_cookie.setMaxAge(2);
			response.addCookie(url_cookie);
		}
	}
}
String cont = request.getContextPath();
String content = "2.4;url=" + cont + "?p-url=" + url;

response.setStatus(401);
%>
<meta http-equiv="refresh" content=<%=content %> />
</head>

<style type="text/css">
html,body,h1,form,fieldset,legend,ol,li {
	margin: 0;
	padding: 1;
}

body {
	background: #ffffff;
	color: #111111;
	font-family: Georgia, "Times New Roman", Times, serif;
	padding: 20px;
}

.container {
	position: fixed;
	top: 50%;
	left: 50%;
	width: 30em;
	height: 18em;
	margin-top: -9em; /*set to a negative number 1/2 of your height*/
	margin-left: -15em; /*set to a negative number 1/2 of your width*/
	background-color: #fffffff;
}
</style>
<body>
	<div class=" container ">
		<p align=center>
			<font color=red size="6"> ACCESS DENIED </font>
		</p>
		<p align=center>
			<font  size="3"> redirecting to login page ... </font>
		</p>
	</div>
</body>
</html>