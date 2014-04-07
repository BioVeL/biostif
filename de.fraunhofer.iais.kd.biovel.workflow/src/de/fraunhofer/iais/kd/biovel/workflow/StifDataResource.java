package de.fraunhofer.iais.kd.biovel.workflow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.DocFlavor.STRING;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

//import org.apache.commons.io.IOUtils;



import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.Base64;

import de.fraunhofer.iais.kd.biovel.common.contract.Check;
import de.fraunhofer.iais.kd.biovel.stifdata.StifDataManager;
import de.fraunhofer.iais.kd.biovel.util.WorkflowException;

@Path("/")
public class StifDataResource {

    private static final Logger LOG = Logger.getLogger(StifDataResource.class.getName());

    private Client client;
    
    private static StifDataManager manager = null;    
    
    protected static final int X_LAYER = 0;
    protected static final int X_SUFFIX = 1;

    public static void putManager(StifDataManager aStifDataManager) {
        manager = aStifDataManager;
    }

    @GET
    @Path("ping")
    public Response dataPing() {
        return Response.ok().build();
    }

    @GET
    public Response dataGet() {
        return Response.status(204).build();
    }
    
    @POST
    @Path("make-credentials")
    @Produces(MediaType.TEXT_PLAIN)
    public Response makeCredentials(@QueryParam("username") String username, //
                                    @QueryParam("runid") String runid) {
        String credentials = manager.makeCredentials(username, runid);
        return Response.ok(credentials).build();
    }

    @GET
    @Path("credentials-{element}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCredentialsElement(@PathParam("element") String elementKey, //
                                          @QueryParam("credentials") String credentialsEncoded) {
        Check.notNull(credentialsEncoded);
        Check.isTrue(Base64.isBase64(credentialsEncoded));
        if ("username".equals(elementKey)) {
            return Response.ok(manager.getCredentialsUsername(credentialsEncoded)).build();
        } else if ("runid".equals(elementKey)) {
            return Response.ok(manager.getCredentialsRunId(credentialsEncoded)).build();
        } else {
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/data")
    public Response postDataUpload(@Context ServletConfig config,
                                   @Context UriInfo uriInfo, //
                                   @QueryParam("source") String sourceUrl, //
                                   @QueryParam("suffix") String suffix, //
                                   @QueryParam("layername") String userLayerName, //
                                   @QueryParam("username") String username,
                                   @QueryParam("workflowid") String workflowRunId,
                                   @HeaderParam("Content-Type") String headerContentType,
                                   @HeaderParam("Content-Length") int headerContentLength,
                                   @Context HttpServletRequest httpServletRequest,
                                   @HeaderParam("X-Auth-Service-Provider") String authServiceProvider,
                                   @HeaderParam("X-Verify-Credentials-Authorization") String credentialsAuthorizationToken,
                                   String entity
                                   ) {
        
        // compensate a call (in main.jsp?), that provides "null" for null
        if (suffix != null && suffix.equals("null")) {
            suffix = null;
        }
        
        if(authServiceProvider != null && credentialsAuthorizationToken != null
                && authServiceProvider.length() > 0 && credentialsAuthorizationToken.length() > 0){
            
            String shimUrl = manager.getProperties().getProperty("SHIM_URL");
            
            this.client = Client.create();
            ClientResponse crPost = this.client.resource(shimUrl+"/raster/authcheck").header("X-Auth-Service-Provider", authServiceProvider).header("X-Verify-Credentials-Authorization", credentialsAuthorizationToken).get(ClientResponse.class);

            if(crPost.getStatus() == 200){
                username = crPost.getEntity(String.class);
            } else {
                final String msg = "ERROR: authCheck in datastore request failed because: " + crPost.getEntity(String.class);
                LOG.log(Level.SEVERE, msg);
                return Response.status(crPost.getStatus()).entity(msg).build();
            }
        } else {
            
            if(!httpServletRequest.getRemoteAddr().toString().equals("127.0.0.1") 
                    && !httpServletRequest.getRemoteAddr().toString().equals("localhost")){
//                System.out.println("DAtaSTORE überschreibe den namen");
//                username = manager.getProperties().getProperty("PUBLIC_WORKSPACE");
                
                final String msg = "Unknown Client outside of localhost";
                LOG.log(Level.WARNING, msg);
                
            }             
        }
       
        if(username == null || username.length() == 0){
            username = manager.getProperties().getProperty("PUBLIC_WORKSPACE");
        }
        
        if(workflowRunId == null || workflowRunId.length() == 0){
            workflowRunId = "all_runs";
        }
        
        String[] resourceName = makeLayerNameAndSuffix(sourceUrl, suffix, userLayerName);
        
//        String resource =
//            manager.makeNewDataResourceUri("XXX-not-a-user-name-XXX", "all_runs", userLayerName, (suffix == null) ? "" : suffix);
        
        String resource =
                manager.makeNewDataResourceUri(username, workflowRunId, resourceName[X_LAYER], resourceName[X_SUFFIX]);        
        
//        System.out.println("storeData  resource Name: " + resource);
        URI location = uriInfo.getBaseUriBuilder().path("/data/" + resource).build();
//        LOG.info("created URI: " + location.getPath());

//        if ((sourceUrl == null) && ((entity == null) || entity.isEmpty())) {
//            return Response.created(location).entity(location.toString()).build();
//        }

        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        
        if (sourceUrl != null) {
            
            try{
                WebResource dwcResource = client.resource(sourceUrl);
                ClientResponse dwcCR = dwcResource.get(ClientResponse.class);
                if (dwcCR.getStatus() != Status.OK.getStatusCode()) {
                    throw new WebApplicationException(new WorkflowException("cannot GET " + sourceUrl));
                }
                InputStream sourceStream = dwcCR.getEntityInputStream();
                manager.putResource(resource, sourceStream);
            } catch (Exception ex) {
               return Response.status(500).entity(ex.getMessage()).build(); 
            }
//        } else if (entity != null) {
//            // store the entity of the request
        } else {
            
            try (InputStream is = httpServletRequest.getInputStream()) {
                
                manager.putResource(resource, is);
                
            } catch (IOException e) {
                String msg = "Error while get the entityInputStream from Datastorerequest : " + e.getMessage();
                LOG.info(msg);
                return Response.status(500).entity(msg).build();
            }
            
            
//            manager.putResource(resource, entity);
        }
//        else { // sourceUrl == null && entity == null
//            // content of the resource must be PUT explicitly
//        }

        LOG.info("stored source into URI: " + location.getPath());
        return Response.created(location).entity(location.toString()).build();
    }
    
    private boolean isNonEmptyStringValue(String value) {
        return value != null && value.length() > 0;
    }

    public String[] makeLayerNameAndSuffix(final String sourceUrl, final String userSuffix, final String userLayerName) {
        LOG.info("layer sourceUrl: " + sourceUrl + " userSuffix: '" + userSuffix + "' userLayerName: '" + userLayerName
            + "'");
        
        String filename = "";
        
        if (isNonEmptyStringValue(userLayerName)) {
            if (userLayerName.lastIndexOf('/') >= 0) {
                throw new RuntimeException("invalid layer format: name has / inside");
            };
            filename = userLayerName;
        } else {
            if (isNonEmptyStringValue(sourceUrl)) {
                final int urlDirPathEnd = sourceUrl.lastIndexOf('/');
                filename = sourceUrl.substring((urlDirPathEnd < 0) ? 0 : urlDirPathEnd + 1);
            } else {
                filename = "streaminput";
            }
        }

        final int dotSeparatorX = filename.lastIndexOf('.');
        final String basename = (dotSeparatorX <= 0) ? filename : filename.substring(0, dotSeparatorX);
        final String suffix = ((dotSeparatorX < 0) ? "" : filename.substring((dotSeparatorX <= 0) ? 0 : dotSeparatorX+1));
        
        final String resultSuffix = ((isNonEmptyStringValue(userSuffix))
                                    ?((userSuffix.startsWith("."))?userSuffix.substring(1): userSuffix)
                                    :suffix);
        final String fullBasename = (suffix.equals(resultSuffix)?basename:filename);    
        
        String[] result = new String[2]; // { fullBasename + "_", resultSuffix.isEmpty()?"":"." + resultSuffix };
        result[X_LAYER] = ((fullBasename + "_").replace(" ", "_"));
        result[X_SUFFIX] = (resultSuffix.isEmpty()?"":"." + resultSuffix).replace(" ", "_");
        return result;
    }
    
    @PUT
    @Path("/data/{dataresource : .+}")
    //    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response putTextPlainFromStream(@Context UriInfo uriInfo, //
                                           @PathParam("dataresource") String dataresource, //
                                           InputStream entityInputStream) {
        LOG.info("put any " + dataresource);
        manager.putResource(dataresource, entityInputStream);
        LOG.info("put any -- finished " + dataresource);
        return Response.status(Status.OK).build();
    }

    @GET
    @Path("/data/{resource : .+}")
    public Response getTextPlain(@Context UriInfo uriInfo, //
                                 @PathParam("resource") String resource) {
        LOG.info("GET any " + resource);
        InputStream result;
        try {
            result = manager.getResourceInputStream(resource);
        } catch (FileNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.status(Status.OK).entity(result).build();
    }

    @POST
    @Path("/data/{resource : .+}")
    public Response localResourceFileURI(@PathParam("resource") String resource) {
        LOG.info("POST any " + resource);
        String fileResource = manager.getLocalResourceFileURI(resource);
        if (fileResource == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.status(Status.OK).entity(fileResource).build();
    }

}