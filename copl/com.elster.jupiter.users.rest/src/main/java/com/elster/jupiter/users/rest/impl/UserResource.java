package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserInfo;
import com.elster.jupiter.users.rest.UserInfos;
import com.elster.jupiter.users.rest.actions.CreateUserTransaction;
import com.elster.jupiter.users.rest.actions.DeleteUserTransaction;
import com.elster.jupiter.users.rest.actions.UpdateUserTransaction;
import com.google.common.base.Optional;

import javax.inject.Inject;
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

    private final TransactionService transactionService;
    private final UserService userService;
    private final RestQueryService restQueryService;

    @Inject
    public UserResource(TransactionService transactionService, UserService userService, RestQueryService restQueryService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.restQueryService = restQueryService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfos createUser(UserInfo info) {
        UserInfos result = new UserInfos();
        result.add(transactionService.execute(new CreateUserTransaction(info, userService)));
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfos deleteUser(@PathParam("id") long id) {
        UserInfo info = new UserInfo();
        info.id = id;
        transactionService.execute(new DeleteUserTransaction(info, userService));
        return new UserInfos();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfos getUser(@PathParam("id") long id) {
        Optional<User> party = userService.getUser(id);
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
        UserInfos infos = new UserInfos(queryParameters.clipToLimit(list));
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public UserInfos updateUser(UserInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdateUserTransaction(info, userService));
        return getUser(info.id);
    }

    private RestQuery<User> getUserRestQuery() {
        Query<User> query = userService.getUserQuery();
        return restQueryService.wrap(query);
    }

}
