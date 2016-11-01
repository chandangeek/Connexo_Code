package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/field")
public class FieldResouce {

    private final MeteringGroupsService meteringGroupsService;

    @Inject
    public FieldResouce(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @GET
    @Path("/usagepointgroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_ESTIMATION_CONFIGURATION, Privileges.Constants.VIEW_ESTIMATION_CONFIGURATION})
    public PagedInfoList getUsagePointGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithDisplayValueInfo> infos = meteringGroupsService.findUsagePointGroups()
                .stream()
                .map(upg -> new IdWithDisplayValueInfo<>(upg.getId(), upg.getName()))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("usagePointGroups", infos, queryParameters);
    }
}
