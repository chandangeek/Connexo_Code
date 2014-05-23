package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.tasks.rest.impl.infos.ActionInfo;
import com.energyict.mdc.tasks.rest.impl.infos.CategoryInfo;
import com.energyict.mdc.tasks.rest.impl.infos.ComTaskInfo;
import com.google.common.base.Optional;

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
import java.util.Arrays;
import java.util.List;

@Path("/comtasks")
public class ComTaskResource extends BaseResource {
    public ComTaskResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getComTasks(@BeanParam QueryParameters queryParameters) {
        List<ComTaskInfo> comTaskInfos =
                ComTaskInfo.from(ListPager.of(getTaskService().findAllComTasks(), new ComTaskComparator()).from(queryParameters).find());
        return PagedInfoList.asJson("data", comTaskInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComTask(@PathParam("id") long id) {
        return Response.status(Response.Status.OK).entity(ComTaskInfo.from(getTaskService().findComTask(id), true)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addComTask(ComTaskInfo comTaskInfo) {
        return Categories.createComTask(getTaskService(), getMasterDataService(), comTaskInfo);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateComTask(@PathParam("id") long id, ComTaskInfo comTaskInfo) {
        return Categories.updateComTask(getTaskService(), getMasterDataService(), comTaskInfo, id);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComTask(@PathParam("id") long id) {
        return Categories.deleteComTask(getTaskService(), id);
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getCategories(@BeanParam QueryParameters queryParameters) {
        List<CategoryInfo> categoryInfos = CategoryInfo.from(ListPager.of(Arrays.asList(Categories.values())).from(queryParameters).find());
        return PagedInfoList.asJson("data", categoryInfos, queryParameters);
    }

    @GET
    @Path("/actions")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getActions(@Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        Optional<String> categoryParameter = Optional.fromNullable(uriInfo.getQueryParameters().getFirst("category"));
        if (categoryParameter.isPresent()) {
            List<ActionInfo> actionInfos = ActionInfo.from(ListPager.of(
                    Categories.valueOf(categoryParameter.get().toUpperCase()).getActions()).from(queryParameters).find());
            return PagedInfoList.asJson("data", actionInfos, queryParameters);
        }
        throw new WebApplicationException("No \"category\" query property is present",
                Response.status(Response.Status.BAD_REQUEST).entity("No \"category\" query property is present").build());
    }
}