package com.aestas.utils.logviewer;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereServletProcessor;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.json.simple.JSONValue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author luciano - luciano@aestasit.com
 */
public class LogViewerHandler extends TailerListenerAdapter implements AtmosphereHandler,  AtmosphereServletProcessor {

    private final static String FILE_TO_WATCH = "/Program Files/apache-tomcat-7.0.62/logs";
    //private final static String FILE_TO_WATCH = "d://temp";
    private static Tailer tailer;
    private BroadcasterFactory broadcasterFactory;

    //private Map<String, Broadcaster> brs = new HashMap<String, Broadcaster>();

    private static List<String> watchableLogs = new ArrayList<String>();
    
	@Override
	public void init(AtmosphereConfig config) throws ServletException {
		this.broadcasterFactory = config.getBroadcasterFactory();
		
	}

    public LogViewerHandler() {

        final File logsDir = new File(FILE_TO_WATCH);

        if (logsDir.exists() && logsDir.isDirectory()) {
            System.out.println("log path: " + logsDir.getAbsolutePath());

            File[] logs = logsDir.listFiles();
            for (File f : logs) {
                if (f.getName().endsWith(".log")) {
                    watchableLogs.add(f.getName());
                }
            }
        } else {
            System.out.println("either logsDir doesn't exist or is not a folder");
        }

         System.out.println("log count: " + watchableLogs.size());
    }

    @Override
    public void onRequest(final AtmosphereResource event) throws IOException {

        HttpServletRequest req = event.getRequest();
        HttpServletResponse res = event.getResponse();
        res.setContentType("text/html");
        res.addHeader("Cache-Control", "private");
        res.addHeader("Pragma", "no-cache");
        
        Broadcaster broadcaster = getBroadcaster(event);

        if (req.getMethod().equalsIgnoreCase("GET")) {

            event.suspend();
            if (watchableLogs.size() != 0) {
            	broadcaster.broadcast(asJsonArray("logs", watchableLogs));
            }
            else {
                System.out.println("log not found");
            }

            res.getWriter().flush();
        } else { // POST

            // Very lame... req.getParameterValues("log")[0] doesn't work
            final String postPayload = req.getReader().readLine();
            if (postPayload != null && postPayload.startsWith("log=")) {
                tailer = Tailer.create(new File(FILE_TO_WATCH + "//" + postPayload.split("=")[1]), this, 500);
            } else if(postPayload != null && postPayload.startsWith("file=")) {
            	tailer = Tailer.create(new File(postPayload.split("=")[1]), this, 500);
            }
            broadcaster.broadcast(asJson("filename", postPayload.split("=")[1]));
            res.getWriter().flush();
        }
    }

	private Broadcaster getBroadcaster(final AtmosphereResource event) {
		Broadcaster broadcaster = broadcasterFactory.lookup("/log-viewer");
		if(broadcaster == null) {
			broadcaster = broadcasterFactory.get("/log-viewer");
			broadcaster.addAtmosphereResource(event);
		}
		return broadcaster;
	}

    @Override
    public void onStateChange(final AtmosphereResourceEvent event) throws IOException {

        HttpServletResponse res = event.getResource().getResponse();
        if (event.isResuming()) {
            res.getWriter().write("Atmosphere closed<br/>");
            res.getWriter().write("</body></html>");
        } else {
            if (event.getMessage() != null) {
                res.getWriter().write(event.getMessage().toString());
            }
            else {
                //res.getWriter().write("");
            }
        }

        res.getWriter().flush();
    }

    

    private final Object o = new Object();

    @Override
    public void destroy() {
        if (tailer != null) {
            tailer.stop();
        }
    }

    List<String> buffer = new ArrayList<String>();

    @Override
    public void handle(String line) {
        buffer.add(line);
    }
    
    @Override
    public void endOfFileReached() {
    	getBroadcaster(null).broadcast(asJsonArray("tail", buffer));
        buffer = new ArrayList<String>();
    }
    
    protected String asJson(final String key, final String value) {
        return "{\"" + key + "\":\"" + value + "\"}";
    }

    protected String asJsonArray(final String key, final List<String> list) {

        return ("{\"" + key + "\":" + JSONValue.toJSONString(list) + "}");
    }

}
