package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.FailToActivateUser;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.PrivilegeInfos;
import com.elster.jupiter.users.rest.UserInfo;
import com.elster.jupiter.users.rest.UserInfos;
import com.elster.jupiter.users.rest.actions.UpdateUserTransaction;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/users")
public class UserResource {

    private final TransactionService transactionService;
    private final UserService userService;
    private final RestQueryService restQueryService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final NlsService nlsService;

    @Inject
    public UserResource(TransactionService transactionService, UserService userService, RestQueryService restQueryService, ConcurrentModificationExceptionFactory conflictFactory, NlsService nlsService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.restQueryService = restQueryService;
        this.conflictFactory = conflictFactory;
        this.nlsService = nlsService;
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public UserInfos getUser(@PathParam("id") long id) {
        Optional<User> user = userService.getUser(id);
        if (!user.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
            return new UserInfos(this.nlsService, user.get());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public UserInfos getUsers(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<User> list = getUserRestQuery().select(queryParameters, Order.ascending("authenticationName").toLowerCase());
        UserInfos infos = new UserInfos(this.nlsService, queryParameters.clipToLimit(list));
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    @GET
    @Path("/privileges")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public PrivilegeInfos getUserPrivileges(@Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        Map<String, List<Privilege>> privileges = user.getApplicationPrivileges();
        PrivilegeInfos infos = new PrivilegeInfos();
        for (String application : privileges.keySet()){
            infos.addAll(this.nlsService, application, privileges.get(application));
        }
        return infos;
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public UserInfos updateUser(UserInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdateUserTransaction(info, userService, conflictFactory));
        return getUser(info.id);
    }

    @PUT
    @Path("/{id}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE})
    public UserInfos activateUser(UserInfo info, @PathParam("id") long id) {
        Optional<User> user = userService.getUser(id);
        if (   !"INT".equals(userService.findUserDirectory(user.get().getDomain()).get().getType())
            && !userService.findUserDirectory(user.get().getDomain()).get().getLdapUserStatus(user.get().getName())) {
            throw new FailToActivateUser(userService.getThesaurus());
        }
        info.active = true;
        info.id = id;
        transactionService.execute(new UpdateUserTransaction(info, userService, conflictFactory));
        return getUser(info.id);
    }

    @PUT
    @Path("/{id}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE})
    public UserInfos deactivateUser(UserInfo info, @PathParam("id") long id) {
        info.active = false;
        info.id = id;
        transactionService.execute(new UpdateUserTransaction(info, userService, conflictFactory));
        return getUser(info.id);

    }

    private RestQuery<User> getUserRestQuery() {
        Query<User> query = userService.getUserQuery();
        return restQueryService.wrap(query);
    }
}
