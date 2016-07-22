package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.validation.security.Privileges;

import com.google.common.collect.Range;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Path("/validationresults")
public class ValidationResultsResource {

    private final DeviceDataValidationService deviceDataValidationService;

    @Inject
    public ValidationResultsResource(DeviceDataValidationService deviceDataValidationService) {
        this.deviceDataValidationService = deviceDataValidationService;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/devicegroups/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, Privileges.Constants.VALIDATE_MANUAL, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getValidationResultsPerDeviceGroup(@Context UriInfo uriInf, @BeanParam JsonQueryParameters queryParameters, @PathParam("id") Long groupId)
            throws JSONException {
        Instant from = Instant.ofEpochMilli(Long.parseLong(String.valueOf(new JSONArray(uriInf.getQueryParameters().get("filter").get(0)).getJSONObject(0).get("value"))));
        Instant to = Instant.ofEpochMilli(Long.parseLong(String.valueOf(new JSONArray(uriInf.getQueryParameters().get("filter").get(0)).getJSONObject(1).get("value"))));
        List<ValidationSummaryInfo> data =
                deviceDataValidationService
                        .getValidationResultsOfDeviceGroup(groupId, queryParameters.getStart(), queryParameters.getLimit(), Range.closed(from,to))
                        .stream()
                        .map(ValidationSummaryInfo::new)
                        .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("summary", data, queryParameters);
    }

}
