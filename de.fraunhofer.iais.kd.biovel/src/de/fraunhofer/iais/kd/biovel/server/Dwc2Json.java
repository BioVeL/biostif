package de.fraunhofer.iais.kd.biovel.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import de.fraunhofer.iais.kd.biovel.shim.util.ShimDateConverter;


public class Dwc2Json extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// Create the logger
	private static final Logger logger = Logger.getLogger(Dwc2Json.class.getPackage().getName());
	protected static Properties conf;
	
	@SuppressWarnings("rawtypes")
	private Map valueMap;
	@SuppressWarnings("rawtypes")
	private Map descriptionMap;
	@SuppressWarnings("rawtypes")
	private Map tableMap;
	
	private String time = "";
	
	@Override
	public void init(ServletConfig config) throws ServletException {	
		super.init(config);
		
		logger.info("=== JsonImport Servlet starting up - " + new Date() + " ====");		
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
		
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

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

	
	

	private void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, FileUploadException, ParserConfigurationException, SAXException  {
		
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
		
		//requested result:
		String jsonResult = "";
		
		if(isMultipart){
			
//			jsonResult = processMultipathReq(req);
			logger.warning("Multipath request is not implemented, use parameter request instead");
			return;
			
		}else{

			Map<String, String> params = getParametersAsString(req);
			
			if (params.isEmpty()) {
				logger.warning(" Dwc2Json request contains no source parameter");
				return;
			}
			
			if(params.containsKey("time")){					
				time = params.get("time");					
			}
			
			if(params.containsKey("source")){
				 
				 try {
					 URL u = new URL(params.get("source"));
					 
//					 String mimeType = getServletContext().getMimeType(u.toString());
//					 
//					 if (!mimeType.equals("application/xml")){
////						 res.setStatus(415);
//						 res.setStatus(415, "mimeType " +mimeType+" is not accepted - get a XML File instead");
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
						 is = new ByteArrayInputStream(isString.getBytes());
						 //parse to XML DOM
						 jsonResult = parseInputStream(is);
					 }else{						 
						 logger.info(" text/plain files are not allowed in the Dwc2Json Method");
						 res.setStatus(415, "mimeType 'text/plain' is not accepted - get a XML(DwC) File instead");
					 }
				 
				 }catch (IOException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();	
				 } catch (ParserConfigurationException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
				 } catch (SAXException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
				 }
				 
			}
		}
		
//		System.out.println("\n" + jsonResult);
		
		//jsonResult to out		    
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");
		OutputStream os = res.getOutputStream();
//
		byte[] jsonResultBytes = jsonResult.getBytes();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(jsonResultBytes.length);
		baos.write(jsonResultBytes);
		baos.writeTo(os);
		os.flush();
		os.close();

	}
	
	/**
	 * Get the uploaded File and Filename from Request
	 * 
	 * @param req
	 * @throws FileUploadException
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
//	
//	private String processMultipathReq(HttpServletRequest req) throws FileUploadException, IOException, ParserConfigurationException, SAXException{
//		
//		String jsonResult = "";
//		
//		// Create a new file upload handler
//		ServletFileUpload upload = new ServletFileUpload();
//		
//		// Parse the request
//		FileItemIterator iter = upload.getItemIterator(req);
//		
//		FileItemStream item = null;
//		InputStream is = null;
//		
//		while (iter.hasNext()) {
//			
//		    item = iter.next();
//		    String name = item.getFieldName();
//		    is = item.openStream();
//		    if (item.isFormField()) {
//		    	// FileName: e.g. Form field fileName with value gkz_group_schaeden.txt detected.
//		        
////		    	System.out.println("Form field " + name + " with value "
////		            + Streams.asString(is) + " detected.");
//		    } else {
//		    	// File: e.g. File field file with file name gkz_group_schaeden.txt detected.
////		        System.out.println("File field " + name + " with file name "
////		            + item.getName() + " detected.");
//		        // Process the input stream
//		    	
//		    	jsonResult = parseInputStream(is);		        
//		        
//		    }		    
//		}
//		is.close();
//		
//		return jsonResult;		
//	}

	
	@SuppressWarnings("unchecked")
	private String parseInputStream(InputStream is) throws ParserConfigurationException, SAXException, IOException{
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(is);		
		
		NodeList nodeList = null;		
		
		nodeList = doc.getElementsByTagNameNS("*", "TaxonOccurrence");
		System.out.println("\n--------- \n# nodes: " + nodeList.getLength()+"\n");
		
		int loop = 0;
			
		StringBuffer jsonResult = new StringBuffer();
		
		if (nodeList.getLength() > 0) {
			
			int nodeListLength = nodeList.getLength();
			
			for (int i = 0; i < nodeListLength; ++i) {
											
				Node node = nodeList.item(i);
				
				if(node.hasChildNodes()){
									
						String lat = getChildValue(node, "decimalLatitude").replace(",", ".");
						String lon = getChildValue(node, "decimalLongitude").replace(",", ".");

							
						try{
							Double.parseDouble(lat);
							Double.parseDouble(lon);
						} catch (Exception e) {
							
							lat = "";
							lon = "";
							
						}
						
						
//						if(lat.indexOf("n") > 0 || lat.indexOf("n") > 0
//								|| lat.indexOf("W") > 0 || lat.indexOf("W") > 0 
//								|| lat.indexOf("-") > 0 || lon.indexOf("-") > 0
////								|| (lat.equals("0")  && lon.equals("0"))
////								|| (lat.equals("0.0")&& lon.equals("0.0"))
//								){
//							
//							lat = "";
//							lon = "";
//						}

						if(lat.length() > 0 && lon.length() > 0){
								
							loop++;							
							addNodeValuesToMap(node);
																						
							String description = createDescription();
							valueMap.put("description", description);
							valueMap.put("tableContent", tableMap);
								
							String jsonItem = createJson();
							jsonResult.append(",").append(jsonItem);
						}			
					}		
			}		
		}
		
		System.out.println("\nNodes with Coordinates: " + loop);
		
		if(loop > 0) jsonResult.deleteCharAt(0);
		return "["+jsonResult.toString()+"]";
	}
	
	private String createDescription(){
		
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
			
			if(descriptionMap.get("OccurrenceID") != null){
				description += descriptionMap.get("OccurrenceID").toString().replace('"', '\"');
			} else {
				description +="";
			}
		
			description+= "</td>"
				+ "<td>";
					
			if(descriptionMap.get("dataProvider") != null){
				description += descriptionMap.get("dataProvider").toString().replace('"', '\"');
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
	
	
	private String createJson(){
		
		String json = "";
        
        try {
        	
        	ObjectMapper mapper = new ObjectMapper();
//			System.out.println(mapper.writeValueAsString(valueMap));
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
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addNodeValuesToMap(Node node) {		
	
		valueMap = new HashMap();
		descriptionMap = new HashMap();
		tableMap = new HashMap();
		
		if(node.hasAttributes()){
			
			String gbifKey = getGbifKey(node).toString().replace('"', '\"');
//			System.out.println("\nTaxonOccurrence: " + gbifKey);
			valueMap.put("id", gbifKey);
			tableMap.put("OccurenceID", gbifKey);
			descriptionMap.put("OccurrenceID", gbifKey);
		}
		
		//get informations from TaxonOccurrence ParentNodes
		collectParentNodeValues(node.getParentNode());
		
		//get informations from TaxonOccurrence ChildNodes
		collectChildNodeValues(node);
		
		String[] tags = {"catalogNumber", "earliestDateCollected", "latestDateCollected","nameComplete","place"};
		for (int i=0; i < tags.length; i++) {
			if (tableMap.get(tags[i]) == null) {
				tableMap.put(tags[i], "");
			}
		}
	
		if (valueMap.get("time") == null) {
			valueMap.put("time", "");
		}
		
        if (valueMap.get("time").toString().length() > 0){
            String timeValue = new ShimDateConverter().convert(valueMap.get("time").toString());
            valueMap.put("time", timeValue);
        }
					
		
	}
	
	private String getGbifKey(Node node){
		
		String attribute = "";
		
		NamedNodeMap mmn = node.getAttributes();
		if (mmn.getLength() > 0) {
			for (int k = 0; k < mmn.getLength(); k++) {
				if(mmn.item(k).getLocalName().equals("gbifKey")){
					attribute = mmn.item(k).getNodeValue();
				}					
			}
		}		
		return attribute;
	}
	
	
	private String getChildValue(Node node, String nodeName){
		
		String value = "";		
		NodeList nodelist = node.getChildNodes();
		
		for(int i = 0; i < nodelist.getLength(); i++){
			
			if(nodelist.item(i).getLocalName() != null){
				if(nodelist.item(i).getLocalName().equals(nodeName)){				
					value = nodelist.item(i).getTextContent();
				}
			}
		}		
		return value;
	}
	
	
	@SuppressWarnings("unchecked")
	private void collectChildNodeValues(Node node){
		
		//add all tablevalues here
		String[] tags = {"catalogNumber", "earliestDateCollected", "latestDateCollected"};
		
		for(int i = 0; i < tags.length; i++){
			
			String value = getChildValue(node, tags[i]).toString().replace('"', '\"');
			if(value.length() > 0){
//				System.out.println(tags[i]+": " + value);					
				tableMap.put(tags[i], value);			
			}
		}
		
		String earliestDateCollected = getChildValue(node, "earliestDateCollected").toString().replace('"', '\"');
		if(earliestDateCollected.length() > 0){
//			System.out.println("earliestDateCollected: " + earliestDateCollected);					
			descriptionMap.put("earliestDateCollected", earliestDateCollected);
		}
		
		String latestDateCollected = getChildValue(node, "latestDateCollected").toString().replace('"', '\"');
		if(latestDateCollected.length() > 0){
//			System.out.println("earliestDateCollected: " + earliestDateCollected);					
			descriptionMap.put("latestDateCollected", latestDateCollected);
			
			if(time.length() > 0){
				valueMap.put("time", time);
			} else{
				valueMap.put("time", latestDateCollected);
			}
			
		}
		
		String taxonName = getChildValue(node, "nameComplete").toString().replace('"', '\"');		
		if(taxonName.length() > 0){
//			System.out.println("nameComplete: " + taxonName);					
			descriptionMap.put("nameComplete", taxonName);			
			tableMap.put("nameComplete", taxonName);		
		}
		
		
		String lat = getChildValue(node, "decimalLatitude").replace(",", ".");
		if(lat.length() > 0){
			
			double zahl = 0;
			try {
				zahl = Double.parseDouble(lat);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
//			System.out.println("lat: " + zahl);					
			valueMap.put("lat", zahl);
		}
		
		String lon = getChildValue(node, "decimalLongitude").replace(",", ".");
		if(lon.length() > 0){
			
			double zahl = 0;
			try {
				zahl = Double.parseDouble(lon);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			System.out.println("lon: " + zahl);					
			valueMap.put("lon", zahl);
		}
		
		String country = getChildValue(node, "country").toString().replace('"', '\"');
		if(country.length() > 0){
//			System.out.println("country: " + country);					
			valueMap.put("place", country);
			tableMap.put("place", country);
			
		}
		

		// Taxon Key
//		if(node.getLocalName() != null){
//			if((node.getLocalName().equals("TaxonConcept"))){
//				
//				if(node.hasAttributes()){
//					
//					String gbifKey = getGbifKey(node);
//					if(gbifKey.length() > 0){
//						System.out.println("TaxonConcept: " + gbifKey);
//						valueMap.put("TaxonConcept", gbifKey);
//					}
//				}			
//			}
//		}
		
		
		if(node.hasChildNodes()){
			
			NodeList nl = node.getChildNodes();
			
			for(int i = 0; i < nl.getLength(); i++){				
				collectChildNodeValues(nl.item(i));
					
			}				
		}		
		
		
	}
	
	
	@SuppressWarnings("unchecked")
	private void collectParentNodeValues(Node node){
		
		if(node.getParentNode() != null){
			Node parent = node.getParentNode();
			
			if(parent.getLocalName() != null){
			
				if(parent.getLocalName().equals("dataResource")){
					
//					if(parent.hasAttributes()){
//						
//						String gbifKey = getGbifKey(parent);
//						if(gbifKey.length() > 0){
//							System.out.println("dataResource: " + gbifKey);
//							valueMap.put("dataResource", gbifKey);
//						}
//					}
					
					if(parent.hasChildNodes()){
						
						String nameValue = getChildValue(parent, "name").toString().replace('"', '\"');
//						System.out.println("dataResourceName: " + childValue);					
						descriptionMap.put("dataResource", nameValue);	
						tableMap.put("dataResource", nameValue);
						
						String rightsValue = getChildValue(parent, "rights").toString().replace('"', '\"');
//						System.out.println("dataProviderName: " + childValue);					
						descriptionMap.put("dataResourceRights", rightsValue);
						tableMap.put("dataResourceRights", nameValue);
					}			
				}
			}
			
			if(parent.getLocalName() != null){
				if(parent.getLocalName().equals("dataProvider")){
					
//					if(parent.hasAttributes()){
//						
//						String gbifKey = getGbifKey(parent);
//						System.out.println("dataProvider: " + gbifKey);
//						valueMap.put("dataProvider", gbifKey);
//					}
					
					if(parent.hasChildNodes()){
						
						String childValue = getChildValue(parent, "name").toString().replace('"', '\"');
//						System.out.println("dataProviderName: " + childValue);					
						descriptionMap.put("dataProvider", childValue);	
						tableMap.put("dataProvider", childValue);	
					}		
					
				}
			}
			
			collectParentNodeValues(parent);				
		}		
	}

	/**
	 * returns a string array. First parameter is the url and second the species Name
	 * if url not avaliable then only 1 element is in the array
	 * 
	 * @param req
	 * @return
	 */
	private Map<String,String> getParametersAsString(HttpServletRequest req) {		
		
		Map<String, String> requestParams = new HashMap<String, String>();
		String url = "";
		String time = "";
		
		Enumeration<String> e = req.getParameterNames();
		while (e.hasMoreElements()) {
			String k = e.nextElement();
//			System.out.println(k + "=" + req.getParameter(k));
			if (k.equals("source")) {
				url = req.getParameter(k);
			} else
			
			if (k.equals("time")) {
				time = req.getParameter(k);
			}
		}
		
		if (url.length() > 0) {
			requestParams.put("source", url);
		}
		
		if (url.length() > 0) {
			requestParams.put("time", time);
		}
		
		return requestParams;

	}

}

