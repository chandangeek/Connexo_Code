/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.impl.calc.DataQualityKpiMemberType;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.Range;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceDataQualityKpiIT extends BaseTestIT {

    public static final Duration ONE_HOUR = Duration.ofHours(1);

    private DataQualityKpiService dataQualityKpiService;

    private ValidationService validationService;
    private EstimationService estimationService;

    @Mock
    private Validator validator;
    @Mock
    private Estimator estimator;

    @Before
    public void setUp() {
        dataQualityKpiService = get(DataQualityKpiService.class);
        validationService = get(ValidationService.class);
        estimationService = get(EstimationService.class);
    }

    @After
    public void tearDown() {
        Mockito.reset(validationService, estimationService);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "deviceGroup", messageId = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    public void createNoDeviceGroup() {
        // Business method
        dataQualityKpiService.newDataQualityKpi(null, ONE_HOUR);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "frequency", messageId = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    public void createNoCalculationFrequency() {
        EndDeviceGroup endDeviceGroup = createEndDeviceGroup();

        // Business method
        dataQualityKpiService.newDataQualityKpi(endDeviceGroup, null);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "deviceGroup", messageId = "{" + MessageSeeds.Constants.DEVICE_GROUP_MUST_BE_UNIQUE + "}")
    public void createWithAlreadyUsedDeviceGroup() {
        EndDeviceGroup endDeviceGroup = createEndDeviceGroup();
        dataQualityKpiService.newDataQualityKpi(endDeviceGroup, ONE_HOUR);

        // Business method
        dataQualityKpiService.newDataQualityKpi(endDeviceGroup, ONE_HOUR);
    }

    @Test
    @Transactional
    public void createSuccess() {
        EndDeviceGroup endDeviceGroup = createEndDeviceGroup();

        // Business method
        DeviceDataQualityKpi kpi = dataQualityKpiService.newDataQualityKpi(endDeviceGroup, ONE_HOUR);

        // Asserts
        Optional<DeviceDataQualityKpi> found = dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId());
        assertThat(found).isPresent();

        DeviceDataQualityKpiImpl dataQualityKpi = (DeviceDataQualityKpiImpl) found.get();
        assertThat(dataQualityKpi.getId()).isEqualTo(kpi.getId());
        assertThat(dataQualityKpi.getVersion()).isEqualTo(kpi.getVersion());
        assertThat(dataQualityKpi.getDeviceGroup()).isEqualTo(endDeviceGroup);
        assertThat(dataQualityKpi.getFrequency()).isEqualTo(ONE_HOUR);

        Optional<RecurrentTask> recurrentTask = get(TaskService.class).getRecurrentTask(dataQualityKpi.getRecurrentTaskName());
        assertThat(recurrentTask).isPresent();
        assertThat(recurrentTask.get().getApplication()).isEqualTo("MultiSense");
        assertThat(recurrentTask.get().getPayLoad()).isEqualTo(dataQualityKpi.getKpiType().recurrentPayload(kpi.getId()));
    }

    @Test
    @Transactional
    public void createUsingUnusedDeviceGroup() {
        EndDeviceGroup deviceGroup1 = createEndDeviceGroup("G1");
        EndDeviceGroup deviceGroup2 = createEndDeviceGroup("G2");

        // Business method
        DeviceDataQualityKpi kpi1 = dataQualityKpiService.newDataQualityKpi(deviceGroup1, ONE_HOUR);
        DeviceDataQualityKpi kpi2 = dataQualityKpiService.newDataQualityKpi(deviceGroup2, ONE_HOUR);

        // Asserts
        List<DeviceDataQualityKpi> kpis = dataQualityKpiService.deviceDataQualityKpiFinder().find();
        assertThat(kpis).hasSize(2);
        assertThat(kpis).contains(kpi1, kpi2);
    }

    @Test
    @Transactional
    public void delete() {
        EndDeviceGroup endDeviceGroup = createEndDeviceGroup();
        DeviceDataQualityKpiImpl kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(endDeviceGroup, ONE_HOUR);

        // Business method
        kpi.delete();

        // Asserts
        Optional<DeviceDataQualityKpi> dataQualityKpi = dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId());
        assertThat(dataQualityKpi).isEmpty();

        Optional<RecurrentTask> recurrentTask = get(TaskService.class).getRecurrentTask(kpi.getRecurrentTaskName());
        assertThat(recurrentTask).isEmpty();
    }

    @Test
    @Transactional
    public void updateMembersEmptyDeviceGroup() {
        EndDeviceGroup endDeviceGroup = createEndDeviceGroup();
        DeviceDataQualityKpiImpl kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(endDeviceGroup, ONE_HOUR);

        // Business method
        kpi.updateMembers();

        // Asserts
        kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).isEmpty();
    }

    @Test
    @Transactional
    public void updateMembers() {
        Meter meter = createMeter("SPE0001");
        EndDeviceGroup endDeviceGroup = createEndDeviceGroup("HasOneMeter", meter);
        DeviceDataQualityKpiImpl kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(endDeviceGroup, ONE_HOUR);
        when(validationService.getAvailableValidators(QualityCodeSystem.MDC)).thenReturn(Collections.singletonList(validator));
        when(estimationService.getAvailableEstimators(QualityCodeSystem.MDC)).thenReturn(Collections.singletonList(estimator));

        // Business method
        kpi.updateMembers();

        // Asserts
        kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(1);

        DataQualityKpiMember dataQualityKpiMember = kpi.getKpiMembers().get(0);
        assertThat(dataQualityKpiMember.getTargetIdentifier()).isEqualTo("" + meter.getId());

        Kpi childKpi = dataQualityKpiMember.getChildKpi();
        assertThat(childKpi.getIntervalLength()).isEqualTo(ONE_HOUR);

        String[] kpiMemberNames = childKpi.getMembers().stream().map(KpiMember::getName).toArray(String[]::new);
        String[] expectedKpiMemberNames =
                Stream.concat(
                        Stream.of(DataQualityKpiMemberType.PredefinedKpiMemberType.values()),
                        Stream.of(new DataQualityKpiMemberType.ValidatorKpiMemberType(validator),
                                new DataQualityKpiMemberType.EstimatorKpiMemberType(estimator)))
                        .map(DataQualityKpiMemberType::getName)
                        .map(member -> member.toUpperCase() + "_" + meter.getId())
                        .toArray(String[]::new);
        assertThat(kpiMemberNames).containsOnly(expectedKpiMemberNames);
    }

    @Test
    @Transactional
    public void updateMembersAfterDeviceRemovedFromGroup() {
        Meter meter_1 = createMeter("SPE0001");
        Meter meter_2 = createMeter("SPE0002");
        EnumeratedEndDeviceGroup endDeviceGroup = createEndDeviceGroup("HasTwoMeters", meter_1, meter_2);
        DeviceDataQualityKpiImpl kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(endDeviceGroup, ONE_HOUR);

        // Business method
        kpi.updateMembers();

        // Asserts
        kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(2);
        assertThat(kpi.getKpiMembers().stream().map(DataQualityKpiMember::getTargetIdentifier).toArray())
                .containsOnly("" + meter_1.getId(), "" + meter_2.getId());
        Kpi removedKpi = kpi.getKpiMembers().stream()
                .filter(dataQualityKpiMember -> dataQualityKpiMember.getTargetIdentifier().equals("" + meter_1.getId()))
                .map(DataQualityKpiMember::getChildKpi)
                .findFirst().get();

        // But once the device group has changed
        endDeviceGroup.setName("HasOneMeter");
        endDeviceGroup.endMembership(meter_1, Instant.now());
        endDeviceGroup.update();

        // Business method
        kpi.updateMembers();

        // Asserts
        kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(1);
        assertThat(kpi.getKpiMembers().stream().map(DataQualityKpiMember::getTargetIdentifier).toArray())
                .containsOnly("" + meter_2.getId());
        assertThat(get(KpiService.class).getKpi(removedKpi.getId())).isEmpty();
    }


    @Test
    @Transactional
    public void updateMembersAfterDeviceAddedIntoGroup() {
        Meter meter_1 = createMeter("SPE0001");
        EnumeratedEndDeviceGroup endDeviceGroup = createEndDeviceGroup("HasOneMeter", meter_1);
        DeviceDataQualityKpiImpl kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(endDeviceGroup, ONE_HOUR);

        // Business method
        kpi.updateMembers();

        // Asserts
        kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(1);
        assertThat(kpi.getKpiMembers().stream().map(DataQualityKpiMember::getTargetIdentifier).toArray()).containsOnly("" + meter_1.getId());

        // But once new device added into the group
        Meter meter_2 = createMeter("SPE0002");
        endDeviceGroup.setName("HasTwoMeters");
        endDeviceGroup.add(meter_2, Range.atLeast(Instant.now()));
        endDeviceGroup.update();

        // Business method
        kpi.updateMembers();

        // Asserts
        kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(2);
        assertThat(kpi.getKpiMembers().stream().map(DataQualityKpiMember::getTargetIdentifier).toArray())
                .containsOnly("" + meter_1.getId(), "" + meter_2.getId());
    }

    @Test
    @Transactional
    public void updateMembersAfterNewValidatorAndEstimatorDeployed() {
        Meter meter = createMeter("SPE0001");
        EndDeviceGroup endDeviceGroup = createEndDeviceGroup("HasOneMeter", meter);
        DeviceDataQualityKpiImpl kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(endDeviceGroup, ONE_HOUR);

        // Business method
        kpi.updateMembers();

        // Asserts
        kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(1);
        Kpi childKpi = kpi.getKpiMembers().get(0).getChildKpi();
        String[] kpiMemberNames = childKpi.getMembers().stream().map(KpiMember::getName).toArray(String[]::new);
        String[] expectedKpiMemberNames =
                Stream.of(DataQualityKpiMemberType.PredefinedKpiMemberType.values())
                        .map(DataQualityKpiMemberType::getName)
                        .map(member -> member.toUpperCase() + "_" + meter.getId())
                        .toArray(String[]::new);
        assertThat(kpiMemberNames).containsOnly(expectedKpiMemberNames);

        // But once new validator and estimator deployed
        when(validationService.getAvailableValidators(QualityCodeSystem.MDC)).thenReturn(Collections.singletonList(validator));
        when(estimationService.getAvailableEstimators(QualityCodeSystem.MDC)).thenReturn(Collections.singletonList(estimator));

        // Business method
        kpi.updateMembers();

        // Asserts
        kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(1);
        childKpi = kpi.getKpiMembers().get(0).getChildKpi();
        kpiMemberNames = childKpi.getMembers().stream().map(KpiMember::getName).toArray(String[]::new);
        expectedKpiMemberNames =
                Stream.concat(
                        Stream.of(DataQualityKpiMemberType.PredefinedKpiMemberType.values()),
                        Stream.of(new DataQualityKpiMemberType.ValidatorKpiMemberType(validator),
                                new DataQualityKpiMemberType.EstimatorKpiMemberType(estimator)))
                        .map(DataQualityKpiMemberType::getName)
                        .map(member -> member.toUpperCase() + "_" + meter.getId())
                        .toArray(String[]::new);
        assertThat(kpiMemberNames).containsOnly(expectedKpiMemberNames);
    }

    @Test
    @Transactional
    public void makeObsolete() {
        EndDeviceGroup endDeviceGroup = createEndDeviceGroup();
        DeviceDataQualityKpiImpl kpi = (DeviceDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(endDeviceGroup, ONE_HOUR);

        // Business method
        kpi.makeObsolete();

        // Asserts
        Optional<DeviceDataQualityKpi> dataQualityKpi = dataQualityKpiService.findDeviceDataQualityKpi(kpi.getId());
        assertThat(dataQualityKpi).isPresent();
        assertThat(dataQualityKpi.get().getObsoleteTime()).isPresent();

        List<DeviceDataQualityKpi> found = dataQualityKpiService.deviceDataQualityKpiFinder().forGroup(endDeviceGroup).find();
        assertThat(found).isEmpty();

        Optional<RecurrentTask> recurrentTask = get(TaskService.class).getRecurrentTask(kpi.getRecurrentTaskName());
        assertThat(recurrentTask).isEmpty();
    }

    private EndDeviceGroup createEndDeviceGroup() {
        return createEndDeviceGroup("EDG");
    }

    private EnumeratedEndDeviceGroup createEndDeviceGroup(String name, Meter... meters) {
        return get(MeteringGroupsService.class).createEnumeratedEndDeviceGroup(meters).setName(name).create();
    }

    private Meter createMeter(String name) {
        AmrSystem amrSystem = get(MeteringService.class).findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        return amrSystem.newMeter(name, name).create();
    }
}
