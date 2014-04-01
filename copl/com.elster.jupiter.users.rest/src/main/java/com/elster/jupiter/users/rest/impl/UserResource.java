package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserInfo;
import com.elster.jupiter.users.rest.UserInfos;
import com.elster.jupiter.users.rest.actions.CreateUserTransaction;
import com.elster.jupiter.users.rest.actions.DeleteUserTransaction;
import com.elster.jupiter.users.rest.actions.UpdateUserTransaction;
import com.elster.jupiter.util.conditions.Order;
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
        User user = transactionService.execute(new CreateUserTransaction(info, userService));
        try (TransactionContext context = transactionService.getContext()) {
            UserInfos result = new UserInfos();
            result.add(user);
            context.commit();
            return result;
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfos deleteUser(UserInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new DeleteUserTransaction(info, userService));
        return new UserInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfos getUser(@PathParam("id") long id) {
        try (TransactionContext context = transactionService.getContext()) {
            Optional<User> party = userService.getUser(id);
            if (!party.isPresent()) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            try {
                return new UserInfos(party.get());
            } finally {
                context.commit();
            }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfos getUsers(@Context UriInfo uriInfo) {
        try (TransactionContext context = transactionService.getContext()) {
            QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
            List<User> list = getUserRestQuery().select(queryParameters, Order.ascending("authenticationName"));
            UserInfos infos = new UserInfos(queryParameters.clipToLimit(list));
            infos.total = queryParameters.determineTotal(list.size());
            try {
                return infos;
            } finally {
                context.commit();
            }
        }
    }

    @PUT
    @Path("/{id}/")
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
