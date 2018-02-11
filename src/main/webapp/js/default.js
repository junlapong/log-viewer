function initLogViewer(location, queryString) {

	var connectedEndpoint;
	var callbackAdded = false;
	var detectedTransport = null;
	var lines = 0, notice = $("#info"), buffer = $('#tail');

	function subscribe() {
		// jquery.atmosphere.response
		function callback(response) {
			// Websocket events.
			$.atmosphere.log('info', [ "response.state: " + response.state ]);
			$.atmosphere.log('info', [ "response.transport: " + response.transport ]);

			detectedTransport = response.transport;
			if (response.transport != 'polling' && response.state != 'connected' && response.state != 'closed') {
				$.atmosphere.log('info', [ "response.responseBody: " + response.responseBody ]);
				if (response.status == 200) {
					if (response.responseBody == "") {
						connectedEndpoint .push( location, null, $.atmosphere.request = { data : decodeURI(queryString) });
					} else if (response.responseBody != "X") {
						var data = jQuery.parseJSON(response.responseBody);
						if (data == null)
							return;
						if (data.filename) {
							notice.html('watching ' + data.filename);
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

		var request = {
			transport : 'websocket'
		};

		$.atmosphere.subscribe(location, !callbackAdded ? callback : null,
				$.atmosphere.request = request);

		connectedEndpoint = $.atmosphere.response;
		callbackAdded = true;

	}

	function connect() {
		if(queryString) {
			subscribe();
		}
	}

	connect();
}

function redirectToFile(e, url) {
	if(url) {
		var file = e.options[e.selectedIndex].value;
		window.location.href = url + "?file=" + file;
	}
}