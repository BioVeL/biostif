package de.fraunhofer.iais.kd.biovel.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import de.fraunhofer.iais.kd.biovel.i18n.I18nUtils;


public class Dwc2Csv extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// Create the logger
	private static final Logger logger = Logger.getLogger(Dwc2Csv.class.getPackage().getName());
	private static String servletWorkdir;
	protected static Properties conf;
	
	private static String xslFileUrl;

	
	//	private static String geoserverDataDir = null;


	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		servletWorkdir = config.getServletContext().getRealPath("/");
		logger.info("=== dwcimport Servlet starting up - " + new Date() + " ====");

		//Name and Path to xls File
		String xslFileName = "gbifResponse_to_csv.xsl";
		
		xslFileUrl = servletWorkdir + "WEB-INF/config/" + xslFileName;
		
		if (xslFileName == null || xslFileName.length() == 0) {

					logger.log(Level.WARNING, "Config file '" + xslFileName + "' "
							+ "cannot be accessed, falling back to default values");
					
					return;

		}

	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
		
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		if ( req.getParameter("i18n") != null ){
			String lang = req.getParameter("lang");
			if( lang.length() == 0 ){
				lang = null;
			}
			returnStringToResponse(res, I18nUtils.toJson(lang), "text/plain", "UTF-8");
			return;
		}

		processRequest(req, res);

		return;
	}
	
	
	private void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
		
		List<String> params = getParametersAsString(req);
		if (params.isEmpty()) {
			// res.sendError(arg0, arg1)
			return;
		}
		if (params.size() == 2) {
			getDwcFromUrl(params.get(0), params.get(1), res);
		} else {
			getDwcFromUrl(params.get(0), "", res);
		}

	}
	
	/**
	 * returns a string array. First parameter is the url and second the species Name
	 * if url not avaliable then only 1 element is in the array
	 * 
	 * @param req
	 * @return
	 */
	private List<String> getParametersAsString(HttpServletRequest req) {
		List<String> requestParams = new ArrayList<String>();
		String url = "";
		String scientificname = "";
		Enumeration<String> e = req.getParameterNames();
		while (e.hasMoreElements()) {
			String k = e.nextElement();
			System.out.println(k + "=" + req.getParameter(k));
			if (k.equalsIgnoreCase("url")) {
				url = req.getParameter(k);
			} else if (k.equalsIgnoreCase("scientificname")) {
				scientificname = req.getParameter(k);
			}
		}
		
		if (scientificname.length() > 0) {
			requestParams.add(scientificname);
		}
		if (url.length() > 0) {
			requestParams.add(url);
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
	public final void getDwcFromUrl(String scientificname, String url, HttpServletResponse resp) throws IOException {

		//Darvin format is requested
		String format = "darwin";
		
		//At the moment only GBIF Site, later others??
		if (url.length() == 0 ){
			url = "http://data.gbif.org/ws/rest/occurrence/list?scientificname="+scientificname+"&format="+format+""; 
		}
		
		System.out.println("url: " + url);

		try {
			URL u = new URL(url);

			URLConnection conn = u.openConnection();

			// read the Plain
			InputStream is = conn.getInputStream();
//			BufferedReader isBufferedReader = new BufferedReader(new InputStreamReader(is));
//			resp.setContentType("text/plain");
//			resp.setCharacterEncoding("UTF-8");
//
//
//			String line;
//			String messagebody = "";
//			while ((line = isBufferedReader.readLine()) != null) {
//				messagebody += line;
//			}
//			isBufferedReader.close();
			
//			System.out.println("line: " + messagebody);
//			transform(messagebody);
			
			transform(is);
			
//			is.close();
			
			
			//create response
			PrintWriter out = resp.getWriter();
			out.println("finish");
		    out.println("scientificname: ->> " + scientificname + " <<- was successfully requested" );
			
			//gbif response to out		    
//			OutputStream os = resp.getOutputStream();
//
//			byte[] messagebytes = messagebody.getBytes();
//			ByteArrayOutputStream baos = new ByteArrayOutputStream(messagebytes.length);
//			baos.write(messagebytes);
//			baos.writeTo(os);
//			os.flush();
//			os.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		

	}
			
	private void transform (InputStream is){//(String inString){
	
//		System.out.println("inString: " + inString);
		System.out.println(xslFileUrl);
		
	// Create transformer factory
		TransformerFactory factory = TransformerFactory.newInstance();
		
		// Use the factory to create a template containing the xsl file
		try {
//			Templates template = factory.newTemplates(new StreamSource(getReader(xslFileUrl)));
			
			InputStream xsltProgramStream = new FileInputStream(new File(xslFileUrl));			
			Templates template = factory.newTemplates(new StreamSource(xsltProgramStream));
			
		// Use the template to create a transformer
		Transformer xformer = template.newTransformer();
		
		// Prepare the input and output files
//		Reader sourceReader = new StringReader(inString);
		Source source = new StreamSource(new InputStreamReader(is));
		StringWriter resultStr = new StringWriter();
		Result result = new StreamResult(resultStr);
		
		// Apply the xsl file to the source file and write the result to the
		// output file
		xformer.transform(source, result);
		
//		System.out.println("result: \n" + resultStr.toString());
			
			
			
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	BufferedReader getReader(String fileUrl) throws IOException {
		
		System.out.println(fileUrl);
		
		InputStreamReader reader;
		try {
			reader = new FileReader(fileUrl);
		} catch (FileNotFoundException e) {
			// try a real URL instead
			URL url = new URL(fileUrl);
			reader = new InputStreamReader(url.openStream());
		}
		return new BufferedReader(reader);
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


}

