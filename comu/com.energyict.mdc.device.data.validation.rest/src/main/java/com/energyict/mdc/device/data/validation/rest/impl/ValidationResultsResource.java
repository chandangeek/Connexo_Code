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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/validationresults")
public class ValidationResultsResource {

    private final DeviceDataValidationService deviceDataValidationService;

    @Inject
    public ValidationResultsResource(DeviceDataValidationService deviceDataValidationService) {
        this.deviceDataValidationService = deviceDataValidationService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/devicegroups")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, Privileges.Constants.VALIDATE_MANUAL, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getValidationResultsPerDeviceGroup(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) throws JSONException {
        Instant from = null;
        Instant to = null;
        List<Long> deviceGroups = new ArrayList<>();
        JSONArray filters = new JSONArray(uriInfo.getQueryParameters().getFirst("filter"));
        for (int i = 0; i < filters.length(); i++) {
            if (filters.getJSONObject(i).get("property").equals("from")) {
                from = Instant.ofEpochMilli((Long) filters.getJSONObject(i).get("value"));
            }
            if (filters.getJSONObject(i).get("property").equals("to")) {
                to = Instant.ofEpochMilli((Long) filters.getJSONObject(i).get("value"));
            }
            if (filters.getJSONObject(i).get("property").equals("deviceGroups")) {
                List<String> groups = Stream.of(filters.getJSONObject(i).get("value")).map(Object::toString).collect(Collectors.toList());
                String[] list = groups.get(0).substring(1, groups.get(0).length() - 1).split(",");
                Arrays.asList(list).stream().forEach(value -> deviceGroups.add(Long.parseLong(value)));
            }
        }
        Range<Instant> range =  from != null && to != null ? Range.closed(from, to) : Range.all();
        List<ValidationSummaryInfo> data = deviceGroups.stream().flatMap(groupId ->
                deviceDataValidationService
                        .getValidationResultsOfDeviceGroup(groupId, range)
                        .stream()
                        .map(ValidationSummaryInfo::new)).distinct().collect(Collectors.toList());
        return PagedInfoList.fromPagedList("summary", data, queryParameters);
    }

}
