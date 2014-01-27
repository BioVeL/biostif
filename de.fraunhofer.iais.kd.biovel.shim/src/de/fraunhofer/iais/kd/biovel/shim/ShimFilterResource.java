package de.fraunhofer.iais.kd.biovel.shim;

import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.DWC;
import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.OCC_CSV;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import de.fraunhofer.iais.kd.biovel.shim.filter.ShimFilterOccCsv2OccCsv;

@Path("/filter")
public class ShimFilterResource {
    private static final Logger LOG = Logger.getLogger(ShimFilterResource.class.getName());
    
    private Client client;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response respondIndexHtml(@Context ServletConfig config) {
        String filename = config.getServletContext().getRealPath("index.html");
        String result = BiovelHelper.readTextContentFromFile(new File(filename));
        return Response.ok(result).build();
    }
    
    
    @POST
    @Consumes(OCC_CSV)
    @Produces(OCC_CSV)
    public Response postCsv2Csv(@Context ServletConfig config, 
                                @Context HttpServletRequest httpServletRequest, 
                                @QueryParam("source") String sourceUrlString, 
                                @QueryParam("asurl") Boolean asUrl,
                                @QueryParam("workspaceid") String workspaceid,
                                @QueryParam("workflowid") String workflowRunId) {
        if (asUrl == null){
            asUrl = false;
        }
        System.out.println("filter csv2csv");
        return filterCsv2Csv(config, httpServletRequest, sourceUrlString, asUrl, workspaceid, workflowRunId);
    }
    
    @POST
    @Consumes(DWC)
    @Produces(DWC)
    public Response postDwc2Dwc(@Context ServletConfig config,
                                @Context HttpServletRequest httpServletRequest, 
                                @QueryParam("source") String sourceUrlString, 
                                @QueryParam("asurl") Boolean asUrl,
                                @QueryParam("workspaceid") String workspaceid,
                                @QueryParam("workflowid") String workflowRunId) {
        if (asUrl == null){
            asUrl = false;
        }
        System.out.println("filter dwc2dwc");
        return filterDwc2Dwc(config, httpServletRequest, sourceUrlString, asUrl, workspaceid, workflowRunId);
    }
    
    private Response filterCsv2Csv(ServletConfig config, HttpServletRequest httpServletRequest, String sourceUrlString, Boolean asUrl, String workspaceid, String workflowRunId){
        
        String result = null;
        
        if(sourceUrlString == null || sourceUrlString.length() == 0){
            
            final String msg = "Filter CSV - source is empty";
            LOG.info(msg);
            return Response.status(404).entity(msg).build();
            
        } else {
            
            System.out.println("sourceurl: " + sourceUrlString);
            
            try {
                
                this.client = Client.create();
                ClientResponse cr =
                        this.client.resource(sourceUrlString).header("Accept-Charset", "UTF-8").get(ClientResponse.class);
                InputStream entity = cr.getEntityInputStream();
                
                    //copy Stream an check if xml or not
                    StringWriter is_c = new StringWriter();
                    IOUtils.copy(entity, is_c, "UTF-8");                    
                    String isString = is_c.toString();
                    
                    if(isString.startsWith("<?xml")){
                        
                        LOG.info("XML files are not allowed in the Filter_CSV Method Method, send error 415");
                        final String msg = "mimeType 'application/xml' is not accepted - get a CSV File instead";
                        return Response.status(415).entity(msg + " - " + isString).build();
                        
                    }else{
                        
                        if(!isString.startsWith("[") && (!isString.startsWith("{"))  ){                          
                            
                            try(InputStream fileStream = new ByteArrayInputStream(isString.getBytes());){
                                
                                try(InputStream iDs = httpServletRequest.getInputStream();){
                                
                                    result = new ShimFilterOccCsv2OccCsv().filter(config, fileStream, iDs);
                                
                                } catch (Exception e) {                        
                                    String msg = "Error while create Stream from Body";
                                    return Response.status(404).entity(msg).build();
                                }
                                
                            } catch (Exception e) {                        
                                String msg = "Error while create Stream";
                                return Response.status(404).entity(msg).build();
                            }
                            
                        }else{
                            LOG.info("Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable");
                            final String msg = "Document starts with '[' - it seems not to be a CSV document, Send Error 406: not Acceptable";
                            return Response.status(406).entity(msg).build();
                        }
                    }
                
            }catch (IOException e) {
                // TODO Auto-generated catch block
                final String msg = "Error on reading document, send Error 415: " + e.getMessage();
                LOG.info(msg);
                return Response.status(415).entity(msg).build();
            }
        }

        
        if (result == null) {
            throw new WebApplicationException();
        }
        
        if(asUrl){
            result = resultAsURL(config, httpServletRequest, result, ".csv", workspaceid, workflowRunId);
        }
        
        Response response = Response.ok(result).build();
        return response;
    }
    
private Response filterDwc2Dwc(ServletConfig config, HttpServletRequest httpServletRequest, String sourceUrlString, Boolean asUrl, String workspaceid, String workflowRunId){
        
        String result = null;
        
        if(sourceUrlString == null || sourceUrlString.length() == 0){
            
            final String msg = "Filter DwC - source is empty";
            LOG.info(msg);
            return Response.status(415).entity(msg).build();
            
        } else {
            
            System.out.println("sourceurl: " + sourceUrlString);
            
            try {
                
                this.client = Client.create();
                
                ClientResponse cr =
                        this.client.resource(sourceUrlString).header("Accept-Charset", "UTF-8").get(ClientResponse.class);
                InputStream entity = cr.getEntityInputStream();
                
                    //copy Stream an check if xml or not
                    StringWriter is_c = new StringWriter();
                    IOUtils.copy(entity, is_c, "UTF-8");                    
                    String isString = is_c.toString();
                    
                    if(!isString.startsWith("<?xml")){
                        
                        LOG.info("XML files are not allowed in the Filter_CSV Method Method, send error 415");
                        final String msg = "DwC XML File is required -  not this";
                        return Response.status(415).entity(msg + " - " + isString).build();
                        
                    }else{
                        
                        if(!isString.startsWith("[") && (!isString.startsWith("{"))  ){                          
                            
                            try(InputStream fileStream = new ByteArrayInputStream(isString.getBytes());){
                                
                                try(InputStream iDs = httpServletRequest.getInputStream();){
                                
                                    result = new ShimFilterOccCsv2OccCsv().filter(config, fileStream, iDs);
                                
                                } catch (Exception e) {                        
                                    String msg = "Error while create Stream from Body";
                                    return Response.status(404).entity(msg).build();
                                }
                                
                            } catch (Exception e) {                        
                                String msg = "Error while create Stream";
                                return Response.status(404).entity(msg).build();
                            }
                            
                        }else{
                            LOG.info("Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable");
                            final String msg = "Document starts with '[' - it seems not to be a CSV document, Send Error 406: not Acceptable";
                            return Response.status(406).entity(msg).build();
                        }
                    }
//                }
                
            }catch (IOException e) {
                // TODO Auto-generated catch block
                LOG.info("Error on reading document, send Error 415: " + e.getMessage());
                final String msg = "Error on reading document, send Error 415: " + e.getMessage();
                return Response.status(415).entity(msg).build();
            }
        }

        
        if (result == null) {
            throw new WebApplicationException();
        }
        
        if(asUrl){
            result = resultAsURL(config, httpServletRequest, result, ".xml", workspaceid, workflowRunId);
        }
        
        Response response = Response.ok(result).build();
        return response;
    }
    

    
    
    private String resultAsURL(ServletConfig config, HttpServletRequest httpServletRequest, String entity, String suffix, String workspaceid, String workflowRunId){
        
        String resultURL = "";
        
        this.client = Client.create();
        Properties props = (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);
        String dataUrl = props.getProperty("DATA_URL");
        
//        System.out.println("entity: \n" + entity);
        
        InputStream bais = null;
        try {
            bais = new ByteArrayInputStream(entity.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
//        System.out.println("dataUrl: " + dataUrl + " req: " +httpServletRequest.getRequestURL().toString());
        
        ClientResponse crPost = this.client.resource(dataUrl).queryParam("username", workspaceid).queryParam("workflowid", workflowRunId).queryParam("suffix", suffix).post(ClientResponse.class,bais);

        URI location = crPost.getLocation();
        
        String serverUrl = httpServletRequest.getScheme()+"://"+httpServletRequest.getServerName();
        int port = httpServletRequest.getServerPort();
        if(port != 80 && port != 443){
           serverUrl += ":"+port;
        }
        
        resultURL = location.toString().replace(dataUrl.replace("/workflow/rest/data", ""), serverUrl);
        
//        System.out.println(resultURL);
        
        return resultURL;
    }

}
