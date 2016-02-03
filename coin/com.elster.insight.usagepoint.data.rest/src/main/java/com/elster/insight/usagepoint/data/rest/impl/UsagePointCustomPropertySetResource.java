package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UsagePointCustomPropertySetResource {

    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final ResourceHelper resourceHelper;
    private final UsagePointConfigurationService usagePointConfigurationService;

    @Inject
    public UsagePointCustomPropertySetResource(CustomPropertySetInfoFactory customPropertySetInfoFactory, ResourceHelper resourceHelper, UsagePointConfigurationService usagePointConfigurationService){

        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.resourceHelper = resourceHelper;
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @GET
    @Path("/metrology")
    public PagedInfoList getMetrologyCustomPropertySetsWithValues(@PathParam("mrid") String usagePointMrid,
                                                                  @BeanParam JsonQueryParameters queryParameters){
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(usagePointMrid);
        Optional<MetrologyConfiguration> linkedMetrologyConfiguration = usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(usagePoint);
        List<CustomPropertySetInfo> infos = Collections.emptyList();
        if (linkedMetrologyConfiguration.isPresent()){

        }
        return PagedInfoList.fromCompleteList("customPropertySets", infos, queryParameters);
    }
}
