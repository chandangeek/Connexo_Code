package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.GroupInfos;
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

@Path("groups")
public class GroupResource {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos createOrganization(GroupInfo info) {
        GroupInfos result = new GroupInfos();
        result.add(Bus.getTransactionService().execute(new CreateGroupTransaction(info)));
        return result;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos deleteGroup(GroupInfo info, @PathParam("id") long id) {
        info.id = id;
        Bus.getTransactionService().execute(new DeleteGroupTransaction(info));
        return new GroupInfos();
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos getGroup(@PathParam("id") long id) {
        Optional<Group> group = Bus.getUserService().getGroup(id);
        if (group.isPresent()) {
            return new GroupInfos(group.get());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GroupInfos getGroups(@Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Group> list = getGroupRestQuery().select(queryParameters);
        GroupInfos infos = new GroupInfos(list);
        infos.total = queryParameters.determineTotal(list.size());
        return infos;
    }

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public GroupInfos updateGroup(GroupInfo info, @PathParam("id") long id) {
        info.id = id;
        Bus.getTransactionService().execute(new UpdateGroupTransaction(info));
        return getGroup(info.id);
    }


    private RestQuery<Group> getGroupRestQuery() {
        Query<Group> query = Bus.getUserService().getGroupQuery();
        return Bus.getRestQueryService().wrap(query);
    }


}
