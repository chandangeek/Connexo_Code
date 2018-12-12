/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.dataquality.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.device.dataquality.DataQualityOverviews;
import com.energyict.mdc.device.dataquality.DeviceDataQualityService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.dataquality.DeviceDataQualityService.DataQualityOverviewBuilder;

@Path("/dataQualityResults")
public class DataQualityResultsResource {

    private final DeviceDataQualityService deviceDataQualityService;
    private final DataQualityOverviewInfoFactory dataQualityOverviewInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public DataQualityResultsResource(DeviceDataQualityService deviceDataQualityService,
                                      DataQualityOverviewInfoFactory dataQualityOverviewInfoFactory,
                                      ResourceHelper resourceHelper) {
        this.deviceDataQualityService = deviceDataQualityService;
        this.dataQualityOverviewInfoFactory = dataQualityOverviewInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_RESULTS})
    public PagedInfoList getDataQualityResults(@BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        DataQualityOverviewBuilder overviewBuilder = deviceDataQualityService.forAllDevices();
        if (jsonQueryFilter != null) {
            for (DataQualityOverviewFilter filter : DataQualityOverviewFilter.values()) {
                filter.applyIfPresent(jsonQueryFilter, overviewBuilder, resourceHelper);
            }
        }
        DataQualityOverviews dataQualityOverviews = overviewBuilder.paged(this.getPageStart(queryParameters), this.getPageEnd(queryParameters));
        return PagedInfoList.fromPagedList("dataQualityResults", this.toInfos(dataQualityOverviews), queryParameters);
    }

    private Integer getPageStart(JsonQueryParameters queryParameters) {
        return queryParameters.getStart().orElse(0) + 1;
    }

    private Integer getPageEnd(JsonQueryParameters queryParameters) {
        return queryParameters
                .getLimit()
                .map(limit -> this.getPageStart(queryParameters) + limit)
                .orElse(Integer.MAX_VALUE);
    }

    private List<DataQualityOverviewInfo> toInfos(DataQualityOverviews dataQualityOverviews) {
        return dataQualityOverviews
                .allOverviews()
                .stream()
                .map(dataQualityOverviewInfoFactory::asInfo)
                .collect(Collectors.toList());
    }
}
