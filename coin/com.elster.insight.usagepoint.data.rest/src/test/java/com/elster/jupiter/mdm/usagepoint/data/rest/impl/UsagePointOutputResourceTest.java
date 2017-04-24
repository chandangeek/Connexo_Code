/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.mdm.usagepoint.config.rest.FormulaInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverablesInfo;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.MetrologyContractCalculationIntrospector;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationEvaluator;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceTest extends UsagePointDataRestApplicationJerseyTest {

    private static final String USAGE_POINT_NAME = "Der Name";
    private static final String EXPECTED_FORMULA_DESCRIPTION = "Formula Description";

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, previousEffectiveMC, oldEffectiveMC;
    @Mock
    private ChannelsContainer channelsContainer1, channelsContainer2;
    @Mock
    private DataValidationTask validationTask;
    @Mock
    private EstimationTask estimationTask;
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private Query<UsagePoint> usagePointQuery;
    @Mock
    private MetrologyContractCalculationIntrospector metrologyContractCalculationIntrospector;

    private MetrologyContract optionalContract;
    private MetrologyContract mandatoryContract1;
    private MetrologyContract mandatoryContract2;

    @Before
    public void before() {
        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC));
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Arrays.asList(oldEffectiveMC, previousEffectiveMC, effectiveMC));
        MetrologyPurpose billing = mockMetrologyPurpose(DefaultMetrologyPurpose.BILLING);
        MetrologyPurpose information = mockMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION);
        UsagePointMetrologyConfiguration metrologyConfiguration1 = mockMetrologyConfigurationWithContract(1, "mc1", billing, information);
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration1);
        when(effectiveMC.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer1));
        when(effectiveMC.getUsagePoint()).thenReturn(usagePoint);
        UsagePointMetrologyConfiguration metrologyConfiguration2 = mockMetrologyConfigurationWithContract(2, "mc2", billing, information);
        when(previousEffectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration2);
        when(previousEffectiveMC.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer2));
        when(previousEffectiveMC.getUsagePoint()).thenReturn(usagePoint);
        UsagePointMetrologyConfiguration metrologyConfiguration3 = mockMetrologyConfigurationWithContract(3, "mc3");
        when(oldEffectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration3);

        when(channelsContainer1.getChannel(any())).thenReturn(Optional.empty());
        ReadingTypeDeliverablesInfo readingTypeDeliverablesInfo = new ReadingTypeDeliverablesInfo();
        readingTypeDeliverablesInfo.formula = new FormulaInfo();
        readingTypeDeliverablesInfo.formula.description = EXPECTED_FORMULA_DESCRIPTION;
        when(readingTypeDeliverableFactory.asInfo(any(ReadingTypeDeliverable.class))).thenReturn(readingTypeDeliverablesInfo);
        when(clock.instant()).thenReturn(Instant.now());
        mandatoryContract1 = metrologyConfiguration1.getContracts().get(0);
        optionalContract = metrologyConfiguration1.getContracts().get(1);
        mandatoryContract2 = metrologyConfiguration2.getContracts().get(0);

        when(usagePointGroup.getId()).thenReturn(51L);
        doReturn(usagePointQuery).when(meteringService).getUsagePointQuery();
        doReturn(Collections.singletonList(usagePoint)).when(usagePointQuery)
                .select(any(Condition.class), anyInt(), anyInt());
        doReturn(Collections.singletonList(estimationTask)).when(estimationService).findEstimationTasks(QualityCodeSystem.MDM);
        when(estimationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));
        when(estimationTask.getId()).thenReturn(32L);
        when(estimationTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(validationService.findValidationTasks()).thenReturn(Collections.singletonList(validationTask));
        when(validationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));
        when(validationTask.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(validationTask.getScheduleExpression()).thenReturn(PeriodicalScheduleExpression
                .every(6)
                .hours()
                .at(10, 0)
                .build());
        when(validationTask.getEndDeviceGroup()).thenReturn(Optional.empty());
        when(validationTask.getLastRun()).thenReturn(Optional.empty());
        when(validationTask.getLastOccurrence()).thenReturn(Optional.empty());
        when(validationTask.getId()).thenReturn(31L);
        when(dataAggregationService.introspect(any(),any(),any())).thenReturn(metrologyContractCalculationIntrospector);
        List<MetrologyContractCalculationIntrospector.CalendarUsage> calendarUsages = Collections.emptyList();
        when(metrologyContractCalculationIntrospector.getCalendarUsagesFor(any())).thenReturn(calendarUsages);
    }

    @Test
    public void testGetOutputsOfUsagePointPurpose() {
        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        // channel output
        assertThat(jsonModel.<Number>get("$.outputs[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.outputs[0].outputType")).isEqualTo("channel");
        assertThat(jsonModel.<String>get("$.outputs[0].name")).isEqualTo("1 regular RT");
        assertThat(jsonModel.<Number>get("$.outputs[0].interval.count")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.outputs[0].interval.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.outputs[0].readingType.mRID")).isEqualTo("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        assertThat(jsonModel.<String>get("$.outputs[0].formula.description")).isEqualTo(EXPECTED_FORMULA_DESCRIPTION);
        // register output
        assertThat(jsonModel.<Number>get("$.outputs[1].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.outputs[1].outputType")).isEqualTo("register");
        assertThat(jsonModel.<String>get("$.outputs[1].name")).isEqualTo("2 irregular RT");
        assertThat(jsonModel.<String>get("$.outputs[1].readingType.mRID")).isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        assertThat(jsonModel.<String>get("$.outputs[1].formula.description")).isEqualTo(EXPECTED_FORMULA_DESCRIPTION);
    }

    @Test
    public void testGetOutputById() {
        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.outputType")).isEqualTo("channel");
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("1 regular RT");
        assertThat(jsonModel.<Number>get("$.interval.count")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.interval.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.readingType.mRID")).isEqualTo("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        assertThat(jsonModel.<String>get("$.formula.description")).isEqualTo(EXPECTED_FORMULA_DESCRIPTION);
    }

    @Test
    public void testValidatePurposeOnRequest() {
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).validate(
                refEq(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer1, mandatoryContract1)),
                eq(purposeInfo.validationInfo.lastChecked));
        verify(validationService).validate(
                refEq(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer2, mandatoryContract2)),
                eq(purposeInfo.validationInfo.lastChecked));
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testEstimatePurposeOnRequest() {
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/estimate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(estimationService).estimate(QualityCodeSystem.MDM, channelsContainer1, channelsContainer1.getRange());
        verify(estimationService).estimate(QualityCodeSystem.MDM, channelsContainer2, channelsContainer2.getRange());
        verifyNoMoreInteractions(estimationService);
    }

    @Test
    public void testValidatePurposeOnRequestConcurrencyCheck() {
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1);
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointById(usagePoint.getId())).thenReturn(Optional.of(usagePoint));
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testEstimatePurposeOnRequestConcurrencyCheck() {
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1);
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointById(usagePoint.getId())).thenReturn(Optional.of(usagePoint));
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/estimate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testPurposeActivation(){
        PurposeInfo purposeInfo = createPurposeInfo(optionalContract);
        when(effectiveMC.getChannelsContainer(optionalContract)).thenReturn(Optional.empty());
        when(effectiveMC.getChannelsContainer(eq(optionalContract), any(Instant.class))).thenReturn(Optional.empty());
        when(usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/101/activate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(effectiveMC).activateOptionalMetrologyContract(eq(optionalContract), any(Instant.class));
    }

    @Test
    public void testPurposeDeactivation(){
        PurposeInfo purposeInfo = createPurposeInfo(optionalContract);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getChannels()).thenReturn(Collections.emptyList());
        when(effectiveMC.getChannelsContainer(eq(optionalContract), any(Instant.class))).thenReturn(Optional.of(channelsContainer));
        ValidationEvaluator validationEvaluator = mock(ValidationEvaluator.class);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        doReturn(Collections.emptyList()).when(validationEvaluator).getValidationStatus(any(), any(Channel.class), any());
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/101/deactivate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(effectiveMC).deactivateOptionalMetrologyContract(eq(optionalContract), any(Instant.class));
    }

    private PurposeInfo createPurposeInfo(MetrologyContract metrologyContract) {
        PurposeInfo purposeInfo = new PurposeInfo();
        purposeInfo.id = metrologyContract.getId();
        purposeInfo.version = metrologyContract.getVersion();
        purposeInfo.validationInfo = new UsagePointValidationStatusInfo();
        purposeInfo.validationInfo.lastChecked = Instant.ofEpochMilli(1467185935140L);
        purposeInfo.parent = new VersionInfo<>(usagePoint.getId(), usagePoint.getVersion());
        return purposeInfo;
    }

    @Test
    public void testGetValidationTasksOnPurpose() throws Exception {
        MetrologyPurpose metrologyPurpose = optionalContract.getMetrologyPurpose();
        when(validationTask.getMetrologyPurpose()).thenReturn(Optional.of(metrologyPurpose));
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/" + optionalContract.getId() + "/validationtasks").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.dataValidationTasks")).hasSize(1);
        assertThat(model.<Integer>get("$.dataValidationTasks[0].id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.dataValidationTasks[0].usagePointGroup.id")).isEqualTo(51);
    }

    @Test
    public void testGetEstimationTasksOnPurpose() throws Exception {
        MetrologyPurpose metrologyPurpose = optionalContract.getMetrologyPurpose();
        when(estimationTask.getMetrologyPurpose()).thenReturn(Optional.of(metrologyPurpose));
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/" + optionalContract.getId() + "/estimationtasks").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.dataEstimationTasks")).hasSize(1);
        assertThat(model.<Integer>get("$.dataEstimationTasks[0].id")).isEqualTo(32);
    }
}
