package com.aestas.utils.logviewer;

import java.io.File;

import org.apache.commons.io.input.Tailer;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterListener;
import org.atmosphere.cpr.DefaultBroadcaster;
import org.atmosphere.cpr.Deliver;

public class LogViewerBroadcaster extends DefaultBroadcaster implements BroadcasterListener {

	private Tailer tailer;
	
	public void startTailer(String filePath) {
		tailer = Tailer.create(new File(filePath), new LogViewerTailerListener(this), 500);
		this.addBroadcasterListener(this);
	}

	@Override
	public void onPostCreate(Broadcaster b) {
		
	}

	@Override
	public void onComplete(Broadcaster b) {
		
	}

	@Override
	public void onPreDestroy(Broadcaster b) {
		tailer.stop();
	}

	@Override
	public void onAddAtmosphereResource(Broadcaster b, AtmosphereResource r) {
		
	}

	@Override
	public void onRemoveAtmosphereResource(Broadcaster b, AtmosphereResource r) {
		
	}

	@Override
	public void onMessage(Broadcaster b, Deliver deliver) {
		
	}
	
}
