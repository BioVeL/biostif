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
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.SAXException;

//import de.fraunhofer.iais.kd.biovel.shim.util.ShimDateConverter;

import au.com.bytecode.opencsv.CSVReader;

public class Csv2Json extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// Create the logger
	private static final Logger logger = Logger.getLogger(Csv2Json.class
			.getPackage().getName());
	protected static Properties conf;

    public static final String BIOSTIF_SERVER_CONF = "biostif.server.conf";    
    public ServletConfig config;
    
	@SuppressWarnings("rawtypes")
	private Map valueMap;
	@SuppressWarnings("rawtypes")
	private Map descriptionMap;
	@SuppressWarnings("rawtypes")
	private Map tableMap;
	
	private String time = "";
	private String popuplabel = "";
	
	int lineNr = 0;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		this.config = config;

		logger.info("=== CSV2JsonImport Servlet starting up - " + new Date()
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
            throw new ServletException("--- Csv2Json init-param biostif.server.conf not specified");
        }

        File confFile = new File(confFileName);
        if (!confFile.isAbsolute()) {
            confFile = new File(config.getServletContext().getRealPath("/"), confFileName);
        }
        if (!confFile.canRead()) {
            throw new ServletException("--- Csv2Json cannot read file: \"" + confFileName + "\"");
        }

        Properties props = new Properties();
        try {
            FileInputStream fis = new FileInputStream(confFile);
            props.load(fis);
        } catch (IOException e) {
            throw new ServletException(" Csv2Json cannot read properties: " + confFile + " : " + e.getMessage(), e);
        }

        config.getServletContext().setAttribute(BIOSTIF_SERVER_CONF, props);
        logger.info("--- Csv2Json.initBase(..) successful");
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
		String jsonResult = "";

		if (isMultipart) {
			logger.warning("Multipath request is not implemented, use parameter request instead");
			return;
		}
		 else{
		
			 Map<String, String> params = getParametersAsString(req);
			
			 if (params.isEmpty()) {
				 logger.warning("CSV2Json request contains no source parameter");
				 return;
			 }
			 
			 if(params.containsKey("time")){					
				time = params.get("time");					
			 }
			 
			 if(params.containsKey("popuplabel")){					
				 popuplabel = params.get("popuplabel");					
			 }
			 
	
			 if(params.containsKey("source")){
				 
				 try {
					 URL u = new URL(params.get("source"));
					 
//					 String mimeType = getServletContext().getMimeType(u.toString());
//					 
//					 if (!mimeType.equals("text/plain")){						 
////						 res.setStatus(415);
//						 res.setStatus(415, "mimeType " +mimeType+" is not accepted - get a CSV File instead");
//						 return;						 
//					 }
					
					 URLConnection conn = u.openConnection();
					
					 // read the Plain
					 InputStream is = conn.getInputStream();
					 
					 //copy Stream an che if xml or not
					 StringWriter is_c = new StringWriter();
					 IOUtils.copy(is, is_c);					
					 String isString = is_c.toString();
					 
					 if(isString.startsWith("<?xml")){
						 logger.info("XML files are not allowed in the Ccv2Json Method, send error 415");
						 res.sendError(res.SC_UNSUPPORTED_MEDIA_TYPE, "mimeType 'application/xml' is not accepted - get a CSV File instead");
						 logger.info("Csv2Json: Servlet execution completed");
					 }else{
						 if(!isString.startsWith("[")){							 
							 is = new ByteArrayInputStream(isString.getBytes());
							 //parse to XML DOM
							 jsonResult = parseInputStream(is, res);							 
						 }else{
							 logger.info("Document starts with '[' - it seems not to be a CSV document, Send Error 406: not Acceptable");
							 res.sendError(res.SC_NOT_ACCEPTABLE, "Document starts with '[' - it seems not to be a CSV document");
							 logger.info("Csv2Json: Servlet execution completed");
						 }
					 }
					 
//					 is = new ByteArrayInputStream(isString.getBytes());					
//					 //parse to XML DOM
//					 jsonResult = parseInputStream(is);
				 
				 }catch (IOException e) {
				 // TODO Auto-generated catch block
					 logger.info("Error on reading document, send Error 415: " + e.getMessage());
					 res.sendError(res.SC_UNSUPPORTED_MEDIA_TYPE, "Document could not be read: " + e.getMessage());							 
					 e.printStackTrace();	
					 logger.info("Csv2Json: Servlet execution completed");
				 } catch (ParserConfigurationException e) {
					 logger.info("Error Parsing the document, send error 206: " + e.getMessage());
					 res.sendError(res.SC_NOT_ACCEPTABLE, "Error parsing the document: " + e.getMessage());							 
					 e.printStackTrace();
					 logger.info("Csv2Json: Servlet execution completed");
				 } catch (SAXException e) {
					 logger.info("Error reading xml document, send error 406");
					 res.sendError(res.SC_NOT_ACCEPTABLE, "Error while parsing xml document: " + e.getMessage());							 
					 e.printStackTrace();
					 logger.info("Csv2Json: Servlet execution completed");
				 }
				 
			 }
		
		 }

		// jsonResult to out
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");
		OutputStream os = res.getOutputStream();
		//
		byte[] jsonResultBytes = jsonResult.getBytes();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				jsonResultBytes.length);
		baos.write(jsonResultBytes);
		baos.writeTo(os);
		os.flush();
		os.close();

		logger.info("#### Csv2Json: Servlet execution completed");
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String parseInputStream(InputStream csv_is, HttpServletResponse res)
			throws ParserConfigurationException, SAXException, IOException {

		lineNr = 0;
		
		CSVReader reader = new CSVReader(new InputStreamReader(csv_is));
		
		List<String[]> lines = reader.readAll();
		
		boolean docHeader = true;
		List<String> firstLine = Arrays.asList(lines.get(0));
		if(firstLine.indexOf("decimalLatitude") == -1){
			docHeader = false;
		}
		
		List<String> csvHeader = null;
		
		if(docHeader){
			
			csvHeader = firstLine;
		
		}else{
			
			// read CSV Header (von Cherian)
	        Properties props = (Properties) config.getServletContext().getAttribute(BIOSTIF_SERVER_CONF);
	        String urlCsvHeader = props.getProperty("URL_CSV_HEADER");
	        
	        if(urlCsvHeader == null || urlCsvHeader.length() == 0){
	        	urlCsvHeader = props.getProperty("URL_CSV_HEADER_LOCAL");
	        }

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
			BufferedReader isBufferedReader = new BufferedReader(
					new InputStreamReader(isc));

			String line = "";
//			List<String> csvHeader = null;
			while ((line = isBufferedReader.readLine()) != null) {
				csvHeader = Arrays.asList(line.split(","));
			}
			isBufferedReader.close();
			isc.close();
			
		}

		int latPos = csvHeader.indexOf("decimalLatitude");
		int lonPos = csvHeader.indexOf("decimalLongitude");

		int idPos = csvHeader.indexOf("occurrenceID");
		
		
		int loop = 0;
		int firstline = 0;

		if (idPos < 0) {
			throw new ParserConfigurationException("No ID field found");
		}
		if (latPos < 0) {
			throw new ParserConfigurationException("No field with information of latitude coordinate found");
		}
		if (lonPos < 0) {
			throw new ParserConfigurationException("No field with information of longitude coordinate found");
		}
		if(docHeader){
			//document has a header
			firstline = 1;			
		}
		
		StringBuffer jsonResult = new StringBuffer();
		
		int failedLoop = 0;
		
		for (int j = lines.size(); j > firstline; j--) {
			
			lineNr = j;

			if(lines.get(j - 1).length == csvHeader.size()){
				
				String id = lines.get(j - 1)[idPos];
						
				String lat = lines.get(j - 1)[latPos].replace(",", ".");
				String lon = lines.get(j - 1)[lonPos].replace(",", ".");
				
//				if(lat.indexOf("n") > 0 || lat.indexOf("n") > 0
//						|| lat.indexOf("W") > 0 || lat.indexOf("W") > 0 
//						|| lat.indexOf("-") > 0 || lon.indexOf("-") > 0
////						|| (lat.equals("0")  && lon.equals("0"))
////						|| (lat.equals("0.0")&& lon.equals("0.0"))
//						){
//					
//					lat = "";
//					lon = "";
//				}
				
//				System.out.println("Row number : "+ lineNr  + " lat/lon: " + lat + " " + lon );
				
				try{
					Double.parseDouble(lat);
					Double.parseDouble(lon);
				} catch (Exception e) {
					
//					System.out.println("Row number : "+ lineNr  + " failed lat/lon: " + lat + " " + lon );
					failedLoop++;
					
					lat = "";
					lon = "";
				}
				
				if (lat.length() > 0 && lon.length() > 0 ) {
					
					double dlat = 0;
					double dlon = 0;
					
					try {
						dlat = Double.parseDouble(lat);
						dlon = Double.parseDouble(lon);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						logger.info("Row number " + (j - 1) + " with no valid lat lon information with occurrenceID: " + id + " will not be converted");
						continue;
					}
					
	
					loop++;
	
					valueMap = new HashMap();
					descriptionMap = new HashMap();
					tableMap = new HashMap();
	
					// collect values from CSV line
					valueMap.put("lat", dlat);
					valueMap.put("lon", dlon);
	
					addLineValuesToMap(csvHeader, lines.get(j - 1));
	
					String description = createDescription();
					valueMap.put("description", description);
					valueMap.put("tableContent", tableMap);
	
					String jsonItem = createJson();
					jsonResult.append(",").append(jsonItem);
				}
				
			} else{
				
				if(lines.get(j - 1).length > csvHeader.size()){
//					System.out.println("line: " + lines.get(j - 1));
					logger.warning("line "+ (j - 1) + " number of row elements -" + lines.get(j - 1).length+ "- is greater then from header -" + csvHeader.size() + " -> line dropped");
				}else if(lines.get(j - 1).length < csvHeader.size()){
//					System.out.println("line: " + lines.get(j - 1));
//					for(int ll = 0; ll < lines.get(j - 1).length; ll++){
//						System.out.println(ll + " - " + lines.get(j - 1)[ll]);
//					}					
					logger.warning("line "+ (j - 1) + " number of row elements -" + lines.get(j - 1).length+ "- is smaller then from header -" + csvHeader.size() + " -> line dropped");
				}
				
			}
		}
		
		if(loop > 0)jsonResult.deleteCharAt(0);

		logger.info("Csv2Json: " + failedLoop + " rows has wrong lat or lon");
		logger.info("###### Csv2Json finish: " + loop + "rows of " + (lines.size()-1) + "from csv was converted");

		return "["+jsonResult.toString()+"]";
	}

	@SuppressWarnings({ "unchecked" })
	private void addLineValuesToMap(List<String> csvHeader, String[] csvLine) {

		List<String> valueMapElements = Arrays.asList("occurrenceid", "latestdatecollected", "earliestdatecollected");
		List<String> descriptionMapElements = Arrays.asList("namecomplete",	"dataprovidername", "occurrenceid", "earliestdatecollected", "latestdatecollected", "dataresourcerights");

		String placeValue = "";
		if(popuplabel.length() == 0){
			placeValue = "country";
		} else {
			placeValue = popuplabel;
		}
		
		for (int i = 0; i < csvHeader.size() - 1; i++) {
			
			int ix = csvHeader.indexOf(csvHeader.get(i));
			
			if (csvHeader.get(i).toLowerCase().equals(placeValue.toLowerCase())) {
				valueMap.put("place", csvLine[ix]);
			}
			
			if (valueMapElements.contains(csvHeader.get(i).toLowerCase())) {
				
				if (csvHeader.get(i).toLowerCase().equals("occurrenceid")) {
					valueMap.put("id", csvLine[ix]);
				}
				
				
				if (time.length() > 0 && csvHeader.get(i).toLowerCase().equals(time.toLowerCase()) ) {
					if(csvLine[ix].length() > 0){
//						valueMap.put("time", csvLine[ix]);
						
                        String timeValue = new ShimDateConverter().convert(csvLine[ix]);
//                        if(timeValue.startsWith("unknown")){
//                        	System.out.println(" Row number " + lineNr + " contains wrong Date: " + timeValue );
//                        }
                        valueMap.put("time", timeValue);
						
					} else {
						valueMap.put("time", "");
					}
				}
				if (time.length() == 0 && csvHeader.get(i).toLowerCase().equals("latestdatecollected")) {
								
					if(csvLine[ix].length() > 0){
//						valueMap.put("time", csvLine[ix]);
						
                        String timeValue = new ShimDateConverter().convert(csvLine[ix]);
                        
//                        if(timeValue.startsWith("unknown")){
//                        	System.out.println(" Row number " + lineNr + " contains wrong Date: " + timeValue );
//                        }
                        
                        valueMap.put("time", timeValue);
					} else {
						valueMap.put("time", "");
					}											
				}
				
				if (time.length() == 0 && csvHeader.get(i).toLowerCase().equals("earliestdatecollected") &&
						(valueMap.get("time") == null || valueMap.get("time").toString().length() == 0)) {
					if(csvLine[ix].length() > 0){
//						valueMap.put("time", csvLine[ix]);
						
                        String timeValue = new ShimDateConverter().convert(csvLine[ix]);
                        
//                        if(timeValue.startsWith("unknown")){
//                        	System.out.println(" Row number " + lineNr + " contains wrong Date: " + timeValue );
//                        }
                        
                        valueMap.put("time", timeValue);
						
					} else {
						valueMap.put("time", "");
					}
				}
			}

			if (descriptionMapElements.contains(csvHeader.get(i).toLowerCase())) {
				
				descriptionMap.put(csvHeader.get(i), csvLine[ix]);	
				
			}
			tableMap.put(csvHeader.get(i), csvLine[ix]);
		}

	}

	private String createDescription() {

		String description = "";

		description = "<p><table><tr>"
				+ "<th>TaxonName</th>"
				+ "<th>OccurrenceID</th>"
				+ "<th>DataProvider</th>"
				+ "<th>earliestDateCollected</th>"
				+ "<th>latestDateCollected</th>"
				+ "<th>dataResourceRights</th>"
				+ "</tr>"
				+ "<tr>"
				+ "<td>";
		
				
			if(descriptionMap.get("nameComplete") != null){
				description += descriptionMap.get("nameComplete").toString().replace('"', '\"');
			} else {
				description +="";
			}
		
			description+= "</td>"
				+ "<td>";
			
			if(descriptionMap.get("occurrenceID") != null){
				description += descriptionMap.get("occurrenceID").toString().replace('"', '\"');
			} else {
				description +="";
			}
		
			description+= "</td>"
				+ "<td>";
					
			if(descriptionMap.get("dataProviderName") != null){
				description += descriptionMap.get("dataProviderName").toString().replace('"', '\"');
			} else {
				description +="";
			}
			
			description+= "</td>"
					+ "<td>";
						
			if(descriptionMap.get("earliestDateCollected") != null){
				description += descriptionMap.get("earliestDateCollected").toString().replace('"', '\"');
			} else {
				description +="";
			}
				
			description+= "</td>"
					+ "<td>";
							
			if(descriptionMap.get("latestDateCollected") != null){
				description += descriptionMap.get("latestDateCollected").toString().replace('"', '\"');
			} else {
				description +="";
			}
			
			description+= "</td>"
					+ "<td>";
							
			if(descriptionMap.get("dataResourceRights") != null){
				description += descriptionMap.get("dataResourceRights").toString().replace('"', '\"');
			} else {
				description +="";
			}

			description += "</td></tr></table></p>";

		return description;
	}

	private String createJson() {

		String json = "";

		try {

			ObjectMapper mapper = new ObjectMapper();
			if(valueMap != null)json = mapper.writeValueAsString(valueMap);

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


	/**
	 * returns a string array. First parameter is the url and second the species
	 * Name if url not avaliable then only 1 element is in the array
	 * 
	 * @param req
	 * @return
	 */
    private Map<String,String> getParametersAsString(HttpServletRequest req) {
		 	
	 Map<String, String> requestParams = new HashMap<String, String>();
	 String source = "";
	 String time = "";
	 String popuplabel = "";
	 Enumeration<String> e = req.getParameterNames();

	 while (e.hasMoreElements()) {
		 String k = e.nextElement();
	//	 System.out.println(k + "=" + req.getParameter(k));
		 
		 if (k.equals("source")) {
			source = req.getParameter(k);
		 }
		 
		 if (k.equals("time")) {
			 time = req.getParameter(k);
		 }
		 
		 if (k.equals("popuplabel")) {
			 popuplabel = req.getParameter(k);
		 }
	 }

	 if (source.length() > 0) {
		 requestParams.put("source", source);
	 }
	 
	 if (time.length() > 0) {
		 requestParams.put("time", time);
	 }
	 
	 if (popuplabel.length() > 0) {
		 requestParams.put("popuplabel", popuplabel);
	 }
	
	 return requestParams;
	
	 }

}
