
function pelars_authenticate_prompt(){
	var email = prompt("Please enter your email", "");
	var pswd = prompt("Please enter your password", "");
	var jsres;
	var res = "";
	jQuery.ajax({
		timeout : 5000,
		crossDomain : true,
		xhrFields : {
			withCredentials : true
		},
		type : "POST",
		url : "${pageContext.request.contextPath}/password?user=" + email + "&pwd=" + pswd,
		async: false,
		success : function(jqXHR, status, result){
		jsres = JSON.parse(jqXHR);
		res = jsres["token"];
		},
		error : function(jqXHR, status) {
			res = 0; }
	});

	return res;
}

function pelars_authenticate(email, pswd){

	var jsres;
	var res = "";
	jQuery.ajax({
		timeout : 5000,
		xhrFields : {
			withCredentials : true
		},
		crossDomain : true,
		type : "POST",
		url : "${pageContext.request.contextPath}/password?user=" + email + "&pwd=" + pswd,
		async: false,
		success : function(jqXHR, status, result){
		jsres = JSON.parse(jqXHR);
		res = jsres["token"];
		},
		error : function(jqXHR, status) {
			res = 0; }
	});

	return res;
}