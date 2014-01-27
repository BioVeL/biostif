package de.fraunhofer.iais.kd.biovel.workflow;

import static de.fraunhofer.iais.kd.biovel.common.BiovelConstants.BIOSTIF;

import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import de.fraunhofer.iais.kd.biovel.feed.FeedManager;

@Path("feed")
public class FeedResource {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FeedResource.class.getName());

    private static FeedManager manager = null;

    public static void putManager(FeedManager aFeedManager) {
        manager = aFeedManager;
    }

    @POST
    @Consumes({ BIOSTIF, MediaType.TEXT_PLAIN })
    public Response createNewEntry(@Context UriInfo uriInfo, String content) {
        String entryId = manager.makeNewBiovelEntryId();
        URI location = uriInfo.getAbsolutePathBuilder().path(entryId).build();
        LOG.info("created entry URI: " + location.getPath());
        manager.putEntry(entryId, content);
        return Response.created(location).entity(location.toString()).build();
    }

    @GET
    @Produces({ MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_HTML })
    public Response getHtml() {
        return Response.ok(manager.getFeed()).build();
    }

    @GET
    @Path("/{entryId}")
    @Produces(BIOSTIF)
    public Response getEntry(@PathParam("entryId") String entryId) {
        String result = manager.getEntry(entryId);
        if (result == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            return Response.ok(result).build();
        }
    }

    @DELETE
    @Path("/{entryId}")
    public Response deleteEntry(@PathParam("entryId") String entryId) {
        String result = manager.getEntry(entryId);
        if (manager.containsEntry(entryId)) {
            manager.deleteEntry(entryId);
            return Response.ok(result).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/{entryId}/reply")
    @Consumes(BIOSTIF)
    public Response putReplyTo(@PathParam("entryId") String entryId, String replyContent) {
        if (manager.isReplyId(entryId)) {
            return Response.status(Status.NOT_FOUND.getStatusCode()).build();
        }
        System.out.println("PUT reply to" + entryId);
        if (manager.containsEntry(entryId)) {
            if (!manager.containsReplyToEntry(entryId)) {
                manager.putReplyTo(entryId, replyContent);
                return Response.ok().build();
            } else {
                return Response.status(Status.CONFLICT).build();
            }
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/{entryId}/reply")
    @Produces(BIOSTIF)
    public Response getReplyTo(@PathParam("entryId") String entryId) {
        String result = manager.getReplyTo(entryId);
        if (result == null) {
            if (manager.containsEntry(entryId)) {
                return Response.status(Status.ACCEPTED).build();
            } else {
                return Response.status(Status.NOT_ACCEPTABLE).build();
            }
        } else {
            return Response.ok(result).build();
        }
    }

}