package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.actions.CreateGroupTransaction;
import com.elster.jupiter.users.rest.actions.DeleteGroupTransaction;
import com.elster.jupiter.users.rest.actions.UpdateGroupTransaction;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/groups")
public class GroupResource {

    private final TransactionService transactionService;
    private final UserService userService;
    private final RestQueryService restQueryService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final NlsService nlsService;
    private final GroupInfoFactory groupInfoFactory;

    @Inject
    public GroupResource(TransactionService transactionService, UserService userService, RestQueryService restQueryService, ConcurrentModificationExceptionFactory conflictFactory, NlsService nlsService, GroupInfoFactory groupInfoFactory) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.restQueryService = restQueryService;
        this.conflictFactory = conflictFactory;
        this.nlsService = nlsService;
        this.groupInfoFactory = groupInfoFactory;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public GroupInfo createOrganization(GroupInfo info) {
        return groupInfoFactory.from(nlsService, transactionService.execute(new CreateGroupTransaction(info, userService)));
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public GroupInfo deleteGroup(GroupInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new DeleteGroupTransaction(info, userService, conflictFactory));
        return new GroupInfo();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public GroupInfo getGroup(@PathParam("id") long id) {
        Optional<Group> group = userService.getGroup(id);
        if (group.isPresent()) {
            return groupInfoFactory.from(this.nlsService, group.get());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public PagedInfoList getGroups(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters jsonQueryParameters) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Group> list = getGroupRestQuery().select(queryParameters, Order.ascending("name").toLowerCase());
        List<GroupInfo> groupInfos = list.stream()
                .map(group -> groupInfoFactory.from(nlsService, group))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("groups", groupInfos, jsonQueryParameters);
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public GroupInfo updateGroup(GroupInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdateGroupTransaction(info, userService, conflictFactory));
        return getGroup(info.id);
    }

    private RestQuery<Group> getGroupRestQuery() {
        Query<Group> query = userService.getGroupQuery();
        return restQueryService.wrap(query);
    }
}
