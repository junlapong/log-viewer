package com.aestas.utils.logviewer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.atmosphere.cpr.Broadcaster;
import org.json.simple.JSONValue;

public class LogViewerTailerListener extends TailerListenerAdapter {
	
	private Broadcaster broadCaster = null;
    
	public LogViewerTailerListener(Broadcaster broadcaster) {
		this.broadCaster = broadcaster;
	}

    List<String> buffer = new ArrayList<String>();

    @Override
    public void handle(String line) {
        buffer.add(line);
    }
    
    @Override
    public void endOfFileReached() {
        broadCaster.broadcast(asJsonArray("tail", buffer));
        buffer = new ArrayList<String>();
    }
    
    protected String asJson(final String key, String value) {
    	value = JSONValue.escape(value);
        return "{\"" + key + "\":\"" + value + "\"}";
    }

    protected String asJsonArray(final String key, final List<String> list) {

        return ("{\"" + key + "\":" + JSONValue.toJSONString(list) + "}");
    }
}
