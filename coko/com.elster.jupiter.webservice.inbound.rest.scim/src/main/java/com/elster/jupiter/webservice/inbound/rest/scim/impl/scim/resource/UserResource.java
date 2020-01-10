package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter.SCIMResourceOnlyFilter;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.SCIMService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.UserSchema;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("Users")
@SCIMResourceOnlyFilter
public class UserResource {

    @Inject
    private SCIMService scimService;

    @Path("{id}")
    @GET
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
    @Consumes("application/scim+json")
    @Produces("application/scim+json")
    public UserSchema updateUser(UserSchema userSchema) {
        return scimService.updateUser(userSchema);
    }

    @Path("{id}")
    @DELETE
    public Response deleteUser(@NotNull @PathParam("id") String id) {
        scimService.deleteUser(id);
        return Response.ok().build();
    }

}
