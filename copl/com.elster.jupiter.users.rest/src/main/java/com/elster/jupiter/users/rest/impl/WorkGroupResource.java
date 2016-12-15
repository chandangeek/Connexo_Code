package com.elster.jupiter.users.rest.impl;


import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.rest.SimplifiedUserInfo;
import com.elster.jupiter.users.rest.WorkGroupInfo;
import com.elster.jupiter.users.rest.actions.CreateWorkGroupTransaction;
import com.elster.jupiter.users.rest.actions.DeleteWorkGroupTransaction;
import com.elster.jupiter.users.rest.actions.UpdateWorkGroupTransaction;
import com.elster.jupiter.users.security.Privileges;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public PagedInfoList getWorkGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<WorkGroupInfo> infos = userService.getWorkGroups()
                .stream()
                .map(WorkGroupInfo::new)
                .sorted((first,second) -> first.name.toLowerCase().compareTo(second.name.toLowerCase()))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("workGroups", ListPager.of(infos).from(queryParameters).find(), queryParameters);
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
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

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public Response deleteWorkGroup(WorkGroupInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new DeleteWorkGroupTransaction(info, userService));
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_USER_ROLE)
    public WorkGroupInfo updateWorkGroup(WorkGroupInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdateWorkGroupTransaction(info, userService, conflictFactory));
        return getWorkGroup(info.id);
    }

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public PagedInfoList getUsers(@BeanParam JsonQueryParameters queryParameters) {
        List<SimplifiedUserInfo> users = userService.getUsers()
                .stream()
                .sorted((first, second) -> first.getName().toLowerCase().compareTo(second.getName().toLowerCase()))
                .map(SimplifiedUserInfo::new)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("users", users, queryParameters);
    }

    @GET
    @Path("/{id}/members")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE, Privileges.Constants.VIEW_USER_ROLE, com.elster.jupiter.dualcontrol.Privileges.Constants.GRANT_APPROVAL})
    public PagedInfoList getWorkGroupMembers(@BeanParam JsonQueryParameters queryParameters,@PathParam("id") long id) {
        WorkGroup workGroup = userService.getWorkGroup(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<SimplifiedUserInfo> users = workGroup.getUsersInWorkGroup().stream().sorted((first, second) -> first.getName().toLowerCase().compareTo(second.getName().toLowerCase()))
                .map(SimplifiedUserInfo::new)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("users", users, queryParameters);
    }

}
