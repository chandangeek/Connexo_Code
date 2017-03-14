/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.validation.Validator;

import com.jayway.jsonpath.JsonModel;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FieldResourceTest extends DeviceDataQualityRestApplicationJerseyTest {

    @Test
    public void getUsagePointGroupsWithKpiReturnsOnlyHavingLatestCalculation() {
        UsagePointGroup group_1 = mockUsagePointGroup(1, "G1");
        UsagePointGroup group_2 = mockUsagePointGroup(2, "G2");
        MetrologyPurpose billing = mockMetrologyPurpose(1, "P1");
        UsagePointDataQualityKpi kpi_1 = mockUsagePointDataQualityKpi(group_1, billing, null);
        UsagePointDataQualityKpi kpi_2 = mockUsagePointDataQualityKpi(group_2, billing, Instant.now());

        DataQualityKpiService.UsagePointDataQualityKpiFinder finder = mock(DataQualityKpiService.UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi_1, kpi_2));

        // Business method
        String response = target("/fields/kpiUsagePointGroups").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<Number>>get("$.usagePointGroups[*].id")).containsExactly(2);
        assertThat(jsonModel.<List<String>>get("$.usagePointGroups[*].name")).containsExactly("G2");
    }

    @Test
    public void getUsagePointGroupsWithKpiReturnsOnlyDistinctGroups() {
        UsagePointGroup group_1 = mockUsagePointGroup(1, "G1");
        MetrologyPurpose purpose_1 = mockMetrologyPurpose(1, "P1");
        MetrologyPurpose purpose_2 = mockMetrologyPurpose(2, "P2");
        UsagePointDataQualityKpi kpi_1 = mockUsagePointDataQualityKpi(group_1, purpose_1, Instant.now());
        UsagePointDataQualityKpi kpi_2 = mockUsagePointDataQualityKpi(group_1, purpose_2, Instant.now());

        DataQualityKpiService.UsagePointDataQualityKpiFinder finder = mock(DataQualityKpiService.UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi_1, kpi_2));

        // Business method
        String response = target("/fields/kpiUsagePointGroups").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<Number>>get("$.usagePointGroups[*].id")).containsExactly(1);
        assertThat(jsonModel.<List<String>>get("$.usagePointGroups[*].name")).containsExactly("G1");
    }

    @Test
    public void getUsagePointGroupsWithKpiSorting() {
        UsagePointGroup group_1 = mockUsagePointGroup(1, "G1");
        UsagePointGroup group_2 = mockUsagePointGroup(2, "g2");
        UsagePointGroup group_3 = mockUsagePointGroup(3, "G3");
        MetrologyPurpose purpose_1 = mockMetrologyPurpose(1, "P1");
        UsagePointDataQualityKpi kpi_1 = mockUsagePointDataQualityKpi(group_1, purpose_1);
        UsagePointDataQualityKpi kpi_2 = mockUsagePointDataQualityKpi(group_2, purpose_1);
        UsagePointDataQualityKpi kpi_3 = mockUsagePointDataQualityKpi(group_3, purpose_1);

        DataQualityKpiService.UsagePointDataQualityKpiFinder finder = mock(DataQualityKpiService.UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi_3, kpi_2, kpi_1));

        // Business method
        String response = target("/fields/kpiUsagePointGroups").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<Number>>get("$.usagePointGroups[*].id")).containsExactly(1, 2, 3);
        assertThat(jsonModel.<List<String>>get("$.usagePointGroups[*].name")).containsExactly("G1", "g2", "G3");
    }

    @Test
    public void getUsagePointGroupsWithKpiPagination() {
        UsagePointGroup group_1 = mockUsagePointGroup(1, "G1");
        UsagePointGroup group_2 = mockUsagePointGroup(2, "G2");
        UsagePointGroup group_3 = mockUsagePointGroup(3, "G3");
        MetrologyPurpose purpose_1 = mockMetrologyPurpose(1, "P1");
        UsagePointDataQualityKpi kpi_1 = mockUsagePointDataQualityKpi(group_1, purpose_1);
        UsagePointDataQualityKpi kpi_2 = mockUsagePointDataQualityKpi(group_2, purpose_1);
        UsagePointDataQualityKpi kpi_3 = mockUsagePointDataQualityKpi(group_3, purpose_1);

        DataQualityKpiService.UsagePointDataQualityKpiFinder finder = mock(DataQualityKpiService.UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi_1, kpi_2, kpi_3));

        // Business method
        String response = target("/fields/kpiUsagePointGroups").queryParam("start", 1).queryParam("limit", 1).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<Number>>get("$.usagePointGroups[*].id")).containsExactly(2);
        assertThat(jsonModel.<List<String>>get("$.usagePointGroups[*].name")).containsExactly("G2");
    }

    @Test
    public void getMetrologyPurposesWithKpiReturnsOnlyHavingLatestCalculation() {
        UsagePointGroup group_1 = mockUsagePointGroup(1, "G1");
        MetrologyPurpose purpose_1 = mockMetrologyPurpose(1, "P1");
        MetrologyPurpose purpose_2 = mockMetrologyPurpose(2, "P2");
        UsagePointDataQualityKpi kpi_1 = mockUsagePointDataQualityKpi(group_1, purpose_1, null);
        UsagePointDataQualityKpi kpi_2 = mockUsagePointDataQualityKpi(group_1, purpose_2, Instant.now());

        DataQualityKpiService.UsagePointDataQualityKpiFinder finder = mock(DataQualityKpiService.UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi_1, kpi_2));

        // Business method
        String response = target("/fields/kpiMetrologyPurposes").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<Number>>get("$.metrologyPurposes[*].id")).containsExactly(2);
        assertThat(jsonModel.<List<String>>get("$.metrologyPurposes[*].name")).containsExactly("P2");
    }

    @Test
    public void getMetrologyPurposesWithKpiReturnsOnlyDistinctPurposes() {
        UsagePointGroup group_1 = mockUsagePointGroup(1, "G1");
        UsagePointGroup group_2 = mockUsagePointGroup(2, "G2");
        MetrologyPurpose purpose_1 = mockMetrologyPurpose(1, "P1");
        UsagePointDataQualityKpi kpi_1 = mockUsagePointDataQualityKpi(group_1, purpose_1, Instant.now());
        UsagePointDataQualityKpi kpi_2 = mockUsagePointDataQualityKpi(group_2, purpose_1, Instant.now());

        DataQualityKpiService.UsagePointDataQualityKpiFinder finder = mock(DataQualityKpiService.UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi_1, kpi_2));

        // Business method
        String response = target("/fields/kpiMetrologyPurposes").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<Number>>get("$.metrologyPurposes[*].id")).containsExactly(1);
        assertThat(jsonModel.<List<String>>get("$.metrologyPurposes[*].name")).containsExactly("P1");
    }

    @Test
    public void getMetrologyPurposesWithKpiSorting() {
        UsagePointGroup group_1 = mockUsagePointGroup(1, "G1");
        MetrologyPurpose purpose_1 = mockMetrologyPurpose(1, "P1");
        MetrologyPurpose purpose_2 = mockMetrologyPurpose(2, "p2");
        MetrologyPurpose purpose_3 = mockMetrologyPurpose(3, "P3");
        UsagePointDataQualityKpi kpi_1 = mockUsagePointDataQualityKpi(group_1, purpose_1);
        UsagePointDataQualityKpi kpi_2 = mockUsagePointDataQualityKpi(group_1, purpose_2);
        UsagePointDataQualityKpi kpi_3 = mockUsagePointDataQualityKpi(group_1, purpose_3);

        DataQualityKpiService.UsagePointDataQualityKpiFinder finder = mock(DataQualityKpiService.UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi_3, kpi_2, kpi_1));

        // Business method
        String response = target("/fields/kpiMetrologyPurposes").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<Number>>get("$.metrologyPurposes[*].id")).containsExactly(1, 2, 3);
        assertThat(jsonModel.<List<String>>get("$.metrologyPurposes[*].name")).containsExactly("P1", "p2", "P3");
    }

    @Test
    public void getMetrologyPurposesWithKpiPagination() {
        UsagePointGroup group_1 = mockUsagePointGroup(1, "G1");
        MetrologyPurpose purpose_1 = mockMetrologyPurpose(1, "P1");
        MetrologyPurpose purpose_2 = mockMetrologyPurpose(2, "P2");
        MetrologyPurpose purpose_3 = mockMetrologyPurpose(3, "P3");
        UsagePointDataQualityKpi kpi_1 = mockUsagePointDataQualityKpi(group_1, purpose_1);
        UsagePointDataQualityKpi kpi_2 = mockUsagePointDataQualityKpi(group_1, purpose_2);
        UsagePointDataQualityKpi kpi_3 = mockUsagePointDataQualityKpi(group_1, purpose_3);

        DataQualityKpiService.UsagePointDataQualityKpiFinder finder = mock(DataQualityKpiService.UsagePointDataQualityKpiFinder.class);
        when(dataQualityKpiService.usagePointDataQualityKpiFinder()).thenReturn(finder);
        when(finder.stream()).thenReturn(Stream.of(kpi_1, kpi_2, kpi_3));

        // Business method
        String response = target("/fields/kpiMetrologyPurposes").queryParam("start", 1).queryParam("limit", 1).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<Number>>get("$.metrologyPurposes[*].id")).containsExactly(2);
        assertThat(jsonModel.<List<String>>get("$.metrologyPurposes[*].name")).containsExactly("P2");
    }

    private UsagePointGroup mockUsagePointGroup(Integer id, String name) {
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(usagePointGroup.getId()).thenReturn(id.longValue());
        when(usagePointGroup.getName()).thenReturn(name);
        return usagePointGroup;
    }

    private MetrologyPurpose mockMetrologyPurpose(Integer id, String name) {
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        when(metrologyPurpose.getId()).thenReturn(id.longValue());
        when(metrologyPurpose.getName()).thenReturn(name);
        return metrologyPurpose;
    }

    private UsagePointDataQualityKpi mockUsagePointDataQualityKpi(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose) {
        return mockUsagePointDataQualityKpi(usagePointGroup, metrologyPurpose, Instant.now());
    }

    private UsagePointDataQualityKpi mockUsagePointDataQualityKpi(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose, Instant latestCalculation) {
        UsagePointDataQualityKpi usagePointDataQualityKpi = mock(UsagePointDataQualityKpi.class);
        when(usagePointDataQualityKpi.getUsagePointGroup()).thenReturn(usagePointGroup);
        when(usagePointDataQualityKpi.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(usagePointDataQualityKpi.getLatestCalculation()).thenReturn(Optional.ofNullable(latestCalculation));
        return usagePointDataQualityKpi;
    }

    @Test
    public void getMetrologyConfigurationsWithPagination() {
        MetrologyConfiguration mc1 = mockMetrologyConfiguration(1, "MC1");
        MetrologyConfiguration mc2 = mockMetrologyConfiguration(2, "MC2");
        MetrologyConfiguration mc3 = mockMetrologyConfiguration(3, "MC3");

        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Arrays.asList(mc1, mc2, mc3));

        // Business method
        String response = target("/fields/metrologyConfigurations").queryParam("start", 1).queryParam("limit", 1).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<Number>>get("$.metrologyConfigurations[*].id")).containsExactly(2);
        assertThat(jsonModel.<List<String>>get("$.metrologyConfigurations[*].name")).containsExactly("MC2");

    }

    private MetrologyConfiguration mockMetrologyConfiguration(Integer id, String name) {
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(id.longValue());
        when(metrologyConfiguration.getName()).thenReturn(name);
        return metrologyConfiguration;
    }

    @Test
    public void getValidatorsWithPaginationAndSorting() {
        Validator validator_1 = mockValidator("V1");
        Validator validator_2 = mockValidator("v2");
        Validator validator_3 = mockValidator("V3");
        Validator validator_4 = mockValidator("V4");

        when(validationService.getAvailableValidators(QualityCodeSystem.MDM)).thenReturn(Arrays.asList(validator_4, validator_3, validator_2, validator_1));

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

        when(estimationService.getAvailableEstimators(QualityCodeSystem.MDM)).thenReturn(Arrays.asList(estimator_4, estimator_3, estimator_2, estimator_1));

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