package de.fraunhofer.iais.kd.biovel.shim;

import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.SLD_XML;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.gdal.gdal.Band;
import org.gdal.gdal.ColorTable;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.RasterAttributeTable;
import org.gdal.gdal.TermProgressCallback;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import de.fraunhofer.iais.kd.biovel.shim.oauth.OauthClient;
import de.fraunhofer.iais.kd.biovel.shim.raster.RestGeoServerAdapter;


@Path("/raster")
public class ShimRasterResource {
    
    private static final Logger LOG = Logger.getLogger(ShimRasterResource.class.getName());
    private Client client;
    private RestGeoServerAdapter gsAdapter;
    private String geoserverUrl;
    private String publicWorkspace;// = "biovel_temp";

    private void setPublicWorkspaces(ServletConfig config){      
            Properties props = (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);
            publicWorkspace = props.getProperty("PUBLIC_WORKSPACE");
    }
    
    @GET
    @Path("ping")
    public Response dataPing() {
        LOG.info("../raster/ping");
        return Response.ok().build();
    }
    
    @GET
    @Path("pingtest")
    public Response dataPingTest(@Context ServletConfig config) {
        LOG.info("../raster/pingtest");
        createAdapter(config);
        gsAdapter.reloadGS();
        return Response.ok().build();
    }

    @HEAD
    public Response dataHead() {
        return Response.status(204).build();
    }
    
    //####### oAuth ##############//
    
    @GET
    @Path("/authrequest")
    public Response authRequest(@Context HttpServletRequest httpServletRequest,
                                @HeaderParam("X-Auth-Service-Provider") String authServiceProvider,
                               @HeaderParam("X-Verify-Credentials-Authorization") String credentialsAuthorizationToken) {
        
        if(authServiceProvider.length() == 0 || credentialsAuthorizationToken.length() == 0){
            final String msg = "ERROR: authCheck failed because no token or authentication URL is available!";
            return Response.status(401).entity(msg).build(); 
        }
        
        OauthClient oauthClient = new OauthClient();
        Response oauthResponse = oauthClient.checkAuthCredentials(credentialsAuthorizationToken, authServiceProvider);
        
        if(oauthResponse.getStatus() == 200){
            return Response.status(200).entity(oauthResponse.getEntity()).build();
        } else {
            final String msg = "ERROR: authCheck failed because: " + oauthResponse.getEntity();
            LOG.log(Level.SEVERE, msg);
            return Response.status(oauthResponse.getStatus()).entity(msg).build();
        }
    }
    
    @GET
    @Path("/authcheck")
    public Response authCheckRequest(@Context HttpServletRequest httpServletRequest,
                                     @HeaderParam("X-Auth-Service-Provider") String authServiceProvider,
                                     @HeaderParam("X-Verify-Credentials-Authorization") String credentialsAuthorizationToken){
//        System.out.println("hier authcheckReq");
//        System.out.println("token: " + authServiceProvider + " - " + credentialsAuthorizationToken);
        return authCheck(authServiceProvider, credentialsAuthorizationToken);
    }
    
    
    private Response authCheck(String authServiceProvider, String credentialsAuthorizationToken) {
        
         OauthClient oauthClient = new OauthClient();
         Response oauthResponse = oauthClient.checkAuthCredentials(credentialsAuthorizationToken, authServiceProvider);
         
         if(oauthResponse.getStatus() == 200){
             return Response.status(200).entity(oauthResponse.getEntity()).build();
         } else {
             final String msg = "" + oauthResponse.getEntity();
             LOG.log(Level.SEVERE, msg);
             return Response.status(oauthResponse.getStatus()).entity(msg).build();
         }
     }
    
    //####### WORKSPACES #########//
    
    @DELETE
    public Response deleteWS() {
        return Response.status(405).build();        
    }
    
    @PUT
    public Response putWS() {
        return Response.status(405).build();        
    }
    
    @GET
    public Response getWS(@Context ServletConfig config,
                          @Context HttpServletRequest httpServletRequest,
                          @HeaderParam("X-Auth-Service-Provider") String authServiceProvider,
                          @HeaderParam("X-Verify-Credentials-Authorization") String credentialsAuthorizationToken) {
        
        
        setPublicWorkspaces(config);
        
        if((authServiceProvider != null && authServiceProvider.length() > 0)
                && (credentialsAuthorizationToken != null && credentialsAuthorizationToken.length() > 0 )){
            
            Response response = authCheck(authServiceProvider, credentialsAuthorizationToken);
            
            if(response.getStatus() == 200){
                publicWorkspace += "," + response.getEntity();
            } else {
                return response;
            }
        }
        
        createAdapter(config);
        
//        boolean xmlResponse = false;
//        if(httpServletRequest.getHeader("Accept").equalsIgnoreCase(XML)){
//            xmlResponse = true;
//        }
        
        String acceptHeader = httpServletRequest.getHeader("Accept");
        
        String geoserverUrlExtern = httpServletRequest.getScheme()+"://"+httpServletRequest.getServerName();
        int port = httpServletRequest.getServerPort();
        if(port != 80 && port != 443){
            geoserverUrlExtern += ":"+port;
        }
        
        //welches Rückgabeformat??
        gsAdapter.reloadGS();
        String wsList = gsAdapter.listWS(geoserverUrlExtern, publicWorkspace, acceptHeader);
//        System.out.println("getWS wsList: " + wsList);
        
        if(wsList.length() > 0){
            return Response.ok(wsList).build();
        } else {
            String msg = "no workspaces available";
            LOG.info(msg);
            return Response.status(404).entity(msg).build();            
        }
        
    }
    
    @POST
    public Response postWS(@Context ServletConfig config,
                           @Context HttpServletRequest httpServletRequest,
                           @QueryParam("workspaceid") String workspaceid) {
        
        createAdapter(config);
        
        String geoserverUrlExtern = httpServletRequest.getScheme()+"://"+httpServletRequest.getServerName();
        int port = httpServletRequest.getServerPort();
        if(port != 80 && port != 443){
            geoserverUrlExtern += ":"+port;
        }
        
        if(workspaceid == null || workspaceid == ""){
            
            try (InputStream is = httpServletRequest.getInputStream()) {
                
                  StringWriter is_c = new StringWriter();
                  IOUtils.copy(is, is_c);                    
                  workspaceid = is_c.toString();

            } catch (Exception e) {
                LOG.info("POST Workspace: error while reading from request body");
            } 
        }

        if(workspaceid == null || workspaceid.length() == 0){
            String msg = "workspaceid from parameter or body is missing";
            System.out.println(msg);
            return Response.status(400).entity(msg).build();
        }else{
            return createWorkspace(workspaceid, geoserverUrlExtern);
        }
    }
    
  //####### WORKSPACE #########//
    
    @PUT
    @Path("/{workspaceid}")
    public Response putWSID() {
        return Response.status(405).build();        
    }
    
    /**
     * 
     * list all coverages in called workspace
     * 
     * @param config
     * @param workspaceid
     * @param httpServletRequest
     * @return
     */
    
    @GET
    @Path("/{workspaceid}")
    public Response getRasterLayers(@Context ServletConfig config,
                                    @PathParam("workspaceid") String workspaceid,
                                    @Context HttpServletRequest httpServletRequest) {
        
        String geoserverUrlExtern = httpServletRequest.getScheme()+"://"+httpServletRequest.getServerName();
        int port = httpServletRequest.getServerPort();
        if(port != 80 && port != 443){
            geoserverUrlExtern += ":"+port;
        }
        
        String acceptHeader = httpServletRequest.getHeader("Accept");
        
//        boolean xmlResponse = false;
//        if(httpServletRequest.getHeader("Accept").equalsIgnoreCase(XML)){
//            xmlResponse = true;
//        }
        
        createAdapter(config);       
        String coverageList = "";
        gsAdapter.reloadGS();
        
        if(gsAdapter.existsWS(workspaceid)){        
            coverageList = gsAdapter.listCS(workspaceid, geoserverUrlExtern, acceptHeader);
        
//            if(coverageList.length() > 0){
                return Response.ok(coverageList).build();
//            } else {
//                String msg = "No coverages available in workspace ("+workspaceid+") ";
//                return Response.status(404).entity(msg).build();            
//            }
        } else {
            String msg = "The requested workspace ("+workspaceid+") is not available";
            return Response.status(404).entity(msg).build(); 
        }
    }
        
    @DELETE
    @Path("/{workspaceid}")
    public Response deleteRasterWorkspace(@Context ServletConfig config,
                                          @PathParam("workspaceid") String workspaceid) {
        
//        createAdapter(config);        
//        return deleteWorkspace(workspaceid);
        
        return Response.status(405).build();
    }

    
//    @POST
//    @Path("/rastertest")
//    public boolean testCoverage(@Context ServletConfig config,
//                               @Context HttpServletRequest httpServletRequest,
//                               @QueryParam("source") String sourceUrlString,
//                               @QueryParam("layername") String userLayerName,
//                               @QueryParam("format") String formatString,
//                               @QueryParam("inputformat") String inputFormatString,
//                               @QueryParam("stylename") String styleNameString,
//                               @PathParam("workspaceid") String workspaceid,
//                               @PathParam("workflowid") String workflowRunId,
//                               @HeaderParam("X-Auth-Service-Provider") String authServiceProvider,
//                               @HeaderParam("X-Verify-Credentials-Authorization") String credentialsAuthorizationToken) {
//   
//        
//    
//        System.out.println("rastertest");
////        
////        System.out.println("Classloader: " + this.getClass().getClassLoader());
////        
//        if (formatString != null && inputFormatString != null){
//            if(formatString.equals(inputFormatString)){
//                final String msg = "Error in StoreRaster, different formatstrings" + formatString + " and " + inputFormatString; 
//                LOG.info(msg);
////                return Response.status(400).entity(msg).build();
//            }
//        } else if(formatString == null && inputFormatString != null){
//            formatString = inputFormatString;
//        }
//        
//        this.client = Client.create();
//        Properties props = (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);
//        String dataUrl = props.getProperty("DATA_URL");
////        
//        String suffix = "";
//        
//        switch(formatString.toLowerCase()){
//        
//        case "erdasimg":
//            suffix = ".img";
//            break;
//            
//        case "arcgrid":
//            suffix = ".arcgrid";
//            break;
//            
//        case "geotiff":
//            suffix = ".tif";
//            break;
//            
//        case "geotif":
//            suffix = ".tif";
//            break;
////            
////        default:
////            suffix = "";
////            break;    
//    }
////        
////
////        //hardcoded:
//        workspaceid = "biovel_temp";
//        workflowRunId = "all_runs";
////        
//        System.out.println("paramscheck");
//        System.out.println("username: " + workspaceid);
//        System.out.println("workflowid: " + workflowRunId);
//        System.out.println("suffix: " + suffix);
//        System.out.println("layername: " + userLayerName);
//        System.out.println("source: " + sourceUrlString);
////
////        
//        ClientResponse crPost =
//                this.client.resource(dataUrl).queryParam("username", workspaceid).queryParam("workflowid", workflowRunId).queryParam("suffix", suffix).queryParam("layername", userLayerName).queryParam("source", sourceUrlString).post(ClientResponse.class);
//        
//        
//        if (crPost.getStatus() != 201) {
//            String msg = "Error while storing coverage on the filesystem: " + crPost.getEntity(String.class);
//            LOG.info(msg);
////            return Response.status(crPost.getStatus()).entity(msg).build();
//        }
////        
//        URI location = crPost.getLocation();
//        
//        ClientResponse crPostLoc =
//                this.client.resource(location).post(ClientResponse.class);
//
//        String sourceLocation = crPostLoc.getEntity(String.class);
//        String sourceLocationFile = sourceLocation.substring( sourceLocation.lastIndexOf('/')+1, sourceLocation.length() );
//        String sourceLocationNameWithoutExtn = sourceLocationFile.substring(0, sourceLocationFile.lastIndexOf('.'));
//
//        
//        
//        String noValue = "";
//        String minValue = "";
//        String maxValue = "";
//        
//        String imageWKT = "";
//        String epsgCode = "";
//        String xSize = "";
//        String ySize = "";
//        
//            
//            try{
//                gdal.AllRegister();
//                
//                Dataset hDataset  = gdal.Open(sourceLocation.replace("file:", ""));
//                
//                xSize = ""+hDataset.getRasterXSize();
//                ySize = ""+hDataset.getRasterYSize();
//               
//                int iBand= hDataset.getRasterCount();
//                Band hBand = hDataset.GetRasterBand(iBand);
//        
//                Double[] noValueD = new Double[1];
//                double[] minMax = new double[2];
//        
//                hBand.ComputeRasterMinMax(minMax);
//                hBand.GetNoDataValue(noValueD);
//        
//                if(noValueD[0] != null){
//                    noValue = ""+noValueD[0];
//                } else {
//                    noValue = "";
//                }
//                
//                double[] copyMinMax = minMax.clone();
//        
//                if(copyMinMax != null){
//                    minValue = ""+ copyMinMax[0];
//                    maxValue = ""+ copyMinMax[1];
//                } else {
//                    minValue = "";
//                    maxValue = "";
//                }
//                
//                if (hDataset.GetProjectionRef() != null) {
//                    SpatialReference hSRS;
//                    String pszProjection;
//
//                    pszProjection = hDataset.GetProjectionRef();
//
//                    hSRS = new SpatialReference(pszProjection);
//                    if (hSRS != null && pszProjection.length() != 0) {
//                        String[] pszPrettyWkt = new String[1];
//                        
////                        int epsg = hSRS.AutoIdentifyEPSG();
//
//                        hSRS.ExportToPrettyWkt(pszPrettyWkt, 0);
////                        System.out.println("Coordinate System is:");
////                        System.out.println(pszPrettyWkt[0]);
////                        System.out.println(epsg);
//                        
////                        GEOGCS["GCS_WGS_1984",
////                               DATUM["WGS_1984",
////                                   SPHEROID["WGS_84",6378137,298.257223563]],
////                               PRIMEM["Greenwich",0],
////                               UNIT["Degree",0.017453292519943295]]
//                        
//                        imageWKT = pszPrettyWkt[0];
//                        
//                        //gdal.CPLFree( pszPrettyWkt );
//                    } else
//                        System.out.println("Coordinate System is `"
//                                + hDataset.GetProjectionRef() + "'");
//
//                    hSRS.delete();
//                }
//        
//                hDataset.delete();
//    //            gdal.GDALDestroyDriverManager();
//            
//            } catch (Exception e) {
//                gdal.GDALDestroyDriverManager();
//                final String msg = "something wrong with GDAL driver: " + e.getMessage();
//                LOG.info(msg);
//    //            return Response.status(500).entity(msg).build();
//                minValue = "";
//                maxValue = "";
//                noValue = "";
//                imageWKT = "";
//                
//            } finally {
//                gdal.GDALDestroyDriverManager();
//            }
//            
//            System.out.println("Size is " + xSize + ", "  + ySize);
//            System.out.println("novalue: " + noValue + " min: " + minValue + " max: " + maxValue);
//
//            if(imageWKT.length() == 0){
//                epsgCode = "4326";
//            } else {
//            
//                try {
//                
//                    ClientResponse getEpsg =
//                            this.client.resource("http://localhost:8080/prj2epsg/search.json?mode=wkt").queryParam("terms", imageWKT).get(ClientResponse.class);
//        //            System.out.println(getEpsg.getEntity(String.class));
//                    
//                    String epsgString = getEpsg.getEntity(String.class);
//                    
//    //                System.out.println("\n"+ epsgString+"\n");
//                    
//                    JSONObject respBody = new JSONObject(epsgString);              
//                    JSONArray epsgCodeArray = respBody.getJSONArray("codes");// getJSONObject("codes").getString("code");
//                    
//                    epsgCode = epsgCodeArray.getJSONObject(0).getString("code");
//                    
//                    System.out.println("found code - EPSG: "+ epsgCode);
//                
//                } catch (JSONException e) {
//                    final String msg = "Error creating user from auth data";
//                    LOG.info(msg + " - " + e.toString());
//    //                return Response.status(500).entity(msg).build();
//                    epsgCode = "4326";                
//                }
//            }
//        
//        return true;
//    }
    
    
    /**
     * creates new Layer on Geoserver
     * 
     * @param config
     * @param httpServletRequest
     * @param sourceUrlString
     * @param userLayerName
     * @param formatString
     * @param styleNameString
     * @param workspaceid
     * @return httpResponse with LayerLinks
     */
        
    @POST
    @Path("/{workspaceid}")
    public Response postCoverage(@Context ServletConfig config,
                               @Context HttpServletRequest httpServletRequest,
                               @QueryParam("source") String sourceUrlString,
                               @QueryParam("layername") String userLayerName,
                               @QueryParam("format") String formatString,
                               @QueryParam("inputformat") String inputFormatString,
                               @QueryParam("stylename") String styleNameString,
                               @PathParam("workspaceid") String workspaceid,
                               @PathParam("workflowid") String workflowRunId,
                               @HeaderParam("X-Auth-Service-Provider") String authServiceProvider,
                               @HeaderParam("X-Verify-Credentials-Authorization") String credentialsAuthorizationToken) {
        
        System.out.println("hier bei ->> shim/raster/workspaceID");
        
        setPublicWorkspaces(config);
        
        //request from Raster upload service e.g. in ENM:
        // {BIOSTIF_SERVER_URL}/shim/rest/raster/{workspaceid}?source={sourceURL}&stylename={styleName}&inputformat={inputformat}&layername={layername}
        
        if (formatString != null && inputFormatString != null){
            if(formatString.equals(inputFormatString)){
                final String msg = "Error in StoreRaster, different formatstrings" + formatString + " and " + inputFormatString; 
                LOG.info(msg);
                return Response.status(400).entity(msg).build();
            }
        } else if(formatString == null && inputFormatString != null){
            formatString = inputFormatString;
        }
        
        String userWorkspace = null;
        
        if(authServiceProvider != null && credentialsAuthorizationToken != null
                && authServiceProvider.length() > 0 && credentialsAuthorizationToken.length() > 0){
            
            Response checkResponse = authCheck(authServiceProvider, credentialsAuthorizationToken);
            
            if(checkResponse.getStatus() == 200){
                userWorkspace = (String) checkResponse.getEntity();
                publicWorkspace = publicWorkspace +","+ userWorkspace;
            } else {
                return checkResponse;
            }
        }
        
        createAdapter(config);
        gsAdapter.reloadGS();
        
        // TODO: nun soll in workspaceid, also biovel_tmp oder userws gespeichert werden???
        // userWorkspace als liste?? bestimmt
        
        System.out.println("workspaceid: " + workspaceid);
        List<String> authWorkspacesList = new ArrayList<String>(Arrays.asList(publicWorkspace.split(",")));
        authWorkspacesList.add("biovel_projections"); // a non public workspace only for intermediate projections

        //biovel_tmp vereinigen mit auth-response? dann checken in Liste? sollte generell richtig sein.un wenn erlaubt dann nehmen sonst verwerfen oder tmp?
        if(workspaceid == null || workspaceid.length() == 0){
            System.out.println("nix ws");
            
            if(credentialsAuthorizationToken.length() > 0){
                workspaceid = userWorkspace;
            } else{
                workspaceid = publicWorkspace;
            }
            
        } else {
            
            if(authWorkspacesList.contains(workspaceid)){
                if(!gsAdapter.existsWS(workspaceid)){
                    
                    Response createWS = gsAdapter.createWorkspace(workspaceid);
                    
                    if(createWS.getStatus() == 201){
                        final String msg = "Creating Workspace '"+ workspaceid+"' successful";
                        LOG.log(Level.SEVERE, msg);                    
                    } else {
                        return createWS;
                    }                
                }            
            } else {
                final String msg = "Unauthorized access to workspace: " + workspaceid + ":: Access denied"; 
                LOG.info(msg);
                return Response.status(401).entity(msg).build(); 
            }
        }
        
//        if(!gsAdapter.existsWS(workspaceid)){
//            final String msg = "Error creating coverage -> Workspace '"+ workspaceid+"' not exists. Create a workspace first";
//            LOG.log(Level.SEVERE, msg);
////            throw new RuntimeException(mesg);
//            return Response.status(400).entity(msg).build();
//        }
        
        System.out.println("sourceUrlString: " + sourceUrlString);
        
        InputStream inputBody = null;
        InputStream iscopy = null;
        
        if(sourceUrlString == null || sourceUrlString.length() == 0){
            
            System.out.println("try input stream");
            
            try (InputStream is = httpServletRequest.getInputStream()) {
                
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
                
                inputBody = new ByteArrayInputStream(baos.toByteArray());
                
                  StringWriter is_c = new StringWriter();
                  IOUtils.copy(inputBody, is_c);
                  
                  if(is_c.toString().startsWith("http:") || is_c.toString().startsWith("https:")){
                      sourceUrlString = is_c.toString(); 
                  } else{
//                     inputBody = new ByteArrayInputStream(is_c.toString().getBytes()); 
                     iscopy = new ByteArrayInputStream(baos.toByteArray());
                  }

            } catch (IOException e) {
                LOG.info("POST Workspace: error while reading from request body");
            } 
        }

        if((sourceUrlString == null || sourceUrlString.length() == 0) && iscopy == null){
            String msg = "Sourcefile URL and requestBody for rasterStorage is missing";
            LOG.info("POST Workspace: " + msg);
            return Response.status(400).entity(msg).build();
        }else{
            return storeCoverage(config, httpServletRequest, sourceUrlString, iscopy, workspaceid, userLayerName, formatString, styleNameString, workflowRunId);
        }        
 }
    
    /**
     * creates new Layer on Geoserver
     * 
     * @param config
     * @param httpServletRequest
     * @param sourceUrlString
     * @param userLayerName
     * @param formatString
     * @param styleNameString
     * @param workspaceid
     * @return httpResponse with LayerLinks
     */
        
    @POST
    @Path("/workspaces")
    public Response postCoverageAuth(@Context ServletConfig config,
                               @Context HttpServletRequest httpServletRequest,
                               @QueryParam("source") String sourceUrlString,
                               @QueryParam("layername") String userLayerName,
                               @QueryParam("format") String formatString,
                               @QueryParam("inputformat") String inputFormatString,
                               @QueryParam("stylename") String styleNameString,
                               @QueryParam("workspaceid") String workspaceid,//addicional pathname
                               @QueryParam("workflowid") String workflowRunId, 
                               @HeaderParam("X-Auth-Service-Provider") String authServiceProvider,
                               @HeaderParam("X-Verify-Credentials-Authorization") String credentialsAuthorizationToken) {
        
        System.out.println("hier bei ->> shim/raster/workspaceAuth");
        
        //request from Raster upload service e.g. in ENM:
        // {BIOSTIF_SERVER_URL}/shim/rest/raster/{workspaceid}?source={sourceURL}&stylename={styleName}&inputformat={inputformat}&layername={layername}
        
        setPublicWorkspaces(config);
        
        if (formatString != null && inputFormatString != null){
            if(formatString.equals(inputFormatString)){
                final String msg = "Error in StoreRaster, different formatstrings" + formatString + " and " + inputFormatString; 
                LOG.info(msg);
                return Response.status(400).entity(msg).build();
            }
        } else if(formatString == null && inputFormatString != null){
            formatString = inputFormatString;
        }
        
        System.out.println("formatString: " + formatString);
        String userWorkspace = null;
        
        if(authServiceProvider != null && credentialsAuthorizationToken != null
                && authServiceProvider.length() > 0 && credentialsAuthorizationToken.length() > 0){
            
            Response checkResponse = authCheck(authServiceProvider, credentialsAuthorizationToken);
            
            if(checkResponse.getStatus() == 200){
                userWorkspace = (String) checkResponse.getEntity();
                publicWorkspace = publicWorkspace +","+ userWorkspace;
            } else {
                return checkResponse;
            }
        }
        
        createAdapter(config);
        gsAdapter.reloadGS();
        
        // TODO: nun soll in workspaceid, also biovel_tmp oder user_ws gespeichert werden???
        // userWorkspace als liste?? bestimmt
        
//        System.out.println("workspaceid: " + workspaceid);
        List<String> authWorkspacesList = Arrays.asList(publicWorkspace.split(","));

        //biovel_tmp vereinigen mit auth-response? dann checken in Liste? sollte generell richtig sein.un wenn erlaubt dann nehmen sonst verwerfen oder tmp?
        if(workspaceid == null || workspaceid.length() == 0){
            
            if(credentialsAuthorizationToken == null || credentialsAuthorizationToken.length() == 0){
                workspaceid = publicWorkspace;
            } else{
                workspaceid = userWorkspace;
            }
            
        }
        
        System.out.println("workspaceid: " + workspaceid);
        
//        else {
            
            if(authWorkspacesList.contains(workspaceid)){
                if(!gsAdapter.existsWS(workspaceid)){
                    
                    Response createWS = gsAdapter.createWorkspace(workspaceid);
                    
                    if(createWS.getStatus() == 201){
                        final String msg = "Creating Workspace '"+ workspaceid+"' successful";
                        LOG.log(Level.SEVERE, msg);                    
                    } else {
                        return createWS;
                    }                
                }            
            } else {
                final String msg = "Unauthorized access to workspace: " + workspaceid + ":: Access denied"; 
                LOG.info(msg);
                return Response.status(401).entity(msg).build(); 
            }
//        }
        
        
        
//        System.out.println("workspaceid: " + workspaceid);

        
//        if(!gsAdapter.existsWS(workspaceid)){
//            final String msg = "Error creating coverage -> Workspace '"+ workspaceid+"' not exists. Create a workspace first";
//            LOG.log(Level.SEVERE, msg);
////            throw new RuntimeException(mesg);
//            return Response.status(400).entity(msg).build();
//        }
        
//        System.out.println("sourceUrlString: " + sourceUrlString);
        
        InputStream inputBody = null;
        InputStream iscopy = null;
        
        if(sourceUrlString == null || sourceUrlString.length() == 0){
            
            System.out.println("try input stream");
            
            try (InputStream is = httpServletRequest.getInputStream()) {
                
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
                
                inputBody = new ByteArrayInputStream(baos.toByteArray());
                
                  StringWriter is_c = new StringWriter();
                  IOUtils.copy(inputBody, is_c);
                  
                  if(is_c.toString().startsWith("http:") || is_c.toString().startsWith("https:")){
                      sourceUrlString = is_c.toString(); 
                  } else{
//                     inputBody = new ByteArrayInputStream(is_c.toString().getBytes()); 
                     iscopy = new ByteArrayInputStream(baos.toByteArray());
                  }

            } catch (IOException e) {
                LOG.info("POST Workspace: error while reading from request body");
            } 
        }
        
//        System.out.println("workflowid: " +workflowRunId);

        if((sourceUrlString == null || sourceUrlString.length() == 0) && iscopy == null){
            String msg = "Sourcefile URL and requestBody for rasterStorage is missing";
            LOG.info("POST Workspace: " + msg);
            return Response.status(400).entity(msg).build();
        }else{
            return storeCoverage(config, httpServletRequest, sourceUrlString, iscopy, workspaceid, userLayerName, formatString, styleNameString, workflowRunId);
        }        
 }
    
  //####### COVERAGE #########//
        
    //****TEMP***MOVE*COV*SOURCE******//
    @PUT
    @Path("/move")
    public Response moveCoverageRessource(@Context ServletConfig config,
                               @Context HttpServletRequest httpServletRequest,
                               @QueryParam("newlocation") String sourceUrlString,
                               @QueryParam("layername") String layerWithPrefix) {
        
        
        String layerName = "";
        String workspaceid = "";
        
        if(layerWithPrefix == null || layerWithPrefix.length()==0){
            String msg = "Layername from parameter is missing";
            LOG.info("PUT move layerressource: " + msg);
            return Response.status(400).entity(msg).build();
        }else{
            layerName = layerWithPrefix.substring( layerWithPrefix.indexOf(':')+1, layerWithPrefix.length() );
            workspaceid = layerWithPrefix.substring(0, layerWithPrefix.lastIndexOf(':'));
        }
        
        createAdapter(config);
        gsAdapter.reloadGS();

        if(!gsAdapter.existsWS(workspaceid)){
            final String msg = "Error moving coverage -> Workspace '"+ workspaceid+"' not exists!";
            LOG.log(Level.SEVERE, msg);
//            throw new RuntimeException(mesg);
            return Response.status(400).entity(msg).build();
        }
        
        gsAdapter.setWorkspace(workspaceid);
        
        if(!gsAdapter.existsCS(layerName)){
            final String msg = "Error moving coverage -> Coverage '"+ layerName+"' not exists!";
            LOG.log(Level.SEVERE, msg);
//            throw new RuntimeException(mesg);
            return Response.status(400).entity(msg).build();
        }
        
        if(sourceUrlString == null || sourceUrlString.length() == 0){
            String msg = "Sourcefile URL from parameter is missing";
            LOG.info("PUT move layerressource: " + msg);
            return Response.status(400).entity(msg).build();
        }
        
        return gsAdapter.moveCoverageLayerRessource(workspaceid, layerName, sourceUrlString);
     
 }
    
    @GET
    @Path("/ressourceurl")
    public Response getCoverageRessourceUrl(@Context ServletConfig config,
                               @Context HttpServletRequest httpServletRequest,
                               @QueryParam("layername") String layerWithPrefix) {
        
        
        String layerName = "";
        String workspaceid = "";
        
        if(layerWithPrefix == null || layerWithPrefix.length()==0){
            String msg = "Layername from parameter is missing";
            LOG.info("PUT move layerressource: " + msg);
            return Response.status(400).entity(msg).build();
        }else{
            layerName = layerWithPrefix.substring( layerWithPrefix.indexOf(':')+1, layerWithPrefix.length() );
            workspaceid = layerWithPrefix.substring(0, layerWithPrefix.lastIndexOf(':'));
        }
        
        createAdapter(config);
        gsAdapter.reloadGS();

        if(!gsAdapter.existsWS(workspaceid)){
            final String msg = "Error moving coverage -> Workspace '"+ workspaceid+"' not exists!";
            LOG.log(Level.SEVERE, msg);
//            throw new RuntimeException(mesg);
            return Response.status(400).entity(msg).build();
        }
        
        gsAdapter.setWorkspace(workspaceid);
        
        if(!gsAdapter.existsCS(layerName)){
            final String msg = "Error moving coverage -> Coverage '"+ layerName+"' not exists!";
            LOG.log(Level.SEVERE, msg);
//            throw new RuntimeException(mesg);
            return Response.status(400).entity(msg).build();
        }
     
        return gsAdapter.getCoverageLayerRessourceURL(workspaceid, layerName);
     
 }
    
    @DELETE
    @Path("/delete")
    public Response deleteCoveragefromRessource(@Context ServletConfig config,
                               @Context HttpServletRequest httpServletRequest,
                               @QueryParam("layername") String layerWithPrefix) {
        
        
        String layerName = "";
        String workspaceid = "";
        
        if(layerWithPrefix == null || layerWithPrefix.length()==0){
            String msg = "Layername from parameter is missing";
            LOG.info("PUT move layerressource: " + msg);
            return Response.status(400).entity(msg).build();
        }else{
            layerName = layerWithPrefix.substring( layerWithPrefix.indexOf(':')+1, layerWithPrefix.length() );
            workspaceid = layerWithPrefix.substring(0, layerWithPrefix.lastIndexOf(':'));
        }
        
        createAdapter(config);
        gsAdapter.reloadGS();

        if(!gsAdapter.existsWS(workspaceid)){
            final String msg = "Error moving coverage -> Workspace '"+ workspaceid+"' not exists!";
            LOG.log(Level.SEVERE, msg);
//            throw new RuntimeException(mesg);
            return Response.status(400).entity(msg).build();
        }
        
        gsAdapter.setWorkspace(workspaceid);
        
        if(!gsAdapter.existsCS(layerName)){
            final String msg = "Error moving coverage -> Coverage '"+ layerName+"' not exists!";
            LOG.log(Level.SEVERE, msg);
//            throw new RuntimeException(mesg);
            return Response.status(400).entity(msg).build();
        }
        
        int resp = gsAdapter.deleteCoverageStore(workspaceid, layerName);
        return Response.status(resp).build();
     
 }
    
    
    
    @POST
    @Path("/{workspaceid}/{coverageid}")
    public Response postCoverage() {
        return Response.status(405).build();        
    }
    
    @GET
    @Path("/{workspaceid}/{coverageid}")
    public Response getCoverage(@Context ServletConfig config,
                                @Context HttpServletRequest httpServletRequest,
                                @PathParam("workspaceid") String workspaceid,
                                @PathParam("coverageid") String coverageid) {
        
        createAdapter(config);
  
        gsAdapter.reloadGS();
        
        if(!gsAdapter.existsWS(workspaceid)){
            final String msg = "Error getting Workspace -> Workspace '"+ workspaceid+"' not exists. Create workspace first";
            return Response.status(400).entity(msg).build();
        
        } else {
            gsAdapter.setWorkspace(workspaceid); 
        }
            
        if(!gsAdapter.existsCS(coverageid)){
            final String msg = "Error getting coverageStore -> coverage '"+ coverageid+"' not exists. Create coverage first";
            return Response.status(400).entity(msg).build();
        
        }        
            
        String geoserverUrlextern = httpServletRequest.getScheme()+"://"+httpServletRequest.getServerName();
        int port = httpServletRequest.getServerPort();
        if(port != 80 && port != 443){
          geoserverUrlextern += ":"+port;
        }
      
        String result = gsAdapter.getCoverageInfo(geoserverUrlextern, workspaceid, coverageid);
               
        return Response.ok(result).build();
        
    }
    
    @DELETE
    @Path("/{workspaceid}/{coverageid}")
    public Response deleteCoverage(@Context ServletConfig config,
                                   @PathParam("workspaceid") String workspaceid,
                                   @PathParam("coverageid") String coverageid) {
        
//        createAdapter(config);        
//        return deleteCoverageStore(workspaceid, coverageid);
        return Response.status(405).build();  
    }
    
    @PUT
    @Path("/{workspaceid}/{coverageid}")
    public Response putCoverage(@Context ServletConfig config,
                               @Context HttpServletRequest httpServletRequest,
                               @QueryParam("source") String sourceUrlString,
                               @QueryParam("layername") String userLayerName,
                               @QueryParam("format") String formatString,
                               @QueryParam("stylename") String styleNameString,
                               @PathParam("workspaceid") String workspaceid,
                               @PathParam("coverageid") String coverageid) {
        
        return Response.status(405).build();
        
//        createAdapter(config);
//        
//        if(!gsAdapter.existsWS(workspaceid)){
//            final String msg = "Error getting workspace -> Workspace '"+ workspaceid+"' not exists. Create workspace First";
//            LOG.log(Level.SEVERE, msg);
////            throw new RuntimeException(mesg);
//            return Response.status(400).entity(msg).build();
//        }
//        
//        if(sourceUrlString == null || sourceUrlString == ""){
//            
//            try (InputStream is = httpServletRequest.getInputStream()) {
//                
//                if(is != null){
//                    
//                  StringWriter is_c = new StringWriter();
//                  IOUtils.copy(is, is_c);                    
//                  sourceUrlString = is_c.toString();  
//                    
//                }
//
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
////                e.printStackTrace();
//                LOG.info("PUT Coverage: error while reading from request body");
//            } 
//            
//        }
//
//        if(sourceUrlString == null || sourceUrlString == ""){
//            String msg = "Sourcefile URL from parameter or body is missing";
//            return Response.status(400).entity(msg).build();
//        }else{
//            return storeCoverage(config, httpServletRequest, sourceUrlString, workspaceid, userLayerName, formatString, styleNameString);
//        }
 }
    
    @POST
    @Path("/repairLayers")
    public Response repair(@Context ServletConfig config,
                           @Context HttpServletRequest httpServletRequest,
                              @QueryParam("source") String source,
                              @QueryParam("stylename") String styleNameString) {
        
        createAdapter(config);
        
        String acceptHeader = httpServletRequest.getHeader("Accept");
        
        String geoserverUrlExtern = getShimProperty(config, ShimServletContainer.GEOSERVER_URL);
        
        String publicWorkspace = getShimProperty(config, ShimServletContainer.PUBLIC_WORKSPACE);
        
        //welches Rückgabeformat??
        gsAdapter.reloadGS();
        String wsList = gsAdapter.listWS(geoserverUrlExtern, publicWorkspace, acceptHeader);
        System.out.println("getWS wsList: " + wsList);
        
        String coverageList = "";
        
        if(gsAdapter.existsWS(publicWorkspace)){ 
            
            System.out.println(publicWorkspace + " exists !!");
            
            coverageList = gsAdapter.listCS(publicWorkspace, geoserverUrlExtern, "application/xml");
        
            System.out.println(" #coverages: " + coverageList.length());
            
            String repairLayers = gsAdapter.repairLayers(geoserverUrlExtern, coverageList);
            
        } else{
            System.out.println(publicWorkspace + " exists NOT");
        }
        
        return Response.status(200).build();
    }

    
    //####### STYLES #########//
   
    @DELETE
    @Path("/style")
    public Response deleteStyles() {
        return Response.status(405).build();        
    }
    
    @PUT
    @Path("/style")
    public Response putStyles() {
        return Response.status(405).build();        
    }
    
    
    @GET
    @Path("/style")
    public Response getStyles(@Context ServletConfig config,
                             @Context HttpServletRequest httpServletRequest) {
        
        createAdapter(config);

        String geoserverUrlExtern = httpServletRequest.getScheme()+"://"+httpServletRequest.getServerName();
        int port = httpServletRequest.getServerPort();
        if(port != 80 && port != 443){
            geoserverUrlExtern += ":"+port;
        }
        
        //welches Rückgabeformat??
        gsAdapter.reloadGS();
        String wsList = gsAdapter.listStyles(geoserverUrlExtern);
        
        if(wsList.length() > 0){
            return Response.ok(wsList).build();
        } else {
            String msg = "no styles available";
            return Response.status(400).entity(msg).build();            
        }
    }
    
    @POST
    @Consumes(SLD_XML)
    @Path("/style")
    public Response postStyles(@Context ServletConfig config,
                              @QueryParam("source") String source,
                              @QueryParam("stylename") String styleNameString) {
        
        createAdapter(config);
        return uploadStyle(styleNameString, source);
    }
    
    
    
  //####### STYLE #########//
    
    @GET
    @Path("/style/{stylename}")
    public Response getStyle() {
        return Response.status(405).build();        
    }
    
    @POST
    @Path("/style/{stylename}")
    public Response postStyle() {
        return Response.status(405).build();        
    }
    
    @DELETE
    @Path("/style/{stylename}")
    public Response deleteStyle(@Context ServletConfig config,
                                @PathParam("stylename") String styleNameString) {
        
//        createAdapter(config);
//        if(gsAdapter.existsStyle(styleNameString)){
//            gsAdapter.deleteStyle(styleNameString);
//        }
//        return Response.ok().build();
        return Response.status(405).build();
    }
    
    @PUT
    @Consumes(SLD_XML)
    @Path("/style/{stylename}")
    public Response putStyle(@Context ServletConfig config,
                             @QueryParam("source") String source,
                             @PathParam("stylename") String styleNameString) {
        
        createAdapter(config);
        if(gsAdapter.existsStyle(styleNameString)){
            gsAdapter.putStyle(styleNameString, source);
            return Response.ok().build();
        }else{
            return uploadStyle(styleNameString, source); 
        }

    }
    
    
    private Response createWorkspace(String workspaceid, String geoserverUrlExtern){
        
        gsAdapter.reloadGS();
        
        if(gsAdapter.existsWS(workspaceid)){
            LOG.info("workspace '" + workspaceid + "' already exists");
            return Response.ok(geoserverUrlExtern+"/shim/rest/raster/"+workspaceid).build();
        } else {
            Response createWorkspaceStatus = gsAdapter.createWorkspace(workspaceid);
            
            if(createWorkspaceStatus.getStatus() != 201){
                return createWorkspaceStatus;
            }else{
                LOG.info("Workspace '"+workspaceid+"' is created");
                return Response.ok(geoserverUrlExtern+"/shim/rest/raster/"+workspaceid).build();
            }
        }
        
    }
          
    
    private Response deleteCoverageStore(String workspaceid, String coverageid) {
    
        int resNr = 0;
        
        gsAdapter.reloadGS();
        if(gsAdapter.existsWS(workspaceid) && gsAdapter.existsCS(coverageid)){
            resNr = gsAdapter.deleteCoverageStore(workspaceid, coverageid);
        }else {
            resNr = 200;
        }
        
        Response response = Response.status(resNr).build();
        return response;
    
    }
    
    private Response deleteWorkspace(String workspaceid) {
        
        int resNr = 0;
        
        gsAdapter.reloadGS();
        if(gsAdapter.existsWS(workspaceid)){
            resNr = gsAdapter.deleteWorkspace(workspaceid);
        }else {
            resNr = 200;
        }
        
//        delete gwc layername:
//        http://geowebcache.org/docs/current/rest/layers.html
//            Delete Layer
//            Finally, to delete a layer, use the HTTP DELETE method against the layer resource:
//            curl -v -u geowebcache:secured -XDELETE "http://localhost:8080/geowebcache/rest/layers/layer1.xml"


        
        Response response = Response.status(resNr).build();
        return response;
    
    }
    
    private Response uploadStyle(String styleNameString, String source) {
        
        int resNr = 0;
        
        if(!gsAdapter.existsStyle(styleNameString)){
            resNr = gsAdapter.createStyleRessource(styleNameString);
        }else {
            LOG.info("Stylename '"+styleNameString+"' already exists");
            throw new WebApplicationException(400);
        }
        
        if (resNr != 201) {
            throw new WebApplicationException(resNr);
        }
        
        int uploadStatus = 0;
        uploadStatus = gsAdapter.uploadStyle(source);
        if (uploadStatus != 201) {
            throw new WebApplicationException(uploadStatus);
        }
        
        int status = 0;
        if(uploadStatus == 0){
            status = resNr;
        } else {
            status = uploadStatus;
        }
        
        Response response = Response.ok(status).build();
        return response;
    
    }
    

    private Response storeCoverage(ServletConfig config, HttpServletRequest httpServletRequest, String sourceUrlString, InputStream inputBody, String workspaceid, String userLayerName, String formatString, String styleNameString, String workflowRunId) {
        
        System.out.println("\n store coverage");
        
        this.client = Client.create();
        Properties props = (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);
        String dataUrl = props.getProperty("DATA_URL");
        
//        String dataManagerService = "http://localhost:8080/workflow/rest/data";
        
        if(formatString == null || formatString.length() == 0){
            formatString = "ERDASImg";
        }
        
        String suffix = "";
        
     switch(formatString.toLowerCase()){
        
        case "erdasimg":
            suffix = ".img";
            styleNameString = "vrt_raster_color";
            break;
            
        case "arcgrid":
            suffix = ".arcgrid";
            if(styleNameString == null || styleNameString.length() == 0){
                styleNameString = "vrt_raster_color";
            }
            break;
            
        case "geotiff":
            suffix = ".tif";
            styleNameString = "tif_raster_color";
            break;
            
        case "geotif":
            suffix = ".tif";
            styleNameString = "tif_raster_color";
            break;
//            
//        default:
//            suffix = "";
//            break;    
    }
     
     
        ClientResponse crPost = null;
        
        System.out.println("check queries:");
        System.out.println("username: " + workspaceid);
        System.out.println("workflowid: " + workflowRunId);
        System.out.println("suffix: " + suffix);
        System.out.println("layername: " + userLayerName);
        System.out.println("source:" + sourceUrlString);
        System.out.println("style:" + styleNameString);
        
        if(workflowRunId == null){
            workflowRunId = "all_runs";
        }
        
        if(sourceUrlString != null){
            System.out.println("store url");
            crPost = this.client.resource(dataUrl).queryParam("username", workspaceid).queryParam("workflowid", workflowRunId).queryParam("suffix", suffix).queryParam("layername", userLayerName).queryParam("source", sourceUrlString).post(ClientResponse.class);
        } else{
            System.out.println("\n store stream");
            crPost = this.client.resource(dataUrl).queryParam("username", workspaceid).queryParam("workflowid", workflowRunId).queryParam("suffix", suffix).queryParam("layername", userLayerName).post(ClientResponse.class,inputBody);
        }
        
        System.out.println("store response: " + crPost.getStatus());
        
        if (crPost.getStatus() != 201) {
            String msg = "Error while storing coverage on the filesystem: " + crPost.getEntity(String.class);
            LOG.info(msg);
            return Response.status(crPost.getStatus()).entity(msg).build();
        }
        
        URI location = crPost.getLocation();
        
        ClientResponse crPostLoc =
                this.client.resource(location).post(ClientResponse.class);

        String sourceLocation = crPostLoc.getEntity(String.class);
        String sourceLocationFile = sourceLocation.substring( sourceLocation.lastIndexOf('/')+1, sourceLocation.length() );
        String sourceLocationNameWithoutExtn = sourceLocationFile.substring(0, sourceLocationFile.lastIndexOf('.'));
        
        //TODO: zum layernamen username und workflowid hinzu??
        String layerName = "";
        if(userLayerName == null || userLayerName.length() == 0){
            if (sourceUrlString != null) {
                String sourceFile =
                    sourceUrlString.substring(sourceUrlString.lastIndexOf('/') + 1, sourceUrlString.length());
                String fileNameWithoutExtn = sourceFile.substring(0, sourceFile.lastIndexOf('.'));
                layerName = fileNameWithoutExtn + "_" + sourceLocationNameWithoutExtn;
            } else {
                layerName = "streaminput";
            }
        } else{
//            layerName = userLayerName+"_"+sourceLocationNameWithoutExtn;
//            layerName = sourceLocationNameWithoutExtn;
            
            if(!sourceLocationNameWithoutExtn.contains(userLayerName)){
                layerName = userLayerName+"_"+sourceLocationNameWithoutExtn;
            } else{
                
                layerName = sourceLocationNameWithoutExtn;
            }
            
        }
        
//        layerName = checkCS(layerName, 0);
       
        gsAdapter.setWorkspace(workspaceid);
        
//        LOG.info("vor ccS: sourceLocation: " + sourceLocation +" layerName: " +  layerName + " formatString:" + formatString);
        
        Response createCoverageStoreStatus = gsAdapter.createCoverageStore(sourceLocation, layerName, formatString);
        System.out.println("ccS status: " + createCoverageStoreStatus.getStatus());
        if(createCoverageStoreStatus.getStatus() != 201){
//            throw new WebApplicationException(createCoverageStoreStatus);
            return createCoverageStoreStatus;
        }
        
        Response addCoverageLayerStatus = gsAdapter.addCoverageLayer();
        System.out.println("acS status: " + addCoverageLayerStatus.getStatus());
        if(addCoverageLayerStatus.getStatus() != 201){
            return addCoverageLayerStatus;
        }
                    
        Response updateLayerStatus = gsAdapter.updateCoverageLayer(sourceLocation, suffix);
        System.out.println("ULS status: " + updateLayerStatus.getStatus());
        if(updateLayerStatus.getStatus() != 200){
            return updateLayerStatus;
        }
        
        if(styleNameString == null){
            styleNameString = "vrt_raster_color";
        }
        
        if(gsAdapter.existsStyle(styleNameString)){
            Response updateStyle = gsAdapter.updateStyle(styleNameString);
            
            if(updateStyle.getStatus() != 200){
                return updateStyle;
            }
           
        } else {
            LOG.info("style paramerter is not exist - update metadata without style ");
            
            Response updateMD = gsAdapter.updateMetadata();
            if(updateMD.getStatus() != 200){
                return updateMD;
            }
            
        }       
        
        LOG.info("Create coverage Layer " + workspaceid +":"+ layerName + " succesful !!!");
        
        String geoserverUrlExtern = httpServletRequest.getScheme()+"://"+httpServletRequest.getServerName();
        int port = httpServletRequest.getServerPort();
        if(port != 80 && port != 443){
            geoserverUrlExtern += ":"+port;
        }
        
        String result = gsAdapter.getCoverageInfo(geoserverUrlExtern, workspaceid, layerName);
        
        return Response.ok(result).build();
    }
    
    private void createAdapter(ServletConfig config) {

        Properties props =
            (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);
        String geoserverUrl = props.getProperty("GEOSERVER_URL");
        String geoserverUser = props.getProperty("GEOSERVER_USER");
        String geoserverPasswd = props.getProperty("GEOSERVER_PASSWD");
        String dataDir = props.getProperty("DATA_DIR");
        String dataURL = props.getProperty("DATA_URL");

        URI gsUri;
        try {
            gsUri = new URI(geoserverUrl);
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        boolean reloadGS = Boolean.parseBoolean(props.getProperty("GEOSERVER_RELOAD"));
        gsAdapter = new RestGeoServerAdapter(reloadGS, geoserverUser, geoserverPasswd, gsUri, dataDir, dataURL);

        if (!gsAdapter.resourceAvailable(geoserverUrl)) {
            throw new WebApplicationException(500);
        }

    };
    
    private String checkCS(String layerName, int ix){
        
        if(gsAdapter.existsCS(layerName)){
            
            if(layerName.contains("-")){
                layerName = layerName.substring(0, layerName.lastIndexOf("-"))+"-"+(++ix);
            } else {
                layerName = layerName+"-"+(++ix);
            }            
            
            layerName = checkCS(layerName, ix);
        }
        
        if(gsAdapter.existsLayer(layerName)){
            
            if(layerName.contains("-")){
                layerName = layerName.substring(0, layerName.lastIndexOf("-"))+"-"+(++ix);
            } else {
                layerName = layerName+"-"+(++ix);
            }
            
            layerName = checkCS(layerName, ix);
        }
        
        return layerName;
    }
    
    private String getShimProperty(ServletConfig config, String propertyName) {
        Properties props =
            (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);
        String dataUrl = props.getProperty(propertyName);
        return dataUrl;
    }    
    


}
