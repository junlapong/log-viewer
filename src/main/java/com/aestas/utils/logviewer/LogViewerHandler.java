package com.aestas.utils.logviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereServletProcessor;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.json.simple.JSONValue;

/**
 * @author luciano - luciano@aestasit.com
 */
public class LogViewerHandler implements AtmosphereHandler,  AtmosphereServletProcessor {

    private final static String FILE_TO_WATCH = "/Programas/apache-tomcat-8.0.47/logs";
    //private final static String FILE_TO_WATCH = "d://temp";
    private BroadcasterFactory broadcasterFactory;

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

        HttpServletRequest req = event.getRequest();//req.getContextPath(); req
        HttpServletResponse res = event.getResponse();
        res.setContentType("text/html");
        res.addHeader("Cache-Control", "private");
        res.addHeader("Pragma", "no-cache");
        
        Broadcaster broadcaster = getBroadcaster(event);

        if (req.getMethod().equalsIgnoreCase("GET")) {
        	//event.getAtmosphereConfig().getBroadcasterFactory().get("prueba")
            event.suspend();
            if (watchableLogs.size() != 0) {
            	broadcaster.broadcast(asJsonArray("logs", watchableLogs));
            }
            else {
                System.out.println("log not found");
            }

            res.getWriter().flush();
        } else { // POST

            final String postPayload = req.getReader().readLine();
            String filePath = null;
            if (postPayload != null && postPayload.startsWith("log=")) {
            	filePath = FILE_TO_WATCH + "//" + postPayload.split("=")[1];
            } else if(postPayload != null && postPayload.startsWith("file=")) {
            	filePath = postPayload.split("=")[1];
            }
            if(filePath != null) {
            	((LogViewerBroadcaster) broadcaster).startTailer(filePath);
            }
            broadcaster.broadcast(asJson("filename", postPayload.split("=")[1]));
            res.getWriter().flush();
        }
    }

	private Broadcaster getBroadcaster(final AtmosphereResource resource) {
		String fileId = getIdFile(resource);
		Broadcaster broadcaster = broadcasterFactory.lookup("/log-viewer/" + fileId);
		if(broadcaster == null) {
			broadcaster = broadcasterFactory.get("/log-viewer/" + fileId);
		}

		broadcaster.addAtmosphereResource(resource);
		return broadcaster;
	}
	
    private String getIdFile(AtmosphereResource resource) {
    	HttpServletRequest req = resource.getRequest();
    	String path = req.getContextPath() + req.getServletPath() + "/";
    	String uri = req.getRequestURI();
    	return uri.replace(path, "");
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

    @Override
    public void destroy() {
    }

    protected String asJson(final String key, final String value) {
        return "{\"" + key + "\":\"" + value + "\"}";
    }

    protected String asJsonArray(final String key, final List<String> list) {

        return ("{\"" + key + "\":" + JSONValue.toJSONString(list) + "}");
    }

}
