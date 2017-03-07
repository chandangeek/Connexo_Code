/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UsagePointDataQualityKpiIT extends BaseTestIT {

    public static final Duration ONE_HOUR = Duration.ofHours(1);

    private MetrologyPurpose metrologyPurpose;
    private DataQualityKpiService dataQualityKpiService;

    @Before
    public void setUp() throws Exception {
        dataQualityKpiService = get(DataQualityKpiService.class);
        metrologyPurpose = get(MetrologyConfigurationService.class).findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION).get();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "usagePointGroup", messageId = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    public void createNoUsagePointGroup() {
        // Business method
        dataQualityKpiService.newDataQualityKpi(null, metrologyPurpose, ONE_HOUR);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "frequency", messageId = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    public void createNoCalculationFrequency() {
        UsagePointGroup usagePointGroup = createUsagePointGroup();

        // Business method
        dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, null);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "metrologyPurpose", messageId = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    public void createNoMetrologyPurpose() {
        UsagePointGroup usagePointGroup = createUsagePointGroup();

        // Business method
        dataQualityKpiService.newDataQualityKpi(usagePointGroup, null, ONE_HOUR);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "usagePointGroup", messageId = "{" + MessageSeeds.Constants.USAGEPOINT_GROUP_AND_PURPOSE_MUST_BE_UNIQUE + "}")
    public void createWithAlreadyUsedUsagePointGroupAndPurpose() {
        UsagePointGroup usagePointGroup = createUsagePointGroup();
        dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, ONE_HOUR);

        // Business method
        dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, ONE_HOUR);
    }

    @Test
    @Transactional
    public void createSuccess() {
        UsagePointGroup usagePointGroup = createUsagePointGroup();

        // Business method
        UsagePointDataQualityKpi kpi = dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, ONE_HOUR);

        // Asserts
        Optional<UsagePointDataQualityKpi> found = dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId());
        assertThat(found).isPresent();

        UsagePointDataQualityKpiImpl dataQualityKpi = (UsagePointDataQualityKpiImpl) found.get();
        assertThat(dataQualityKpi.getId()).isEqualTo(kpi.getId());
        assertThat(dataQualityKpi.getVersion()).isEqualTo(kpi.getVersion());
        assertThat(dataQualityKpi.getUsagePointGroup()).isEqualTo(usagePointGroup);
        assertThat(dataQualityKpi.getMetrologyPurpose()).isEqualTo(metrologyPurpose);
        assertThat(dataQualityKpi.getFrequency()).isEqualTo(ONE_HOUR);

        Optional<RecurrentTask> recurrentTask = get(TaskService.class).getRecurrentTask(dataQualityKpi.getRecurrentTaskName());
        assertThat(recurrentTask).isPresent();
        assertThat(recurrentTask.get().getApplication()).isEqualTo("Insight");
        assertThat(recurrentTask.get().getPayLoad()).isEqualTo(dataQualityKpi.getKpiType().recurrentPayload(kpi.getId()));
    }

    @Test
    @Transactional
    public void createUsingUnusedUsagePointGroup() {
        UsagePointGroup usagePointGroup1 = createUsagePointGroup("G1");
        UsagePointGroup usagePointGroup2 = createUsagePointGroup("G2");

        // Business method
        UsagePointDataQualityKpi kpi1 = dataQualityKpiService.newDataQualityKpi(usagePointGroup1, metrologyPurpose, ONE_HOUR);
        UsagePointDataQualityKpi kpi2 = dataQualityKpiService.newDataQualityKpi(usagePointGroup2, metrologyPurpose, ONE_HOUR);

        // Asserts
        List<UsagePointDataQualityKpi> kpis = dataQualityKpiService.usagePointDataQualityKpiFinder().find();
        assertThat(kpis).hasSize(2);
        assertThat(kpis).contains(kpi1, kpi2);
    }

    @Test
    @Transactional
    public void createUsingUnusedMetrologyPurposeAndUsedUsagePointGroup() {
        UsagePointGroup usagePointGroup = createUsagePointGroup();
        MetrologyPurpose information = get(MetrologyConfigurationService.class).findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION).get();
        MetrologyPurpose billing = get(MetrologyConfigurationService.class).findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get();

        // Business method
        UsagePointDataQualityKpi kpi1 = dataQualityKpiService.newDataQualityKpi(usagePointGroup, information, ONE_HOUR);
        UsagePointDataQualityKpi kpi2 = dataQualityKpiService.newDataQualityKpi(usagePointGroup, billing, ONE_HOUR);

        // Asserts
        List<UsagePointDataQualityKpi> kpis = dataQualityKpiService.usagePointDataQualityKpiFinder().find();
        assertThat(kpis).hasSize(2);
        assertThat(kpis).contains(kpi1, kpi2);
    }

    @Test
    @Transactional
    public void delete() {
        UsagePointGroup usagePointGroup = createUsagePointGroup();
        UsagePointDataQualityKpiImpl kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, ONE_HOUR);

        // Business method
        kpi.delete();

        // Asserts
        Optional<UsagePointDataQualityKpi> dataQualityKpi = dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId());
        assertThat(dataQualityKpi).isEmpty();

        Optional<RecurrentTask> recurrentTask = get(TaskService.class).getRecurrentTask(kpi.getRecurrentTaskName());
        assertThat(recurrentTask).isEmpty();
    }

    @Test
    @Transactional
    public void makeObsolete() {
        UsagePointGroup usagePointGroup = createUsagePointGroup();
        UsagePointDataQualityKpiImpl kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, ONE_HOUR);

        // Business method
        kpi.makeObsolete();

        // Asserts
        Optional<UsagePointDataQualityKpi> dataQualityKpi = dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId());
        assertThat(dataQualityKpi).isPresent();
        assertThat(dataQualityKpi.get().getObsoleteTime()).isPresent();

        List<UsagePointDataQualityKpi> found = dataQualityKpiService.usagePointDataQualityKpiFinder().forGroup(usagePointGroup).forPurpose(metrologyPurpose).find();
        assertThat(found).isEmpty();

        Optional<RecurrentTask> recurrentTask = get(TaskService.class).getRecurrentTask(kpi.getRecurrentTaskName());
        assertThat(recurrentTask).isEmpty();
    }

    private UsagePointGroup createUsagePointGroup() {
        return createUsagePointGroup("UPG");
    }

    private UsagePointGroup createUsagePointGroup(String name) {
        return get(MeteringGroupsService.class).createEnumeratedUsagePointGroup().setName(name).create();
    }
}
