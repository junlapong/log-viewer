<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.aestas.utils.logviewer.HashHelper" %>
<%@ page import="com.aestas.utils.logviewer.LogViewerHandler" %>

<!DOCTYPE html>
<html>
<head>
    <title>Websockets tail Server</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <link rel="stylesheet" type="text/css" href="css/default.css">
</head>
<body>
<div id="selector">
<select onchange="redirectToFile(this, '${pageContext.request.requestURL}')">
    <option value="" selected="selected">-- select a log --</option>
    <%
    for(String file: LogViewerHandler.getWatchableLogs()) {
    %>
    	<option value="<%=LogViewerHandler.getFileToWatch()+ "/" + file %>"><%=file %></option>
    <%
    }
    %>
</select>
</div>
<div>
	<div id="info" class="trebuchet"></div>
	<textarea id="tail" class="monospace selection"></textarea>
</div>
<script src="js/jquery-1.4.3.js"></script>
<script src="js/jquery.form.js"></script>
<script src="js/jquery.atmosphere.js"></script>
<script type="text/javascript" src="js/default.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		initLogViewer("log-viewer/<%=HashHelper.getHashString(request.getParameter("file")) %>", "${pageContext.request.queryString}");
	});

</script>
</body>
</html>
