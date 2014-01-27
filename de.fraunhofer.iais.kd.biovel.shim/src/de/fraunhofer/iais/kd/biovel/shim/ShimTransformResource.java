package de.fraunhofer.iais.kd.biovel.shim;

import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.DWC;
import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.CSV;
import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.OCC_CSV;
import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.SLW_CSV;
import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.SLW_JSON;
import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.STIF_JSON;
import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.JSON;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.fraunhofer.iais.kd.biovel.common.BiovelHelper;
import de.fraunhofer.iais.kd.biovel.shim.transform.ShimTransformerAllCsv2Json;
import de.fraunhofer.iais.kd.biovel.shim.transform.ShimTransformerAllJson2Csv;
import de.fraunhofer.iais.kd.biovel.shim.transform.ShimTransformerDwc2OccCsv;
import de.fraunhofer.iais.kd.biovel.shim.transform.ShimTransformerDwc2StifJson;
import de.fraunhofer.iais.kd.biovel.shim.transform.ShimTransformerOccCsv2StifJson;
import de.fraunhofer.iais.kd.biovel.shim.transform.ShimTransformerSlwCsv2OccCsv;
import de.fraunhofer.iais.kd.biovel.shim.transform.ShimTransformerSlwGeoJson2OccCsv;

@Path("/transform")
public class ShimTransformResource {
    private static final Logger LOG = Logger.getLogger(ShimTransformResource.class.getName());

    private Client client;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response respondIndexHtml(@Context ServletConfig config) {
        String filename = config.getServletContext().getRealPath("index.html");
        String result = BiovelHelper.readTextContentFromFile(new File(filename));
        return Response.ok(result).build();
    }

    @POST
    @Consumes(DWC)
    @Produces(OCC_CSV)
    public Response postDwc2Csv(@Context ServletConfig config,
                                      @Context HttpServletRequest httpServletRequest,
                                      @QueryParam("source") String urlString,
                                      @QueryParam("asurl") Boolean asUrl,
                                      @QueryParam("workspaceid") String workspaceid,
                                      @QueryParam("workflowid") String workflowRunId,
                                      String entity) {
        if (asUrl == null){
            asUrl = false;
        }
        return transformDwc2Csv(config, httpServletRequest, urlString, entity, asUrl, workspaceid, workflowRunId);
    }

//    @POST
//    @Consumes(DWC)
//    @Produces(MediaType.TEXT_HTML)
//    public Response postDwc2Csv(@Context ServletConfig config,
//                                @Context HttpServletRequest httpServletRequest,
//                                @QueryParam("source") String sourceUrlString,
//                                @QueryParam("asurl") Boolean asUrl,
//                                String entity) {
//        if (asUrl == null){
//            asUrl = false;
//        }
//        return transformDwc2Csv(config, httpServletRequest, sourceUrlString, entity, asUrl);
//    }

    @POST
    @Consumes(DWC)
    @Produces(STIF_JSON)
    public Response postDwc2Stif_Json(@Context ServletConfig config,
                                      @Context HttpServletRequest httpServletRequest,
                                      @QueryParam("source") String sourceUrlString,
                                      @QueryParam("time") String time,
                                      @QueryParam("asurl") Boolean asUrl,
                                      @QueryParam("workspaceid") String workspaceid,
                                      @QueryParam("workflowid") String workflowRunId) {
        if (asUrl == null){
            asUrl = false;
        }
        return transformDwc2Stif_Json(config, httpServletRequest, sourceUrlString, time, asUrl, workspaceid, workflowRunId);
    }

    @POST
    @Consumes(SLW_CSV)
    @Produces(OCC_CSV)
    public Response postSlw2Csv(@Context ServletConfig config,
                                @Context HttpServletRequest httpServletRequest,
                                @QueryParam("source") String sourceUrlString,
                                @QueryParam("asurl") Boolean asUrl,
                                @QueryParam("workspaceid") String workspaceid,
                                @QueryParam("workflowid") String workflowRunId) {
        if (asUrl == null){
            asUrl = false;
        }
        return transformSlw2Csv(config, httpServletRequest, sourceUrlString, asUrl, workspaceid, workflowRunId);
    }

    @POST
    @Consumes(SLW_JSON)
    @Produces(OCC_CSV)
    public Response postGeoJson2Csv(@Context ServletConfig config,
                                    @Context HttpServletRequest httpServletRequest,
                                    @QueryParam("source") String sourceUrlString,
                                    @QueryParam("asurl") Boolean asUrl,
                                    @QueryParam("workspaceid") String workspaceid,
                                    @QueryParam("workflowid") String workflowRunId) {
        if (asUrl == null){
            asUrl = false;
        }
        return transformGeoJson2Csv(config, httpServletRequest, sourceUrlString, asUrl, workspaceid, workflowRunId);
    }
    
    @POST
    @Consumes(JSON)
    @Produces(CSV)
    public Response postAllJson2Csv(@Context ServletConfig config,
                                    @Context HttpServletRequest httpServletRequest,
                                    @QueryParam("source") String sourceUrlString,
                                    @QueryParam("occurenceid") String occurenceid,
                                    @QueryParam("lat") String lat,
                                    @QueryParam("lon") String lon,
                                    @QueryParam("place") String place,
                                    @QueryParam("time") String time,
                                    @QueryParam("name") String name,
                                    @QueryParam("asurl") Boolean asUrl,
                                    @QueryParam("workspaceid") String workspaceid,
                                    @QueryParam("workflowid") String workflowRunId) {
        if (asUrl == null){
            asUrl = false;
        }
        return transformAllJson2Csv(config, httpServletRequest, sourceUrlString, occurenceid, lat, lon, place, time, name, asUrl, workspaceid, workflowRunId);
    }

    @POST
    @Consumes(OCC_CSV)
    @Produces(STIF_JSON)
    public Response postOccCsv2StifJson(@Context ServletConfig config,
                                          @Context HttpServletRequest httpServletRequest,
                                          @QueryParam("source") String sourceUrlString,
                                          @QueryParam("popuplabel") String popuplabel,
                                          @QueryParam("time") String time,
                                          @QueryParam("asurl") Boolean asUrl,
                                          @QueryParam("workspaceid") String workspaceid,
                                          @QueryParam("workflowid") String workflowRunId) {
        if (asUrl == null){
            asUrl = false;
        }
        return transformOccCsv2StifJson(config, httpServletRequest, sourceUrlString, popuplabel, time, asUrl, workspaceid, workflowRunId);
    }
    
    @POST
    @Consumes(CSV)
    @Produces(JSON)
    public Response postAllCsv2StifJson(@Context ServletConfig config,
                                          @Context HttpServletRequest httpServletRequest,
                                          @QueryParam("source") String sourceUrlString,
                                          @QueryParam("occurenceid") String occurenceid,
                                          @QueryParam("lat") String lat,
                                          @QueryParam("lon") String lon,
                                          @QueryParam("place") String place,
                                          @QueryParam("time") String time,
                                          @QueryParam("asurl") Boolean asUrl,
                                          @QueryParam("workspaceid") String workspaceid,
                                          @QueryParam("workflowid") String workflowRunId) {
        if (asUrl == null){
            asUrl = false;
        }
        return transformAllCsv2Json(config, httpServletRequest, sourceUrlString, occurenceid, lat, lon, place, time, asUrl, workspaceid, workflowRunId);
    }

    private Response transformDwc2Csv(ServletConfig config, HttpServletRequest httpServletRequest, String dwcSourceUrl, String dwcEntity, Boolean asUrl, String workspaceid, String workflowRunId) {
        
//        System.out.println("here transformDwc2Csv");
        
        Properties props = (Properties) config.getServletContext().getAttribute(ShimServletContainer.BIOSTIF_SERVER_CONF);

        String xsltProgramUrl = props.getProperty("URL_DWC_TO_CSV_XSLT");

        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);

        WebResource xsltProgramResource = client.resource(xsltProgramUrl);

        InputStream xsltProgramStream = null;
        Reader dwcReader = null;
        String result = null;

        ClientResponse cr = xsltProgramResource.get(ClientResponse.class);
        int status = cr.getStatus();
        
        if (status == Status.OK.getStatusCode()) {
            
            xsltProgramStream = cr.getEntityInputStream();
            
        } else {
            LOG.warning("The official transformation DwC to csv is not available. Using a substitute transformation");
            String xsltFilename = props.getProperty("FILENAME_DWC_TO_CSV_XSLT");
            try {
                xsltProgramStream = new FileInputStream(new File(xsltFilename));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        if (dwcSourceUrl == null) {
//            dwcReader = new StringReader(dwcEntity);
            
            if (!dwcEntity.startsWith("<?xml")) {

                LOG.info("XML Files are required in the Dwc2Csv Method, send error 415");
                final String msg = "XML Files are required in the Dwc2Csv Method";
                return Response.status(415).entity(msg + " - " + dwcEntity).build();

            } else {
                dwcReader = new StringReader(dwcEntity);
            }
            
        } else {

            this.client = Client.create();
            ClientResponse cr1 =
                this.client.resource(dwcSourceUrl).header("Accept-Charset", "UTF-8").get(ClientResponse.class);

            InputStream entity = cr1.getEntityInputStream();

            StringWriter is_c = new StringWriter();
            try {
                IOUtils.copy(entity, is_c, "UTF-8");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String isString = is_c.toString();

            if (!isString.startsWith("<?xml")) {

                LOG.info("XML Files are required in the Dwc2Csv Method, send error 415");
                final String msg = "XML Files are required in the Dwc2Csv Method";
                return Response.status(415).entity(msg + " - " + isString).build();

            } else {
//                    dwcReader = new InputStreamReader(new ByteArrayInputStream(isString.getBytes()));
                    dwcReader = new StringReader(isString);
            }
        }
        result = new ShimTransformerDwc2OccCsv().transform(config, dwcReader, xsltProgramStream);
        if (result == null) {
            throw new WebApplicationException();
        }
        
        if (asUrl) {
            result = resultAsURL(config, httpServletRequest, result, ".csv", workspaceid, workflowRunId);
        }
        
        Response response = Response.ok(result).build();
        return response;
    }

    private Response transformDwc2Stif_Json(ServletConfig config, HttpServletRequest httpServletRequest,
                                            String dwcSourceUrl, String time, Boolean asUrl, String workspaceid, String workflowRunId) {
        
//        System.out.println("HERE transformDwc2Stif_Json");
        
//        Properties props = (Properties) config.getServletContext().getAttribute(ShimServletContainer.STIF_SERVER_CONF);
//        String xsltProgramUrl = props.getProperty("URL_DWC_TO_CSV_XSLT");

//        DefaultClientConfig clientConfig = new DefaultClientConfig();
//        Client client = Client.create(clientConfig);

//        WebResource xsltProgramResource = client.resource(xsltProgramUrl);

//        InputStream xsltProgramStream = null;
//        Reader dwcReader = null;

//        ClientResponse cr = xsltProgramResource.get(ClientResponse.class);
//        int status = cr.getStatus();

        String result = null;
        
        if (dwcSourceUrl == null || dwcSourceUrl.length() == 0) {

            System.out.println("no source URL - get Body");

            try (InputStream is = httpServletRequest.getInputStream();) {

                StringWriter is_c = new StringWriter();
                IOUtils.copy(is, is_c, "UTF-8");
                String isString = is_c.toString();

                if (isString.startsWith("[") || isString.startsWith("{")) {

                    LOG.info("XML Files are required in the OCC_CSV2JSON Method, send error 415");
                    final String msg = "File begins with '{' or '[' ist is not a XML file";
                    return Response.status(415).entity(msg + " - " + isString).build();

                } else {

                    try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {

                        result = new ShimTransformerDwc2StifJson().transform(is1, time);

                    } catch (Exception e) {
                        String msg = "Error while create Stream";
                        return Response.status(404).entity(msg).build();
                    }

//                    if (status == Status.OK.getStatusCode()) {

//                        try (InputStream xsltProgramStream = cr.getEntityInputStream();) {
//                            try (Reader dwcReader =
//                                new InputStreamReader(new ByteArrayInputStream(isString.getBytes()));) {
//                                result = new ShimTransformerDwc2Stif_Json().transform(dwcReader, xsltProgramStream);
//                            }catch (Exception e) {                        
//                                String msg = "Error while create Reader from Bodystring 1";
//                                return Response.status(404).entity(msg).build();
//                            }

//                        } catch (Exception e) {                        
//                          String msg = "Error while create Stream from XSLT_URL 1";
//                          return Response.status(404).entity(msg).build();
//                        }

//                    } else {
//                        LOG.warning("The official transformation DwC to csv is not available. Using a substitute transformation");
//                        String xsltFilename = props.getProperty("FILENAME_DWC_TO_CSV_XSLT");
//
//                        try (InputStream xsltProgramStream = new FileInputStream(new File(xsltFilename));) {
//                            try (Reader dwcReader =
//                                new InputStreamReader(new ByteArrayInputStream(isString.getBytes()));) {
//                                result = new ShimTransformerDwc2Stif_Json().transform(dwcReader, xsltProgramStream);
//                            }catch (Exception e) {                        
//                                String msg = "Error while create Reader from Bodystring 2";
//                                return Response.status(404).entity(msg).build();
//                            }
//                            
//                        }catch (Exception e) {                        
//                            String msg = "Error while create Stream from XLST_FILE 2";
//                            return Response.status(404).entity(msg).build();
//                        }
//                    }
                }

            } catch (Exception e) {
                String msg = "Error while create Stream from RequestBody";
                return Response.status(404).entity(msg).build();
            }

//                    try {
//                        if (status == Status.OK.getStatusCode()) {
//                            xsltProgramStream = cr.getEntity(InputStream.class);
//                        } else {
//                            LOG.warning("The official transformation DwC to csv is not available. Using a substitute transformation");
//                            String xsltFilename = props.getProperty("FILENAME_DWC_TO_CSV_XSLT");
//                            xsltProgramStream = new FileInputStream(new File(xsltFilename));
//                        }
//
//                        if (dwcSourceUrl == null) {
//                            dwcReader = new StringReader(dwcEntity);
//                        } else {
//                            URL url = new URL(dwcSourceUrl);
//                            URLConnection conn = url.openConnection();
//                            dwcReader = new InputStreamReader(conn.getInputStream());
//                        }
//                        result = new ShimTransformerDwc2Csv().transform(dwcReader, xsltProgramStream);
//                    } catch (Exception exn) {
//                        throw new WebApplicationException(exn);
//                    } finally {
//                        Exception exn2 = null;
//                        try {
//                            if (dwcReader != null) {
//                                dwcReader.close();
//                            }
//                        } catch (Exception exn) {
//                            exn2 = exn;
//                        } finally {
//                            try {
//                                if (xsltProgramStream != null) {
//                                    xsltProgramStream.close();
//                                }
//                            } catch (Exception exn) {
//                                exn2 = exn;
//                            } finally {
//                                if (exn2 != null) {
//                                    throw new WebApplicationException(exn2);
//                                }
//                            }
//                        }
//                    }

//                }

//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }

        } else {

            System.out.println("sourceurl: " + dwcSourceUrl);

            try {

                this.client = Client.create();

                ClientResponse cre =
                    this.client.resource(dwcSourceUrl).header("Accept-Charset", "UTF-8").get(ClientResponse.class);

                InputStream entity = cre.getEntityInputStream();

                //copy Stream an check if xml or not
                StringWriter is_c = new StringWriter();
                IOUtils.copy(entity, is_c, "UTF-8");
                String isString = is_c.toString();
                
                if (isString.startsWith("[") || isString.startsWith("{")) {

                    LOG.info("XML Files are required in the OCC_CSV2JSON Method, send error 415");
                    final String msg = "File begins with '{' or '[' ist is not a XML file";
                    return Response.status(415).entity(msg + " - " + isString).build();

                } else {

//                            try(InputStream is1 = new ByteArrayInputStream(isString.getBytes());){
//                                
//                                result = new ShimTransformerOcc_Csv2Stif_Json().transform(is1, popuplabel, time, config);
//                                
//                            } catch (Exception e) {                        
//                                String msg = "Error while create Stream";
//                                return Response.status(404).entity(msg).build();
//                            }

                    try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {

                        result = new ShimTransformerDwc2StifJson().transform(is1, time);

                    } catch (Exception e) {
                        String msg = "Error while create Stream from SourceURL -> " + dwcSourceUrl;
                        return Response.status(404).entity(msg).build();
                    }

//                        if (status == Status.OK.getStatusCode()) {
//
//                            try (InputStream xsltProgramStream = cr.getEntityInputStream();) {
//                                try (Reader dwcReader =
//                                    new InputStreamReader(new ByteArrayInputStream(isString.getBytes()));) {
//                                    result = new ShimTransformerDwc2Stif_Json().transform(dwcReader, xsltProgramStream);
//                                }catch (Exception e) {                        
//                                    String msg = "Error while create Reader from Bodystring 3";
//                                    return Response.status(404).entity(msg).build();
//                                }
//                                
//                            } catch (Exception e) {                        
//                              String msg = "Error while create Stream from XSLT_URL 3";
//                              return Response.status(404).entity(msg).build();
//                            }
//                            
//                        } else {
//                            LOG.warning("The official transformation DwC to csv is not available. Using a substitute transformation");
//                            String xsltFilename = props.getProperty("FILENAME_DWC_TO_CSV_XSLT");
//
//                            try (InputStream xsltProgramStream = new FileInputStream(new File(xsltFilename));) {
//                                try (Reader dwcReader =
//                                    new InputStreamReader(new ByteArrayInputStream(isString.getBytes()));) {
//                                    result = new ShimTransformerDwc2Stif_Json().transform(dwcReader, xsltProgramStream);
//                                }catch (Exception e) {                        
//                                    String msg = "Error while create Reader from Bodystring 4";
//                                    return Response.status(404).entity(msg).build();
//                                }
//                                
//                            }catch (Exception e) {                        
//                                String msg = "Error while create Stream from XLST_FILE 4";
//                                return Response.status(404).entity(msg).build();
//                            }
//                        }
                }

//                    if(isString.startsWith("<?xml")){
//                        
//                        LOG.info("XML files are not allowed in the OCC_CSV2JSON Method Method, send error 415");
//                        final String msg = "mimeType 'application/xml' is not accepted - get a CSV File instead";
//                        return Response.status(415).entity(msg + " - " + isString).build();
//                        
//                    }else{
//                        
//                        if(!isString.startsWith("[") && (!isString.startsWith("{"))  ){                          
//                            
//                            try(InputStream is1 = new ByteArrayInputStream(isString.getBytes());){
//                                
//                                result = new ShimTransformerSlw2Csv().transform(is1);
//                                
//                            } catch (Exception e) {                        
//                                String msg = "Error while create Stream";
//                                return Response.status(404).entity(msg).build();
//                            }
//                            
//                        }else{
//                            LOG.info("Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable");
//                            final String msg = "Document starts with '[' - it seems not to be a CSV document, Send Error 406: not Acceptable";
//                            return Response.status(406).entity(msg).build();
//                        }
//                    }

//                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOG.info("Error on reading document, send Error 415: " + e.getMessage());
                final String msg = "Error on reading document, send Error 415: " + e.getMessage();
                return Response.status(415).entity(msg).build();
            }
        }

//        try {
//            if (status == Status.OK.getStatusCode()) {
//                xsltProgramStream = cr.getEntity(InputStream.class);
//            } else {
//                LOG.warning("The official transformation DwC to csv is not available. Using a substitute transformation");
//                String xsltFilename = props.getProperty("FILENAME_DWC_TO_CSV_XSLT");
//                xsltProgramStream = new FileInputStream(new File(xsltFilename));
//            }
//
//            if (dwcSourceUrl == null) {
//                dwcReader = new StringReader(dwcEntity);
//            } else {
//                URL url = new URL(dwcSourceUrl);
//                URLConnection conn = url.openConnection();
//                dwcReader = new InputStreamReader(conn.getInputStream());
//            }
//            result = new ShimTransformerDwc2Csv().transform(dwcReader, xsltProgramStream);
//        } catch (Exception exn) {
//            throw new WebApplicationException(exn);
//        } finally {
//            Exception exn2 = null;
//            try {
//                if (dwcReader != null) {
//                    dwcReader.close();
//                }
//            } catch (Exception exn) {
//                exn2 = exn;
//            } finally {
//                try {
//                    if (xsltProgramStream != null) {
//                        xsltProgramStream.close();
//                    }
//                } catch (Exception exn) {
//                    exn2 = exn;
//                } finally {
//                    if (exn2 != null) {
//                        throw new WebApplicationException(exn2);
//                    }
//                }
//            }
//        }

        if (result == null) {
            throw new WebApplicationException();
        }

        if (asUrl) {
            result = resultAsURL(config, httpServletRequest, result, ".json", workspaceid, workflowRunId);
        }

        Response response = Response.ok(result).build();
        return response;
    }

    private Response transformSlw2Csv(ServletConfig config, HttpServletRequest httpServletRequest,
                                      String sourceUrlString, Boolean asUrl, String workspaceid, String workflowRunId) {

//        System.out.println("HERE transformSlw2Csv");
        String result = null;

        if (sourceUrlString == null || sourceUrlString.length() == 0) {

            System.out.println("no source URL - get Body");

            try (InputStream is = httpServletRequest.getInputStream();) {

//                bufferedReader = new BufferedReader(new InputStreamReader(is));
//                StringBuilder stringBuilder = new StringBuilder();
//                String line = null;
//                
//                while ((line = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(line + "\n");
//                }
//                
//                bufferedReader.close();
//                System.out.println(stringBuilder.toString());

                StringWriter is_c = new StringWriter();
                IOUtils.copy(is, is_c, "UTF-8");
                String isString = is_c.toString();
                
//                System.out.println("isSttr: " + isString);

                if (isString.startsWith("<?xml")) {

                    LOG.info("XML files are not allowed in the SLW_CSV2BioVeL_CSV Method, send error 415");
                    final String msg = "mimeType 'application/xml' is not accepted - get a CSV File instead";
                    return Response.status(415).entity(msg + " - " + isString).build();

                } else {

                    if (!isString.startsWith("[") && !isString.startsWith("{")) {

                        try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {

                            result = new ShimTransformerSlwCsv2OccCsv().transform(is1);

                        } catch (Exception e) {
                            String msg = "Error while create Stream";
                            return Response.status(404).entity(msg).build();
                        }

                    } else {
                        LOG.info("Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable");
                        final String msg =
                            "Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable";
                        return Response.status(406).entity(msg).build();
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
//                e.printStackTrace();
                String msg = "transformSlw2Csv: Error read stream from body";
                LOG.info(msg);
            }

        } else {

            System.out.println("sourceurl: " + sourceUrlString);

            try {

                this.client = Client.create();
                ClientResponse cr =
                    this.client.resource(sourceUrlString).header("Accept-Charset", "UTF-8").get(ClientResponse.class);
                InputStream entity = cr.getEntityInputStream();
//                URL u = new URL(sourceUrlString);           
//                URLConnection conn = u.openConnection();


//                try(InputStream is = conn.getInputStream();){

//              Charset utf8CS = Charset.forName("ANSI");
//              CharsetDecoder decoder = utf8CS.newDecoder();
//                
//                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity, Charset.forName("UTF-8")));
//                    StringBuilder stringBuilder = new StringBuilder();
//                    String line = null;
//                    
//                    while ((line = bufferedReader.readLine()) != null) {
//                        stringBuilder.append(line + "\n");
//                    }
//                    
//                    bufferedReader.close();
//                    System.out.println(stringBuilder.toString());

                //copy Stream an check if xml or not
                StringWriter is_c = new StringWriter();
                IOUtils.copy(entity, is_c, "UTF-8");
                String isString = is_c.toString();
                
//                System.out.println("isSttr: transformSlw2Csv" + isString);
                System.out.println("isSttr: transformSlw2Csv");

                if (isString.startsWith("<?xml")) {

                    LOG.info("XML files are not allowed in the SLW_CSV2BioVeL_CSV Method Method, send error 415");
                    final String msg = "mimeType 'application/xml' is not accepted - get a CSV File instead";
                    return Response.status(415).entity(msg + " - " + isString).build();

                } else {

                    if (!isString.startsWith("[") && (!isString.startsWith("{"))) {

                        try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {

                            result = new ShimTransformerSlwCsv2OccCsv().transform(is1);

                        } catch (Exception e) {
                            String msg = "Error while create Stream";
                            return Response.status(404).entity(msg).build();
                        }

                    } else {
                        LOG.info("Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable");
                        final String msg =
                            "Document starts with '[' - it seems not to be a CSV document, Send Error 406: not Acceptable";
                        return Response.status(406).entity(msg).build();
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOG.info("Error on reading document, send Error 415: " + e.getMessage());
                final String msg = "Error on reading document, send Error 415: " + e.getMessage();
                return Response.status(415).entity(msg).build();
            }
        }

        if (result == null) {
            throw new WebApplicationException();
        }

        if (asUrl) {
            result = resultAsURL(config, httpServletRequest, result, ".csv", workspaceid, workflowRunId);
        }

        Response response = Response.ok(result).build();
        return response;
    }

    private Response transformGeoJson2Csv(ServletConfig config, HttpServletRequest httpServletRequest,
                                          String sourceUrlString, Boolean asUrl, String workspaceid, String workflowRunId) {

//        System.out.println("here transformGeoJson2Csv");
        
        String result = null;

        if (sourceUrlString == null || sourceUrlString.length() == 0) {

            System.out.println("no source URL - get Body");

            try (InputStream is = httpServletRequest.getInputStream();) {

                String isString = "";

                StringWriter is_c = new StringWriter();
                IOUtils.copy(is, is_c, "UTF-8");
                isString = is_c.toString();

                if (isString.startsWith("<?xml")) {

                    LOG.info("XML files are not allowed in the SLW_Json2BioVeL_CSV Method, send error 415");
                    final String msg = "mimeType 'application/xml' is not accepted - get a Json File instead";
                    return Response.status(415).entity(msg + " - " + isString).build();

                } else {

                    if (!isString.startsWith("[")) {

                        try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {

                            result = new ShimTransformerSlwGeoJson2OccCsv().transform(is1);

                        } catch (Exception e) {
                            String msg = "Error while create Stream";
                            return Response.status(404).entity(msg).build();
                        }

                    } else {
                        LOG.info("Document starts with '[' - it seems not to be a Json Collection, Send Error 406: not Acceptable");
                        final String msg =
                            "Document starts with '[' - it seems not to be a Json Collection, Send Error 406: not Acceptable";
                        return Response.status(406).entity(msg).build();
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

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

                if (isString.startsWith("<?xml")) {

                    LOG.info("XML files are not allowed in the SLW_Json2BioVeL_CSV, send error 415");
                    final String msg = "mimeType 'application/xml' is not accepted - get a JSON File instead";
                    return Response.status(415).entity(msg + " - " + isString).build();

                } else {

                    if (!isString.startsWith("[")) {

                        try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {

                            result = new ShimTransformerSlwGeoJson2OccCsv().transform(is1);

                        } catch (Exception e) {
                            String msg = "Error while create Stream";
                            return Response.status(404).entity(msg).build();
                        }

                    } else {
                        LOG.info("Document starts with '[' - it seems not to be a Json Collection, Send Error 406: not Acceptable");
                        final String msg =
                            "Document starts with '[' - it seems not to be a Json Collection, Send Error 406: not Acceptable";
                        return Response.status(406).entity(msg).build();
                    }
                }
//                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOG.info("Error on reading document, send Error 415: " + e.getMessage());
                final String msg = "Error on reading document, send Error 415: " + e.getMessage();
                return Response.status(415).entity(msg).build();
            }
        }

        if (result == null) {
            throw new WebApplicationException();
        }

        if (asUrl) {
            result = resultAsURL(config, httpServletRequest, result, ".csv", workspaceid, workflowRunId);
        }

        Response response =
            Response.ok(result).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN + "; Charset=UTF-8").build();
        return response;
    }
    
    private Response transformAllJson2Csv(ServletConfig config, HttpServletRequest httpServletRequest,
                                          String sourceUrlString, String occurenceid, String lat, String lon, String place, String time, String name, Boolean asUrl , String workspaceid, String workflowRunId) {

        String result = null;

        if (sourceUrlString == null || sourceUrlString.length() == 0) {

            System.out.println("no source URL - get Body");

            try (InputStream is = httpServletRequest.getInputStream();) {

                String isString = "";

                StringWriter is_c = new StringWriter();
                IOUtils.copy(is, is_c, "UTF-8");
                isString = is_c.toString();
                
                if (isString.startsWith("<?xml")) {

                    LOG.info("XML files are not allowed in the SLW_Json2BioVeL_CSV Method, send error 415");
                    final String msg = "mimeType 'application/xml' is not accepted - get a Json File instead";
                    return Response.status(415).entity(msg + " - " + isString).build();

                } else {

                    if (!isString.startsWith("[")) {

                        try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {

                            result = new ShimTransformerAllJson2Csv().transform(is1, occurenceid, lat, lon, place, time, name);

                        } catch (Exception e) {
                            String msg = "Error while create Stream";
                            return Response.status(404).entity(msg).build();
                        }

                    } else {
                        LOG.info("Document starts with '[' - it seems not to be a Json Collection, Send Error 406: not Acceptable");
                        final String msg =
                            "Document starts with '[' - it seems not to be a Json Collection, Send Error 406: not Acceptable";
                        return Response.status(406).entity(msg).build();
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

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
                
                System.out.println("iss: " + isString);

                if (isString.startsWith("<?xml")) {

                    LOG.info("XML files are not allowed in the SLW_Json2BioVeL_CSV, send error 415");
                    final String msg = "mimeType 'application/xml' is not accepted - get a JSON File instead";
                    return Response.status(415).entity(msg + " - " + isString).build();

                } else {

                    if (!isString.startsWith("[")) {

                        try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {

                            result = new ShimTransformerAllJson2Csv().transform(is1, occurenceid, lat, lon, place, time, name);

                        } catch (Exception e) {
                            String msg = "Error while create Stream";
                            return Response.status(404).entity(msg).build();
                        }

                    } else {
                        LOG.info("Document starts with '[' - it seems not to be a Json Collection, Send Error 406: not Acceptable");
                        final String msg =
                            "Document starts with '[' - it seems not to be a Json Collection, Send Error 406: not Acceptable";
                        return Response.status(406).entity(msg).build();
                    }
                }
//                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOG.info("Error on reading document, send Error 415: " + e.getMessage());
                final String msg = "Error on reading document, send Error 415: " + e.getMessage();
                return Response.status(415).entity(msg).build();
            }
        }

        if (result == null) {
            throw new WebApplicationException();
        }

        if (asUrl) {
            result = resultAsURL(config, httpServletRequest, result, ".csv" , workspaceid, workflowRunId);
        }

        Response response =
            Response.ok(result).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN + "; Charset=UTF-8").build();
        return response;
    }

    private Response transformOccCsv2StifJson(ServletConfig config, HttpServletRequest httpServletRequest,
                                                String sourceUrlString, String popuplabel, String time, Boolean asUrl, String workspaceid, String workflowRunId) {

//        System.out.println("here transformOcc_Csv2Stif_Json");
        
        String result = "";

        if (sourceUrlString == null || sourceUrlString.length() == 0) {

            System.out.println("no source URL - get Body");

            try (InputStream is = httpServletRequest.getInputStream();) {

                StringWriter is_c = new StringWriter();
                IOUtils.copy(is, is_c, "UTF-8");
                String isString = is_c.toString();

                if (isString.startsWith("<?xml")) {

                    LOG.info("XML files are not allowed in the OCC_CSV2JSON Method, send error 415");
                    final String msg = "mimeType 'application/xml' is not accepted - get a CSV File instead";
                    return Response.status(415).entity(msg + " - " + isString).build();

                } else {

                    if (!isString.startsWith("[") && !isString.startsWith("{")) {

                        try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {
                            
                            result = new ShimTransformerOccCsv2StifJson().transform(is1, popuplabel, time, config);

                            if(result.startsWith("ERROR:")){                               
                                return Response.status(500).entity(result).build();
                            }

                        } catch (Exception e) {
                            String msg = "Error while create Stream from source URL";
                            return Response.status(404).entity(msg).build();
                        }

                    } else {
                        LOG.info("Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable");
                        final String msg =
                            "Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable";
                        return Response.status(406).entity(msg).build();
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

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
                

                if (isString.startsWith("<?xml")) {

                    LOG.info("XML files are not allowed in the OCC_CSV2JSON Method Method, send error 415");
                    final String msg = "mimeType 'application/xml' is not accepted - get a CSV File instead";
                    return Response.status(415).entity(msg + " - " + isString).build();

                } else {

                    if (!isString.startsWith("[") && (!isString.startsWith("{"))) {

                        try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {
                            
                            result = new ShimTransformerOccCsv2StifJson().transform(is1, popuplabel, time, config);
                            
                            if(result.startsWith("ERROR:")){                               
                                return Response.status(500).entity(result).build();
                            }
                        
                        } catch (Exception e) {
                            String msg = "Error while create Stream";
                            return Response.status(404).entity(msg).build();
                        }

                    } else {
                        LOG.info("Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable");
                        final String msg =
                            "Document starts with '[' - it seems not to be a CSV document, Send Error 406: not Acceptable";
                        return Response.status(406).entity(msg).build();
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOG.info("Error on reading document, send Error 415: " + e.getMessage());
                final String msg = "Error on reading document, send Error 415: " + e.getMessage();
                return Response.status(415).entity(msg).build();
            }
        }

        if (result == null) {
            throw new WebApplicationException();
        }

        if (asUrl) {
            result = resultAsURL(config, httpServletRequest, result, ".json" , workspaceid, workflowRunId);
        }

        Response response = Response.ok(result).build();

        return response;

    }
    
    private Response transformAllCsv2Json(ServletConfig config, HttpServletRequest httpServletRequest, String sourceUrlString, String occurenceid, String lat, String lon, String place, String time, Boolean asUrl, String workspaceid, String workflowRunId) {

      System.out.println("here transformAllCsv2Json");
      
      String result = "";

      if (sourceUrlString == null || sourceUrlString.length() == 0) {

          System.out.println("no source URL - get Body");

          try (InputStream is = httpServletRequest.getInputStream();) {

              StringWriter is_c = new StringWriter();
              IOUtils.copy(is, is_c, "UTF-8");
              String isString = is_c.toString();

              if (isString.startsWith("<?xml")) {

                  LOG.info("XML files are not allowed in the OCC_CSV2JSON Method, send error 415");
                  final String msg = "mimeType 'application/xml' is not accepted - get a CSV File instead";
                  return Response.status(415).entity(msg + " - " + isString).build();

              } else {

                  if (!isString.startsWith("[") && !isString.startsWith("{")) {

                      try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {

                          result = new ShimTransformerAllCsv2Json().transform(is1, occurenceid, lat, lon, place, time, config);

                      } catch (Exception e) {
                          String msg = "Error while create Stream";
                          return Response.status(404).entity(msg).build();
                      }

                  } else {
                      LOG.info("Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable");
                      final String msg =
                          "Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable";
                      return Response.status(406).entity(msg).build();
                  }
              }

          } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }

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
              
              if (isString.startsWith("<?xml")) {

                  LOG.info("XML files are not allowed in the OCC_CSV2JSON Method Method, send error 415");
                  final String msg = "mimeType 'application/xml' is not accepted - get a CSV File instead";
                  return Response.status(415).entity(msg + " - " + isString).build();

              } else {

                  if (!isString.startsWith("[") && (!isString.startsWith("{"))) {

                      try (InputStream is1 = new ByteArrayInputStream(isString.getBytes());) {
                          
                          result = new ShimTransformerAllCsv2Json().transform(is1, occurenceid, lat, lon, place, time, config);
                          
                      } catch (Exception e) {
                          String msg = "Error while create Stream";
                          return Response.status(404).entity(msg).build();
                      }

                  } else {
                      LOG.info("Document starts with '[' or '{' - it seems not to be a CSV document, Send Error 406: not Acceptable");
                      final String msg =
                          "Document starts with '[' - it seems not to be a CSV document, Send Error 406: not Acceptable";
                      return Response.status(406).entity(msg).build();
                  }
              }
//              }

          } catch (IOException e) {
              // TODO Auto-generated catch block
              LOG.info("Error on reading document, send Error 415: " + e.getMessage());
              final String msg = "Error on reading document, send Error 415: " + e.getMessage();
              return Response.status(415).entity(msg).build();
          }
      }

      if (result == null) {
          throw new WebApplicationException();
      }

      if (asUrl) {
          result = resultAsURL(config, httpServletRequest, result, ".json", workspaceid, workflowRunId);
      }

      Response response = Response.ok(result).build();

      return response;

  }

    private String resultAsURL(ServletConfig config, HttpServletRequest httpServletRequest, String entity, String suffix, String workspaceid, String workflowRunId) {

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

        ClientResponse crPost =
            this.client.resource(dataUrl).queryParam("username", workspaceid).queryParam("workflowid", workflowRunId).queryParam("suffix", suffix).post(ClientResponse.class, bais);
        
        URI location = crPost.getLocation();

        String serverUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName();
        int port = httpServletRequest.getServerPort();
        if (port != 80 && port != 443) {
            serverUrl += ":" + port;
        }

        resultURL = location.toString().replace(dataUrl.replace("/workflow/rest/data", ""), serverUrl);

//        System.out.println(resultURL);

        return resultURL;
    }

}
