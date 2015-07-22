package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.validation.security.Privileges;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;
import com.energyict.mdc.device.data.validation.ValidationOverview;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

@Path("/validationresults")
public class ValidationResultsResource {

    private final DeviceDataValidationService deviceDataValidationService;

    @Inject
    public ValidationResultsResource(DeviceDataValidationService deviceDataValidationService) {
        this.deviceDataValidationService = deviceDataValidationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/devicegroups/{id}")
    @RolesAllowed({Privileges.VIEW_VALIDATION_CONFIGURATION, Privileges.VALIDATE_MANUAL, Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getValidationResultsPerDeviceGroup(@Context UriInfo uriInf, @BeanParam JsonQueryParameters queryParameters, @PathParam("id") Long groupId) {

        List<ValidationOverview> list = deviceDataValidationService.getValidationResultsOfDeviceGroup(groupId, queryParameters.getStart(), queryParameters.getLimit());

        List<ValidationSummaryInfo> data = list.stream().map(each -> new ValidationSummaryInfo(each)).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("summary", data, queryParameters);

    }


}
