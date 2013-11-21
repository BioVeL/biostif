package de.fraunhofer.iais.kd.biovel.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class Imageupload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// Create the logger
	private static final Logger logger = Logger.getLogger(Imageupload.class
			.getPackage().getName());
	protected static Properties conf;

    public static final String BIOSTIF_SERVER_CONF = "biostif.server.conf";    
    public ServletConfig config;
    
    private Client client;


	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		this.config = config;

		logger.info("=== Imageupload Servlet starting up - " + new Date()
				+ " ====");
		
//        try {
//            initBase(this.config);
//        } catch (Throwable e) {
//            logger.log(Level.SEVERE, e.getMessage(), e);
//            throw new ServletException();
//        }
	}
	
    private void initBase(ServletConfig config) throws ServletException {

        String confFileName = null;
        if ((confFileName = System.getProperty(BIOSTIF_SERVER_CONF)) == null) {
            confFileName = config.getInitParameter(BIOSTIF_SERVER_CONF);
        }
        if (confFileName == null) {
            throw new ServletException("--- Imageupload init-param biostif.server.conf not specified");
        }

        File confFile = new File(confFileName);
        if (!confFile.isAbsolute()) {
            confFile = new File(config.getServletContext().getRealPath("/"), confFileName);
        }
        if (!confFile.canRead()) {
            throw new ServletException("--- Imageupload cannot read file: \"" + confFileName + "\"");
        }

        Properties props = new Properties();
        try {
            FileInputStream fis = new FileInputStream(confFile);
            props.load(fis);
        } catch (IOException e) {
            throw new ServletException(" Imageupload cannot read properties: " + confFile + " : " + e.getMessage(), e);
        }

        config.getServletContext().setAttribute(BIOSTIF_SERVER_CONF, props);
        logger.info("--- Imageupload.initBase(..) successful");
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
		boolean nosuffix = false;

		if (isMultipart) {
//			logger.warning("Multipath request is not implemented, use parameter request instead");
			
			
		
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload();

			// Parse the request
			FileItemIterator iter = upload.getItemIterator(req);

			FileItemStream item = null;
			InputStream is = null;
			
			String name = "";//item.getName();
			InputStream iscopy = null;
			
			while (iter.hasNext()) {
				
				System.out.println("iter");

				item = iter.next();
//				String iname = item.getName();
//				System.out.println("iname: " + iname);
				name = item.getName();
				is = item.openStream();
//				InputStream iscopy = null;
				if (item.isFormField()) {
					// FileName: e.g. Form field fileName with value
					// gkz_group_schaeden.txt detected.

//					System.out.println("Form field " + Streams.asString(is));
					
//					if(name.length() == 0){
//						name = Streams.asString(is);
//					}
					
					
//					 System.out.println("Form field " + name + " with value "
//					 + Streams.asString(is) + " detected.");
					
				} else {
					// File: e.g. File field file with file name
					// gkz_group_schaeden.txt detected.
					// System.out.println("File field " + name + " with file name "
					// + item.getName() + " detected.");
					// Process the input stream
					
				
				    ByteArrayOutputStream baos = new ByteArrayOutputStream();
				    // Fake code simulating the copy
				    // You can generally do better with nio if you need...
				    // And please, unlike me, do something about the Exceptions :D
				    byte[] buffer = new byte[1024];
				    int len;
				    while ((len = is.read(buffer)) > -1 ) {
				        baos.write(buffer, 0, len);
				    }
				    baos.flush();

				    // Open new InputStreams using the recorded bytes
				    // Can be repeated as many times as you wish
				    iscopy = new ByteArrayInputStream(baos.toByteArray()); 

					
				}
				
//				System.out.println(" innerhalbname: " +name); 
////				
//				String filename = name.substring(0, name.lastIndexOf("."));
//				System.out.println("filename" + filename);
//				
//				// endung abschneiden und checken
//				
//		        // http://biovel.iais.fraunhofer.de/shim/rest/raster/{workspaceid}?source={sourceURL}&stylename={styleName}&inputformat={inputformat}&layername={layername}
//
//				String stylename = "";
//				String inputformat = "";
//				String layername = "";
//				
//				String shimurl = "http://localhost:8080/shim/rest/raster/biovel_temp";	
////				String params = "?stylename={"+stylename+"}&inputformat={"+inputformat+"}&layername={"+layername+"}";	
////				String target = shimurl + params;
//
//				
//		        this.client = Client.create();
//		        ClientResponse crPost = this.client.resource(shimurl).queryParam("stylename", stylename).queryParam("inputformat", inputformat).queryParam("layername", layername).post(ClientResponse.class,iscopy);
//                
//	            if (crPost.getStatus() != 201) {
////	                throw new WebApplicationException(crPost.getStatus());
////	                return Response.status(crPost.getStatus()).entity(crPost.getEntity(String.class)).build();
//	            	jsonResult = "Error while uploading the file: " + crPost.getEntity(String.class);
//	            }        
//	            else {
//					jsonResult = "file is sucessful transferred";
//	            }
				

				
			}
			
			System.out.println("name: " +name); 
			
			String layername = name.substring(0, name.lastIndexOf("."));
			System.out.println("filename: " + layername);
			String suffix = name.substring(name.lastIndexOf(".")+1, name.length());
			System.out.println("suffix: " + suffix);
			
			String inputformat = "";
			String stylename = "";
			
		     switch(suffix.toLowerCase()){
		        
		        case "img":
		        	inputformat = "ERDASImg";
		        	stylename = "vrt_raster_style";
		            break;
		            
		        case "arcgrid":
		        	inputformat = "ArcGrid";
		        	stylename = "";
		            break;
		            
		        case "tiff":
		        	inputformat = "GeoTIFF";
		        	stylename = "tif_raster_color";
		            break;
		            
		        case "tif":
		        	inputformat = "GeoTIFF";
		            break;
		            
		        default:
		        	jsonResult = "Input format '"+suffix+"' not known";
		        	nosuffix = true;
                break;
   
		    }
		     
		     System.out.println("suffix: " + suffix);
		     
		     if(!nosuffix){
				
				String shimurl = "http://localhost:8080/shim/rest/raster/biovel_temp";	
				
		        this.client = Client.create();
		        ClientResponse crPost = this.client.resource(shimurl).queryParam("stylename", stylename).queryParam("inputformat", inputformat).queryParam("layername", layername).post(ClientResponse.class,iscopy);
	            
		        System.out.println("upload status: " + crPost.getStatus());
		        
		        
	            if (crPost.getStatus() != 200) {
	//                throw new WebApplicationException(crPost.getStatus());
	//                return Response.status(crPost.getStatus()).entity(crPost.getEntity(String.class)).build();
	            	jsonResult = "Error while uploading the file: " + crPost.getEntity(String.class);
	            }        
	            else {
					jsonResult = "file is sucessful transferred! \n" + crPost.getEntity(String.class);
	            }
				
				is.close();
			}

		}
		 else{
			 jsonResult = "imageupload error, wrong request";
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



	

}
