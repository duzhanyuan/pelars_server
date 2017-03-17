
function getTable(m_url, selector, single){
	//set cookie with URL here
	$.getScript("cookie-handler.js", function(){
		saveCookie("p-url", "http://pelars.sssup.it/pelars/view.html#" + m_url);
	});

	var n = m_url.indexOf("?");
	var character = "?"

		if(n != -1){
			character = "&"
		}

	var old = location.href.indexOf("8080");
	var port = ""

		if(old != -1){
			port = ":8080"
		}

	jQuery.ajax({
		timeout : 20000,
		type : "GET",
		dataType : "json",
		url : "/pelars/" + m_url + character +"html=true",
		success : function(response) {
			var headerTr$ = $('<tr/>');
			
			if(!single){
				var i,j,temparray,chunk = 1000;
				var columns = [];
				for (i=0,j=response.length; i<j; i+=chunk) {

					temparray = response.slice(i,i+chunk);
					addAllColumnHeaders(temparray, headerTr$, columns);
				}

				$("#thead").append(headerTr$);

				for (i=0,j=response.length; i<j; i+=chunk) {

					temparray = response.slice(i,i+chunk);
					buildHtmlTable(selector, temparray, columns);
				}
				$("#table").dataTable();
			}
			else{
				ShowHtmlElement(selector,response);
			}
		},
		error : function(jqXHR){
			if(jqXHR.status == 401){
				window.location.replace("/pelars/unauthorized.jsp?p-url=" +window.location.href);
			}
		}
	});
}

function buildHtmlTable(selector, myList, columns){

	//columns = columns.concat(addAllColumnHeaders(myList, selector));

	for(var i = 0 ; i < myList.length ; i++){
		var row$ = $('<tr/>');
		for(var colIndex = 0 ; colIndex < columns.length ; colIndex++){
			var cellValue = myList[i][columns[colIndex]];
			if (cellValue == null) {
				cellValue = ""; 
			}
			if(cellValue > 2000000000){
				cellValue = new Date(parseInt(cellValue)).toGMTString();
			}
			row$.append($('<td/>').html(cellValue));
		}
		$("#tbody").append(row$);
	}
}

//Adds a header row to the table and returns the set of columns.
//Need to do union of keys from all records as some records may not contain
//all records
function addAllColumnHeaders(myList, headerTr$, columnSet)
{
	//var columnSet = [];

	for(var i = 0 ; i < myList.length ; i++){
		var rowHash = myList[i];
		for(var key in rowHash){
			if($.inArray(key, columnSet) == -1){
				columnSet.push(key);
				headerTr$.append($('<th/>').html(key));
			}
		}
	}
	return columnSet;
}

function ShowHtmlElement(selector, myList){
	var columns = addColumnHeaders(myList, selector);

	var row$ = $('<tr/>');
	for(var colIndex = 0 ; colIndex < columns.length ; colIndex++){
		var cellValue = myList[columns[colIndex]];
		if(cellValue == null){
			cellValue = ""; 
		}
		row$.append($('<td/>').html(cellValue));
	}
	$("#tbody").append(row$);
}

//Adds a header row to the table and returns the set of columns.
//Need to do union of keys from all records as some records may not contain
//all records
function addColumnHeaders(myList, selector)
{

	var columnSet = [];
	var headerTr$ = $('<tr/>');

	var rowHash = myList;
	for(var key in rowHash){
		if($.inArray(key, columnSet) == -1){
			columnSet.push(key);
			headerTr$.append($('<th/>').html(key));
		}
	}
	$("#thead").append(headerTr$);
	return columnSet;
}