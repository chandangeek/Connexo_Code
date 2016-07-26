package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.validation.security.Privileges;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;

import com.google.common.collect.Range;
import org.json.JSONArray;
import org.json.JSONException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
    public PagedInfoList getValidationResultsPerDeviceGroup(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @PathParam("id") Long groupId) throws JSONException {
        Instant from = null;
        Instant to = null;
        JSONArray filters = new JSONArray(uriInfo.getQueryParameters().getFirst("filter"));
        for(int i = 0; i < filters.length(); i++){
            if(filters.getJSONObject(i).get("property").equals("from")){
                from = Instant.ofEpochMilli((Long) filters.getJSONObject(i).get("value"));
            }else if(filters.getJSONObject(i).get("property").equals("to")){
                to = Instant.ofEpochMilli((Long) filters.getJSONObject(i).get("value"));
            }
        }
        List<ValidationSummaryInfo> data =
                deviceDataValidationService
                        .getValidationResultsOfDeviceGroup(groupId, from != null && to != null ? Range.closed(from, to) : Range.all())
                        .stream()
                        .map(ValidationSummaryInfo::new)
                        .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("summary", data, queryParameters);
    }

}
