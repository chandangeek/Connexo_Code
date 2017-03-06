/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.validation.Validator;
import com.energyict.mdc.device.config.DeviceType;

import com.jayway.jsonpath.JsonModel;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FieldResourceTest extends DeviceDataQualityRestApplicationJerseyTest {

    @Test
    public void getDeviceGroupsWithPagination() {
        DeviceDataQualityKpi kpi_1 = mockDeviceDataQualityKpi(1L, "G1", null);
        DeviceDataQualityKpi kpi_2 = mockDeviceDataQualityKpi(2L, "G2", Instant.now());
        DeviceDataQualityKpi kpi_3 = mockDeviceDataQualityKpi(3L, "G3", Instant.now());
        DeviceDataQualityKpi kpi_4 = mockDeviceDataQualityKpi(4L, "G4", Instant.now());
        DeviceDataQualityKpi kpi_5 = mockDeviceDataQualityKpi(5L, "G5", Instant.now());

        DataQualityKpiService.DeviceDataQualityKpiFinder finder = mock(DataQualityKpiService.DeviceDataQualityKpiFinder.class);
        when(dataQualityKpiService.deviceDataQualityKpiFinder()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi_1, kpi_2, kpi_3, kpi_4, kpi_5));

        // Business method
        String response = target("/fields/kpiDeviceGroups").queryParam("start", 1).queryParam("limit", 2).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(4);
        assertThat(jsonModel.<List<Number>>get("$.deviceGroups[*].id")).containsExactly(3, 4);
        assertThat(jsonModel.<List<String>>get("$.deviceGroups[*].name")).containsExactly("G3", "G4");
    }

    private DeviceDataQualityKpi mockDeviceDataQualityKpi(long deviceGroupId, String deviceGroupName, Instant latestCalculation) {
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(deviceGroupId);
        when(endDeviceGroup.getName()).thenReturn(deviceGroupName);
        DeviceDataQualityKpi deviceDataQualityKpi = mock(DeviceDataQualityKpi.class);
        when(deviceDataQualityKpi.getDeviceGroup()).thenReturn(endDeviceGroup);
        when(deviceDataQualityKpi.getLatestCalculation()).thenReturn(Optional.ofNullable(latestCalculation));
        return deviceDataQualityKpi;
    }

    @Test
    public void getDeviceTypes() {
        DeviceType dt_1 = mockDeviceType(1L, "DT1");
        DeviceType dt_2 = mockDeviceType(2L, "DT2");
        DeviceType dt_3 = mockDeviceType(3L, "DT3");
        Finder<DeviceType> deviceTypeFinder = mock(Finder.class);
        when(deviceTypeFinder.from(any())).thenReturn(deviceTypeFinder);
        when(deviceTypeFinder.stream()).thenReturn(Stream.of(dt_1, dt_2, dt_3));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(deviceTypeFinder);

        // Business method
        String response = target("/fields/deviceTypes").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<Number>>get("$.deviceTypes[*].id")).containsExactly(1, 2, 3);
        assertThat(jsonModel.<List<String>>get("$.deviceTypes[*].name")).containsExactly("DT1", "DT2", "DT3");
    }

    private DeviceType mockDeviceType(long deviceTypeId, String deviceTypeName) {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(deviceTypeId);
        when(deviceType.getName()).thenReturn(deviceTypeName);
        return deviceType;
    }

    @Test
    public void getValidatorsWithPaginationAndSorting() {
        Validator validator_1 = mockValidator("V1");
        Validator validator_2 = mockValidator("v2");
        Validator validator_3 = mockValidator("V3");
        Validator validator_4 = mockValidator("V4");

        when(validationService.getAvailableValidators(QualityCodeSystem.MDC)).thenReturn(Arrays.asList(validator_4, validator_2, validator_3, validator_1));

        // Business method
        String response = target("/fields/validators").queryParam("start", 1).queryParam("limit", 2).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(4);
        assertThat(jsonModel.<List<String>>get("$.validators[*].id")).containsExactly(validator_2.getClass().getName(), validator_3.getClass().getName());
        assertThat(jsonModel.<List<String>>get("$.validators[*].name")).containsExactly("v2", "V3");
    }

    private Validator mockValidator(String name) {
        Validator validator = mock(Validator.class);
        when(validator.getDisplayName()).thenReturn(name);
        return validator;
    }

    @Test
    public void getEstimatorsWithPaginationAndSorting() {
        Estimator estimator_1 = mockEstimator("E1");
        Estimator estimator_2 = mockEstimator("e2");
        Estimator estimator_3 = mockEstimator("E3");
        Estimator estimator_4 = mockEstimator("E4");

        when(estimationService.getAvailableEstimators(QualityCodeSystem.MDC)).thenReturn(Arrays.asList(estimator_4, estimator_2, estimator_3, estimator_1));

        // Business method
        String response = target("/fields/estimators").queryParam("start", 1).queryParam("limit", 2).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(4);
        assertThat(jsonModel.<List<String>>get("$.estimators[*].id")).containsExactly(estimator_2.getClass().getName(), estimator_2.getClass().getName());
        assertThat(jsonModel.<List<String>>get("$.estimators[*].name")).containsExactly("e2", "E3");
    }

    private Estimator mockEstimator(String name) {
        Estimator estimator = mock(Estimator.class);
        when(estimator.getDisplayName()).thenReturn(name);
        return estimator;
    }
}