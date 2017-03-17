<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="stylesheet" type="text/css" href="style.css">
<script type="text/javascript"
	src="jquery/dist/jquery.min.js"></script>

<%
	String r_session = request.getParameter("session");
	Long session_id = null;
	if (r_session != null)
		session_id = Long.parseLong(r_session);
%>

</head>
<body>
	<script type="text/javascript">
	
	var parameter = <%=session_id%>;
			
			$(document).ready(function() {

			if (parameter != null) {
				getStatus(parameter);
			}
			});
			
			function getStatus(session){
				
				var data;
				
				jQuery.ajax({
					dataType : "json",
					url : "live/" + session,
					data : data,
					type : 'GET',
					success : function(data) {
						$('#visualization').html("<p>session " + session + " is " + data['status'] + "</p>");
						setTimeout(getStatus(session), 50000);
					},
					error : function(xhr, ajaxOptions, thrownError) {
						alert(xhr.status);
					}
				});	
			}

	</script>

	<div id="visualization"></div>
	<form onsubmit="return getStatus(this.session.value);" id="sub_form">
		session id: <input type="text" name="session" required>
	</form>
	<div id="loading"></div>

</body>
</html>