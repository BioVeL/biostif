package de.fraunhofer.iais.kd.biovel.shim;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class ShimPingRessource {
    
    @GET
    @Path("ping")
    public Response dataPing() {
        return Response.ok().build();
    }

    @GET
    public Response dataGet() {
        return Response.status(204).build();
    }

    @HEAD
    public Response dataHead() {
        return Response.status(204).build();
    }
}
