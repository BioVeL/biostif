package de.fraunhofer.iais.kd.biovel.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class FilterDwC extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // Create the logger
    private static final Logger logger = Logger.getLogger(FilterDwC.class.getPackage().getName());
    protected static Properties conf;

    @SuppressWarnings("rawtypes")
	private Map valueMap;
    @SuppressWarnings("rawtypes")
	private Map descriptionMap;
    @SuppressWarnings("rawtypes")
	private Map tableMap;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        logger.info("=== FilterDwC Servlet starting up - " + new Date() + " ====");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);

    }

    @Override
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

    private void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException,
            FileUploadException, ParserConfigurationException, SAXException {

        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);

        //requested result:
        String jsonResult = "";

        if (isMultipart) {

            System.out.println("is multipart");

            jsonResult = processMultipathReq(req);

        } else {

//            System.out.println("no multipart");

            Map<String, String> params = getParametersAsString(req);

            if (params.isEmpty()) {
                // res.sendError(arg0, arg1)
                return;
            }
            
   		 if(params.containsKey("source")){		 
   			
//			 jsonResult = getCsvFromUrl("", params.get("source"));
		 
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
					 is = new ByteArrayInputStream(isString.getBytes());
					 //parse to XML DOM
					 jsonResult = filterDwC(is, fis);
				 }else{						 
					 logger.info(" text/plain files are not allowed in the Dwc2Json Method");
					 res.setStatus(415, "mimeType 'text/plain' is not accepted - get a XML(DwC) File instead");
				 }
				
				 //parse to XML DOM
//				 jsonResult = filterDwC(is, fis);
				 is.close();
				 fis.close();
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 		 
		 
		 	}
            
            
//            if (params.size() == 2) {
//                filterResult = getDwcFromUrl(params.get("scientificname"), params.get("source"), req);
//            } else {
//
//                if (params.containsKey("scientificname")) {
//
//                    filterResult = getDwcFromUrl(params.get("scientificname"), "", req);
//                }
//                if (params.containsKey("source")) {
//
//                    filterResult = getDwcFromUrl("", params.get("source"), req);
//                }
//
////				jsonResult =  getDwcFromUrl(params.get(0), "");
//            }

        }

//		System.out.println("\n" + jsonResult);

        //jsonResult to out
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");
        OutputStream os = res.getOutputStream();
//
        byte[] filterResultBytes = jsonResult.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(filterResultBytes.length);
        baos.write(filterResultBytes);
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

    private String processMultipathReq(HttpServletRequest req) throws FileUploadException, IOException,
            ParserConfigurationException, SAXException {

        String jsonResult = "";

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();

        // Parse the request
        FileItemIterator iter = upload.getItemIterator(req);

        FileItemStream item = null;
        InputStream is = null;

        String sourceValue = "";

        while (iter.hasNext()) {

            item = iter.next();
            String name = item.getFieldName();
            is = item.openStream();
            if (item.isFormField()) {
                // FileName: e.g. Form field fileName with value gkz_group_schaeden.txt detected.

//		    	System.out.println("Form field " + name + " with value "
//		            + Streams.asString(is) + " detected.");

                if (name.equals("source")) {
                    sourceValue = Streams.asString(is);
                }

            } else {
                // File: e.g. File field file with file name gkz_group_schaeden.txt detected.
//		        System.out.println("File field " + name + " with file name "
//		            + item.getName() + " detected.");
                // Process the input stream

//		    	jsonResult = parseInputStream(is);		        

            }
        }
        is.close();

//        jsonResult = parseInputStream(is, sourceValue);

        return jsonResult;
    }

    /**
     * @param is
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     */

//	public OutputStream filterDwC(InputStream dwcIs, InputStream filterIs, OutputStream out){
//		public OutputStream filterDwC(Document doc, InputStream filterIs, OutputStream out){
    public String filterDwC(InputStream sourceIs, InputStream filterStream) {


        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        Document doc = null;

 
		try {
	        docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(sourceIs);
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
        BufferedReader isBufferedReader = new BufferedReader(new InputStreamReader(filterStream));
        String filterString = "";
        String line;

        try {
            while ((line = isBufferedReader.readLine()) != null) {
                filterString += line;
            }

            isBufferedReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<String> filterList = Arrays.asList(filterString.split(","));

//		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//		docFactory.setNamespaceAware(true);
//		DocumentBuilder docBuilder;
//		Document doc = null;			
//		
//		try {
//			
//			docBuilder = docFactory.newDocumentBuilder();
//			doc = docBuilder.parse(dwcIs);
//			
//		} catch (ParserConfigurationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

        NodeList nodeList = doc.getElementsByTagName("*");

//		System.out.println("liste vor" + nodeList.getLength());

        int ixID = 0;
        
        //remove filtered occurenceNodes  
        for (int i = nodeList.getLength(); i > 0; i--) {

            Node node = nodeList.item(i);

            if ((node != null) && node.getLocalName().equals("TaxonOccurrence") && node.hasAttributes()) {

                String gbifKey = getGbifKey(node);

                if (gbifKey.length() > 0) {
                    if (!filterList.contains(gbifKey)) {
                        node.getParentNode().removeChild(node);
                    }
                } else {
                	ixID++;
                }
            }

        }
        
        StringWriter sw = new StringWriter();
        
        if(nodeList.getLength() == ixID){
        	
        	sw.append("DwC document contains no TaxonOccurence IDs in " + nodeList.getLength() + " entries");
        } else {
	//		System.out.println("liste nach" + nodeList.getLength());
	
	        //remove dataProvider Nodes without occurenceNodes 
	        for (int j = nodeList.getLength(); j > 0; j--) {
	
	            Node node = nodeList.item(j);
	
	            if ((node != null) && node.getLocalName().equals("dataProvider") && node.hasAttributes()) {
	
	                Document d;
	                try {
	                    d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	                    d.appendChild(d.importNode(node, true));
	
	                    NodeList taxList = d.getElementsByTagNameNS("*", "TaxonOccurrence");
	
	                    if (taxList.getLength() == 0) {
	                        node.getParentNode().removeChild(node);
	                    }
	
	                } catch (ParserConfigurationException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
	
	            }
	        }
	
	//		System.out.println("liste nach leeren" + nodeList.getLength());
	
	        //remove whitespaces after removeChild
	        for (int k = nodeList.getLength(); k > 0; k--) {
	
	            Node child = nodeList.item(k);
	            if (child instanceof Element) {
	                removeWhitespaceNodes((Element) child);
	            }
	        }
	
	        Transformer serializer;
	        try {
	
	            serializer = TransformerFactory.newInstance().newTransformer();
	            serializer.transform(new DOMSource(nodeList.item(0)), new StreamResult(sw));
	
	        } catch (TransformerException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (TransformerFactoryConfigurationError e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	
	//        System.out.println("result \n" + result);
        }

        return sw.toString();
    }

    /**
     * Removes whitespace from Document
     * 
     * @see http://forums.java.net/jive/thread.jspa?messageID=345459
     * @param e
     */
    private static void removeWhitespaceNodes(Element e) {

        NodeList children = e.getChildNodes();

        for (int i = children.getLength() - 1; i >= 0; i--) {

            Node child = children.item(i);
            Node child1 = children.item(i + 1);

            if (((child instanceof Text) && (((Text) child).getData().trim().length() == 0))
                    && ((child1 instanceof Text) && (((Text) child1).getData().trim().length() == 0))) {

                e.removeChild(child);
            } else if (child instanceof Element) {

                removeWhitespaceNodes((Element) child);
            }
        }
    }


    private String getGbifKey(Node node) {

        String attribute = "";

        NamedNodeMap mmn = node.getAttributes();
        if (mmn.getLength() > 0) {
            for (int k = 0; k < mmn.getLength(); k++) {
                if (mmn.item(k).getLocalName().equals("gbifKey")) {
                    attribute = mmn.item(k).getNodeValue();
                }
            }
        }
        return attribute;
    }

    /**
     * returns a string array. First parameter is the url and second the species
     * Name if url not avaliable then only 1 element is in the array
     * 
     * @param req
     * @return
     */
    private Map<String, String> getParametersAsString(HttpServletRequest req) {

        Map<String, String> requestParams = new HashMap<String, String>();
        String url = "";
        String scientificname = "";
        Enumeration<String> e = req.getParameterNames();
        while (e.hasMoreElements()) {
            String k = e.nextElement();
//            System.out.println("requested parameter: " + k + "=" + req.getParameter(k));
            if (k.equals("source")) {
                url = req.getParameter(k);
            }
//            else if (k.equals("scientificname")) {
//                scientificname = req.getParameter(k);
//            }
        }

//        if (scientificname.length() > 0) {
//            requestParams.put("scientificname", scientificname);
//        }
        if (url.length() > 0) {
            requestParams.put("source", url);
        }

        return requestParams;

    }

}
