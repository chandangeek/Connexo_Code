/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.dataquality.security.Privileges;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Path("/fields")
public class FieldResource {

    private final DataQualityKpiService dataQualityKpiService;
    private final ValidationService validationService;
    private final EstimationService estimationService;
    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public FieldResource(DataQualityKpiService dataQualityKpiService, ValidationService validationService,
                         EstimationService estimationService, MetrologyConfigurationService metrologyConfigurationService) {
        this.dataQualityKpiService = dataQualityKpiService;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @GET
    @Path("/kpiUsagePointGroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_RESULTS})
    public PagedInfoList getUsagePointGroupsWithDataQualityKpi(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = dataQualityKpiService.usagePointDataQualityKpiFinder()
                .stream()
                .filter(kpi -> kpi.getLatestCalculation().isPresent())
                .map(UsagePointDataQualityKpi::getUsagePointGroup)
                .distinct()
                .map(IdWithNameInfo::new)
                .sorted(byNameIgnoreCase())
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("usagePointGroups", infos, queryParameters);
    }

    @GET
    @Path("/kpiMetrologyPurposes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_RESULTS})
    public PagedInfoList getMetrologyPurposesWithDataQualityKpi(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = dataQualityKpiService.usagePointDataQualityKpiFinder()
                .stream()
                .filter(kpi -> kpi.getLatestCalculation().isPresent())
                .map(UsagePointDataQualityKpi::getMetrologyPurpose)
                .distinct()
                .map(IdWithNameInfo::new)
                .sorted(byNameIgnoreCase())
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("metrologyPurposes", infos, queryParameters);
    }

    @GET
    @Path("/metrologyConfigurations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_RESULTS})
    public PagedInfoList getMetrologyConfigurations(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = metrologyConfigurationService.findAllMetrologyConfigurations()
                .stream()
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("metrologyConfigurations", infos, queryParameters);
    }

    @GET
    @Path("/validators")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_RESULTS})
    public PagedInfoList getValidators(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = validationService.getAvailableValidators(QualityCodeSystem.MDM)
                .stream()
                .map(this::asInfo)
                .sorted(byNameIgnoreCase())
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("validators", infos, queryParameters);
    }

    @GET
    @Path("/estimators")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_RESULTS})
    public PagedInfoList getEstimators(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = estimationService.getAvailableEstimators(QualityCodeSystem.MDM)
                .stream()
                .map(this::asInfo)
                .sorted(byNameIgnoreCase())
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("estimators", infos, queryParameters);
    }

    private Comparator<IdWithNameInfo> byNameIgnoreCase() {
        return Comparator.comparing(info -> info.name.toUpperCase());
    }

    private IdWithNameInfo asInfo(Validator validator) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = validator.getClass().getName();
        info.name = validator.getDisplayName();
        return info;
    }

    private IdWithNameInfo asInfo(Estimator estimator) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = estimator.getClass().getName();
        info.name = estimator.getDisplayName();
        return info;
    }
}
