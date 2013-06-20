package com.elster.jupiter.users.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/users")
public class UserResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfos createUser(UserInfo info) {
        UserInfos result = new UserInfos();
        result.add(Bus.getTransactionService().execute(new CreateUserTransaction(info)));
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfos deleteUser(UserInfo info, @PathParam("id") long id) {
        info.id = id;
        Bus.getTransactionService().execute(new DeleteUserTransaction(info));
        return new UserInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfos getUser(@PathParam("id") long id) {
        Optional<User> party = Bus.getUserService().getUser(id);
        if (party.isPresent()) {
            return new UserInfos(party.get());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfos getUsers(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<User> list = getUserRestQuery().select(queryParameters);
        UserInfos infos = new UserInfos(list);
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public UserInfos updateUser(UserInfo info, @PathParam("id") long id) {
        info.id = id;
        Bus.getTransactionService().execute(new UpdateUserTransaction(info));
        return getUser(info.id);
    }

    private RestQuery<User> getUserRestQuery() {
        Query<User> query = Bus.getUserService().getUserQuery();
        return Bus.getRestQueryService().wrap(query);
    }

}
