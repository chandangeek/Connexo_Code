package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/13/15.
 */
@Path("devices/{mrid}/connectionmethods")
public class ConnectionTaskResource {

    private final DeviceService deviceService;
    private final ConnectionTaskInfoFactory connectionTaskInfoFactory;
    private final ConnectionTaskService connectionTaskService;

    @Inject
    public ConnectionTaskResource(DeviceService deviceService, ConnectionTaskInfoFactory connectionTaskInfoFactory, ConnectionTaskService connectionTaskService) {
        this.deviceService = deviceService;
        this.connectionTaskInfoFactory = connectionTaskInfoFactory;
        this.connectionTaskService = connectionTaskService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getConnectionMethods(@PathParam("mrid") String mrid,
                                         @Context UriInfo uriInfo,
                                         @BeanParam FieldSelection fieldSelection,
                                         @BeanParam JsonQueryParameters queryParameters) {
        Device device = deviceService.findByUniqueMrid(mrid).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        List<ConnectionTaskInfo> infos = ListPager.
                of(device.getConnectionTasks(), (a, b) -> a.getName().compareTo(b.getName())).
                from(queryParameters).stream().
                map(ct -> connectionTaskInfoFactory.asHypermedia(ct, uriInfo, fieldSelection.getFields())).
                collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(ConnectionTaskResource.class).resolveTemplate("mrid", device.getmRID());
        return Response.ok(PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{id}")
    public Response getConnectionTask(@PathParam("mrid") String mrid,
                                        @PathParam("id") long id,
                                         @Context UriInfo uriInfo,
                                         @BeanParam FieldSelection fieldSelection,
                                         @BeanParam JsonQueryParameters queryParameters) {
        ConnectionTask<?,?> connectionTask = connectionTaskService.findConnectionTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        if (!connectionTask.getDevice().getmRID().equals(mrid)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode());
        }
        ConnectionTaskInfo info = connectionTaskInfoFactory.asHypermedia(connectionTask, uriInfo, fieldSelection.getFields());
        return Response.ok(info).build();
    }


}
