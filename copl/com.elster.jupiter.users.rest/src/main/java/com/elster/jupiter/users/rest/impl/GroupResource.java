package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.GroupInfos;
import com.elster.jupiter.users.rest.UserInGroupInfo;
import com.elster.jupiter.users.rest.actions.*;
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

@Path("/groups")
public class GroupResource {

    private final TransactionService transactionService;
    private final UserService userService;

    @Inject
    public GroupResource(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos createOrganization(GroupInfo info) {
        GroupInfos result = new GroupInfos();
        result.add(transactionService.execute(new CreateGroupTransaction(info, userService)));
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos deleteGroup(@PathParam("id") long id) {
        GroupInfo info = new GroupInfo();
        info.id = id;
        transactionService.execute(new DeleteGroupTransaction(info, userService));
        return new GroupInfos();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos getGroup(@PathParam("id") long id) {
        Optional<Group> group = userService.getGroup(id);
        if (group.isPresent()) {
            return new GroupInfos(group.get());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos getGroups(@Context UriInfo uriInfo) {
    	return new GroupInfos(userService.getGroups());
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public GroupInfos updateGroup(GroupInfo info, @PathParam("id") long id) {
        info.id = id;
        transactionService.execute(new UpdateGroupTransaction(info, userService));
        return getGroup(info.id);
    }

    @POST
    @Path("/{idgroup}/users/{iduser}")
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos joinGroup(@PathParam("idgroup") long idGroup, @PathParam("iduser") long idUser) {
        UserInGroupInfo info = new UserInGroupInfo();
        info.userId = idUser;

        info.groupInfo = new GroupInfo();
        info.groupInfo.id = idGroup;

        GroupInfos result = new GroupInfos();
        result.addAll(transactionService.execute(new AddUserToGroupTransaction(info, userService)));

        return result;
    }

    @DELETE
    @Path("/{idgroup}/users/{iduser}")
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos leaveGroup(@PathParam("idgroup") long idGroup, @PathParam("iduser") long idUser) {
        UserInGroupInfo info = new UserInGroupInfo();
        info.userId = idUser;

        info.groupInfo = new GroupInfo();
        info.groupInfo.id = idGroup;

        GroupInfos result = new GroupInfos();
        result.addAll(transactionService.execute(new DeleteUserFromGroupTransaction(info, userService)));

        return result;
    }

    @GET
    @Path("/users/{iduser}")
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos userMembership(@PathParam("iduser") long idUser) {
        Optional<User> foundUser = userService.getUser(idUser);
        if(!foundUser.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        GroupInfos result = new GroupInfos();
        result.addAll(foundUser.get().getGroups());

        return result;
    }

}
