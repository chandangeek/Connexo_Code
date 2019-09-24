/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.device.LocationShortInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.search.location.SearchLocationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.issue.rest.request.RequestHelper.LIKE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/locations")
public class LocationResource extends BaseResource {

    private final SearchLocationService searchLocationService;

    @Inject
    public LocationResource(SearchLocationService searchLocationService) {
        this.searchLocationService = searchLocationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public Response getLocations(@BeanParam StandardParametersBean params) {
        validateMandatory(params, START, LIMIT);
        String searchText = params.getFirst(LIKE);
        List<LocationShortInfo> locationsInfo = new ArrayList<>();
        searchLocationService.findLocations(searchText).forEach((id, name) -> locationsInfo.add(new LocationShortInfo(id, name)));
        return entity(locationsInfo, LocationShortInfo.class, params.getStart(), params.getLimit()).build();
    }
}
