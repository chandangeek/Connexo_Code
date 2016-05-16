package com.elster.jupiter.validation.rest.impl;



import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.validation.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/field")
public class MetrologyConfigurationsAndContractsResource {


    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public MetrologyConfigurationsAndContractsResource(MetrologyConfigurationService metrologyConfigurationService) {

        this.metrologyConfigurationService = metrologyConfigurationService;
    }


    @GET
    @Path("/metrologyconfigurations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public PagedInfoList getMetrologyConfigurations(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = metrologyConfigurationService.findAllMetrologyConfigurations()
                .stream()
                .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("metrologyConfigurations", infos, queryParameters);
    }

    @GET
    @Path("/metrologyconfigurations/{id}/contracts")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION})
    public PagedInfoList getMetrologyContracts(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = metrologyConfigurationService.findMetrologyConfiguration(id).get().getContracts()
                .stream()
                .map(mc -> new IdWithNameInfo(mc.getId(), mc.getMetrologyPurpose().getName()))
                .collect(Collectors.toList());

        return PagedInfoList.fromCompleteList("metrologyContracts", infos, queryParameters);
    }
}
