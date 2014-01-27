package de.fraunhofer.iais.kd.biovel.shim.raster;

import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.XML;
import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.TXT_URI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.DocFlavor.STRING;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.osr.SpatialReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;

import de.fraunhofer.iais.kd.biovel.common.contract.Check;
import de.fraunhofer.iais.kd.biovel.common.util.MmpBase64;

/**
 * This class handles the adding and removing of aggregation layers, as well as
 * handles the setting of a style for a given layer. Internally it uses
 * GeoServer's REST API.
 * 
 * @author utaddei, rkulawik
 */
public class RestGeoServerAdapter implements IGeoServerAdapter {

    private static final Logger LOG = Logger.getLogger(RestGeoServerAdapter.class.getName());

    /** Possible log modes for HTTP requests */
    enum LogMode {
        NONE, CONSOLE, LOGGER
    };

    /* GS Constants */
    private static final String WS = "workspaces";
    private static final String DS = "datastores";
    private static final String CS = "coveragestores";
    private static final String FTs = "featuretypes";
    private static final String LAYERS = "layers";

    private static final Object syncObject = new Object(); 
    
    private Client client;

    private ClientFilter loggingFilter;

    private final URI baseUri;

    private String workspaceName;

    private String datastoreName;
    
    private String coverageStoreName;
    
    private String styleName;

    private String user;

    private String passwd;
    
    private String dataDir;
    
    private String dataURL;

    /**
     * Creates a new instance of a {@link RestGeoServerAdapter}.
     * 
     * @param baseUri the base URI of the GeoServer. This is generally
     *            'http://localhost:8080/geoserver/rest' and cannot be null.
     * @param wsName the GeoServer workspace name. Cannot be null.
     * @param dsName the GeoServer datastore name. Cannot be null.
     * @param user the GeoServer admin user name. Cannot be null.
     * @param passwd the GeoServer admin password name. Cannot be null.
     */
    public RestGeoServerAdapter(URI baseUri, String wsName, String dsName, String user, String passwd, String dataDir, String dataURL) {
        Check.notNull(baseUri);
        this.baseUri = baseUri;
        
        System.out.println("geoserver adapter: this.baseUri: " + this.baseUri);
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Initialising adapter mit URI " + this.baseUri);
        }
        this.client = Client.create();

        setWorkspace(wsName);
        setDatastore(dsName);

        setUser(user);
        setPasswd(passwd);
        setDataDir(dataDir);
        setDataURL(dataURL);

    }
    
    
    public RestGeoServerAdapter(String user, String passwd, URI baseUri, String dataDir, String dataURL) {
        Check.notNull(baseUri);
        Check.notNull(passwd);
        Check.notNull(user);
        Check.notNull(dataDir);
        
        this.baseUri = baseUri;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Initialising adapter mit URI " + this.baseUri);
        }
        this.client = Client.create();

        setUser(user);
        setPasswd(passwd);
        setDataDir(dataDir);
        setDataURL(dataURL);
    }

    /**
     * Creates a new instance of a {@link RestGeoServerAdapter}. The user name
     * and password are the GeoServer's out-of-the-box default.
     * 
     * @param baseUri the base URI of the GeoServer. This is generally
     *            'http://localhost:8080/geoserver/rest' and cannot be null.
     * @param wsName the GeoServer workspace name. Cannot be null.
     * @param dsName the GeoServer datastore name. Cannot be null.
     */
    public RestGeoServerAdapter(URI baseUri, String wsName, String dsName, String dataDir, String dataURL) {
        this(baseUri, wsName, dsName, "admin", "wrongPassword123", dataDir, dataURL);
    }

    /**
     * Creates a new instance of a {@link RestGeoServerAdapter} with the
     * workspace 'mmp' and datastore 'mmp_ds'. The user name and password are
     * the GeoServer's out-of-the-box default.
     * 
     * @param baseUri the base URI of the GeoServer. This is generally
     *            'http://localhost:8080/geoserver' and cannot be null.
     */
    public RestGeoServerAdapter(URI baseUri) {
        this(baseUri, "mmp", "mmp_ds", "dataDir", "dataURL");
    }
    
    /**
     * Creates a new - not default - workspace
     * 
     */
    
    public Response createWorkspace(String workspaceName){
    	
    	Check.notNull(workspaceName);

        final String resAddr = this.baseUri + "/rest/" + WS;
        String exprString = "<workspace><name>"+ workspaceName +"</name></workspace>";

        ClientResponse cr =
            this.client.resource(resAddr).type("application/xml").header("Authorization",
                createAuthentication()).post(ClientResponse.class, exprString);
        
        if (cr.getStatus() == 201) {
            reloadGS();
        	setWorkspace(workspaceName);
        	LOG.info("Created workspace '" + workspaceName + "' to " + resAddr);
        	return Response.status(cr.getStatus()).build();
        } else {
          final String mesg = "Error creating workspace " + cr.getEntity(String.class);
          LOG.log(Level.SEVERE, mesg);
          return Response.status(cr.getStatus()).entity(mesg).build();
        }
        
    }
    
    public int deleteWorkspace(String workspaceName){
        
    	Check.notNull(workspaceName);
    	
        final String resAddr = this.baseUri + "/rest/" + WS + "/" + workspaceName+"?recurse=true";

        ClientResponse cr =
            this.client.resource(resAddr).type("application/xml").header("Authorization",
                createAuthentication()).delete(ClientResponse.class);
        
//        System.out.println("cs workspace status del- " + cr.getStatus());
        
        if (cr.getStatus() == 200) {
        	setWorkspace("");
        	LOG.info("Deleted workspace '" + workspaceName + "' to " + resAddr);
        } else {
          final String mesg = "Error deleting workspace " + cr.getEntity(String.class);
          LOG.log(Level.SEVERE, mesg);
          throw new RuntimeException(mesg);
        }
        
        int crStatus = cr.getStatus();
        cr.close();
        
        return crStatus;
    	
    }
    
    public int deleteCoverageStore(String workspaceName, String coverageStoreName){

    	Check.notNull(workspaceName);
    	Check.notNull(coverageStoreName);

        final String resAddr = this.baseUri + "/rest/" + WS + "/" + workspaceName+"/"+CS+"/"+coverageStoreName;

        System.out.println(resAddr);
        
        ClientResponse cr =
            this.client.resource(resAddr).type("application/xml").header("Authorization",
                createAuthentication()).delete(ClientResponse.class);
                       
        if (cr.getStatus() == 200) {
        	setWorkspace("");
        	LOG.info("Deleted workspace '" + workspaceName + "' to " + resAddr);
        } else {
          final String mesg = "Error deleting workspace " + cr.getEntity(String.class);
          LOG.log(Level.SEVERE, mesg);
//          throw new RuntimeException(mesg);
        }
        
        int crStatus = cr.getStatus();
        cr.close();
        
        return crStatus;
    	
    }

    
    public int deleteCoverageLayer(String workspaceName, String coverageStoreName){

    	Check.notNull(workspaceName);

        final String resAddr = this.baseUri + "/rest/" + WS + "/" + workspaceName+"/"+CS+"/"+coverageStoreName+"/coverages/"+coverageStoreName+"?recurse=true";

        ClientResponse cr =
            this.client.resource(resAddr).type("application/xml").header("Authorization",
                createAuthentication()).delete(ClientResponse.class);
                       
        if (cr.getStatus() == 200) {
        	setWorkspace("");
        	LOG.info("Deleted workspace '" + workspaceName + "' to " + resAddr);
        } else {
          final String mesg = "Error deleting workspace " + cr.getEntity(String.class);
          LOG.log(Level.SEVERE, mesg);
          throw new RuntimeException(mesg);
        }
        
        int crStatus = cr.getStatus();
        cr.close();
        
        return crStatus;   	
    }
    
    public Response createCoverageStore(String storageFilePath, String storageFileNameWithoutExtension, String format){
    	
    	Check.notNull(storageFileNameWithoutExtension);
    	Check.notNull(storageFilePath);
   	
        final String resAddr = this.baseUri + "/rest/" + WS + "/" + workspaceName +"/"+ CS;
        String exprString = "<coverageStore><name>"+storageFileNameWithoutExtension+
        					"</name><workspace>"+workspaceName+"</workspace><enabled>true</enabled><type>"+format+"</type>"+
        					"<url>"+storageFilePath+"</url></coverageStore>";
        
        System.out.println("ccS exprString: " + exprString);
        
        ClientResponse cr =
            this.client.resource(resAddr).type("application/xml").header("Authorization",
                createAuthentication()).post(ClientResponse.class, exprString);
        
//        ClientResponse cr =
//                this.client.resource(resAddr).type("application/xml").accept(MediaType.APPLICATION_OCTET_STREAM_TYPE).header("Authorization",
//                    createAuthentication()).post(ClientResponse.class, exprString);
        
        if (cr.getStatus() == 201) {
            reloadGS();
        	setCoverageStore(storageFileNameWithoutExtension);
        	LOG.info("Created coverageStore '" + storageFileNameWithoutExtension + "' to " + resAddr);
        	return Response.status(cr.getStatus()).build();
        } else {
            final String mesg = "Error creating coverageStore " + cr.getEntity(String.class);
            LOG.log(Level.SEVERE, mesg);
            return Response.status(cr.getStatus()).entity(mesg).build();
        }
        
    }

    
    public Response addCoverageLayer(){

        final String resAddr = this.baseUri + "/rest/" + WS + "/" + workspaceName + "/" + CS + "/" + coverageStoreName + "/coverages";
        String exprString = "<coverage><name>"+ coverageStoreName +"</name><title>"+ coverageStoreName +"</title></coverage>";
        
        System.out.println("resAddr: " + resAddr);
        System.out.println("exprString: " + exprString);
        
        ClientResponse cr =
            this.client.resource(resAddr).type("application/xml").header("Authorization",
                createAuthentication()).post(ClientResponse.class, exprString);
        
        System.out.println("status CL: " + cr.getStatus());
        
        if (cr.getStatus() == 201) {
            reloadGS();
        	LOG.info("Created CoverageLayer '" + workspaceName + "' to " + resAddr);
        	return Response.status(cr.getStatus()).build();
        } else {
           int delCov = deleteCoverageStore(workspaceName, coverageStoreName);
           String delcovmsg = "";
           if(delCov != 200){
               delcovmsg = "Error trying delete coverageStore " + workspaceName + " - " + coverageStoreName;
           }
          final String mesg = "Error creating CoverageLayer " + cr.getEntity(String.class) + " - " + delcovmsg;
          LOG.log(Level.SEVERE, mesg);
          return Response.status(cr.getStatus()).entity(mesg).build();
        }
    }
    
    
    
    public Response updateCoverageLayer(String sourceLocation, String suffix){
    	
//    	String resAddr = "http://localhost:8090/geoserver/rest/workspaces/testws/coveragestores/admin_20120411_103242_947_2ExfbS/coverages/admin_20120411_103242_947_2ExfbS.xml";
    	final String resAddr = this.baseUri + "/rest/" + WS + "/" + workspaceName + "/" + CS + "/" + coverageStoreName + "/coverages/"+coverageStoreName+".xml";

		String noValue = "";
		String minValue = "";
		String maxValue = "";
        
        String imageWKT = "";
        String epsgCode = "";
//        String xSize = "";
//        String ySize = "";
		
		synchronized (syncObject) {
            
	            try{
	                gdal.AllRegister();
	                
	                Dataset hDataset  = gdal.Open(sourceLocation.replace("file:", ""));
	                
//	                xSize = ""+hDataset.getRasterXSize();
//	                ySize = ""+hDataset.getRasterYSize();
	               
	                int iBand= hDataset.getRasterCount();
	                Band hBand = hDataset.GetRasterBand(iBand);
	        
	                Double[] noValueD = new Double[1];
	                double[] minMax = new double[2];
	        
	                hBand.ComputeRasterMinMax(minMax);
	                hBand.GetNoDataValue(noValueD);
	        
	                if(noValueD[0] != null){
	                    noValue = ""+noValueD[0];
	                } 
	                
	                double[] copyMinMax = minMax.clone();
	        	                             
	                if(copyMinMax != null){
	                    minValue = ""+ copyMinMax[0];
	                    maxValue = ""+ copyMinMax[1];
	                } 
	                
	                if (hDataset.GetProjectionRef() != null) {
	                    SpatialReference hSRS;
	                    String pszProjection;

	                    pszProjection = hDataset.GetProjectionRef();
	                    hSRS = new SpatialReference(pszProjection);
	                    
	                    if (hSRS != null && pszProjection.length() != 0) {
	                        String[] pszPrettyWkt = new String[1];
	                        
	                        hSRS.ExportToPrettyWkt(pszPrettyWkt, 0);
	                        
//	                        GEOGCS["GCS_WGS_1984",
//	                               DATUM["WGS_1984",
//	                                   SPHEROID["WGS_84",6378137,298.257223563]],
//	                               PRIMEM["Greenwich",0],
//	                               UNIT["Degree",0.017453292519943295]]
	                        
	                        imageWKT = pszPrettyWkt[0];
	                        
	                    } else {
	                        System.out.println("Coordinate System is `"
	                                + hDataset.GetProjectionRef() + "'");
	                    }
	                    
	                    hSRS.delete();
	                }
	        
	                hDataset.delete();
	            
	            } catch (Exception e) {
	                gdal.GDALDestroyDriverManager();
	                final String msg = "something wrong with GDAL driver: " + e.getMessage();
	                LOG.info(msg);
	                
	            } finally {
	                gdal.GDALDestroyDriverManager();
	            }

	            if(imageWKT.length() == 0){
	                epsgCode = "4326";
	            
	            } else {
	                
	                String epsgString = "";
	            
	                try {
	                
	                    ClientResponse getEpsg =
	                            this.client.resource("http://localhost:8080/prj2epsg/search.json?mode=wkt").queryParam("terms", imageWKT).get(ClientResponse.class);
	                    
	                    epsgString = getEpsg.getEntity(String.class);
	                    	                    	                    
	                    JSONObject respBody = new JSONObject(epsgString);              
	                    JSONArray epsgCodeArray = respBody.getJSONArray("codes");
	                    epsgCode = epsgCodeArray.getJSONObject(0).getString("code");
	                    
	                } catch (JSONException e) {
	                    final String msg = "Error getting EPSG Code from WKT because: " + epsgString;
	                    LOG.info(msg + " - " + e.toString());
	                    epsgCode = "4326";                
	                }
	            }
		
		} //end synchronize
		
		System.out.println("found code - EPSG: "+ epsgCode);        
//        System.out.println("Size is " + xSize + ", "  + ySize);
		System.out.println("novalue: " + noValue + " min: " + minValue + " max: " + maxValue);
		
		if(epsgCode.equals("EPSG:404000")){
		    epsgCode = "EPSG:4326";
		}
		
		String params = "";

		if(suffix.equals(".img")){
		    
		    if(noValue.length() == 0){
		        noValue = "101";
		    }
		    if(minValue.length() == 0){
		        minValue = "0";
		    }
		    if(maxValue.length() == 0){
		        maxValue = "100";
		    }
		    
		
		params =
				"<coverage>"
				
				+"<description>Generated from ERDASImg</description>"
				+ "<keywords>"
				+ "<string>WCS</string>"
				+ "<string>ERDASImg</string>"
				+ "<string>"+coverageStoreName+"</string>"
				+ "</keywords>"
				
				+ "<metadata>"
				+ "<entry key=\"cachingEnabled\">false</entry>"
				+ "<entry key=\"dirName\">"+coverageStoreName+"_"+coverageStoreName+"</entry>"
				+ "</metadata>"
				
				+ "<srs>EPSG:4326</srs>"
				+ "<projectionPolicy>FORCE_DECLARED</projectionPolicy>"
				+ "<enabled>true</enabled>"
				
				+ "<nativeFormat>ERDASImg</nativeFormat>"
//				
				+ "<supportedFormats>"
				+ "<string>GIF</string>"
				+ "<string>PNG</string>"
				+ "<string>JPEG</string>"
				+ "<string>TIFF</string>"
				+ "<string>GEOTIFF</string>"
				+ "<string>ARCGRID</string>"
				+ "</supportedFormats>"
//				
				+ "<interpolationMethods>"
				+ "<string>bilinear</string>"
				+ "<string>bicubic</string>"
				+ "</interpolationMethods>"

				+"<dimensions>"
			    +"<coverageDimension>"
			    +"<name>GRAY_INDEX</name>"
			    +"<description>GridSampleDimension["+minValue+","+maxValue+"]</description>"
			    +"<range>"
			    +"<min>"+minValue+"</min>"
			    +"<max>"+maxValue+"</max>"
			    +"</range>"
			    +"<nullValues>"
			    +"<double>"+noValue+"</double>"
			    +"</nullValues>"
			    +"</coverageDimension>"
                +"</dimensions>"
				
				+ "<requestSRS>"
				+ "<string>EPSG:"+epsgCode+"</string>"
				+ "</requestSRS>"
				+ "<responseSRS>"
				+ "<string>EPSG:"+epsgCode+"</string>"
				+ "</responseSRS>"
				
				+ "<parameters>"
				+ "<entry>"
				+ "<string>USE_MULTITHREADING</string>"
				+ "<string>false</string>"
				+ "</entry>"
				+ "<entry>"
				+ "<string>SUGGESTED_TILE_SIZE</string>"
				+ "<string>512,512</string>"
				+ "</entry>"
				+ "<entry>"
				+ "<string>USE_JAI_IMAGEREAD</string>"
				+ "<string>true</string>"
				+ "</entry>"
				+ "</parameters>"
				
				+"</coverage>";
		
		} else if(suffix.equals(".arcgrid")){
            
            if(noValue.length() == 0 || noValue.equalsIgnoreCase("-9999.0")){
                noValue = "101";
            }
            if(minValue.length() == 0){
                minValue = "0";
            }
            if(maxValue.length() == 0 || maxValue.equalsIgnoreCase("0")){
                maxValue = "1";
            }
		    
		      params =
		                "<coverage>"
		                
		                +"<description>Generated from ArcGrid</description>"
		                + "<keywords>"
		                + "<string>WCS</string>"
		                + "<string>ArcGrid</string>"
		                + "<string>"+coverageStoreName+"</string>"
		                + "</keywords>"
		                
		                + "<metadata>"
		                + "<entry key=\"cachingEnabled\">false</entry>"
		                + "<entry key=\"dirName\">"+coverageStoreName+"_"+coverageStoreName+"</entry>"
		                + "</metadata>"
		                
		                + "<srs>EPSG:4326</srs>"
		                + "<projectionPolicy>FORCE_DECLARED</projectionPolicy>"
		                + "<enabled>true</enabled>"
		                
		                + "<nativeFormat>ArcGrid</nativeFormat>"
//		              
		                + "<supportedFormats>"
		                + "<string>GIF</string>"
		                + "<string>PNG</string>"
		                + "<string>JPEG</string>"
		                + "<string>TIFF</string>"
		                + "<string>GEOTIFF</string>"
		                + "<string>ARCGRID</string>"
		                + "</supportedFormats>"
//		              
		                + "<interpolationMethods>"
		                + "<string>bilinear</string>"
		                + "<string>bicubic</string>"
		                + "</interpolationMethods>"

                        +"<dimensions>"
                        +"<coverageDimension>"
                        +"<name>GRAY_INDEX</name>"
                        +"<description>GridSampleDimension[-Infinity,Infinity]</description>"
                        +"<range>"
                        +"<min>"+minValue+"</min>"
                        +"<max>"+maxValue+"</max>"
                        +"</range>"
                        +"<nullValues>"
                        +"<double>"+noValue+"</double>"
                        +"</nullValues>"
                        +"</coverageDimension>"
                        +"</dimensions>"
  
		                + "<requestSRS>"
		                + "<string>EPSG:"+epsgCode+"</string>"
		                + "</requestSRS>"
		                + "<responseSRS>"
		                + "<string>EPSG:"+epsgCode+"</string>"
		                + "</responseSRS>"
		                
		                +"</coverage>";
		    
		}
		    else if(suffix.equals(".tif")){
		        
	            if(noValue.length() == 0){
	                noValue = "255";
	            }
	            if(minValue.length() == 0){
	                minValue = "0";
	            }
	            if(maxValue.length() == 0){
	                maxValue = "254";
	            }
		
//		    noValue = "129.0"; // as tif default value
            
            params =
                      "<coverage>"
                      
                      +"<description>Generated from GeoTIFF</description>"
                      + "<keywords>"
                      + "<string>WCS</string>"
                      + "<string>GeoTIFF</string>"
                      + "<string>"+coverageStoreName+"</string>"
                      + "</keywords>"
                      
                      + "<metadata>"
                      + "<entry key=\"cachingEnabled\">false</entry>"
                      + "<entry key=\"dirName\">"+coverageStoreName+"_"+coverageStoreName+"</entry>"
                      + "</metadata>"
                      
                      + "<srs>EPSG:4326</srs>"
                      + "<projectionPolicy>FORCE_DECLARED</projectionPolicy>"
                      + "<enabled>true</enabled>"
                      
                      + "<nativeFormat>GeoTIFF</nativeFormat>"
//                  
                      + "<supportedFormats>"
                      + "<string>GEOTIFF</string>"
                      + "<string>GIF</string>"
                      + "<string>PNG</string>"
                      + "<string>JPEG</string>"
                      + "<string>TIFF</string>"
                      + "<string>ARCGRID</string>"
                      + "</supportedFormats>"
//                  
                      + "<interpolationMethods>"
                      + "<string>nearest neighbor</string>"
                      + "<string>bilinear</string>"
                      + "<string>bicubic</string>"
                      + "</interpolationMethods>"

                      +"<dimensions>"
                      +"<coverageDimension>"
                      +"<name>GRAY_INDEX</name>"
                      +"<description>GridSampleDimension["+minValue+","+maxValue+"]</description>"
                      +"<range>"
                      +"<min>"+minValue+"</min>"
                      +"<max>"+maxValue+"</max>"
                      +"</range>"
                      +"<nullValues>"
                      +"<double>"
                      +noValue
                      +"</double>"
//                      +"<singleValue>"+noValue+"</singleValue>"
                      +"</nullValues>"
                      +"</coverageDimension>"
                      +"</dimensions>"
//                  
                      + "<requestSRS>"        
                      + "<string>EPSG:"+epsgCode+"</string>"
                      + "</requestSRS>"
                      + "<responseSRS>"
                      + "<string>EPSG:"+epsgCode+"</string>"
                      + "</responseSRS>"
                      
                      +"<parameters>"
                      +"<entry>"
                      +"<string>InputTransparentColor</string>"
                      +"<string></string>"
                      +"</entry>"
                      +"<entry>"
                      +"<string>SUGGESTED_TILE_SIZE</string>"
                      +"<string>512,512</string>"
                      +"</entry>"
                      +"</parameters>"
                      
                      +"</coverage>";
          
      }
		
//		System.out.println("params: \n" + params);
		
		if(params.length() > 0){
		
        ClientResponse cr =
                this.client.resource(resAddr).type("application/xml").header("Authorization",
                    createAuthentication()).put(ClientResponse.class, params);
        
//        System.out.println("change layer info-" + cr.getStatus());
            if(cr.getStatus()==200){
                reloadGS();
                LOG.info("Layer sucessful updated");
                return Response.status(cr.getStatus()).build();
            } else {
                String msg = "Layer update failed: " + cr.getEntity(String.class);
                return Response.status(cr.getStatus()).entity(msg).build();
            }
		}else {
		    String msg = "Layer not updated - parameter mised or not supported format";
		    return Response.status(200).entity(msg).build();
		}
    }
    
    public Response getCoverageLayerRessourceURL (String workspaceid, String layerName){
        
        final String resAddr = this.baseUri + "/rest/" + WS + "/" + workspaceid + "/" + CS + "/" + layerName + ".xml";
        
        WebResource webResource = this.client.resource(resAddr);
        ClientResponse dwcCR = webResource.type("application/xml").header("Authorization",createAuthentication()).get(ClientResponse.class);
        
        String sourceContent = dwcCR.getEntity(String.class);
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        Document doc = null;
 
        try (InputStream in = new ByteArrayInputStream(sourceContent.getBytes());){
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(in);
        } catch (ParserConfigurationException | SAXException | IOException e2) {
            String msg = "Error while reading coverage informations" + e2.getMessage();
            LOG.info(msg);
            return Response.status(500).entity(msg).build();
        }
        
        Node rootNode = doc.getDocumentElement();
        
        String sourceFileName = "";
        
        for (int i = 0; i<rootNode.getChildNodes().getLength(); i++){
            
            if(rootNode.getChildNodes().item(i).getLocalName()!=null){  
                if(rootNode.getChildNodes().item(i).getLocalName().equals("url")){
                    sourceFileName = rootNode.getChildNodes().item(i).getFirstChild().getTextContent().replace("file:/", "");
                }
            }
        }
        
        if(sourceFileName.length() == 0){
            return Response.status(Status.NO_CONTENT).build();
        } else {
            return Response.status(Status.OK).entity(sourceFileName).build();
        }
    }
    
    public Response moveCoverageLayerRessource(String workspaceid, String layerName, String sourceUrlString){
        
//      String resAddr = "http://localhost:8090/geoserver/rest/workspaces/testws/coveragestores/admin_20120411_103242_947_2ExfbS/coverages/admin_20120411_103242_947_2ExfbS.xml";
        final String resAddr = this.baseUri + "/rest/" + WS + "/" + workspaceid + "/" + CS + "/" + layerName + ".xml";
        
        String params =
                "<coverageStore>"                
                +"<url>file:/"+sourceUrlString+"</url>"
                + "</coverageStore>";
        
        ClientResponse cr =
                this.client.resource(resAddr).type("application/xml").header("Authorization",
                    createAuthentication()).put(ClientResponse.class, params);
        
//        System.out.println("change layer info-" + cr.getStatus());
        if(cr.getStatus()==200){
            reloadGS();
            LOG.info("Layer sucessful updated");
            return Response.status(cr.getStatus()).build();
        } else {
            String msg = "Layer update failed: " + cr.getEntity(String.class);
            return Response.status(cr.getStatus()).entity(msg).build();
        }
    }
    
    public String getCoverageURL(String geoserverUrlExtern, String workspaceid, String coverageid){
        
        String coverage = "";
        coverageid = coverageid.replace(workspaceid+":", "");
        
        InputStream entity = null;
        
        reloadGS();
        
        try {
            coverage = this.baseUri + "/rest/"+WS+"/"+workspaceid+"/"+CS+"/"+coverageid+".xml";
//            LOG.info("coverage xml: " + coverage);
            ClientResponse cr = this.client.resource(coverage).header("Authorization", createAuthentication()).get(ClientResponse.class);
            entity = cr.getEntityInputStream();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up workspaces ", e.getMessage());
            return "";
        }
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        Document doc = null;
        
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(entity);
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
                
        String url = "";
        NodeList nodeList = doc.getElementsByTagName("url");
        
        if( nodeList == null ||nodeList.getLength() == 0){
           return "Something wrong with collect getCoverageURL"; 
        } else {
            if(nodeList.item(0).getFirstChild().getNodeValue() != null){
                url = nodeList.item(0).getFirstChild().getNodeValue();
            } else {
                return "ERROR: URL value not available";
            }
        }
        
        
        String replacedUrl = url.replace("file:", "").replace(dataDir, geoserverUrlExtern+"/workflow/rest/data");

        if(replacedUrl.startsWith("/")){
           replacedUrl = replacedUrl.substring(1, replacedUrl.length());
        }
        
        return replacedUrl;
        
    }
    
    public String getCoverageInfo(String geoserverUrlExtern, String workspaceid, String coverageid){
         
        String coverage = "";
        InputStream entity = null;
        
        reloadGS();
        
        try {
            coverage = this.baseUri + "/rest/"+WS+"/"+workspaceid+"/"+CS+"/"+coverageid+"/coverages/"+coverageid+".xml";
            LOG.info("coverage xml: " + coverage);
            ClientResponse cr =
                this.client.resource(coverage).header("Authorization", createAuthentication()).get(ClientResponse.class);

            entity = cr.getEntityInputStream();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up workspaces ", e.getMessage());
            return "";
        }
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        Document doc = null;
        
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(entity);
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
                
        String srs = "", boundingbox = "", nativeFormat = "", resolution = "", supportedFormats = "";
        NodeList nodeList = doc.getElementsByTagName("coverage");

        if( nodeList == null ||nodeList.getLength() == 0){
           return "Something wrong with collect getCoverageInfo"; 
        } else {
        
            for (int i = 0; i < nodeList.getLength(); i++) {
    
                Node node = nodeList.item(i);
                NodeList nodeList1 = node.getChildNodes();
                
                if(nodeList1.getLength() > 0){
                    
                    for (int j = 0; j < nodeList1.getLength(); j++){
                        
                        Node node1 = nodeList1.item(j);
                        if(node1.getLocalName() != null){
                            
                            String localName = node1.getLocalName();
                            
                            switch(localName){
                            
                                case "srs":
                                    srs = node1.getFirstChild().getNodeValue();
                                    break;
                                    
                                case "nativeFormat":
                                    nativeFormat = node1.getFirstChild().getNodeValue();
                                    break;
                                    
                                case "latLonBoundingBox":{                                    
                                    NodeList nodelist = node1.getChildNodes();
                                    String minx = "", miny = "", maxx = "", maxy = "";
                                    for (int aa = 0; aa < nodelist.getLength(); aa++){
                                        Node node2 = nodelist.item(aa);
                                        if(node2.getLocalName() != null){
                                            switch(node2.getLocalName()){
                                                case "minx":{minx = node2.getFirstChild().getNodeValue(); break;}
                                                case "miny":{miny = node2.getFirstChild().getNodeValue(); break;}
                                                case "maxx":{maxx = node2.getFirstChild().getNodeValue(); break;}
                                                case "maxy":{maxy = node2.getFirstChild().getNodeValue(); break;}
                                            }                                            
                                        }                                  
                                    }
                                    boundingbox = minx+","+miny+","+maxx+","+maxy;
                                    break;
                                }                                
                                case "grid":{                                    
                                    NodeList nodelist1 = node1.getChildNodes();
                                    for (int aa = 0; aa < nodelist1.getLength(); aa++){
                                        Node node2 = nodelist1.item(aa);
                                        if(node2.getLocalName() != null){
                                            if(node2.getLocalName().equals("range")){
                                                NodeList nodelist2 = node2.getChildNodes();
                                                for (int bb = 0; bb < nodelist2.getLength(); bb++){
                                                    Node node3 = nodelist2.item(bb);
                                                    if(node3.getLocalName() != null){
                                                        if(node3.getLocalName().equals("high")){
                                                            String res = node3.getFirstChild().getNodeValue().replace(" ", ",");
                                                            resolution = res;
                                                        }
                                                    }
                                                }
                                            }
                                        }                                  
                                    }
                                    break;
                                }
                                case "supportedFormats":{
                                    NodeList nodelist = node1.getChildNodes();
                                    StringBuffer sb = new StringBuffer();
                                    int ix = 0;
                                    for (int aa = 0; aa < nodelist.getLength(); aa++){
                                        Node node2 = nodelist.item(aa);
                                        if(node2.getLocalName() != null){
                                           if(ix > 0){ sb.append(","); }
                                           sb.append(node2.getFirstChild().getNodeValue());                                         
                                           ix++;
                                        }
                                    }
                                    supportedFormats = sb.toString();
                                    break;
                                }
                            }
                        }
                    } 
                }
            }
        }
        
        String[] splitResult = resolution.split(","); 
        
        String coverageInfo =
                
                "{\"wcstiffurl\":\"" + geoserverUrlExtern + "/geoserver/ows?service=WCS&version=1.0.0&request=GetCoverage&sourcecoverage="+ workspaceid+":"+coverageid+"&FORMAT=image/tiff&bbox="+boundingbox+"&crs="+srs+"&width="+splitResult[0]+"&height="+splitResult[1]+"\",\n"+
                 "\"wcsgridurl\":\"" + geoserverUrlExtern + "/geoserver/ows?service=WCS&version=1.0.0&request=GetCoverage&sourcecoverage="+ workspaceid+":"+coverageid+"&FORMAT=application/arcgrid&bbox="+boundingbox+"&crs="+srs+"&width="+splitResult[0]+"&height="+splitResult[1]+"\",\n"+
                 "\"pngurl\":\"" + geoserverUrlExtern + "/geoserver/ows?service=WMS&version=1.1.0&request=GetMap&layers="+ workspaceid+":"+coverageid+"&styles=&bbox="+boundingbox+"&crs="+srs+"&width="+splitResult[0]+"&height="+splitResult[1]+"&format=image%2Fpng\",\n"+
                 "\"wmsurl\":\"" + geoserverUrlExtern + "/geoserver/ows?service=WMS&version=1.1.0&request=GetMap"+"\",\n"+
                 "\"layername\":\""+ workspaceid+":"+coverageid+"\",\n"+
                 "\"nativeFormat\":\""+ nativeFormat+"\",\n"+
                 "\"srs\":\""+ srs+"\",\n"+
                 "\"boundingbox\":\""+ boundingbox+"\",\n"+ 
                 "\"resolution\":\""+ resolution+"\",\n"+
                 "\"supportedFormats\":\""+ supportedFormats+"\"}";
        
//        http://biovel.iais.fraunhofer.de:80/geoserver/ows?service=wcs&request=GetCoverage&version=1.0.0&sourcecoverage=admin:diff_fileA_fileB_20120627_155115_339&FORMAT=application/arcgrid&bbox=-180.0,-90.0,180.0,90&crs=EPSG:4326&width=720&height=360
            
//        {"diffresult tiff":"http://biovel.iais.fraunhofer.de:80/geoserver/ows?service=WMS&version=1.1.0&request=GetMap&layers=admin:diff_fileA_fileB_20120627_155115_339&FORMAT=image/tiff&bbox=-180.0,-90.0,180.0,90&crs=EPSG:4326&width=720&height=360"
//            "diffresult arcgrid":"http://biovel.iais.fraunhofer.de:80/geoserver/ows?service=wcs&request=GetCoverage&version=1.0.0&sourcecoverage=admin:diff_fileA_fileB_20120627_155115_339&FORMAT=application/arcgrid&bbox=-180.0,-90.0,180.0,90&crs=EPSG:4326&width=720&height=360"
                        
        
            return coverageInfo;
    }
    
    public int createStyleRessource(String inStyleName){
        
      Check.notNull(inStyleName);
        
        final String resAddr = this.baseUri + "/rest/styles";
        String exprString = "<style><name>"+inStyleName+"</name><filename>"+inStyleName+".sld</filename></style>";
        
        ClientResponse cr =
            this.client.resource(resAddr).type("application/xml").header("Authorization",
                createAuthentication()).post(ClientResponse.class, exprString);
        
        if (cr.getStatus() == 201) {
            setStyleName(inStyleName);
            LOG.info("Created StyleRessource '" + inStyleName + "' to " + resAddr);
        } else {
          final String mesg = "Error creating StyleRessource " + cr.getEntity(String.class);
          LOG.log(Level.SEVERE, mesg);
          throw new RuntimeException(mesg);
        }
        
//        System.out.println("cs create coverage-" + cr.getStatus());
        
        reloadGS();     
        
        int crStatus = cr.getStatus();
        cr.close();
        
        return crStatus;   
        
    }
    
    public int uploadStyle(String style){
        
      Check.notNull(style);

      final String resAddr = this.baseUri + "/rest/styles/"+styleName;
        
        ClientResponse cr =
            this.client.resource(resAddr).type("application/vnd.ogc.sld+xml").header("Authorization",
                createAuthentication()).put(ClientResponse.class, style);
        
        if (cr.getStatus() == 201) {
            LOG.info("uploaded Style '" + style + "' to " + resAddr);
        } else {
          final String mesg = "Error uploading Style " + cr.getEntity(String.class);
          LOG.log(Level.SEVERE, mesg);
          throw new RuntimeException(mesg);
        }
        
//        System.out.println("cs create coverage-" + cr.getStatus());
        if(cr.getStatus()==200){
            LOG.info("Style sucessful uploaded");
        }
        
        reloadGS();     
        
        int crStatus = cr.getStatus();
        cr.close();
        
        return crStatus;       
    }
    
    public Response updateStyle(String styleName){
        
//        http://localhost:8090/geoserver/rest/layers/2ExfbS.xml
        
//        System.out.println("update style to " + styleName);
        //will nicht!
        
        final String resAddr = this.baseUri + "/rest/layers/"+coverageStoreName+".xml";
        
        System.out.println("resadr: " + resAddr);
        
        String params =
                
               "<layer>"
               + "<name>"+coverageStoreName+"</name>"
               + "<type>RASTER</type>"
               + "<defaultStyle>"
               + "<name>"+styleName+"</name>"
               + "<atom:link xmlns:atom=\"http://www.w3.org/2005/Atom\" rel=\"alternate\" href=\"http://localhost:8080/geoserver/rest/styles/"+styleName+".xml\" type=\"application/xml\"/>"
               + "</defaultStyle>"
               + "<resource class=\"coverage\">"
               + "<name>"+coverageStoreName+"</name>"
               + "<atom:link xmlns:atom=\"http://www.w3.org/2005/Atom\" rel=\"alternate\" href=\"http://localhost:8080/geoserver/rest/workspaces/"+workspaceName+"/coveragestores/"+coverageStoreName+"/coverages/"+coverageStoreName+".xml\" type=\"application/xml\"/>"
               + "</resource>"
               + "<enabled>true</enabled>"
               + "<metadata>"
               + "<entry key=\"GWC.gridSets\">EPSG:900913,EPSG:4326</entry>"
               + "<entry key=\"GWC.cacheFormats\">image/gif,image/png,image/jpeg</entry>"
               + "<entry key=\"GWC.gutter\">0</entry>"
               + "<entry key=\"GWC.enabled\">true</entry>"
               + "<entry key=\"GWC.autoCacheStyles\">true</entry>"
               + "<entry key=\"GWC.metaTilingX\">4</entry>"
               + "<entry key=\"GWC.metaTilingY\">4</entry>"
               + "</metadata>"
               + "<attribution>"
               + "<logoWidth>0</logoWidth>"
               + "<logoHeight>0</logoHeight>"
               + "</attribution>"
               + "</layer>";
        
//        System.out.println("style params: \n " + params);
        

        ClientResponse cr =
                this.client.resource(resAddr).type("application/xml").header("Authorization",
                    createAuthentication()).put(ClientResponse.class, params);
        
        if (cr.getStatus() != 200) {
            final String mesg =
                "Error adding layerstyle status: " + cr.getStatus() + " (String)entity: " + cr.getEntity(String.class);
            LOG.log(Level.SEVERE, mesg);
            return Response.status(cr.getStatus()).entity(mesg).build();
        }else {
            reloadGS();
            LOG.info("Style sucessful updated");
            return Response.status(cr.getStatus()).build();
        }
    }
    
    public Response updateMetadata(){
        
//      http://localhost:8090/geoserver/rest/layers/2ExfbS.xml
      
      final String resAddr = this.baseUri + "/rest/layers/"+coverageStoreName+".xml";
      
      String params =
              
             "<layer>"
             + "<name>"+coverageStoreName+"</name>"
             + "<type>RASTER</type>"
             + "<resource class=\"coverage\">"
             + "<name>"+coverageStoreName+"</name>"
             + "<atom:link xmlns:atom=\"http://www.w3.org/2005/Atom\" rel=\"alternate\" href=\"http://localhost:8080/geoserver/rest/workspaces/"+workspaceName+"/coveragestores/"+coverageStoreName+"/coverages/"+coverageStoreName+".xml\" type=\"application/xml\"/>"
             + "</resource>"
             + "<enabled>true</enabled>"
             + "<metadata>"
             + "<entry key=\"GWC.gridSets\">EPSG:900913,EPSG:4326</entry>"
             + "<entry key=\"GWC.cacheFormats\">image/gif,image/png,image/jpeg</entry>"
             + "<entry key=\"GWC.gutter\">0</entry>"
             + "<entry key=\"GWC.enabled\">true</entry>"
             + "<entry key=\"GWC.autoCacheStyles\">true</entry>"
             + "<entry key=\"GWC.metaTilingX\">4</entry>"
             + "<entry key=\"GWC.metaTilingY\">4</entry>"
             + "</metadata>"
             + "<attribution>"
             + "<logoWidth>0</logoWidth>"
             + "<logoHeight>0</logoHeight>"
             + "</attribution>"
             + "</layer>";
      
      
      ClientResponse cr =
              this.client.resource(resAddr).type("application/xml").header("Authorization",
                  createAuthentication()).put(ClientResponse.class, params);
      
      if (cr.getStatus() != 200) {
          final String mesg =
              "Error updating metadata: " + cr.getStatus() + " (String)entity: " + cr.getEntity(String.class);
          LOG.log(Level.SEVERE, mesg);
          return Response.status(cr.getStatus()).entity(mesg).build();
      } else {
          reloadGS();
          LOG.info("Style sucessful updated");
          return Response.status(cr.getStatus()).build();
      }

  }
    
    
    public void reloadGS(){
    	
        ClientResponse cr =
                this.client.resource(this.baseUri + "/rest/reload").type("application/xml").header("Authorization",
                    createAuthentication()).post(ClientResponse.class);
        
        if(cr.getStatus() != 200){
        	LOG.info("something wrong with reload GeoServer - status: " + cr.getStatus() + " // " + cr.getClientResponseStatus());
        }
    	
    }
    

    public URI addLayer(String layerName) {

        //not nice, but speeds up initialisation
        final String fixedBbox = "<minx>5.0</minx><maxx>15.0</maxx><miny>47</miny><maxy>55</maxy><crs>EPSG:4326</crs>";

        final String data =
            "<featureType><name>" + layerName + "</name>" + "<nativeBoundingBox>" + fixedBbox + "</nativeBoundingBox>"
                    + "<latLonBoundingBox>" + fixedBbox + "</latLonBoundingBox>" + "</featureType>";

        final String ftResPath = createFeatureTypePath();

        LOG.info("Adding layer '" + layerName + "' to " + ftResPath + " with data " + data);

        //Note: if this is throwing a NPE, then the GS is probably not OK.
        //I'm still trying to understand why the NPE is thrown here. The client says the
        //resource exists
        ClientResponse cr =
            this.client.resource(ftResPath).header("Authorization", createAuthentication()).header("Content-Length",
                data.length()).accept("*/*").type("text/xml").post(ClientResponse.class, data);

        if (cr.getStatus() != 201) {
            final String mesg =
                "Error adding layer. status: " + cr.getStatus() + " (String)entity: " + cr.getEntity(String.class);
            LOG.log(Level.SEVERE, mesg);
            //throw new RuntimeException(mesg); XXX KHS activate
        }
        cr.close();

        URI u;
        try {
            u = new URI(this.baseUri + "/layers/" + layerName);
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return u;

    }

    

    private String createFeatureTypePath() {

        final StringBuilder resPath = new StringBuilder();

        resPath.append(this.baseUri).append('/').append(WS).append('/').append(this.workspaceName).append('/').append(
            DS).append('/').append(this.datastoreName).append('/').append(FTs);

        return resPath.toString();
    }

    public void removeLayer(String layerName) {

        LOG.info("Removing layer '" + layerName);

        final StringBuilder elResPath = new StringBuilder();
        elResPath.append(this.baseUri).append('/').append(LAYERS).append('/').append(layerName);

        final String layerResPath = elResPath.toString();

        if (LOG.isLoggable(Level.FINE)) {
            LOG.info("Deleting layer '" + layerName + "' at " + layerResPath);
        }

        final String auth = createAuthentication();

        ClientResponse cr =
            this.client.resource(layerResPath).header("Authorization", auth).accept("*/*").delete(ClientResponse.class);
        if (cr.getStatus() != 200) {
            final String mesg = "Error deleting layer. " + cr.getEntity(String.class);
            LOG.log(Level.SEVERE, mesg);
            // throw only an exception, if the status is not "DOCUMENT NOT FOUND"
            // which basically means .. we're already safe .. there is nothing to delete
            if (cr.getStatus() != 404) {
                throw new RuntimeException(mesg);
            }
        }
        if (cr != null) {
            cr.close();
        }
        final String ftResPath = createFeatureTypePath() + '/' + layerName;

        if (LOG.isLoggable(Level.FINE)) {
            LOG.info("Deleting featuretype '" + layerName + "' at " + ftResPath);
        }

        cr = this.client.resource(ftResPath).header("Authorization", auth).accept("*/*").delete(ClientResponse.class);

        if (cr.getStatus() != 200) {
            final String mesg = "Error deleting feature type. " + cr.getEntity(String.class);
            LOG.log(Level.SEVERE, mesg);
            // throw only an exception, if the status is not "DOCUMENT NOT FOUND"
            // which basically means .. we're already safe .. there is nothing to delete
            if (cr.getStatus() != 404) {
                throw new RuntimeException(mesg);
            }
        }
        if (cr != null) {
            cr.close();
        }
    }

    private String createAuthentication() {
        //Note to TQS: string instatiation from a byte[] is exactly what is wanted here -> do no touch
        final String b64 = new String(MmpBase64.encode(this.user + ":" + this.passwd));
        return "Basic " + b64;
    }
    
    public static String createAuthentication(String user, String passwd) {
        //Note to TQS: string instatiation from a byte[] is exactly what is wanted here -> do no touch
        final String b64 = new String(MmpBase64.encode(user + ":" + passwd));
        return "Basic " + b64;
    }

    public final void setDatastore(String dsName) {
        Check.notNull(dsName);
        Check.isFalse(dsName.contains("/"));
        this.datastoreName = dsName;
    }

    public final void setWorkspace(String wsName) {
        Check.notNull(wsName);
        Check.isFalse(wsName.contains("/"));
        this.workspaceName = wsName;
    }
    
    public final String getWorkspacenameFromUsername() {
        this.workspaceName = getUser();
        return this.workspaceName;
    }
    
    public final void setCoverageStore(String csName) {
        Check.notNull(csName);
        Check.isFalse(csName.contains("/"));
        this.coverageStoreName = csName;
    }

    /**
     * Sets the user name. Cannot be null.
     * 
     * @param user
     */
    public final void setUser(String user) {
        Check.notNull(user);
        Check.isTrue(user.length() > 0);
        this.user = user;

    }
    
    /**
     * Sets the data directory. Cannot be null.
     * 
     * @param dataDir
     */
    public final void setDataDir(String dataDir) {
        Check.notNull(dataDir);
        Check.isTrue(dataDir.length() > 0);
        this.dataDir = dataDir;

    }
    
    /**
     * Sets the data URL. Cannot be null.
     * 
     * @param dataDir
     */
    public final void setDataURL(String dataURL) {
        Check.notNull(dataURL);
        Check.isTrue(dataURL.length() > 0);
        this.dataURL = dataURL;

    }    
    
    
    /**
     * get the current username
     * @return
     */
    
    public final String getUser() {
        return this.user;
    }

    /**
     * Sets the password. Cannot be null.
     * 
     * @param passwd
     */
    public final void setPasswd(String passwd) {
        Check.notNull(passwd);
        Check.isTrue(passwd.length() > 0);
        this.passwd = passwd;
    }
    
    public final void setStyleName(String styleName) {
        Check.notNull(styleName);
        Check.isTrue(styleName.length() > 0);
        this.styleName = styleName;
    }
    
    public String getDatastore() {
        return this.datastoreName;
    }

    public String getWorkspace() {
        return this.workspaceName;
    }
    
    public String getCoverageStore() {
        return this.coverageStoreName;
    }

    public URI getBaseUri() {
        return this.baseUri;
    }

    /**
     * Sets the log mode for HTTP requests. {@link LogMode.None}: no output will
     * be performed {@link LogMode.Console}: output direct to the console
     * (System.out) {@link LogMode.None}: output to this class' logger
     * 
     * @param mode
     */
    public void setLogMode(LogMode mode) {
        if (this.loggingFilter != null) {
            this.client.removeFilter(this.loggingFilter);
            this.loggingFilter = null;
        }

        switch (mode) {

        case LOGGER:
            this.loggingFilter = new LoggingFilter(RestGeoServerAdapter.LOG);
            break;

        case CONSOLE:
            this.loggingFilter = new LoggingFilter(System.out);
            break;

        default: //None
            break;
        }

        if (this.loggingFilter != null) {
            this.client.addFilter(this.loggingFilter);
        }

    }

    public URI postStyle(String layerName, String attributeName) {

        Check.notNull(layerName);
        Check.notNull(attributeName);

        final String styleName = layerName;
        try {
            deleteStyle(layerName + "_style");
        } catch (Exception exn) {
            LOG.warning("postStyle: ignore MmpException when deleting style:" + layerName + "_style");
        }

        LOG.info("Posting style " + styleName);

        final String data =
            "<style><name>" + styleName + "_style</name><filename>" + styleName + "_style.sld</filename></style>";

        final String resAddr = this.baseUri + "/styles";

        if (LOG.isLoggable(Level.FINE)) {
            LOG.info("Creating style '" + data + "' at " + resAddr);
        }

        ClientResponse cr =
            this.client.resource(resAddr).type(MediaType.TEXT_XML).header("Authorization", createAuthentication())
                .post(ClientResponse.class, data);

        if (cr.getStatus() != 201) {
            final String mesg =
                "Error creating style. status: " + cr.getStatus() + " (String)entity: " + cr.getEntity(String.class);
            LOG.log(Level.SEVERE, mesg);
            throw new RuntimeException(mesg);
        }

        final String us = cr.getHeaders().getFirst("Location");

        URI u;
        try {
            u = new URI(us);
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        //must tell layer its default style is now this one
        updateLayer(layerName);

        return u;
    }

    private void updateLayer(String layerName) {
//        curl -i -u admin:geoserver -XPUT -H 'Content-type: text/xml' 
//        -d @plz_layer.xml http://localhost:8080/geoserver/rest/layers/plz

        String req = createLayerUpdateRequest(layerName);

        ClientResponse cr =
            this.client.resource(this.baseUri + "/layers/" + layerName).type("text/xml").header("Authorization",
                createAuthentication()).put(ClientResponse.class, req);
        
        if(cr != null){
            cr.close();
        }

    }

    public void putStyle(String styleName, String styleData) {

        Check.notNull(styleName);
        Check.notNull(styleData);

        final String resAddr = this.baseUri + "/styles/" + styleName;

        LOG.info("Putting style '" + styleName + "' to " + resAddr);

        ClientResponse cr =
            this.client.resource(resAddr).type("application/vnd.ogc.sld+xml").header("Authorization",
                createAuthentication()).put(ClientResponse.class, styleData);

        if (cr.getStatus() != 200) {
            final String mesg = "Error putting style. " + cr.getEntity(String.class);
            LOG.log(Level.SEVERE, mesg);
            throw new RuntimeException(mesg);
        } else {
            System.out.println("NUKEE " + cr.getStatus());
        }
        
        if(cr != null){
            cr.close();
        }

    }
    
    public String listStyles(String geoserverUrlExtern) {
        
        String workspaces = "";
        InputStream entity = null;
        
        try {
            workspaces = this.baseUri + "/rest/styles.xml";
            ClientResponse cr =
                this.client.resource(workspaces).header("Authorization", createAuthentication()).get(ClientResponse.class);

            entity = cr.getEntity(InputStream.class);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up styles ", e.getMessage());
            return "";
        }
        
        List<String> styleList = new ArrayList<String>();
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        Document doc = null;
        
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(entity);
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        NodeList nodeList = doc.getElementsByTagName("style");

        for (int i = 0; i < nodeList.getLength(); i++) {

            Node node = nodeList.item(i);
            NodeList nodeList1 = node.getChildNodes();
            
            if(nodeList1.getLength() > 0){
                
                for (int j = 0; j < nodeList1.getLength(); j++){
                    
                    Node node1 = nodeList1.item(j);
                    
                    if(node1.getLocalName() != null && node1.getLocalName().equals("name")){
                        styleList.add(node1.getFirstChild().getNodeValue());
                    }
                } 
            }
        }
        
        String htmlResponse = 
                
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
                +"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
                +"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n"
                +"<head>\n"
                +"<title>BioVeL GeoServer Workspaces</title>\n"
                +"<meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"/>\n"
                +"</head>\n"
                +"<body>\n"
                +"Existing Workspaces\n"
                +"<ul>\n";
        
        for(int k = 0; k < styleList.size(); k++){
            htmlResponse += "<li><a>"+styleList.get(k)+"</a></li>\n";
        }

        htmlResponse +=
                "</ul>"
                +"</body>"
                +"</html>";
        
        return htmlResponse;
    }

    public void deleteStyle(String styleName) {

        Check.notNull(styleName);

        final String resAddr = this.baseUri + "/styles/" + styleName + "?purge=true";

        LOG.info("Deleting style '" + styleName + "' from " + resAddr);

        ClientResponse cr =
            this.client.resource(resAddr).header("Authorization", createAuthentication()).delete(ClientResponse.class);

        final int crStatus = cr.getStatus();
        // 200 OK: delete sucessful
        // 404 NotFound: style does not exist, delete is not necessary.
        if ((crStatus != 200) && (crStatus != 404)) {
            final String msg =
                "Error deleting style. Response Status==" + cr.getStatus() + " '" + cr.getEntity(String.class) + "'";
            LOG.log(Level.SEVERE, msg);
            cr.close();
//            throw new MmpException(msg);
        }
        if (cr != null) {
            cr.close();
        }

    }

    private String createLayerUpdateRequest(String layerName) {

        final String styleName = layerName + "_style";

        StringBuilder sb = new StringBuilder();
        sb.append("<layer><name>").append(layerName).append("</name><path>/</path><type>VECTOR</type>").append(
            "<defaultStyle><name>").append(styleName).append("</name>").append(
            "<atom:link xmlns:atom=\"http://www.w3.org/2005/Atom\" rel=\"alternate\" ").append(" href=\"").append(
            this.baseUri).append("/rest/styles/").append(styleName).append(
            ".xml\" type=\"application/xml\"/></defaultStyle>").append(" <resource class=\"featureType\">").append(
            "<name>").append(layerName).append("</name>").append(
            "<atom:link xmlns:atom=\"http://www.w3.org/2005/Atom\"").append("rel=\"alternate\" ").append("href=\"")
            .append(this.baseUri).append("/workspaces/").append(this.workspaceName).append("/datastores/").append(
                this.datastoreName).append("/featuretypes/").append(layerName).append(".xml\"").append( //fix datastorename, mm 20110704
                " type=\"application/xml\"/> ").append("</resource><enabled>true</enabled></layer> ");

        String r = sb.toString();
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Update layer style request: " + r);
        }

        return r;
    }
    
    public String listWS(String geoserverUrlExtern, String authWorkspaces, String acceptHeader) {
        
//        reloadGS();
        
        String workspaces = "";
        InputStream entity = null;
        List<String> authWorkspacesList = Arrays.asList(authWorkspaces.split(","));
        
        //check if WS exist
        for (int wsp = 0; wsp< authWorkspacesList.size(); wsp++){
//            System.out.println("wsp: " + authWorkspacesList.get(wsp));
            if(!existsWS(authWorkspacesList.get(wsp))){
               createWorkspace(authWorkspacesList.get(wsp)); 
            }
        }
        
        try {
            workspaces = this.baseUri + "/rest/workspaces.xml";
            ClientResponse cr =
                this.client.resource(workspaces).header("Authorization", createAuthentication()).get(ClientResponse.class);

            entity = cr.getEntity(InputStream.class);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up workspaces ", e.getMessage());
            return "";
        }
        
        List<String> wsList = new ArrayList<String>();
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        Document doc = null;
        
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(entity);
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        NodeList nodeList = doc.getElementsByTagName("workspace");

//        if(nodeList.getLength() == 0){
//           return ""; 
//        } else {
        
            for (int i = 0; i < nodeList.getLength(); i++) {
    
                Node node = nodeList.item(i);
                NodeList nodeList1 = node.getChildNodes();
                
                if(nodeList1.getLength() > 0){
                    
                    for (int j = 0; j < nodeList1.getLength(); j++){
                        
                        Node node1 = nodeList1.item(j);
                        
                        if(node1.getLocalName() != null && node1.getLocalName().equals("name")){
                            //only auth checked WS added
                            if(authWorkspacesList.contains(node1.getFirstChild().getNodeValue())){
                                wsList.add(node1.getFirstChild().getNodeValue());
                            }
                        }
                    } 
                }
            }
            
            StringBuilder response = new StringBuilder();
            
            if(acceptHeader.equalsIgnoreCase(XML)){
                
                response.append("<workspaces>");
                for(int k = 0; k < wsList.size(); k++){
                    response.append(
                    	"\n<workspace>"
                        +"\n<name>"+wsList.get(k)+"</name>"
                        +"\n<url>"+geoserverUrlExtern+"/shim/rest/raster/"+wsList.get(k)+"</url>"
//                        +"\n<atom:link xmlns:atom=\"http://www.w3.org/2005/Atom\" a href=\""+geoserverUrlExtern+"/shim/rest/raster/"+wsList.get(k)+" type=\"application/xml\"/>" 
                        +"\n</workspace>");
                };
                response.append("\n</workspaces>");

            } else {
                
            response.append( 
                    
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
                    +"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
                    +"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n"
                    +"<head>\n"
                    +"<title>BioVeL GeoServer Workspaces</title>\n"
                    +"<meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"/>\n"
                    +"</head>\n"
                    +"<body>\n"
                    +"Existing Workspaces\n"
                    +"<ul>\n"
                    );
            
            for(int k = 0; k < wsList.size(); k++){
                response.append("<li><a href=\""+geoserverUrlExtern+"/shim/rest/raster/"+wsList.get(k)+"\">"+wsList.get(k)+"</a></li>\n");
            }
    
            response.append(
                    "</ul>"
                    +"</body>"
                    +"</html>");
        }
            
            return response.toString();
//        }
    }
    
    /* check whether workspace exists in any */

    public boolean existsWS(String wsName) {
    	
        String workspaces = "";
        InputStream entity = null;
        
        try {
            workspaces = this.baseUri + "/rest/workspaces.xml";
            ClientResponse cr =
                    this.client.resource(workspaces).header("Authorization", createAuthentication()).get(ClientResponse.class);
            entity = cr.getEntity(InputStream.class);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up workspace " + wsName + " at " + workspaces, e.getMessage());
            return false;
        }
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        Document doc = null;
        
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(entity);
        } catch (ParserConfigurationException | SAXException | IOException e2) {
            LOG.info("something is wrong to parse workspaces xml" + e2.getMessage());
            return false;
        }
        
        NodeList nodeList = doc.getElementsByTagName("workspace");
        
        for (int i = 0; i < nodeList.getLength(); i++) {

            Node node = nodeList.item(i);
            NodeList nodeList1 = node.getChildNodes();
            
            if(nodeList1.getLength() > 0){
                
                for (int j = 0; j < nodeList1.getLength(); j++){
                    
                    Node node1 = nodeList1.item(j);
                    
                    if(node1.getLocalName() != null && node1.getLocalName().equals("name")){
                        if(node1.getFirstChild().getNodeValue().equals(wsName)){
                            return true;
                        }
                    }
                } 
            }
        }
        
        return false;      
    }
    
    
//    gsAdapter.repairLayers(geoserverUrlExtern, coverageList);
    
    public String repairLayers(String geoserverUrlExtern, String coverageList) {
        
        System.out.println("repair \n");
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(false);
        DocumentBuilder docBuilder;
        Document doc = null;
        
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(new ByteArrayInputStream(coverageList.getBytes()));
        } catch (ParserConfigurationException | SAXException | IOException e2) {
            e2.printStackTrace();
        }
        
        NodeList nodeList = doc.getElementsByTagName("layer");
        
        
            for (int i = 0; i < nodeList.getLength(); i++) {
    
                Node node = nodeList.item(i);
                NodeList nodeList1 = node.getChildNodes();
                
                if(nodeList1.getLength() > 0){
                    
                    for (int j = 0; j < nodeList1.getLength(); j++){
                        
                        Node node1 = nodeList1.item(j);
                        
                        if(node1.getNodeName().equals("name")){
                            String nodeValue = node1.getFirstChild().getNodeValue().replace("'", "");
                            
                            String[] splitResult = nodeValue.split(":");
                            
//                            System.out.println("\n res: " + splitResult[0] + " - "+ splitResult[1]);
                            
                            String resAddr = geoserverUrlExtern + "/rest/" + WS + "/" + splitResult[0] + "/" + CS + "/" + splitResult[1] + "/coverages/"+splitResult[1]+".xml";
                        
//                            InputStream entity = null;
                            String entity = "";
                            
                            try {
//                                layers = this.baseUri + "/rest/workspaces/"+workspaceid+"/coveragestores.xml";
                                ClientResponse cr =
                                    this.client.resource(resAddr).header("Authorization", createAuthentication()).get(ClientResponse.class);
//                                entity = cr.getEntity(InputStream.class);
                                entity = cr.getEntity(String.class);
//                                System.out.println(cr.getEntity(String.class));
                                
//                                System.out.println("enti: "+ entity);
                                if(entity.contains("No such coverage:")||entity.contains("No such coveragestore:")){
                                   continue; 
                                }
                                
                                DocumentBuilderFactory docFactory1 = DocumentBuilderFactory.newInstance();
                                docFactory1.setNamespaceAware(false);
                                DocumentBuilder docBuilder1;
                                Document doc1 = null;
                                
                                try {
                                    docBuilder1 = docFactory1.newDocumentBuilder();
                                    doc1 = docBuilder1.parse(new ByteArrayInputStream(entity.getBytes()));
                                } catch (ParserConfigurationException | SAXException | IOException e2) {
                                    e2.printStackTrace();
                                }
                                
                                //responseSRS 
                                //requestSRS
                                
                                NodeList nodeListWS = doc1.getElementsByTagName("responseSRS");
                                Node nodeWS = nodeListWS.item(0);
//                                System.out.println(nodeWS.getNodeName());
                                
                                String epsg = nodeWS.getChildNodes().item(1).getFirstChild().getNodeValue();
//                                System.out.println("nodeWS.getLocalName: "+nodeWS.getChildNodes().item(1).getFirstChild().getNodeValue());
                                
                                if(epsg.contains("EPSG:")){
                                    continue;
                                }
                                System.out.println(epsg);
                                
                              String params =
                              "<coverage>"
                              
                              + "<requestSRS>"
                              + "<string>EPSG:"+epsg+"</string>"
                              + "</requestSRS>"
                              + "<responseSRS>"
                              + "<string>EPSG:"+epsg+"</string>"
                              + "</responseSRS>"
                              
                              +"</coverage>";
                              
                              ClientResponse cru =
                                      this.client.resource(resAddr).type("application/xml").header("Authorization",
                                          createAuthentication()).put(ClientResponse.class, params);
                              
//                              System.out.println("change layer info-" + cr.getStatus());
                                  if(cru.getStatus()==200){
                                      reloadGS();
                                      LOG.info("Layer sucessful updated");
//                                      return Response.status(cr.getStatus()).build();
                                  } else {
                                      String msg = "Layer update failed: " + cru.getEntity(String.class);
                                      System.out.println(msg);
//                                      return Response.status(cr.getStatus()).entity(msg).build();
                                  }
                                
                            }
                            
                            
                                catch (Exception e) {
//                                LOG.log(Level.WARNING, "Something wrong when looking up coveragestores ", e.getMessage());
//                                return "";
//                            }
                        
                        }
                    } 
                }
            }
        
        

        
//        String epsgCode = "";
//        
//        String params =
//                "<coverage>"
//                
//                + "<requestSRS>"
//                + "<string>EPSG:"+epsgCode+"</string>"
//                + "</requestSRS>"
//                + "<responseSRS>"
//                + "<string>EPSG:"+epsgCode+"</string>"
//                + "</responseSRS>"
//                
//                +"</coverage>";
        
            }
     return "";
            
        
    }
    
    
    @SuppressWarnings("rawtypes")
    public String listCS(String workspaceid, String geoserverUrlExtern, String acceptHeader) {
        
        String layers = "";
        InputStream entity = null;
        
        try {
            layers = this.baseUri + "/rest/workspaces/"+workspaceid+"/coveragestores.xml";
            ClientResponse cr =
                this.client.resource(layers).header("Authorization", createAuthentication()).get(ClientResponse.class);
            entity = cr.getEntity(InputStream.class);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up coveragestores ", e.getMessage());
            return "";
        }
        
        List<String> csList = new ArrayList<String>();
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder;
        Document doc = null;
        
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(entity);
        } catch (ParserConfigurationException | SAXException | IOException e2) {
            e2.printStackTrace();
        }
        
        NodeList nodeList = doc.getElementsByTagName("coverageStore");
        
//        if(nodeList.getLength() == 0){
//            return "";
//        } else {

            for (int i = 0; i < nodeList.getLength(); i++) {
    
                Node node = nodeList.item(i);
                NodeList nodeList1 = node.getChildNodes();
                
                if(nodeList1.getLength() > 0){
                    
                    for (int j = 0; j < nodeList1.getLength(); j++){
                        
                        Node node1 = nodeList1.item(j);
                        
                        if(node1.getLocalName() != null && node1.getLocalName().equals("name")){
                            csList.add(node1.getFirstChild().getNodeValue());
                        }
                    } 
                }
            }
            
            StringBuilder response = new StringBuilder();
            
            if(acceptHeader.equalsIgnoreCase(XML)){ 
                
                Map<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
                for(int la = 0; la < csList.size(); la++){
                    
                    
                    String gs_LayerName_1 = csList.get(la);
                    int gsl_len_1 = gs_LayerName_1.length();
                    
                    String sortDate_1 = "";
                    if(gsl_len_1 > 19){
                        sortDate_1 = gs_LayerName_1.substring((gsl_len_1-19), gsl_len_1);
                    } else {
                        sortDate_1 = "00000000_"+la;
                    }
                    
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(workspaceid+":"+gs_LayerName_1);
                    list.add(geoserverUrlExtern);
                    
                    map.put(sortDate_1, list);
                    
                };
                
                Map<String, ArrayList<String>> treeMap = new TreeMap<String, ArrayList<String>>(map);
                Map<String, ArrayList<String>> descTreeMap = ((TreeMap<String, ArrayList<String>>) treeMap).descendingMap();
                
                response.append("<layers>");
                for (Map.Entry entry : descTreeMap.entrySet()) {
                    @SuppressWarnings("unchecked")
                    ArrayList<String> list =  (ArrayList<String>) entry.getValue();
                    
                  response.append(
                      "\n<layer>"
//                      +"\n<date>"+entry.getKey()+"</date>"
                      +"\n<name>"+list.get(0)+"</name>"
                      +"\n<url>"+list.get(1)+"/geoserver/ows?</url>"
//                      +"\n<atom:link xmlns:atom=\"http://www.w3.org/2005/Atom\" a href=\""+geoserverUrlExtern+"/shim/rest/raster/"+csList.get(k)+" type=\"application/xml\"/>" 
                      +"\n</layer>");
                    
                }
                
                response.append("\n</layers>");
                
            } else if (acceptHeader.equalsIgnoreCase(TXT_URI)){
                
                Map<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
                for(int la = 0; la < csList.size(); la++){
                    
                    
                    String gs_LayerName_1 = csList.get(la);
                    int gsl_len_1 = gs_LayerName_1.length();
                    
                    String sortDate_1 = "";
                    if(gsl_len_1 > 19){
                        sortDate_1 = gs_LayerName_1.substring((gsl_len_1-19), gsl_len_1);
                    } else {
                        sortDate_1 = "00000000_"+la;
                    }
                    
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(workspaceid+":"+gs_LayerName_1);
                    list.add(geoserverUrlExtern);
                    
                    map.put(sortDate_1, list);
                    
                };
                
                Map<String, ArrayList<String>> treeMap = new TreeMap<String, ArrayList<String>>(map);
                Map<String, ArrayList<String>> descTreeMap = ((TreeMap<String, ArrayList<String>>) treeMap).descendingMap();
                
                response.append("<layers>");
                for (Map.Entry entry : descTreeMap.entrySet()) {
                    @SuppressWarnings("unchecked")
                    ArrayList<String> list =  (ArrayList<String>) entry.getValue();
                    
                    //layerinfo
                    
                    String coverageURL = getCoverageURL(geoserverUrlExtern, workspaceid, list.get(0));
                    
//                    if(coverageURL.endsWith(".img")){
                    
                      response.append(
                          "\n<layer>"
    //                      +"\n<date>"+entry.getKey()+"</date>"
                          +"\n<name>"+list.get(0)+"</name>"
                          +"\n<url>"+list.get(1)+"/geoserver/ows?</url>"
                          +"\n<file>"+coverageURL+"</file>"
    //                      +"\n<atom:link xmlns:atom=\"http://www.w3.org/2005/Atom\" a href=\""+geoserverUrlExtern+"/shim/rest/raster/"+csList.get(k)+" type=\"application/xml\"/>" 
                          +"\n</layer>");
                    } 
                    
//                }
                
                response.append("\n</layers>");
                
            
            } else {
            
                //HTML Reasponse
                response.append( 
                        
                        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
                        +"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
                        +"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n"
                        +"<head>\n"
                        +"<title>BioVeL GeoServer Coverages</title>\n"
                        +"<meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"/>\n"
                        +"</head>\n"
                        +"<body>\n"
                        +"Existing Coverages in Workspace " + workspaceid + "\n"
                        +"<ul>\n");
                
                for(int k = 0; k < csList.size(); k++){
        //            htmlResponse += "<li><a href=\""+geoserverUrlExtern+"/shim/rest/raster/"+workspaceid+"/"+csList.get(k)+"\">"+csList.get(k)+"</a></li>\n";
                    response.append("<li><a>"+csList.get(k)+"</a></li>\n");
                }
        
                response.append(
                        "</ul>"
                        +"</body>"
                        +"</html>");
            
            }
            
            return response.toString();
//        }
    }
    
    public static void removeAll(Node node, short nodeType, String name) {
        if (node.getNodeType() == nodeType && (name == null || node.getNodeName().equals(name))) {
          node.getParentNode().removeChild(node);
        } else {
          NodeList list = node.getChildNodes();
          for (int i = 0; i < list.getLength(); i++) {
            removeAll(list.item(i), nodeType, name);
          }
        }
      }
    
    
    /* check whether coveragestore exists in any */

    public boolean existsCS(String csName) {
        // old code for checking in any coveragestores
       
        System.out.println("hier cs");
        
        String coveragestores = "";
        try {
            coveragestores = this.baseUri + "/rest/workspaces/"+workspaceName+"/"+CS+".json";
            ClientResponse cr =
                this.client.resource(coveragestores).header("Authorization", createAuthentication()).get(ClientResponse.class);

            String entity = cr.getEntity(String.class);
            return (entity != null) && entity.contains(csName);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up coveragestore " + csName + " at " + coveragestores, e
                .getMessage());
            return false;
        }
    }
    
    /* check whether coveragestore exists in given workspace (=prefix) */
    public boolean existsCL(String clName) {
        // old code for checking in any coveragestores
        
        String coverageLayers = "";
        try {
            coverageLayers = this.baseUri + "/rest/workspaces/"+workspaceName+"/"+CS+".json";
            ClientResponse cr =
                this.client.resource(coverageLayers).header("Authorization", createAuthentication()).get(ClientResponse.class);

            String entity = cr.getEntity(String.class);
            return (entity != null) && entity.contains(clName);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up coveragestore " + clName + " at " + coverageLayers, e
                .getMessage());
            return false;
        }
    }
    
    /* check whether coveragestore exists in given workspace (=prefix) */
    public boolean existsStyle(String styleName) {
        // old code for checking in any coveragestores
        
        String styles = "";
        try {
            styles = this.baseUri + "/rest/styles.json";
            ClientResponse cr =
                this.client.resource(styles).header("Authorization", createAuthentication()).get(ClientResponse.class);

            String entity = cr.getEntity(String.class);
            return (entity != null) && entity.contains(styleName);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up styles " + styleName + " at " + styles, e
                .getMessage());
            return false;
        }
    }
    
    /* check whether layer exists in any */

    public boolean existsLayer(String aLayer) {
        // old code for checking in any datastore
        String layers = "";
        try {
            layers = this.baseUri + "/layers.json";
            ClientResponse cr =
                this.client.resource(layers).header("Authorization", createAuthentication()).get(ClientResponse.class);

            String entity = cr.getEntity(String.class);
            return (entity != null) && entity.contains(aLayer);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up layer " + aLayer + " at " + layers, e
                .getMessage());
            return false;
        }

    }

    /* check whether layer exists in any */

    public boolean exists(String aLayer) {
        // old code for checking in any datastore
        String layers = "";
        try {
            layers = this.baseUri + "/layers.json";
            ClientResponse cr =
                this.client.resource(layers).header("Authorization", createAuthentication()).get(ClientResponse.class);

            String entity = cr.getEntity(String.class);
            return (entity != null) && entity.contains(aLayer);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Something wrong when looking up layer " + aLayer + " at " + layers, e
                .getMessage());
            return false;
        }
// code for checking in a specific datastore . problem - cannot create layer with same name in a different datastores, names must be unique
//        Log.info("Checkin for layer " + aLayer + " in datastore " + this.datastoreName);
//        final String resAddr = createFeatureTypePath() + ".json";
//        ClientResponse cr = null;
//        try {
//            cr =
//                this.client.resource(resAddr).type(MediaType.TEXT_XML).header("Authorization", createAuthentication())
//                    .get(ClientResponse.class);
//
//        } catch (Exception e) {
//            final String mesg = "Error checking layers " + e.getMessage();
//            logger.log(Level.SEVERE, mesg);
//            throw new RuntimeException(mesg);
//        }
//        Check.notNull(cr, "check for layer failed");
//
//        if (cr.getStatus() != 200) {
//            final String mesg = "Error getting layers " + cr + cr.getEntity(String.class);
//            logger.log(Level.SEVERE, mesg);
//            throw new RuntimeException(mesg);
//        }
//        String ftString = cr.getEntity(String.class);
//
//        JSONObject jsonObject = new JSONObject(ftString);
//        JSONObject featureTypes = null;
//        try {
//            featureTypes = jsonObject.getJSONObject("featureTypes");
//        } catch (JSONException e) {
//            Log.info("no layers found in datastore" + DS);
//        }
//        boolean found = false;
//        if (featureTypes != null) {
//
//            JSONArray jarray = featureTypes.getJSONArray("featureType");
//
//            for (int i = 0; (i < jarray.length()) && !found; i++) {
//                final String layerName = jarray.getJSONObject(i).getString("name");
//                found = aLayer.equals(layerName);
//            }
//        } else {
//            Log.info("no layers found in datastore" + DS);
//        }
//        return found;
    }

    public boolean resourceAvailable(String aResource) {
        
        try {

            ClientResponse cr =
                this.client.resource(aResource+"/rest").header("Authorization", createAuthentication()).get(
                    ClientResponse.class);
            return (cr.getStatus() == 200);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Geo-Server resource not available " + aResource, e.getMessage());
            return false;
        }
    }

    /*
     * Delete all Layers of the current datastore
     */

    public void deleteLayersAndStyles() {
        LOG.info("Deleting layers in datastore" + DS);
        final String resAddr = createFeatureTypePath() + ".json";
        ClientResponse cr = null;
        try {
            cr =
                this.client.resource(resAddr).type(MediaType.TEXT_XML).header("Authorization", createAuthentication())
                    .get(ClientResponse.class);

        } catch (Exception e) {
            final String mesg = "Error deleting style. " + e.getMessage();
            LOG.log(Level.SEVERE, mesg);
            throw new RuntimeException(mesg);
        }
        Check.notNull(cr, "delete Layers failed");

        if (cr.getStatus() != 200) {
            final String mesg = "Error getting layers " + cr + cr.getEntity(String.class);
            LOG.log(Level.SEVERE, mesg);
            throw new RuntimeException(mesg);
        }
        String ftString = cr.getEntity(String.class);

        JSONObject jsonObject = new JSONObject(ftString);
        JSONObject featureTypes = null;
        try {
            featureTypes = jsonObject.getJSONObject("featureTypes");
        } catch (JSONException e) {
            LOG.info("no layers found in datastore" + DS);
        }
        if (featureTypes != null) {

            JSONArray jarray = featureTypes.getJSONArray("featureType");

            for (int i = 0; i < jarray.length(); i++) {
                final String layerName = jarray.getJSONObject(i).getString("name");
                removeLayer(layerName);
                deleteStyle(layerName + "_style");
            }
        } else {
            LOG.info("no layers found in datastore" + DS);
        }
    }
}
