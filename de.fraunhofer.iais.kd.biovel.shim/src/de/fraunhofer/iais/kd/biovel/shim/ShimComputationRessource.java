package de.fraunhofer.iais.kd.biovel.shim;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import de.fraunhofer.iais.kd.biovel.common.BiovelHelper;
import de.fraunhofer.iais.kd.biovel.shim.raster.RestGeoServerAdapter;

@Path("/computation")
public class ShimComputationRessource {
    
    
    private static final Logger LOG = Logger.getLogger(ShimComputationRessource.class.getName());
    private Client client;
    private RestGeoServerAdapter gsAdapter;
    private String geoserverUrl;
    
    @GET
    @Path("ping")
    public Response dataPing() {
        return Response.ok().build();
    }
    
    @HEAD
    public Response dataHead() {
        return Response.status(204).build();
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response respondIndexHtml(@Context ServletConfig config) {
        String filename = config.getServletContext().getRealPath("index.html");
        String result = BiovelHelper.readTextContentFromFile(new File(filename));
        return Response.ok(result).build();
    }
    
    @POST
    @Path("/raster/diff")
    public Response rasterDiff(@Context ServletConfig config,
                               @Context HttpServletRequest httpServletRequest,
//                             @QueryParam("source1") String sourceUrlString1,
//                             @QueryParam("source2") String sourceUrlString2,
                               @QueryParam("layer1") String userLayerName1,
                               @QueryParam("layer2") String userLayerName2,
                               @QueryParam("stylename") String styleNameString,
//                             @QueryParam("inputformat") String inputFormatString,
                               @QueryParam("workspaceid") String workspaceid,
                               @QueryParam("workflowid") String workflowRunId,
                               @QueryParam("mask") Boolean addNoValueMask) {
        createAdapter(config);
        
//        http://localhost:8080/shim/rest/computation/raster/diff?source1=http://localhost:8080/biostif/data/fileA.img&source2=http://localhost:8080/biostif/data/fileB.img
        
//        if(sourceUrlString1 == null || sourceUrlString2 == null){
//            return Response.status(406).build();
//        } else {
        
//        Response resp = computeDiff(config, sourceUrlString1, sourceUrlString2, userLayerName1, userLayerName2, workspaceid,
//                inputFormatString, styleNameString, httpServletRequest);
        
        if (addNoValueMask == null){
            addNoValueMask = true;
        }
        
        Response resp = computeDiff(config, userLayerName1, userLayerName2, styleNameString, workspaceid,
            addNoValueMask, httpServletRequest, workflowRunId);
        
        return resp;
//        }
    }
    
    @POST
    @Path("/raster/mask")
    public Response rasterMask(@Context ServletConfig config,
                               @Context HttpServletRequest httpServletRequest,
                               @QueryParam("format") String format,
                               @QueryParam("cellsize") Double cellSize,
                               @QueryParam("nodata") Integer noData,
                               @QueryParam("data") Integer data,
                               @QueryParam("stylename") String styleNameString,
                               @QueryParam("layername") String userLayerName,
                               @QueryParam("workflowid") String workflowRunId,
                               @QueryParam("workspaceid") String workspaceid) {

//        System.out.println("requested URL getRemoteAddr(): " + httpServletRequest.getRemoteAddr().toString());
//        System.out.println("requested URL getRequestURL(): " + httpServletRequest.getRequestURL().toString());
//        System.out.println("requested URL getRemoteHost(): " + httpServletRequest.getRemoteHost().toString());
//        System.out.println("requested URL getRemotePort(): " + httpServletRequest.getRemotePort());
//        System.out.println("requested URL getLocalName(): " + httpServletRequest.getLocalName().toString());
        
        createAdapter(config);
        
        if(format == null || format.length() == 0){
            String msg = "Format parameter is missing";
            LOG.info("POST Workspace: " + msg);
            return Response.status(400).entity(msg).build(); 
        }

        String vectorFeature = "";

        try (InputStream is = httpServletRequest.getInputStream()) {
            
            StringWriter is_c = new StringWriter();
            IOUtils.copy(is, is_c);                    
            vectorFeature = is_c.toString();
            
        } catch (IOException e) {
            LOG.info("POST Workspace: error while reading from request body");
        }

        if (vectorFeature.length() == 0) {
            String msg = "Sourcefile URL from parameter or body is missing";
            LOG.info("POST Workspace: " + msg);
            return Response.status(400).entity(msg).build();
        }
        
        Response resp =
            computeMask(config, vectorFeature, format, cellSize, noData, data, workspaceid, styleNameString,
                userLayerName, httpServletRequest, workflowRunId);

        return resp;

    }
    
    /**
     * this method manage the storing of the source files (if wished), computing and storing their differenceFile (if wished) 
     * 
     * 
     * @param config
     * @param sourceUrlString1
     * @param sourceUrlString2
     * @param userLayerName1
     * @param userLayerName2
     * @param workspaceid
     * @param inputFormatString
     * @param outputFormatString
     * @param styleNameString
     * @param httpServletRequest
     *
     * @return Response - JSON string with Geoserver links to diffResult and WCS URL
     */
    
//    private Response computeDiff(ServletConfig config, String sourceUrlString1, String sourceUrlString2,
//                                 String userLayerName1, String userLayerName2, String workspaceid,
//                                 String inputFormatString, String styleNameString, HttpServletRequest httpServletRequest){
    
    private Response computeDiff(ServletConfig config, String userLayerName1, String userLayerName2, String styleNameString,String workspaceid,
                                 Boolean addNoValueMask,  HttpServletRequest httpServletRequest, String workflowRunId){
        
        // getcov get request
        // http://biovel.iais.fraunhofer.de:8080/geoserver/ows?request=getcoverage&service=wcs&version=1.0.0&bbox=-180.0,-90.0,180.0,90.0&CRS=EPSG:4326&coverage=biovel:Gu09z3&width=256&height=256&format=ArcGrid

        String geoserverUrlExtern = geoserverUrl+"/wps";
//        System.out.println("geoserverUrlExtern: " + geoserverUrlExtern);
        
        String layer1 = "";
        String layer2 = "";
        
//        if(sourceUrlString1 == null && sourceUrlString2 == null){
            
            layer1 = userLayerName1;
            layer2 = userLayerName2;
            
//        } else {
//        
//            layer1 = storeCoverage(config, sourceUrlString1, null, workspaceid, inputFormatString, userLayerName1, styleNameString);
//            layer2 = storeCoverage(config, sourceUrlString2, null, workspaceid, inputFormatString, userLayerName2, styleNameString);
//        }
        
            gsAdapter.reloadGS();
            
            if(workspaceid == null || workspaceid.length() == 0){
                workspaceid = gsAdapter.getWorkspacenameFromUsername();
                if(!gsAdapter.existsWS(workspaceid)){
//                    createWorkspace(workspaceid);
                    String msg = "Error while store the diffresult to geoserver: -> the workspace '"+workspaceid+"' is not exists";
                    LOG.info(msg);
                    return Response.status(404).entity(msg).build();
                }
            }
            
            LOG.info("get workspace: " + workspaceid);
            
            
        //TODO outputFormatString, mimetype for output
        
        String postRequest = createDiffRequest(addNoValueMask, layer1, layer2);
        
        this.client = Client.create();

        ClientResponse cr = this.client.resource(geoserverUrlExtern).post(ClientResponse.class, postRequest);
        
        //System.out.println("response: " + cr.getStatus() + "\n" + cr.getEntity(String.class));
        
        String response = cr.getEntity(String.class);
        if (response.startsWith("<?xml")){
            String msg = "Error while processing: \n" + response;
            LOG.info(msg);
            return Response.status(400).entity(msg).build();
        } else {
            
            String diffLayer = "";
            String diffLayerName = "";
            
            try(InputStream is = new ByteArrayInputStream(response.getBytes());){
    
                String subName1 = layer1.substring(layer1.lastIndexOf(':')+1, layer1.length());
                String subName2 = layer2.substring(layer2.lastIndexOf(':')+1, layer2.length());
                diffLayerName = "diff_"+subName1+"_"+subName2;
                
                Response diffLayerResponse = storeCoverage(config, null, is, workspaceid, "ArcGrid", diffLayerName, styleNameString, workflowRunId);
                
                if(diffLayerResponse.getStatus() !=201){
                    return Response.status(diffLayerResponse.getStatus()).entity(diffLayerResponse).entity(String.class).build();
                } else {
                    diffLayer = diffLayerResponse.getEntity().toString(); 
                }
                
            } catch (Exception e) {
                String msg = "Error while storing the diffresult: '"+ diffLayerName + "' in the filesystem";
                return Response.status(404).entity(msg).build();
            }
                
            String geoserverUrl = httpServletRequest.getScheme()+"://"+httpServletRequest.getServerName();
            int port = httpServletRequest.getServerPort();
            if(port != 80 && port != 443){
               geoserverUrl += ":"+port;
            }
                
                //result as json string
    //            result = "{\"diffresult\":\"" + geoserverUrl + "/geoserver/ows?service=WMS&version=1.1.0&request=GetMap&layers="+layer1+"&styles=&bbox=-180.0,-90.0,180.0,90.0&width=660&height=330&srs=EPSG:4326&format=image%2Fpng\" \n"+
    //                      "\"source2\":\"" + geoserverUrl + "/geoserver/ows?service=WMS&version=1.1.0&request=GetMap&layers="+layer2+"&styles=&bbox=-180.0,-90.0,180.0,90.0&width=660&height=330&srs=EPSG:4326&format=image%2Fpng\" \n"+
    //                      "\"diffresult\":\"" + geoserverUrl + "/geoserver/ows?service=WMS&version=1.1.0&request=GetMap&layers="+diffLayer+"&styles=&bbox=-180.0,-90.0,180.0,90.0&width=660&height=330&srs=EPSG:4326&format=image%2Fpng\"}";
    //            
                
//            result = "{\"diffresult tiff\":\"" + geoserverUrl + "/geoserver/ows?service=WMS&version=1.1.0&request=GetMap&layers="+diffLayer+"&FORMAT=image/tiff&bbox=-180.0,-90.0,180.0,90&crs=EPSG:4326&width=720&height=360\" \n"+
//                    "\"diffresult arcgrid\":\"" + geoserverUrl + "/geoserver/ows?service=wcs&request=GetCoverage&version=1.0.0&sourcecoverage="+diffLayer+"&FORMAT=application/arcgrid&bbox=-180.0,-90.0,180.0,90&crs=EPSG:4326&width=720&height=360\" \n"+   
//                    "\"wcsurl\":\"" + geoserverUrl + "/geoserver/ows?service=wcs\"}";
            
            String result = gsAdapter.getCoverageInfo(geoserverUrl, workspaceid, diffLayer);
                
            return Response.ok(result).build();
        }

    }
    
    private Response computeMask(ServletConfig config, String vectorFeature, String format, Double cellSize,
                                 Integer noData, Integer data, String workspaceid, String styleNameString,
                                 String userLayerName, HttpServletRequest httpServletRequest, String workflowRunId) {

        String geoserverUrlExtern = geoserverUrl + "/wps";
//        System.out.println("geoserverUrl: " + geoserverUrlExtern);
        
        //set raster style
        if (styleNameString == null){
            styleNameString = "vrt_mask_color";
        }
        
        //set cellsize to min 60 arcsecons:
        if(cellSize == null || cellSize < 60){
            cellSize = 60.0;
        }

        gsAdapter.reloadGS();

        if (workspaceid == null || workspaceid.length() == 0) {
            workspaceid = "biovel_temp";
//            workspaceid = gsAdapter.getWorkspacenameFromUsername();
            if (!gsAdapter.existsWS(workspaceid)) {
                String msg =
                    "Error while store the diffresult to geoserver: -> the workspace '" + workspaceid
                            + "' is not exists";
                LOG.info(msg);
                return Response.status(404).entity(msg).build();
            }
        }

//        LOG.info("get workspace: " + workspaceid);

        String postRequest = createMaskRequest(vectorFeature, format, cellSize, noData, data);

        this.client = Client.create();

        ClientResponse cr = this.client.resource(geoserverUrlExtern).post(ClientResponse.class, postRequest);

//      System.out.println("response: " + cr.getStatus() + "\n" + cr.getEntity(String.class));

        String response = cr.getEntity(String.class);
        if (response.startsWith("<?xml")) {
            String msg = "Error while processing: \n" + response;
            LOG.info(msg);
            return Response.status(400).entity(msg).build();
        
        } else {
            
            response = response.replace("-9999", "101");

            String diffLayer = "";

            try (InputStream is = new ByteArrayInputStream(response.getBytes());) {

                if (userLayerName == null || userLayerName.length() == 0) {
                    userLayerName = "rastermask";
                }
                
                Response diffLayerResponse =
                    storeCoverage(config, null, is, workspaceid, "ArcGrid", userLayerName, styleNameString, workflowRunId);

                if (diffLayerResponse.getStatus() != 201) {
                    return Response.status(diffLayerResponse.getStatus()).entity(diffLayerResponse)
                        .entity(String.class).build();
                } else {
                    diffLayer = diffLayerResponse.getEntity().toString();
                }

            } catch (Exception e) {
                String msg = "Error while storing the mask: '" + userLayerName + "' in the filesystem";
                return Response.status(404).entity(msg).build();
            }

            String geoserverUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName();
            int port = httpServletRequest.getServerPort();
            if (port != 80 && port != 443) {
                geoserverUrl += ":" + port;
            }

            String result = gsAdapter.getCoverageInfo(geoserverUrl, workspaceid, diffLayer);

            return Response.ok(result).build();
        }
    }
    
    private Response storeCoverage(ServletConfig config, String sourceUrlString, InputStream sourceInputStream, String workspaceid, String formatString,
                                 String userLayerName, String styleNameString, String workflowRunId){
        
//        String storedCoverageName = "";
        
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
                break;
                
            case "arcgrid":
                suffix = ".arcgrid";
                break;
        }

        ClientResponse crPost = null;
        URI location;
        
        //if source is a file
        if(sourceUrlString != null){
       
            crPost = this.client.resource(dataUrl).queryParam("username", workspaceid).queryParam("workflowid", workflowRunId).queryParam("suffix", suffix).queryParam("layername", userLayerName).queryParam("source", sourceUrlString).post(ClientResponse.class);
                        
            if (crPost.getStatus() != 201) {
//                throw new WebApplicationException(crPost.getStatus());
                return Response.status(crPost.getStatus()).entity(crPost.getEntity(String.class)).build();
            }
            
            location = crPost.getLocation();
            
        } else {
            //if input is a stream
            crPost = this.client.resource(dataUrl).queryParam("username", workspaceid).queryParam("workflowid", workflowRunId).queryParam("suffix", suffix).queryParam("layername", userLayerName).post(ClientResponse.class,sourceInputStream);
            if (crPost.getStatus() != 201) {
//                throw new WebApplicationException(crPost.getStatus());
                return Response.status(crPost.getStatus()).entity(crPost.getEntity(String.class)).build();
            }
           
            location = crPost.getLocation();
        }
        
        ClientResponse crPostLoc = this.client.resource(location).post(ClientResponse.class);
        
        String sourceLocation = crPostLoc.getEntity(String.class);
        String sourceLocationFile = sourceLocation.substring( sourceLocation.lastIndexOf('/')+1, sourceLocation.length() );
        String sourceLocationNameWithoutExtn = sourceLocationFile.substring(0, sourceLocationFile.lastIndexOf('.'));
        
//        gsAdapter.reloadGS();
//        
//        if(workspaceid == null || workspaceid.length() == 0){
//            workspaceid = gsAdapter.getWorkspacenameFromUsername();
//            if(!gsAdapter.existsWS(workspaceid)){
////                createWorkspace(workspaceid);
//                String msg = "Error while store the diffresult to geoserver: -> the workspace '"+workspaceid+"' is not exists";
//                LOG.info(msg);
//                return Response.status(404).entity(msg).build();
//            }
//        }
        
        //TODO: zum layernamen username und workflowid hinzu??
        String layerName = "";
        if(userLayerName == null || userLayerName.length() == 0){
            String sourceFile = sourceUrlString.substring( sourceUrlString.lastIndexOf('/')+1, sourceUrlString.length() );
            String fileNameWithoutExtn = sourceFile.substring(0, sourceFile.lastIndexOf('.'));
            layerName = fileNameWithoutExtn+"_"+sourceLocationNameWithoutExtn;
        } else{
            
            if(!sourceLocationNameWithoutExtn.contains(userLayerName)){
                layerName = userLayerName+"_"+sourceLocationNameWithoutExtn;
            } else{
                
                layerName = sourceLocationNameWithoutExtn;
            }
            
        }
        
        gsAdapter.setWorkspace(workspaceid);
        
        Response createCoverageStoreStatus = gsAdapter.createCoverageStore(sourceLocation, layerName, formatString);
        if(createCoverageStoreStatus.getStatus() != 201){
            return createCoverageStoreStatus;
        }
        
        Response addCoverageLayerStatus = gsAdapter.addCoverageLayer();
        if(addCoverageLayerStatus.getStatus() != 201){
            return addCoverageLayerStatus;
        }
                    
        Response updateLayerStatus = gsAdapter.updateCoverageLayer(sourceLocation, suffix);
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
        
//        storedCoverageName = workspaceid +":"+ layerName;
        return Response.status(201).entity(layerName).build();
        
    }
    
    
    private void createAdapter(ServletConfig config){
        
        Properties props = (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);
        geoserverUrl = props.getProperty("GEOSERVER_URL");
        String geoserverUser = props.getProperty("GEOSERVER_USER");
        String geoserverPasswd = props.getProperty("GEOSERVER_PASSWD");
        String dataDir = props.getProperty("DATA_DIR");
        String dataURL = props.getProperty("DATA_URL");
        
        //System.out.println("geoserverUrl: " + geoserverUrl);
        
        URI gsUri;
        try {
            gsUri = new URI(geoserverUrl);
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        
//        gsAdapter = new RestGeoServerAdapter(geoserverUser, geoserverPasswd, gsUri);
        gsAdapter = new RestGeoServerAdapter(geoserverUser, geoserverPasswd, gsUri, dataDir, dataURL);
        
        if(!gsAdapter.resourceAvailable(geoserverUrl)){
            throw new WebApplicationException(500);
        }
        
    };
    
    private String createDiffRequest(Boolean addNoValueMask, String layer1, String layer2){
        
        String request = "";
        
        if(addNoValueMask){
            
            //diff adding noValueMask
            request =

                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><wps:Execute version=\"1.0.0\" service=\"WPS\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.opengis.net/wps/1.0.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:wcs=\"http://www.opengis.net/wcs/1.1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd\">"+
                            "<ows:Identifier>gs:SubtractCoveragesAddingNoValueMask</ows:Identifier>"+
                            "<wps:DataInputs>"+
                              "<wps:Input>"+
                                "<ows:Identifier>coverageA</ows:Identifier>"+
                                "<wps:Reference mimeType=\"application/arcgrid\" xlink:href=\"http://geoserver/wcs\" method=\"POST\">"+
                                  "<wps:Body>"+
                                    "<wcs:GetCoverage service=\"WCS\" version=\"1.1.1\">"+
                                      "<ows:Identifier>"+layer1+"</ows:Identifier>"+
                                      "<wcs:DomainSubset>"+
                                        "<gml:BoundingBox crs=\"http://www.opengis.net/gml/srs/epsg.xml#4326\">"+
                                          "<ows:LowerCorner>-180.0 -90.0</ows:LowerCorner>"+
                                          "<ows:UpperCorner>180.0 90.0</ows:UpperCorner>"+
                                        "</gml:BoundingBox>"+
                                      "</wcs:DomainSubset>"+
                                      "<wcs:Output format=\"application/arcgrid\"/>"+
                                    "</wcs:GetCoverage>"+
                                  "</wps:Body>"+
                                "</wps:Reference>"+
                              "</wps:Input>"+
                              "<wps:Input>"+
                                "<ows:Identifier>coverageB</ows:Identifier>"+
                                "<wps:Reference mimeType=\"application/arcgrid\" xlink:href=\"http://geoserver/wcs\" method=\"POST\">"+
                                  "<wps:Body>"+
                                    "<wcs:GetCoverage service=\"WCS\" version=\"1.1.1\">"+
                                      "<ows:Identifier>"+layer2+"</ows:Identifier>"+
                                      "<wcs:DomainSubset>"+
                                        "<gml:BoundingBox crs=\"http://www.opengis.net/gml/srs/epsg.xml#4326\">"+
                                          "<ows:LowerCorner>-180.0 -90.0</ows:LowerCorner>"+
                                          "<ows:UpperCorner>180.0 90.0</ows:UpperCorner>"+
                                        "</gml:BoundingBox>"+
                                      "</wcs:DomainSubset>"+
                                      "<wcs:Output format=\"application/arcgrid\"/>"+
                                    "</wcs:GetCoverage>"+
                                  "</wps:Body>"+
                                "</wps:Reference>"+
                              "</wps:Input>"+
                            "</wps:DataInputs>"+
                            "<wps:ResponseForm>"+
                              "<wps:RawDataOutput mimeType=\"application/arcgrid\">"+
                                "<ows:Identifier>result</ows:Identifier>"+
                              "</wps:RawDataOutput>"+
                            "</wps:ResponseForm>"+
                          "</wps:Execute>";        
                    
            
        } else {
            
            //diff only
            request = 
                    
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><wps:Execute version=\"1.0.0\" service=\"WPS\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.opengis.net/wps/1.0.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:wcs=\"http://www.opengis.net/wcs/1.1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd\">"
                            +"<ows:Identifier>gs:DifferenceCoverages</ows:Identifier>"
                             +"<wps:DataInputs>"
                               +"<wps:Input>"
                                 +"<ows:Identifier>coverageA</ows:Identifier>"
                                 +"<wps:Reference mimeType=\"application/arcgrid\" xlink:href=\"http://geoserver/wcs\" method=\"POST\">"
                                   +"<wps:Body>"
                                     +"<wcs:GetCoverage service=\"WCS\" version=\"1.1.1\">"
//                                       +"<ows:Identifier>admin:bIGTGo_20120605_140304_512</ows:Identifier>"
                                       +"<ows:Identifier>"+layer1+"</ows:Identifier>"
                                       +"<wcs:DomainSubset>"
                                         +"<gml:BoundingBox crs=\"http://www.opengis.net/gml/srs/epsg.xml#404000\">"
                                           +"<ows:LowerCorner>-180.0 -90.0</ows:LowerCorner>"
                                           +"<ows:UpperCorner>180.0 90.0</ows:UpperCorner>"
                                         +"</gml:BoundingBox>"
                                       +"</wcs:DomainSubset>"
                                       +"<wcs:Output format=\"application/arcgrid\"/>"
                                     +"</wcs:GetCoverage>"
                                   +"</wps:Body>"
                                 +"</wps:Reference>"
                               +"</wps:Input>"
                               +"<wps:Input>"
                                 +"<ows:Identifier>coverageB</ows:Identifier>"
                                 +"<wps:Reference mimeType=\"application/arcgrid\" xlink:href=\"http://geoserver/wcs\" method=\"POST\">"
                                   +"<wps:Body>"
                                     +"<wcs:GetCoverage service=\"WCS\" version=\"1.1.1\">"
//                                       +"<ows:Identifier>admin:Fy8qos_20120605_135631_337</ows:Identifier>"
                                       +"<ows:Identifier>"+layer2+"</ows:Identifier>"
                                       +"<wcs:DomainSubset>"
                                         +"<gml:BoundingBox crs=\"http://www.opengis.net/gml/srs/epsg.xml#4326\">"
                                           +"<ows:LowerCorner>-180.0 -90.0</ows:LowerCorner>"
                                           +"<ows:UpperCorner>180.0 90.0</ows:UpperCorner>"
                                         +"</gml:BoundingBox>"
                                       +"</wcs:DomainSubset>"
                                       +"<wcs:Output format=\"application/arcgrid\"/>"
                                     +"</wcs:GetCoverage>"
                                   +"</wps:Body>"
                                 +"</wps:Reference>"
                               +"</wps:Input>"
                             +"</wps:DataInputs>"
                             +"<wps:ResponseForm>"
                               +"<wps:RawDataOutput mimeType=\"application/arcgrid\">"
//                               +"<wps:RawDataOutput mimeType=\"image/tiff\">"
                                 +"<ows:Identifier>result</ows:Identifier>"
                               +"</wps:RawDataOutput>"
                             +"</wps:ResponseForm>"
                           +"</wps:Execute>";
            
        }
   


        
//        // zusammengesetzte teilprozesse
//        
//        String compositeRequest =  
//
//"<?xml version=\"1.0\" encoding=\"UTF-8\"?><wps:Execute version=\"1.0.0\" service=\"WPS\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.opengis.net/wps/1.0.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:wcs=\"http://www.opengis.net/wcs/1.1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd\">"+
//  "<ows:Identifier>gs:AddCoverages</ows:Identifier>"+
//  "<wps:DataInputs>"+
//    "<wps:Input>"+
//      "<ows:Identifier>coverageA</ows:Identifier>"+
//      "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://geoserver/wps\" method=\"POST\">"+
//        "<wps:Body>"+
//          "<wps:Execute version=\"1.0.0\" service=\"WPS\">"+
//            "<ows:Identifier>gs:CreateNoValueMask</ows:Identifier>"+
//            "<wps:DataInputs>"+
//              "<wps:Input>"+
//                "<ows:Identifier>coverageA</ows:Identifier>"+
//                "<wps:Reference mimeType=\"application/arcgrid\" xlink:href=\"http://geoserver/wcs\" method=\"POST\">"+
//                  "<wps:Body>"+
//                    "<wcs:GetCoverage service=\"WCS\" version=\"1.1.1\">"+
//                      "<ows:Identifier>"+layer1+"</ows:Identifier>"+
//                      "<wcs:DomainSubset>"+
//                        "<gml:BoundingBox crs=\"http://www.opengis.net/gml/srs/epsg.xml#4326\">"+
//                          "<ows:LowerCorner>-180.0 -90.0</ows:LowerCorner>"+
//                          "<ows:UpperCorner>180.0 90.0</ows:UpperCorner>"+
//                        "</gml:BoundingBox>"+
//                      "</wcs:DomainSubset>"+
//                      "<wcs:Output format=\"application/arcgrid\"/>"+
//                    "</wcs:GetCoverage>"+
//                  "</wps:Body>"+
//                "</wps:Reference>"+
//              "</wps:Input>"+
//            "</wps:DataInputs>"+
//            "<wps:ResponseForm>"+
//              "<wps:RawDataOutput mimeType=\"application/arcgrid\">"+
//                "<ows:Identifier>result</ows:Identifier>"+
//              "</wps:RawDataOutput>"+
//            "</wps:ResponseForm>"+
//          "</wps:Execute>"+
//        "</wps:Body>"+
//      "</wps:Reference>"+
//    "</wps:Input>"+
//    "<wps:Input>"+
//      "<ows:Identifier>coverageB</ows:Identifier>"+
//      "<wps:Reference mimeType=\"application/arcgrid\" xlink:href=\"http://geoserver/wps\" method=\"POST\">"+
//        "<wps:Body>"+
//          "<wps:Execute version=\"1.0.0\" service=\"WPS\">"+
//            "<ows:Identifier>gs:DifferenceCoverages</ows:Identifier>"+
//            "<wps:DataInputs>"+
//              "<wps:Input>"+
//                "<ows:Identifier>coverageA</ows:Identifier>"+
//                "<wps:Reference mimeType=\"application/arcgrid\" xlink:href=\"http://geoserver/wcs\" method=\"POST\">"+
//                  "<wps:Body>"+
//                    "<wcs:GetCoverage service=\"WCS\" version=\"1.1.1\">"+
//                      "<ows:Identifier>"+layer1+"</ows:Identifier>"+
//                      "<wcs:DomainSubset>"+
//                        "<gml:BoundingBox crs=\"http://www.opengis.net/gml/srs/epsg.xml#4326\">"+
//                          "<ows:LowerCorner>-180.0 -90.0</ows:LowerCorner>"+
//                          "<ows:UpperCorner>180.0 90.0</ows:UpperCorner>"+
//                        "</gml:BoundingBox>"+
//                      "</wcs:DomainSubset>"+
//                      "<wcs:Output format=\"application/arcgrid\"/>"+
//                    "</wcs:GetCoverage>"+
//                  "</wps:Body>"+
//                "</wps:Reference>"+
//              "</wps:Input>"+
//              "<wps:Input>"+
//                "<ows:Identifier>coverageB</ows:Identifier>"+
//                "<wps:Reference mimeType=\"application/arcgrid\" xlink:href=\"http://geoserver/wcs\" method=\"POST\">"+
//                  "<wps:Body>"+
//                    "<wcs:GetCoverage service=\"WCS\" version=\"1.1.1\">"+
//                      "<ows:Identifier>"+layer2+"</ows:Identifier>"+
//                      "<wcs:DomainSubset>"+
//                        "<gml:BoundingBox crs=\"http://www.opengis.net/gml/srs/epsg.xml#4326\">"+
//                          "<ows:LowerCorner>-180.0 -90.0</ows:LowerCorner>"+
//                          "<ows:UpperCorner>180.0 90.0</ows:UpperCorner>"+
//                        "</gml:BoundingBox>"+
//                      "</wcs:DomainSubset>"+
//                      "<wcs:Output format=\"application/arcgrid\"/>"+
//                    "</wcs:GetCoverage>"+
//                  "</wps:Body>"+
//                "</wps:Reference>"+
//              "</wps:Input>"+
//            "</wps:DataInputs>"+
//            "<wps:ResponseForm>"+
//              "<wps:RawDataOutput mimeType=\"application/arcgrid\">"+
//                "<ows:Identifier>result</ows:Identifier>"+
//              "</wps:RawDataOutput>"+
//            "</wps:ResponseForm>"+
//          "</wps:Execute>"+
//        "</wps:Body>"+
//      "</wps:Reference>"+
//    "</wps:Input>"+
//  "</wps:DataInputs>"+
//  "<wps:ResponseForm>"+
//    "<wps:RawDataOutput mimeType=\"application/arcgrid\">"+
//      "<ows:Identifier>result</ows:Identifier>"+
//    "</wps:RawDataOutput>"+
//  "</wps:ResponseForm>"+
//"</wps:Execute>";
      
      
      return request;
        
    }
    
    private String createMaskRequest(String vectorFeature, String format, Double cellSize, Integer noData, Integer data){
        
        String mimeType = "";
        
         switch(format.toLowerCase()){
            
            case "gml311":
                mimeType = "application/gml-3.1.1";
                break;
                
            case "gml212":
                mimeType = "application/gml-2.1.2";
                break;
                
            case "wkt":
                mimeType = "application/wkt";
                break;
        }
        
        String request = "";

            request =
             
           "<?xml version=\"1.0\" encoding=\"UTF-8\"?><wps:Execute version=\"1.0.0\" service=\"WPS\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.opengis.net/wps/1.0.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:wcs=\"http://www.opengis.net/wcs/1.1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd\">"
            +"<ows:Identifier>gs:FeatureToCoverageMask</ows:Identifier>"
            +"<wps:DataInputs>"
              +"<wps:Input>"
               +"<ows:Identifier>cropShape</ows:Identifier>"
                +"<wps:Data>"
                  +"<wps:ComplexData mimeType=\""+mimeType+"\"><![CDATA["+vectorFeature+"]]></wps:ComplexData>"
                +"</wps:Data>"
              +"</wps:Input>";
            
            if(cellSize != null){
              request +="<wps:Input>"
                +"<ows:Identifier>cellSize</ows:Identifier>"
                +"<wps:Data>"
                  +"<wps:LiteralData>"+cellSize+"</wps:LiteralData>"
                +"</wps:Data>"
              +"</wps:Input>";
            }
            
            if(noData != null){
              request+="<wps:Input>"
                +"<ows:Identifier>noData</ows:Identifier>"
                +"<wps:Data>"
                  +"<wps:LiteralData>"+noData+"</wps:LiteralData>"
                +"</wps:Data>"
              +"</wps:Input>";
            }
            
            if(data != null){
              request+="<wps:Input>"
                +"<ows:Identifier>data</ows:Identifier>"
                +"<wps:Data>"
                  +"<wps:LiteralData>"+data+"</wps:LiteralData>"
                +"</wps:Data>"
              +"</wps:Input>";
            }
              
            request+="</wps:DataInputs>"
            +"<wps:ResponseForm>"
              +"<wps:RawDataOutput mimeType=\"application/arcgrid\">"
                +"<ows:Identifier>result</ows:Identifier>"
              +"</wps:RawDataOutput>"
            +"</wps:ResponseForm>"
          +"</wps:Execute>";      
      
      return request;
        
    }
    

}
