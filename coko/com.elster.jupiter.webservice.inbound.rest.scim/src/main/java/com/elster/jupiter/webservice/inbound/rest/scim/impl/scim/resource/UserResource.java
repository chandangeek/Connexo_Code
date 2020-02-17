package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter.SCIMResourceOnlyFilter;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.SCIMService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.UserSchema;

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

@Path("Users")
@SCIMResourceOnlyFilter
public class UserResource {

    @Inject
    private SCIMService scimService;

    @GET
    @Path("{id}")
    @Produces("application/scim+json")
    public UserSchema getUser(@NotNull @PathParam("id") String id) {
        return scimService.getUser(id);
    }

    @POST
    @Consumes("application/scim+json")
    @Produces("application/scim+json")
    public UserSchema createUser(UserSchema userSchema) {
        return scimService.createUser(userSchema);
    }

    @PUT
    @Path("{id}")
    @Consumes("application/scim+json")
    @Produces("application/scim+json")
    public UserSchema updateUser(@NotNull @PathParam("id") String id, UserSchema userSchema) {
        return scimService.updateUser(userSchema);
    }

    @DELETE
    @Path("{id}")
    public Response deleteUser(@NotNull @PathParam("id") String id) {
        scimService.deleteUser(id);
        return Response.ok().build();
    }

}
