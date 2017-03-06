/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.security.Privileges;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.energyict.mdc.device.config.DeviceConfigurationService;

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
    private final DeviceConfigurationService deviceConfigurationService;
    private final ValidationService validationService;
    private final EstimationService estimationService;

    @Inject
    public FieldResource(DataQualityKpiService dataQualityKpiService, DeviceConfigurationService deviceConfigurationService,
                         ValidationService validationService, EstimationService estimationService) {
        this.dataQualityKpiService = dataQualityKpiService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.validationService = validationService;
        this.estimationService = estimationService;
    }

    @GET
    @Transactional
    @Path("/kpiDeviceGroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_RESULTS})
    public PagedInfoList getDeviceGroupsWithDataQualityKpi(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = dataQualityKpiService.deviceDataQualityKpiFinder()
                .stream()
                .filter(kpi -> kpi.getLatestCalculation().isPresent())
                .map(DeviceDataQualityKpi::getDeviceGroup)
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("deviceGroups", infos, queryParameters);
    }

    @GET
    @Transactional
    @Path("/deviceTypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_RESULTS})
    public PagedInfoList getDeviceTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = deviceConfigurationService.findAllDeviceTypes()
                .from(queryParameters)
                .stream()
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceTypes", infos, queryParameters);
    }

    @GET
    @Transactional
    @Path("/validators")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_RESULTS})
    public PagedInfoList getValidators(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = validationService.getAvailableValidators(QualityCodeSystem.MDC)
                .stream()
                .map(this::asInfo)
                .sorted(Comparator.comparing(info -> info.name.toUpperCase()))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("validators", infos, queryParameters);
    }

    @GET
    @Transactional
    @Path("/estimators")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_RESULTS})
    public PagedInfoList getEstimators(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> infos = estimationService.getAvailableEstimators(QualityCodeSystem.MDC)
                .stream()
                .map(this::asInfo)
                .sorted(Comparator.comparing(info -> info.name.toUpperCase()))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("estimators", infos, queryParameters);
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
