/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.dataquality.impl.calc.DataQualityKpiMemberType;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleNlsKey;
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

import static com.elster.jupiter.dataquality.impl.UsagePointDataQualityKpiImpl.KPI_MEMBER_NAME_SUFFIX_SEPARATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointDataQualityKpiIT extends BaseTestIT {

    public static final Duration ONE_HOUR = Duration.ofHours(1);

    private DataQualityKpiService dataQualityKpiService;
    private ValidationService validationService;
    private EstimationService estimationService;

    private MetrologyPurpose metrologyPurpose;

    @Mock
    private Validator validator;
    @Mock
    private Estimator estimator;

    @Before
    public void setUp() throws Exception {
        dataQualityKpiService = get(DataQualityKpiService.class);
        metrologyPurpose = createMetrologyPurpose("Purpose");
        validationService = get(ValidationService.class);
        estimationService = get(EstimationService.class);
    }

    @After
    public void tearDown() {
        Mockito.reset(validationService, estimationService);
    }

    private MetrologyPurpose createMetrologyPurpose(String name) {
        return get(MetrologyConfigurationService.class).createMetrologyPurpose(
                SimpleNlsKey.key("CMP", Layer.DOMAIN, name),
                SimpleNlsKey.key("CMP", Layer.DOMAIN, name)
        );
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
        String recurrentTaskName = kpi.getRecurrentTaskName();

        // Business method
        kpi.makeObsolete();

        // Asserts
        Optional<UsagePointDataQualityKpi> dataQualityKpi = dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId());
        assertThat(dataQualityKpi).isPresent();
        assertThat(dataQualityKpi.get().getObsoleteTime()).isPresent();

        List<UsagePointDataQualityKpi> found = dataQualityKpiService.usagePointDataQualityKpiFinder().forGroup(usagePointGroup).forPurpose(metrologyPurpose).find();
        assertThat(found).isEmpty();

        Optional<RecurrentTask> recurrentTask = get(TaskService.class).getRecurrentTask(recurrentTaskName);
        assertThat(recurrentTask).isEmpty();
    }

    @Test
    @Transactional
    public void updateMembersEmptyUsagePointGroup() {
        UsagePointGroup usagePointGroup = createUsagePointGroup();
        UsagePointDataQualityKpiImpl kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, ONE_HOUR);

        // Business method
        kpi.updateMembers(Range.all());

        // Asserts
        kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).isEmpty();
    }

    @Test
    @Transactional
    public void updateMembers() {
        UsagePoint usagePoint = createUsagePoint("UP0001");
        ChannelsContainer channelsContainer = mockChannelsContainerForPurpose(usagePoint, metrologyPurpose);
        when(channelsContainer.getId()).thenReturn(100L);
        UsagePointGroup usagePointGroup = createUsagePointGroup("HasOneUsagePoint", usagePoint);
        UsagePointDataQualityKpiImpl kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, ONE_HOUR);
        when(validationService.getAvailableValidators(QualityCodeSystem.MDM)).thenReturn(Collections.singletonList(validator));
        when(estimationService.getAvailableEstimators(QualityCodeSystem.MDM)).thenReturn(Collections.singletonList(estimator));

        // Business method
        kpi.updateMembers(Range.all());

        // Asserts
        kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(1);

        DataQualityKpiMember dataQualityKpiMember = kpi.getKpiMembers().get(0);
        assertThat(dataQualityKpiMember.getTargetIdentifier())
                .isEqualTo(usagePoint.getId() + KPI_MEMBER_NAME_SUFFIX_SEPARATOR + channelsContainer.getId());

        Kpi childKpi = dataQualityKpiMember.getChildKpi();
        assertThat(childKpi.getIntervalLength()).isEqualTo(ONE_HOUR);

        String[] kpiMemberNames = childKpi.getMembers().stream().map(KpiMember::getName).toArray(String[]::new);
        String[] expectedKpiMemberNames =
                Stream.concat(
                        Stream.of(DataQualityKpiMemberType.PredefinedKpiMemberType.values()),
                        Stream.of(new DataQualityKpiMemberType.ValidatorKpiMemberType(validator),
                                new DataQualityKpiMemberType.EstimatorKpiMemberType(estimator)))
                        .map(DataQualityKpiMemberType::getName)
                        .map(member -> member.toUpperCase() + "_" + usagePoint.getId() + KPI_MEMBER_NAME_SUFFIX_SEPARATOR + channelsContainer.getId())
                        .toArray(String[]::new);
        assertThat(kpiMemberNames).containsOnly(expectedKpiMemberNames);
    }

    @Test
    @Transactional
    public void updateMembersAfterUsagePointRemovedFromGroup() {
        UsagePoint usagePoint_1 = createUsagePoint("UP0001");
        ChannelsContainer channelsContainer_1 = mockChannelsContainerForPurpose(usagePoint_1, metrologyPurpose);
        UsagePoint usagePoint_2 = createUsagePoint("UP0002");
        ChannelsContainer channelsContainer_2 = mockChannelsContainerForPurpose(usagePoint_2, metrologyPurpose);
        EnumeratedUsagePointGroup usagePointGroup = createUsagePointGroup("HasTwoUsagePoints", usagePoint_1, usagePoint_2);
        UsagePointDataQualityKpiImpl kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, ONE_HOUR);

        // Business method
        kpi.updateMembers(Range.all());

        // Asserts
        kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(2);
        assertThat(kpi.getKpiMembers().stream().map(DataQualityKpiMember::getTargetIdentifier).toArray()).containsOnly(
                usagePoint_1.getId() + KPI_MEMBER_NAME_SUFFIX_SEPARATOR + channelsContainer_1.getId(),
                usagePoint_2.getId() + KPI_MEMBER_NAME_SUFFIX_SEPARATOR + channelsContainer_2.getId());
        Kpi removedKpi = kpi.getKpiMembers().stream()
                .filter(dataQualityKpiMember -> dataQualityKpiMember.getTargetIdentifier()
                        .equals(usagePoint_1.getId() + KPI_MEMBER_NAME_SUFFIX_SEPARATOR + channelsContainer_1.getId()))
                .map(DataQualityKpiMember::getChildKpi)
                .findFirst().get();

        // But once the usage point group has changed
        usagePointGroup.setName("HasOneMeter");
        usagePointGroup.endMembership(usagePoint_1, Instant.now());
        usagePointGroup.update();

        // Business method
        kpi.updateMembers(Range.all());

        // Asserts
        kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(1);
        assertThat(kpi.getKpiMembers().stream().map(DataQualityKpiMember::getTargetIdentifier).toArray())
                .containsOnly(usagePoint_2.getId() + KPI_MEMBER_NAME_SUFFIX_SEPARATOR + channelsContainer_2.getId());
        assertThat(get(KpiService.class).getKpi(removedKpi.getId())).isEmpty();
    }

    @Test
    @Transactional
    public void updateMembersAfterNewValidatorAndEstimatorDeployed() {
        UsagePoint usagePoint = createUsagePoint("UP0001");
        ChannelsContainer channelsContainer = mockChannelsContainerForPurpose(usagePoint, metrologyPurpose);
        EnumeratedUsagePointGroup usagePointGroup = createUsagePointGroup("HasOneUsagePoint", usagePoint);
        UsagePointDataQualityKpiImpl kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, ONE_HOUR);

        // Business method
        kpi.updateMembers(Range.all());

        // Asserts
        kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(1);
        Kpi childKpi = kpi.getKpiMembers().get(0).getChildKpi();
        String[] kpiMemberNames = childKpi.getMembers().stream().map(KpiMember::getName).toArray(String[]::new);
        String[] expectedKpiMemberNames =
                Stream.of(DataQualityKpiMemberType.PredefinedKpiMemberType.values())
                        .map(DataQualityKpiMemberType::getName)
                        .map(member -> member.toUpperCase() + "_" + usagePoint.getId() + KPI_MEMBER_NAME_SUFFIX_SEPARATOR + channelsContainer.getId())
                        .toArray(String[]::new);
        assertThat(kpiMemberNames).containsOnly(expectedKpiMemberNames);

        // But once new validator and estimator deployed
        when(validationService.getAvailableValidators(QualityCodeSystem.MDM)).thenReturn(Collections.singletonList(validator));
        when(estimationService.getAvailableEstimators(QualityCodeSystem.MDM)).thenReturn(Collections.singletonList(estimator));

        // Business method
        kpi.updateMembers(Range.all());

        // Asserts
        kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).hasSize(1);
        childKpi = kpi.getKpiMembers().get(0).getChildKpi();
        kpiMemberNames = childKpi.getMembers().stream().map(KpiMember::getName).toArray(String[]::new);
        expectedKpiMemberNames =
                Stream.concat(
                        Stream.of(DataQualityKpiMemberType.PredefinedKpiMemberType.values()),
                        Stream.of(new DataQualityKpiMemberType.ValidatorKpiMemberType(validator),
                                new DataQualityKpiMemberType.EstimatorKpiMemberType(estimator)))
                        .map(DataQualityKpiMemberType::getName)
                        .map(member -> member.toUpperCase() + "_" + usagePoint.getId() + KPI_MEMBER_NAME_SUFFIX_SEPARATOR + channelsContainer.getId())
                        .toArray(String[]::new);
        assertThat(kpiMemberNames).containsOnly(expectedKpiMemberNames);
    }

    @Test
    @Transactional
    public void updateMembersIfNoActivePurposesOnUsagePoint() {
        UsagePoint usagePoint = createUsagePoint("UP0001");
        UsagePointGroup usagePointGroup = createUsagePointGroup("HasOneUsagePoint", usagePoint);
        UsagePointDataQualityKpiImpl kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.newDataQualityKpi(usagePointGroup, metrologyPurpose, ONE_HOUR);

        // Business method
        kpi.updateMembers(Range.all());

        // Asserts
        kpi = (UsagePointDataQualityKpiImpl) dataQualityKpiService.findUsagePointDataQualityKpi(kpi.getId()).get();
        assertThat(kpi.getKpiMembers()).isEmpty();
    }

    private UsagePointGroup createUsagePointGroup() {
        return createUsagePointGroup("UPG");
    }

    private EnumeratedUsagePointGroup createUsagePointGroup(String name, UsagePoint... members) {
        return get(MeteringGroupsService.class).createEnumeratedUsagePointGroup(members).setName(name).create();
    }

    private UsagePoint createUsagePoint(String name) {
        MeteringService meteringService = get(MeteringService.class);
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new IllegalStateException(ServiceKind.ELECTRICITY.getDisplayName() + " is not available"));
        UsagePointBuilder usagePointBuilder = serviceCategory.newUsagePoint(name, Instant.now());
        return spy(usagePointBuilder.create());
    }

    private ChannelsContainer mockChannelsContainerForPurpose(UsagePoint usagePoint, MetrologyPurpose metrologyPurpose) {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        MetrologyContract contract = mock(MetrologyContract.class);
        when(contract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(contract));
        when(usagePoint.getEffectiveMetrologyConfigurations(Range.all())).thenReturn(Collections.singletonList(effectiveMC));
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getId()).thenReturn(1L);
        when(effectiveMC.getChannelsContainer(contract)).thenReturn(Optional.of(channelsContainer));
        return channelsContainer;
    }
}
