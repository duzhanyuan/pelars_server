<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" type="text/css" href="style.css">
<link rel="icon" type="image/png" href="pelarslogo.png" sizes="16x16">
<script src="signals.min.js" type="text/javascript"></script>
<script src="crossroads.min.js" type="text/javascript"></script>
<script src="tables.js" type="text/javascript"></script>
<script type="text/javascript"
	src="jquery/dist/jquery.min.js"></script>
<meta charset="US-ASCII">

<script type="text/javascript">
	function postPersonal(form) {
		var myurl = "${pageContext.request.contextPath}/user/".concat(opener.user_id);
		console.log(myurl);
		var sub_data = {};
		if (form.name.value != "") {
			sub_data.new_name = form.name.value;
		}
		if (form.affiliation.value != "") {
			sub_data.affiliation = form.affiliation.value;
		}
		if (form.country.value != "") {
			sub_data.namespace = form.country.value;
		}
		jQuery.ajax({
			timeout : 5000,
			type : "POST",
			url : myurl,
			//ALWAYS befor sending JSON data! 
			data : JSON.stringify(sub_data),
			dataType : "json",
			contentType : "application/json; charset=utf-8",
			success : function(jqXHR, status) {
				opener.window.location.reload();
				window.close();
			},
			error : function(xhr, ajaxOptions, thrownError) {
				alert(xhr.status);
				alert(xhr.responseText);
				alert(thrownError);
			}
		});
		return false;
	}

	function back() {
		window.close();
	}
</script>


</head>


<body>
	<div class="container">
		<form onsubmit="return postPersonal(this);" id="sub_form">
			<fieldset>
				<legend>Change your details</legend>
				<ol>
					<li>Name: <input type="text" name="name">
					</li>
					<li>Affiliation: <input type="text" name="affiliation">
					</li>
					<li>Country: <input type="text" name="country">
					</li>
				</ol>
				<input type="submit" value="Submit">
			</fieldset>
		</form>
		<form onsubmit="return back();">
			<input type="submit" value="Back">
		</form>
	</div>
</body>

</html>