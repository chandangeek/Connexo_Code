package com.elster.insight.usagepoint.config.rest.impl;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationResource {

    private final TransactionService transactionService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final Clock clock;

    @Inject
    public MetrologyConfigurationResource(TransactionService transactionService, Clock clock, UsagePointConfigurationService usagePointConfigurationService) {
        this.transactionService = transactionService;
        this.clock = clock;
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    //    @Consumes(MediaType.APPLICATION_JSON)
    // not protected by privileges yet because a combo-box containing all the groups needs to be shown when creating an export task
    public PagedInfoList getMeterologyConfigurations(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<MetrologyConfiguration> allMetrologyConfigurations = usagePointConfigurationService.findAllMetrologyConfigurations();
        List<MetrologyConfigurationInfo> metrologyConfigurationsInfos = ListPager.of(allMetrologyConfigurations).from(queryParameters).stream().map(m -> new MetrologyConfigurationInfo(m))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("metrologyconfigurations", metrologyConfigurationsInfos, queryParameters);
    }
    
    @GET
//    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MetrologyConfigurationInfo getMeterologyConfiguration(@PathParam("id") long id, @Context SecurityContext securityContext) {
        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return new MetrologyConfigurationInfo(metrologyConfiguration);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    //    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public MetrologyConfigurationInfo createMetrologyConfiguration(MetrologyConfigurationInfo metrologyConfigurationInfo) {
        MetrologyConfiguration metrologyConfiguration = transactionService.execute(new Transaction<MetrologyConfiguration>() {
            @Override
            public MetrologyConfiguration perform() {
                return usagePointConfigurationService.newMetrologyConfiguration(metrologyConfigurationInfo.name);
            }
        });
        return new MetrologyConfigurationInfo(metrologyConfiguration);
    }

    @PUT
    //  @RolesAllowed({Privileges.ADMIN_OWN, Privileges.ADMIN_ANY})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MetrologyConfigurationInfo updateMetrologyConfiguration(@PathParam("id") long id, MetrologyConfigurationInfo metrologyConfigurationInfo, @Context SecurityContext securityContext) {
        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        
        MetrologyConfiguration updatedMetrologyConfiguration = transactionService.execute(new Transaction<MetrologyConfiguration>() {
            @Override
            public MetrologyConfiguration perform() {
                metrologyConfigurationInfo.writeTo(metrologyConfiguration);
                metrologyConfiguration.save();
                return metrologyConfiguration;
            }
        });
        
        
        return new MetrologyConfigurationInfo(updatedMetrologyConfiguration);
    }
    
}