<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Websockets tail Server</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <style type="text/css" rel="stylesheet">
        body {
            background-color: #222;
        }

        #selector {
            float: left;
        }

        #info {
            font-size: 32px;
            color: #eee;
            text-shadow: #aaa 1px 1px 2px;
            text-align: right;
            margin: 20px 10px;
            text-transform: lowercase;
        }

        #tail {
            clear: both;
            border: 1px solid #444;
            overflow-x: hidden;
            overflow-y: auto;
            background-color: #333;
            color: #EEE;
            text-shadow: #000 0 0 2px;
            height: 600px;
            padding: 10px;
            font-size: 12px;
            line-height: 20px;
			width: 97%;
			white-space: nowrap;
			overflow: auto;
        }

        .trebuchet {
            font-family: "Trebuchet MS", "Lucida Sans Unicode", "Lucida Grande", "Lucida Sans", Arial, sans-serif;
        }

        .monospace {
            font-family: Monaco, "Bitstream Vera Sans Mono", "Lucida Console", Terminal, monospace;
        }

        .selection::selection, .selection *::selection {
            background: #EEE;
            color: #000;
            border-color: #000;
            text-shadow: #fff 0 0 2px;
        }

        .selection::-moz-selection, .selection *::-moz-selection {
            background: #EEE;
            color: #000;
            border-color: #000;
            text-shadow: #fff 0 0 2px;
        }
    </style>
</head>
<body>
<div id="selector">
<select>
    <option value="" selected>-- select a log --</option>
</select>
</div>
<div>
	<div id="info" class="trebuchet"></div>
	<textarea id="tail" class="monospace selection"></textarea>
</div>
<script src="js/jquery-1.4.3.js"></script>
<script src="js/jquery.form.js"></script>
<script src="js/jquery.atmosphere.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
        var connectedEndpoint;
        var callbackAdded = false;
        var detectedTransport = null;
        var lines = 0, notice = $("#info"), buffer = $('#tail');

        function subscribe() {
            // jquery.atmosphere.response
            function callback(response) {
                // Websocket events.
                $.atmosphere.log('info', ["response.state: " + response.state]);
                $.atmosphere.log('info', ["response.transport: " + response.transport]);
				
                detectedTransport = response.transport;
                if (response.transport != 'polling' && response.state != 'connected' && response.state != 'closed') {
                    $.atmosphere.log('info', ["response.responseBody: " + response.responseBody]);
                    if (response.status == 200) {
                    	if(response.responseBody == "") {
                    		connectedEndpoint.push('${pageContext.request.requestURL}log-viewer' ,null,
            	                    $.atmosphere.request = {data: decodeURI('${pageContext.request.queryString}') });
                    	} else {
	                        var data = jQuery.parseJSON(response.responseBody);
	                        if (data == null) return;
	                        if (data.filename) {
	                            notice.html('watching ' + data.filename);
	                        } else if (data.logs) {
	                            var selector = $("#selector select");
	                            $.each(data.logs, function() {
	                                var log = new Option(this, this);
	                                if ($.browser.msie) selector[0].add(log); else selector[0].add(log, null);
	                            });
	                            selector.bind('change', function(e) {
	                                var log = selector[0];
	                                if (log.selectedIndex == 0) {
	                                    $("#info,#tail").empty();
	                                    return;
	                                }
	                                //socket.send({log:log.options[log.selectedIndex].value});
	                                connectedEndpoint.push('${pageContext.request.requestURL}log-viewer' ,null,
	                                    $.atmosphere.request = {data: 'log=' +log.options[log.selectedIndex].value});
	                            });
	                        } else if (data.tail) {
								var tempLog = "";
	                            
								$.each(data.tail, function(index, value) {
									tempLog += value + "\n";
								});
	                            lines = lines + data.tail.length;
								
								buffer.val(buffer.val() + tempLog)
								buffer.scrollTop(lines * 100);
	                            tempLog = "";
	                        } else {
	                            //
	                        }
                    	}

                    }
                }
            }

            var location = '${pageContext.request.requestURL}log-viewer';
			var request = { transport: 'websocket' };
			
            $.atmosphere.subscribe(location, !callbackAdded ? callback : null, $.atmosphere
                    .request = request);
											
            connectedEndpoint = $.atmosphere.response;
            callbackAdded = true;
			
			
        }
        
        function connect() {
            subscribe();
        }

        connect();
    });

</script>
</body>
</html>
