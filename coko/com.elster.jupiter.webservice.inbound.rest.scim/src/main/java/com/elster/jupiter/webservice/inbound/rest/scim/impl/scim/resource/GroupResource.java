package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.SCIMService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.GroupSchema;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("Groups")
public class GroupResource {

    @Inject
    private SCIMService scimService;

    @GET
    @Path("{id}")
    @Produces("application/scim+json")
    public GroupSchema getGroup(@NotNull @PathParam("id") String id) {
        return scimService.getGroup(id);
    }

    @POST
    @Consumes("application/scim+json")
    @Produces("application/scim+json")
    public GroupSchema createGroup(GroupSchema groupSchema) {
        return scimService.createGroup(groupSchema);
    }

    @PUT
    @Path("{id}")
    @Consumes("application/scim+json")
    @Produces("application/scim+json")
    public GroupSchema updateGroup(@NotNull @PathParam("id") String id, GroupSchema groupSchema) {
        return scimService.updateGroup(groupSchema);
    }

    @DELETE
    @Path("{id}")
    public Response deleteGroup(@NotNull @PathParam("id") String id) {
        scimService.deleteGroup(id);
        return Response.ok().build();
    }

}
