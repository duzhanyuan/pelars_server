<%
String key=(String)session.getAttribute("key");
String user = request.getParameter("number");
String url = request.getHeader("referer");
String spath=request.getServerName();
int val=0;
val= spath.indexOf("www.");

if(val != -1)
{
   spath=spath.replaceFirst("www.","");
}
int domain=url.indexOf(spath); 
if(domain != -1)
{
if(key.compareTo(user)==0)			
{
response.setStatus(200);
}
else{
response.setStatus(401);
}
}
else
{}%>
