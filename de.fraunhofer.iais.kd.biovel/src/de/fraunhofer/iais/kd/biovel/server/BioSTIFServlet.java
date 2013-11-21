package de.fraunhofer.iais.kd.biovel.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import de.fraunhofer.iais.kd.biovel.i18n.I18nUtils;


public class BioSTIFServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String BIOSTIF_CLIENT_CONF = "biostif.client.conf";
	
	// Create the logger
	private static final Logger LOG = Logger.getLogger(BioSTIFServlet.class.getPackage().getName());
	private static String servletWorkdir;
	protected static Properties conf;
	//	private static String geoserverDataDir = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		// FhG main cert not in global key store :-(
		// We add to our local store and use this one
		//		System.setProperty("java.protocol.handler.pkgs",
		//		"com.sun.net.ssl.internal.www.protocol");
		//		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		//
		//		System.setProperty
		//		("javax.net.ssl.trustStore", "/fhg/tomcat/var/.keystore");
		//		System.setProperty
		//		("javax.net.ssl.trustStorePassword", "changeit");

		servletWorkdir = config.getServletContext().getRealPath("/") + "/";
		LOG.info("===  BioSTIF Servlet starting up - " + new Date() + " ====");

//		// Get the desired information from config
		//String confFileName = config.getInitParameter("CONFIG_FILE");
		
		
		String confFileName = null;
        if ((confFileName = System.getProperty(BIOSTIF_CLIENT_CONF)) == null) {
            confFileName = config.getInitParameter(BIOSTIF_CLIENT_CONF);
        }
        
        if (confFileName == null) {
            throw new ServletException("--- BioSTIF Server initialisation failed: Missing init parameter " + BIOSTIF_CLIENT_CONF);
        }        
		
		if (confFileName != null && confFileName.length() > 0) {			
				
				LOG.info("BioSTIF Client config file read at " + confFileName);
				try {
					conf = readConfig(confFileName);
					LOG.info("BioSTIF Client config: " + conf.toString());
				} catch (IOException e1) {
					throw new ServletException("--- BioSTIF Server initialisation failed: Config file '" + confFileName + "' "
							+ "cannot be accessed");
				}

		}

	}

	


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		if (req.getParameter("ping") != null) {
			res.getWriter().write("pong");
			return;
		}

		if ( req.getParameter("i18n") != null ){
			String lang = req.getParameter("lang");
			if( lang.length() == 0 ){
				lang = null;
			}
			returnStringToResponse(res, I18nUtils.toJson(lang), "text/plain", "UTF-8");
			return;
		}
		
		if (req.getParameter("config") != null) {
			returnStringToResponse(res, getAsJson(conf), "text/plain", "UTF-8");
			return;
		}

		processGETRequest(req, res);

		return;


	}

	private void processGETRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
		List<String> params = getParametersAsString(req);
		if (params.isEmpty()) {
			// res.sendError(arg0, arg1)
			return;
		}
		if (params.size() == 2) {
			redirect(params.get(0), params.get(1), res);
		} else {
			redirect(params.get(0), "", res);
		}

	}

	/**
	 * returns a string array. First parameter is the url and second the params
	 * if url not avaliable then only 1 element is in the array
	 * 
	 * @param req
	 * @return
	 */
	private List<String> getParametersAsString(HttpServletRequest req) {
		List<String> requestParams = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		String url = "";
		Enumeration<String> e = req.getParameterNames();
		while (e.hasMoreElements()) {
			String k = e.nextElement();
			System.out.println(k + "=" + req.getParameter(k));
			if (!k.equalsIgnoreCase("url")) {
				sb.append('&').append(k).append('=').append(req.getParameter(k));
			} else {
				url = req.getParameter(k);
			}
		}
		if (url.length() > 0) {
			requestParams.add(url);
		}
		if (sb.length() > 0) {
			requestParams.add(sb.toString());
		}
		return requestParams;
	}

	/**
	 * sends a getFeature Request to a WF and sends the resutlt to the servlet
	 * response
	 * 
	 * @param pars
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	public final void redirect(String url, String pars, HttpServletResponse resp) throws IOException {

		// FIXME should be get from parameters.
		String target = url + pars;
		try {
			URL u = new URL(target);

			URLConnection conn = u.openConnection();

			// read the Plain
			InputStream is = conn.getInputStream();
			BufferedReader isBufferedReader = new BufferedReader(new InputStreamReader(is));
			resp.setContentType("text/plain");
			resp.setCharacterEncoding("UTF-8");

			String line;
			String messagebody = "";
			while ((line = isBufferedReader.readLine()) != null) {
				messagebody += line;
			}
			isBufferedReader.close();
			OutputStream os = resp.getOutputStream();

			byte[] messagebytes = messagebody.getBytes();
			ByteArrayOutputStream baos = new ByteArrayOutputStream(messagebytes.length);
			baos.write(messagebytes);
			baos.writeTo(os);
			os.flush();
			os.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

	}


	private void returnStringToResponse(HttpServletResponse resp, String messagebody, String mimetype, String charencode)
	throws IOException {

		resp.setContentType(mimetype);
		resp.setCharacterEncoding(charencode);

		OutputStream os = resp.getOutputStream();
		byte[] messagebytes = messagebody.getBytes();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(messagebytes.length);
		baos.write(messagebytes);
		baos.writeTo(os);
		os.flush();
		os.close();
	}
	
	/**
	 * Method to read config
	 * @return the config properties
	 * @throws IOException if CONF_FILE is not readable
	 */
	private static Properties readConfig( String filename ) throws IOException{
		Properties properties = new Properties();
		FileInputStream stream = new FileInputStream( filename );
		properties.load(stream);
		stream.close();
		return properties;
	}


	private String getAsJson(Object value){
		
		String json = "";
        
        try {
        	
        	ObjectMapper mapper = new ObjectMapper();
//			System.out.println(mapper.writeValueAsString(valueMap));
        	if(value != null) {
        		json = mapper.writeValueAsString(value);
        	}
			
			
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        
		return json;		
		
	}
}