package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationResource {

    private final MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory;
    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public MetrologyConfigurationResource(MetrologyConfigurationInfoFactory metrologyConfigurationInfoFactory, MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationInfoFactory = metrologyConfigurationInfoFactory;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getMetrologyConfigurations(@BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointMetrologyConfiguration> allMetrologyConfigurations =
                metrologyConfigurationService
                        .findAllMetrologyConfigurations()
                        .stream()
                        .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                        .map(UsagePointMetrologyConfiguration.class::cast)
                        .collect(Collectors.toList());
        List<MetrologyConfigurationInfo> metrologyConfigurationsInfos = ListPager.of(allMetrologyConfigurations).from(queryParameters).find()
                .stream()
                .map(metrologyConfigurationInfoFactory::asInfo)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("metrologyConfigurations", metrologyConfigurationsInfos, queryParameters);
    }
}