package com.elster.jupiter.users.rest.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.rest.WorkGroupInfo;
import com.elster.jupiter.users.rest.actions.CreateWorkGroupTransaction;
import com.elster.jupiter.users.security.Privileges;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

@Path("/workgroups")
public class WorkGroupResource {

    private final TransactionService transactionService;
    private final UserService userService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final RestQueryService restQueryService;

    @Inject
    public WorkGroupResource(TransactionService transactionService, UserService userService, ConcurrentModificationExceptionFactory conflictFactory, RestQueryService restQueryService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.conflictFactory = conflictFactory;
        this.restQueryService = restQueryService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public PagedInfoList getWorkGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<WorkGroupInfo> infos = userService.getWorkGroups()
                .stream()
                .map(WorkGroupInfo::new)
                .sorted((first,second) -> first.name.compareTo(second.name))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("workGroups", infos, queryParameters);
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE})
    public WorkGroupInfo getWorkGroup(@PathParam("id") long id) {
        return userService.getWorkGroup(id).map(WorkGroupInfo::new).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public WorkGroupInfo createWorkGroup(WorkGroupInfo info) {
        return new WorkGroupInfo(transactionService.execute(new CreateWorkGroupTransaction(info, userService)));
    }

}
