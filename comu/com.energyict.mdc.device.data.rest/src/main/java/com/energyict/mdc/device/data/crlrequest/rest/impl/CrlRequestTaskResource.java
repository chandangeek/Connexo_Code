package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTask;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;
import com.energyict.mdc.device.data.crlrequest.rest.CrlRequestTaskInfo;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import com.energyict.mdc.device.data.security.Privileges;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Path("/crls")
public class CrlRequestTaskResource {
    private final MeteringGroupsService meteringGroupsService;
    private final CrlRequestTaskService crlRequestService;
    private final ExceptionFactory exceptionFactory;
    private final CrlRequestTaskInfoFactory crlRequestTaskInfoFactory;
    private final SecurityManagementService securityManagementService;

    @Inject
    public CrlRequestTaskResource(MeteringGroupsService meteringGroupsService,
                                  CrlRequestTaskService crlRequestService,
                                  ExceptionFactory exceptionFactory,
                                  CrlRequestTaskInfoFactory crlRequestTaskInfoFactory,
                                  SecurityManagementService securityManagementService) {
        this.meteringGroupsService = meteringGroupsService;
        this.crlRequestService = crlRequestService;
        this.exceptionFactory = exceptionFactory;
        this.crlRequestTaskInfoFactory = crlRequestTaskInfoFactory;
        this.securityManagementService = securityManagementService;
    }

    @GET
    @Transactional
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response getAvailableDeviceGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<EndDeviceGroup> allGroups = meteringGroupsService.getEndDeviceGroupQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
        return Response.ok(PagedInfoList.fromPagedList("deviceGroups", allGroups.stream()
                .map(gr -> new IdWithNameInfo(gr.getId(), gr.getName())).collect(Collectors.toList()), queryParameters)).build();
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public PagedInfoList gelAllCrls(@BeanParam JsonQueryParameters queryParameters) {
        List<CrlRequestTaskInfo> infos = crlRequestService.findAllCrlRequestTasks().stream()
                .map(crlRequestTaskInfoFactory::asInfo)
                .collect(toList());
        return PagedInfoList.fromPagedList("crls", infos, queryParameters);
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_CRL_REQUEST, Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public CrlRequestTaskInfo getKpiById(@PathParam("id") long id) {
        return crlRequestTaskInfoFactory.asInfo(crlRequestService.findCrlRequestTask(id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CRL_REQUEST_TASK, id)));
    }

    @DELETE
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response deleteKpi(@PathParam("id") long id, CrlRequestTaskInfo info) {
        CrlRequestTask crlRequestTask = crlRequestService.findCrlRequestTask(id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CRL_REQUEST_TASK, id));
        crlRequestTask.delete();
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response createKpi(CrlRequestTaskInfo info) {
        EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroup(info.deviceGroup.id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_GROUP, info.deviceGroup.id));
        SecurityAccessorType accessorType = securityManagementService.findSecurityAccessorTypeById(info.securityAccessor.id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE, info.deviceGroup.id));

        //todo:
        CrlRequestTaskService.CrlRequestTaskBuilder builder = crlRequestService.newCrlRequestTask();
        builder.withDeviceGroup(endDeviceGroup);
        builder.withCaName(info.caName);
        builder.withFrequency(info.requestFrequency);

        builder.save();

        return Response.status(Response.Status.CREATED).entity(info).build();
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTER_CRL_REQUEST})
    public Response updateKpi(@PathParam("id") long id, CrlRequestTaskInfo info) {
        //todo:
        return Response.status(Response.Status.CREATED).entity(info).build();
    }


}
