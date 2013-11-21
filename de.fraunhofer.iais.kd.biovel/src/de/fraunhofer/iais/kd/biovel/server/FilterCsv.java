package de.fraunhofer.iais.kd.biovel.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVReader;

public class FilterCsv extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// Create the logger
	private static final Logger logger = Logger.getLogger(FilterCsv.class.getName());
	protected static Properties conf;
	
    public static final String BIOSTIF_SERVER_CONF = "biostif.server.conf";    
    public ServletConfig config;


	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		this.config = config;

		logger.info("=== FilterCsv Servlet starting up - " + new Date()
				+ " ====");
		
	      try {
	            initBase(this.config);
	        } catch (Throwable e) {
	            logger.log(Level.SEVERE, e.getMessage(), e);
	            throw new ServletException();
	        }
	}
	
    private void initBase(ServletConfig config) throws ServletException {

        String confFileName = null;
        if ((confFileName = System.getProperty(BIOSTIF_SERVER_CONF)) == null) {
            confFileName = config.getInitParameter(BIOSTIF_SERVER_CONF);
        }
        if (confFileName == null) {
            throw new ServletException("--- FilterCsv init-param biostif.server.conf not specified");
        }

        File confFile = new File(confFileName);
        if (!confFile.isAbsolute()) {
            confFile = new File(config.getServletContext().getRealPath("/"), confFileName);
        }
        if (!confFile.canRead()) {
            throw new ServletException("--- FilterCsv cannot read file: \"" + confFileName + "\"");
        }

        Properties props = new Properties();
        try {
            FileInputStream fis = new FileInputStream(confFile);
            props.load(fis);
        } catch (IOException e) {
            throw new ServletException(" FilterCsv cannot read properties: " + confFile + " : " + e.getMessage(), e);
        }

        config.getServletContext().setAttribute(BIOSTIF_SERVER_CONF, props);
        logger.info("--- FilterCsv.initBase(..) successful");
    }

	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doPost(req, res);

	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		try {

			processRequest(req, res);

		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}

	private void processRequest(HttpServletRequest req, HttpServletResponse res)
			throws IOException, FileUploadException,
			ParserConfigurationException, SAXException {

		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);

		// requested result:
		String filterResult = "";

		if (isMultipart) {
			logger.warning("Multipath request is not implemented, use parameter request instead");
			return;
		}
		else{
		
		 Map<String, String> params = getParametersAsString(req);
		
		 if (params.isEmpty()) {
			 logger.warning("Request contains no parameters");
			 return;
		 }

		 if(params.containsKey("source")){		 
		
			 try {
				 URL u = new URL(params.get("source"));				
				 URLConnection conn = u.openConnection();
				 InputStream is = conn.getInputStream();				 
				 
				 InputStream fis = req.getInputStream();
				 
				 //copy Stream an che if xml or not
				 StringWriter is_c = new StringWriter();
				 IOUtils.copy(is, is_c);					
				 String isString = is_c.toString();
				 
				 if(isString.startsWith("<?xml")){
					 logger.info("XML files are not allowed in the Ccv2Json Method");
					 res.setStatus(415, "mimeType 'application/xml' is not accepted - get a CSV File instead");
				 }else{
					 if(!isString.startsWith("[")){							 
						 is = new ByteArrayInputStream(isString.getBytes());
						 //parse to XML DOM
						 filterResult = filterCsv(is, fis, res);						 
					 }else{
						 logger.info("Document starts with '[' - it seems not to be a CSV document");
						 res.setStatus(415, "Document starts with '[' - it seems not to be a CSV document");							 
					 }
				 }
				
//				 filterResult = filterCsv(is, fis);

				 is.close();
				 fis.close();
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 		 
		 	} else {
				 logger.warning("Request contains no source parameter");
				 return;
		 	}
		 }


		//System.out.println("filter output: \n" + res);
		// jsonResult to out
		res.setContentType("text/plain");
		res.setCharacterEncoding("UTF-8");
		OutputStream os = res.getOutputStream();
		//
		byte[] jsonResultBytes = filterResult.getBytes();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				jsonResultBytes.length);
		baos.write(jsonResultBytes);
		baos.writeTo(os);
		os.flush();
		os.close();
		
		logger.info("#### FilterCsv: Servlet execution completed");

	}



	private String filterCsv(InputStream csv_is, InputStream filterIs, HttpServletResponse res) {
		
		//read filterList
		BufferedReader fisBufferedReader = new BufferedReader(new InputStreamReader(filterIs));
		String filterString = "";
		String fline;

		try {
			while ((fline = fisBufferedReader.readLine()) != null) {
				filterString += fline;
			}

			fisBufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		System.out.println("filterstring: \n" + filterString);
		
		List<String> filterList = Arrays.asList(filterString.split(","));
		
		// read CSV Header (von Cherian)
        Properties props = (Properties) config.getServletContext().getAttribute(BIOSTIF_SERVER_CONF);
        String urlCsvHeader = props.getProperty("URL_CSV_HEADER");
        
        if(urlCsvHeader == null || urlCsvHeader.length() == 0){
        	urlCsvHeader = props.getProperty("URL_CSV_HEADER_LOCAL");
        }

		List<String> csvHeader = null;		
		String[] firstline = null;
		CSVReader reader = null;
		
		try {
			URL url = new URL(urlCsvHeader);
			URLConnection conn = url.openConnection();
			
			if(conn.getContentLength() < 1){
				logger.info("Csv2Json: get the local URL_CSV_HEADER");
				urlCsvHeader = props.getProperty("URL_CSV_HEADER_LOCAL");
				url = new URL(urlCsvHeader);
				conn = url.openConnection();
			}
			if(conn.getContentLength() < 1){

				try {
					throw new ServletException("please check the value of the URL_CSV_HEADER on server.config file");
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					res.sendError(res.SC_BAD_REQUEST, e.getMessage());
					return"";
				}
			}
			
			InputStream isc = conn.getInputStream();
			BufferedReader isBufferedReader = new BufferedReader(new InputStreamReader(isc));
			
			String line = "";
			
			while ((line = isBufferedReader.readLine()) != null) {
				csvHeader = Arrays.asList(line.split(","));
			}
			
			isBufferedReader.close();
			isc.close();
			
			char separator = ',';
			char quotechar = '"';
			
			reader = new CSVReader(new InputStreamReader(csv_is), separator, quotechar);
//			System.out.println("reader: " + reader.readAll().size());
			firstline = reader.readNext();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		List<String[]> lines = new LinkedList<String[]>();
		
		int idx = csvHeader.indexOf( "occurrenceID" );
		int loop = 0;
		boolean docHeader = true;
		List<String> firstLine = Arrays.asList(firstline);
		if(firstLine.indexOf("decimalLatitude") == -1){
			docHeader = false;
			
			loop++;
			
			String idValue = firstline[idx];
			if(filterList.contains(idValue)){
				String[] newFirstLine = new String[firstline.length];
				
				for(int tt = 0; tt<firstline.length;tt++){
					
						if(firstline[tt].contains(",")){
							String line = '"'+firstline[tt] + '"';
							newFirstLine[tt] =line;							
						} else {
							newFirstLine[tt] =firstline[tt];
						}				
					}
				lines.add(newFirstLine);	
			}
			
		}
		
		if (firstLine.indexOf("decimalLatitude") < 0) {
			
			logger.info("Apparently no header with decimalLatitude column " + 
					 firstline + " -> get headers from repository document");
		} else {
			csvHeader = firstLine;
			idx = csvHeader.indexOf( "occurrenceID" );
			logger.info("-Header from document: ");
		}
		
		String[] nextLine = null;
		int ixID = 0;
		
		int noCont = 0;
		int cont = 0;
		
		try {

			while ((nextLine = reader.readNext()) != null) {

				loop++;

				if (nextLine.length > idx) {

					String idValue = nextLine[idx];
//					System.out.println("idValue: " + idValue + " idx: " + idx);
//					System.out.println(idValue);
					if (idValue.length() == 0) {
						ixID++;
					} else {

						if (filterList.contains(idValue)) {
							
							cont++;
//							System.out.println("filterlost contains idvalue");

							String[] newNextLine = new String[nextLine.length];

							for (int tt = 0; tt < nextLine.length; tt++) {
								
//								System.out.println("nextLine[tt]: " + nextLine[tt]);

								if (nextLine[tt].indexOf('"') >= 0) {
									// System.out.println("contains DQ at " +
									// nextLine[tt].indexOf('"'));
									// System.out.println("nextLine[tt]: " +
									// nextLine[tt]);
									nextLine[tt] = nextLine[tt].replaceAll(
											"\"", "");
									// System.out.println("         weg: " +
									// nextLine[tt]);
								} else if (nextLine[tt].contains(",")) {
									String line = '"' + nextLine[tt] + '"';
									newNextLine[tt] = line;
								} else {
									newNextLine[tt] = nextLine[tt];
								}
							}
							lines.add(newNextLine);
						} else {
							noCont++;
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("\nfilterList " + filterList.size()  +" contains(idValue):" + cont + " NOT contains:" + noCont);
		
		logger.info("****** FilterCsv number of filterids: " + filterList.size());
		logger.info("****** FilterCsv Filterresult: not avaiable occourenceIDs in document: " + ixID);
		logger.info("****** FilterCsv Filterresult: # lines:" + loop + " filtered lines: " + lines.size());
		
		StringBuffer filterResult = new StringBuffer();

		if(lines.size() == 0){
			filterResult.append("Error while filtering: "+lines.size() + " from " + loop + " lines filtered, because " + ixID + " occurrenceIDs was not foundet in the document");
		} else {
		
			if(docHeader){
				//document has a header
				String stringFirstLine = StringUtils.join(firstLine, ",");
				filterResult.append(stringFirstLine).append("\n");
			}
			
			for (int j = lines.size(); j > 0; j--) {
				
	//			for(int tt = 0; tt<lines.get(j - 1).length;tt++){
	//			System.out.println("- " + lines.get(j - 1)[tt] +"\n");
	//			}
				
				String stringLine = StringUtils.join(lines.get(j-1), ",");
				filterResult.append(stringLine).append("\n");
				
	//			System.out.println(stringLine);
			}
		}
		
		return filterResult.toString();		
	}


	/**
	 * returns a string array. First parameter is the url and second the species
	 * Name if url not avaliable then only 1 element is in the array
	 * 
	 * @param req
	 * @return
	 */
	 private Map<String,String> getParametersAsString(HttpServletRequest req){
	
	 Map<String, String> requestParams = new HashMap<String, String>();
	 String url = "";
	 Enumeration<String> e = req.getParameterNames();
	 
	 while (e.hasMoreElements()) {
		 String k = e.nextElement();
//		 System.out.println(k + "=" + req.getParameter(k));
		 
		 if (k.equals("source")) {
			 url = req.getParameter(k);
		 }
	 }
	
	 if (url.length() > 0) {
		 requestParams.put("source", url);
	 }
	
	 return requestParams;
	
	 }

}
