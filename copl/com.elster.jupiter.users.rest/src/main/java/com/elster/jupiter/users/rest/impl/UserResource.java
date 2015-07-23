package com.elster.jupiter.users.rest.impl;

import java.util.List;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.PrivilegeInfos;
import com.elster.jupiter.users.rest.UserInfo;
import com.elster.jupiter.users.rest.UserInfos;
import com.elster.jupiter.users.rest.actions.UpdateUserTransaction;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;

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

// - To be added in the future?
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    public UserInfos createUser(UserInfo info) {
//        User user = transactionService.execute(new CreateUserTransaction(info, userService));
//        try (TransactionContext context = transactionService.getContext()) {
//            UserInfos result = new UserInfos();
//            result.add(user);
//            context.commit();
//            return result;
//        }
//    }

//    @DELETE
//    @Path("/{id}")
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    public UserInfos deleteUser(UserInfo info, @PathParam("id") long id) {
//        info.id = id;
//        transactionService.execute(new DeleteUserTransaction(info, userService));
//        return new UserInfos();
//    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_USER_ROLE,Privileges.VIEW_USER_ROLE})
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_USER_ROLE,Privileges.VIEW_USER_ROLE})
    public UserInfos getUsers(@Context UriInfo uriInfo) {
        try (TransactionContext context = transactionService.getContext()) {
            QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
            List<User> list = getUserRestQuery().select(queryParameters, Order.ascending("authenticationName").toLowerCase());
            UserInfos infos = new UserInfos(queryParameters.clipToLimit(list));
            infos.total = queryParameters.determineTotal(list.size());
            try {
                return infos;
            } finally {
                context.commit();
            }
        }
    }

    @GET
    @Path("/privileges")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public PrivilegeInfos getUserPrivileges(@Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        return new PrivilegeInfos(user.getPrivileges());
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_USER_ROLE)
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
