<%@ page language="java" contentType="text/html; charset=US-ASCII"
	pageEncoding="US-ASCII"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="US-ASCII">
<title>Login Page</title>
<link rel="stylesheet" type="text/css" href="style.css">
<link rel="icon" type="image/png" href="pelarslogo.png" sizes="16x16">

<script type="text/javascript"
	src="cookie-handler.js"></script>
<script type="text/javascript"
	src="jquery/dist/jquery.min.js"></script>

<script type="text/javascript">

	deleteCookie("p-url");

	function ChangePassword(form) {
		
		window.open("change-password.html");	
	}

	$(document).ready(function() {
	
		deleteCookie("p-url");
		deleteCookie("page_scroll");
		getCookie("token");
		
	});
</script>

</head>



<body>
	<%
		String url = request.getParameter("p-url");
		String email = (String) request.getAttribute("email");
		if (email == null) {
			email = "";
		}
		String password = (String) request.getAttribute("password");
		if (password == null) {
			password = "";
		}
	%>
	<div class="container">
		<form action="password" method="post" id="sub_form">
			<fieldset>
				<legend>Pelars Web Interface</legend>
				<p>
					Email: <input type="text" name="user">
				</p>

				<p>
					Password: <input type="password" name="pwd">
				</p>

				<input type="hidden" name="p-url" value=<%=url%>> <input
					type="submit" value="Login">
			</fieldset>
		</form>
	</div>
	<p></p>
	<div class="container">
		<form action="registration.jsp" id="sub_form">
			<fieldset>
				<legend>New user?</legend>
				<input type="hidden" name="p-url" value=<%=url%>> <input
					type="submit" value="Subscribe">
			</fieldset>
		</form>
	</div>
	<div class="container">
		<form onsubmit="return ChangePassword(this)" id="sub_form">
			<fieldset>
				<legend>Change password?</legend>
				<input type="hidden" name="p-url" value=<%=url%>> <input
					type="submit" value="Change password">
			</fieldset>
		</form>
	</div>
	<div id="status"></div>
</body>
</html>
