package com.elster.jupiter.users.rest.impl;

import java.util.List;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
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

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.GroupInfos;
import com.elster.jupiter.users.rest.actions.CreateGroupTransaction;
import com.elster.jupiter.users.rest.actions.DeleteGroupTransaction;
import com.elster.jupiter.users.rest.actions.UpdateGroupTransaction;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;

@Path("/groups")
public class GroupResource {

    private final TransactionService transactionService;
    private final UserService userService;
    private final RestQueryService restQueryService;

    @Inject
    public GroupResource(TransactionService transactionService, UserService userService, RestQueryService restQueryService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.restQueryService = restQueryService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public GroupInfos createOrganization(GroupInfo info) {
        GroupInfos result = new GroupInfos();
        result.add(transactionService.execute(new CreateGroupTransaction(info, userService)));
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public GroupInfos deleteGroup(GroupInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new DeleteGroupTransaction(info, userService));
        return new GroupInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public GroupInfos getGroup(@PathParam("id") long id) {
        Optional<Group> group = userService.getGroup(id);
        if (group.isPresent()) {
            return new GroupInfos(group.get());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public GroupInfos getGroups(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Group> list = getGroupRestQuery().select(queryParameters, Order.ascending("name").toLowerCase());
        GroupInfos infos = new GroupInfos(queryParameters.clipToLimit(list));
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public GroupInfos updateGroup(GroupInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdateGroupTransaction(info, userService));
        return getGroup(info.id);
    }

    private RestQuery<Group> getGroupRestQuery() {
        Query<Group> query = userService.getGroupQuery();
        return restQueryService.wrap(query);
    }

}
