package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

@Path("/devices/{mrid}/comtaskexecutions")
public class ComTaskExecutionResource {

    private final ComTaskExecutionInfoFactory comTaskExecutionInfoFactory;
    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ComTaskExecutionResource(DeviceService deviceService, ComTaskExecutionInfoFactory comTaskExecutionInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceService = deviceService;
        this.comTaskExecutionInfoFactory = comTaskExecutionInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{comTaskExecutionId}")
    public ComTaskExecutionInfo getComTaskExecution(@PathParam("mrid") String mRID, @PathParam("comTaskExecutionId") long comTaskExecutionId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return deviceService.findByUniqueMrid(mRID)
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                 .getComTaskExecutions().stream()
                 .filter(comTaskExecution -> comTaskExecution.getId()==comTaskExecutionId)
                 .findFirst()
                 .map(ct -> comTaskExecutionInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_COM_TASK_EXECUTION));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public PagedInfoList<ComTaskExecutionInfo> getComTaskExecutions(@PathParam("mrid") String mRID, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ComTaskExecutionInfo> infoList = deviceService.findByUniqueMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                .getComTaskExecutions().stream()
                .map(cte -> comTaskExecutionInfoFactory.from(cte, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ComTaskExecutionResource.class)
                .resolveTemplate("mrid", mRID);
        return PagedInfoList.from(infoList, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public List<String> getFields() {
        return comTaskExecutionInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
