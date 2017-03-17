<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="icon" type="image/png" href="pelarslogo.png" sizes="16x16">
<meta charset="US-ASCII">
<title>Registration page</title>
<script type="text/javascript"
	src="jquery/dist/jquery.min.js""></script>
<script type="text/javascript"
	src="sisyphus/sisyphus.min.js"></script>
	<link rel="stylesheet" type="text/css" href="style.css">
</head>

<%
	String url = request.getParameter("p-url");
%>

<script type="text/javascript">
	function checkForm(form){

		if(form.pwd.value != form.pwd2.value){
			var str = document.getElementById("status").innerHTML;
			str = "<p> passwords do not match </p>";
			document.getElementById("status").innerHTML = str;
			return false;
		}

		if(form.pwd.value.length < 8){
			var str = document.getElementById("status").innerHTML;
			str = "<p> password must be at least 8 characters long </p>";
			document.getElementById("status").innerHTML = str;
			return false;
		}

		if(form.pwd.value.length > 16){
			var str = document.getElementById("status").innerHTML;
			str = "<p> password must be at most 8 characters long </p>";
			document.getElementById("status").innerHTML = str;
			return false;
		}

		//var form_data = JSON.stringify($("#sub_form").serializeArray());
		submit(form);
		return false;
	}

	function addRedirect(){
		window.location.replace("reg-success.jsp");
	}

	function addError(){
		var str = document.getElementById("status").innerHTML;
		str = "<p> There is already a user with the same email </p>";
		document.getElementById("status").innerHTML = str;
		return false;
	}

	function addServiceError(){
		var str = document.getElementById("status").innerHTML;
		str = "<p> Service unavaible, try again later </p>";
		document.getElementById("status").innerHTML = str;
		return false;
	}

	function addError_c(){
		var str = document.getElementById("status").innerHTML;
		str = "<p> Invalid captcha </p>";
		document.getElementById("status").innerHTML = str;
		return false;
	}

	function submit(form){
		mycontent = "{\"name\":\"" + form.name.value + "\", \"password\":\""
				+ form.pwd.value + "\", \"email\": \"" + form.email.value
				+ "\", \"affiliation\": \"" + form.affiliation.value
				+ "\", \"namespace\": \"" + form.country.value + "\" }"
		jQuery.ajax({
			timeout : 20000,
			type : "GET",
			url : "validate.jsp?number="+ form.number.value,
			success : function(jqXHR, status){
						jQuery.ajax({
							timeout : 20000,
							type : "PUT",
							url : "user",
							data : mycontent,
							dataType : "json",
							success : function(jqXHR, status){
								addRedirect();
							},
							error : function(jqXHR, status){
								if (jqXHR.status == 403){
									addError();
								}
								else{
									addServiceError();
								}
							}
						}
						);
			},
			error : function(jqXHR, status) {
				addError_c();
			}
		});
		return true;
	}

</script>
<script type="text/javascript">
	$(window).load(function() {
		$("form").sisyphus({
			timeout : 0,
			autoRelease: true
		});
	});
	
	
</script>

<body id="all">

	<div class="container">
		<form onsubmit="return checkForm(this);" id="sub_form">
			<fieldset>
				<legend>Your details</legend>
				<ol>
					<li>Name: <input type="text" name="name" required>
					</li>
					<li>Password: <input type="password" name="pwd" required>
					</li>
					<li>Confirm password: <input type="password" name="pwd2"
						required>
					</li>
					<li>Email: <input type="email" name="email" required>
					</li>
					<li>Affiliation: <input type="text" name="affiliation"
						required>
					</li>
					<li>Group: <input type="text" name="country" required>
					</li>
				</ol>
				<table border="1" width="380" align="center" cellspacing="2"
					cellpadding="0" bgcolor="#A4EEFF">
					<TR>
						<TD>
							<table bgcolor="" align="center" id="captcha">
								<tr>
									<td align="center" colspan="2"><img src="Cap_Img.jsp"><br>
										<br> <input type="button" value="Refresh Image"
										onClick="window.location.href=window.location.href"></td>
								</tr>
								<tr>
									<td align="center">Please enter the string shown in the
										image.</td>
								<tr>
									<td align="center"><input name="number" type="text"></td>
								<tr>
									<td width=100% align=right><a
										style="color: green; font-size: 11px; text-decoration: none;"
										id="dum" href="http://www.hscripts.com">Tool by - &copy;
											hscripts.com </a></td>
								</tr>
							</table>
						</TD>
					</TR>
				</table>
			</fieldset>

			<input type="submit" value="Submit">
			<div id="status"></div>
		</form>
	</div>


</body>
</html>


