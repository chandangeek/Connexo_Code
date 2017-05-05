/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.mdm.usagepoint.config.rest.FormulaInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverablesInfo;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
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
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceTest extends UsagePointDataRestApplicationJerseyTest {

    private static final String USAGE_POINT_NAME = "Der Name";
    private static final String EXPECTED_FORMULA_DESCRIPTION = "Formula Description";
    private static final Instant INSTANT = Instant.ofEpochMilli(1467185935140L);
    private static final Instant PREVIOUS_INSTANT = INSTANT.minus(60, ChronoUnit.MINUTES);
    private static final Instant NEXT_INSTANT = INSTANT.plus(60, ChronoUnit.MINUTES);
    private static final Instant OLD_INSTANT = PREVIOUS_INSTANT.minus(60, ChronoUnit.MINUTES);

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
        doReturn(Collections.singletonList(usagePoint)).when(usagePointQuery).select(any(Condition.class), anyInt(), anyInt());
        doReturn(Collections.singletonList(estimationTask)).when(estimationService).findEstimationTasks(QualityCodeSystem.MDM);
        when(estimationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));
        when(estimationTask.getId()).thenReturn(32L);
        when(estimationTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(validationService.findValidationTasks()).thenReturn(Collections.singletonList(validationTask));
        when(validationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));
        when(validationTask.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(validationTask.getScheduleExpression()).thenReturn(PeriodicalScheduleExpression.every(6).hours().at(10, 0).build());
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
        Range<Instant> range1 = Range.atLeast(PREVIOUS_INSTANT);
        when(channelsContainer1.getInterval()).thenReturn(Interval.of(range1));
        Range<Instant> range2 = Range.closedOpen(OLD_INSTANT, PREVIOUS_INSTANT);
        when(channelsContainer2.getInterval()).thenReturn(Interval.of(range2));
        when(validationService.getLastChecked(channelsContainer1)).thenReturn(Optional.of(INSTANT));
        when(validationService.getLastChecked(channelsContainer2)).thenReturn(Optional.of(PREVIOUS_INSTANT));
        mockValidationRuleSet(mandatoryContract1, range1);
        mockValidationRuleSet(mandatoryContract2, range1);
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, OLD_INSTANT);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).validate(
                refEq(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer1, mandatoryContract1)),
                eq(OLD_INSTANT.plusMillis(1)));
        verify(validationService).getLastChecked(channelsContainer2);
        verify(validationService).validate(
                refEq(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer2, mandatoryContract2)),
                eq(OLD_INSTANT.plusMillis(1)));
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testValidatePurposeOnRequestWithOnlyOneContractValidatedDueToLastChecked() {
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        Range<Instant> range1 = Range.atLeast(PREVIOUS_INSTANT);
        when(channelsContainer1.getInterval()).thenReturn(Interval.of(range1));
        Range<Instant> range2 = Range.closedOpen(OLD_INSTANT, PREVIOUS_INSTANT);
        when(channelsContainer2.getInterval()).thenReturn(Interval.of(range2));
        when(validationService.getLastChecked(channelsContainer1)).thenReturn(Optional.of(INSTANT));
        when(validationService.getLastChecked(channelsContainer2)).thenReturn(Optional.of(PREVIOUS_INSTANT));
        mockValidationRuleSet(mandatoryContract1, range1);
        mockValidationRuleSet(mandatoryContract2, range1);
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, NEXT_INSTANT);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).getLastChecked(channelsContainer2);
        verify(validationService).validate(
                refEq(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer1, mandatoryContract1)),
                eq(INSTANT.plusMillis(1)));
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testValidatePurposeOnRequestWithOnlyOneContractValidatedDueToRuleSetVersions() {
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        Range<Instant> range1 = Range.atLeast(PREVIOUS_INSTANT);
        when(channelsContainer1.getInterval()).thenReturn(Interval.of(range1));
        Range<Instant> range2 = Range.closedOpen(OLD_INSTANT, PREVIOUS_INSTANT);
        when(channelsContainer2.getInterval()).thenReturn(Interval.of(range2));
        when(validationService.getLastChecked(channelsContainer1)).thenReturn(Optional.of(INSTANT));
        when(validationService.getLastChecked(channelsContainer2)).thenReturn(Optional.of(PREVIOUS_INSTANT));
        mockValidationRuleSet(mandatoryContract1, range2);
        mockValidationRuleSet(mandatoryContract2, range2);
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, OLD_INSTANT);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).getLastChecked(channelsContainer2);
        verify(validationService).validate(
                refEq(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer2, mandatoryContract2)),
                eq(OLD_INSTANT.plusMillis(1)));
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testValidatePurposeOnRequestWithoutActiveRuleSetVersions() throws IOException {
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        Range<Instant> range1 = Range.greaterThan(PREVIOUS_INSTANT);
        when(channelsContainer1.getInterval()).thenReturn(Interval.of(range1));
        Range<Instant> range2 = Range.openClosed(OLD_INSTANT, PREVIOUS_INSTANT);
        when(channelsContainer2.getInterval()).thenReturn(Interval.of(range2));
        when(validationService.getLastChecked(channelsContainer1)).thenReturn(Optional.of(INSTANT));
        when(validationService.getLastChecked(channelsContainer2)).thenReturn(Optional.of(PREVIOUS_INSTANT));
        mockValidationRuleSet(mandatoryContract1, range2);
        mockValidationRuleSet(mandatoryContract2, range1);
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, OLD_INSTANT);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).getLastChecked(channelsContainer2);
        verifyNoMoreInteractions(validationService);

        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<List<String>>get("$.errors[*].id")).containsExactly("validationInfo.lastChecked");
        assertThat(model.<List<String>>get("$.errors[*].msg")).containsExactly(MessageSeeds.NOTHING_TO_VALIDATE.getDefaultFormat());
    }

    @Test
    public void testValidatePurposeOnRequestWithNoRuleSetsConfigured() throws IOException {
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        Range<Instant> range1 = Range.atLeast(PREVIOUS_INSTANT);
        when(channelsContainer1.getInterval()).thenReturn(Interval.of(range1));
        Range<Instant> range2 = Range.closedOpen(OLD_INSTANT, PREVIOUS_INSTANT);
        when(channelsContainer2.getInterval()).thenReturn(Interval.of(range2));
        when(validationService.getLastChecked(channelsContainer1)).thenReturn(Optional.of(INSTANT));
        when(validationService.getLastChecked(channelsContainer2)).thenReturn(Optional.of(PREVIOUS_INSTANT));
        when(usagePointConfigurationService.getValidationRuleSets(any(MetrologyContract.class))).thenReturn(Collections.emptyList());
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, OLD_INSTANT);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).getLastChecked(channelsContainer2);
        verifyNoMoreInteractions(validationService);

        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<List<String>>get("$.errors[*].id")).containsExactly("validationInfo.lastChecked");
        assertThat(model.<List<String>>get("$.errors[*].msg")).containsExactly(MessageSeeds.NOTHING_TO_VALIDATE.getDefaultFormat());
    }

    private void mockValidationRuleSet(MetrologyContract metrologyContract, Range<Instant> active) {
        ValidationRuleSet validationRuleSet = mock(ValidationRuleSet.class);
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(validationRuleSet));
        ValidationRuleSetVersion validationRuleSetVersion = mock(ValidationRuleSetVersion.class);
        doReturn(Collections.singletonList(validationRuleSetVersion)).when(validationRuleSet).getRuleSetVersions();
        when(validationRuleSetVersion.getRange()).thenReturn(active);
        ValidationRule validationRule = mock(ValidationRule.class);
        doReturn(Collections.singletonList(validationRule)).when(validationRuleSetVersion).getRules();
        Set<ReadingType> readingTypes = metrologyContract.getDeliverables().stream()
                .map(ReadingTypeDeliverable::getReadingType)
                .collect(Collectors.toSet());
        doReturn(Collections.singletonList(validationRule)).when(validationRuleSetVersion).getRules(readingTypes);
        when(validationRule.isActive()).thenReturn(true);
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
        verifyNoMoreInteractions(validationService);
    }

    @Test
    public void testEstimatePurposeOnRequestAndRevalidate() {
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));

        Instant now = Instant.now();
        Instant lastChecked = now.plus(1, ChronoUnit.DAYS);
        when(validationService.getLastChecked(any(Channel.class))).thenReturn(Optional.of(lastChecked));
        ReadingType rt1 = mock(ReadingType.class);
        ReadingType rt2 = mock(ReadingType.class);
        Channel channel1 = mockChannel(channelsContainer1, rt1);
        Channel channel2 = mockChannel(channelsContainer1, rt2);
        EstimationReport estimationReport = mockEstimationReport(ImmutableMap.of(
                rt1, mockEstimationResult(
                        mockEstimationBlock(channel1, mockEstimatable(now)),
                        mockEstimationBlock(channel1, mockEstimatable(now), mockEstimatable(now.minus(1, ChronoUnit.DAYS)))),
                rt2, mockEstimationResult(
                        mockEstimationBlock(channel2, mockEstimatable(now)))
        ));
        when(estimationService.estimate(QualityCodeSystem.MDM, channelsContainer1, channelsContainer1.getRange())).thenReturn(estimationReport);

        EstimationReport emptyEstimationResult = mockEstimationReport(ImmutableMap.of(
                rt1, mockEstimationResult(/* without estimated blocks*/)
        ));
        when(estimationService.estimate(QualityCodeSystem.MDM, channelsContainer2, channelsContainer2.getRange())).thenReturn(emptyEstimationResult);

        EstimatePurposeRequestInfo info = new EstimatePurposeRequestInfo();
        info.revalidate = true;
        info.parent = new VersionInfo<>(usagePoint.getId(), usagePoint.getVersion());

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/estimate").request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<ValidationContext> validationContextArgumentCaptor = ArgumentCaptor.forClass(ValidationContext.class);
        ArgumentCaptor<Range> rangeArgumentCaptor = ArgumentCaptor.forClass(Range.class);
        verify(validationService, times(2)).validate(validationContextArgumentCaptor.capture(), rangeArgumentCaptor.capture());
        List<ValidationContext> validationContexts = validationContextArgumentCaptor.getAllValues();
        List<Range> validationRanges = rangeArgumentCaptor.getAllValues();

        Map<ReadingType, Range<Instant>> arguments = ImmutableMap.of(
                validationContexts.get(0).getReadingType().get(), (Range<Instant>) validationRanges.get(0),
                validationContexts.get(1).getReadingType().get(), (Range<Instant>) validationRanges.get(1)
        );
        assertThat(arguments).contains(
                MapEntry.entry(rt1, Range.closed(now.minus(1, ChronoUnit.DAYS), lastChecked)),
                MapEntry.entry(rt2, Range.closed(now, lastChecked))
        );
    }

    private EstimationReport mockEstimationReport(Map<ReadingType, EstimationResult> estimationResult) {
        EstimationReport estimationReport = mock(EstimationReport.class);
        when(estimationReport.getResults()).thenReturn(estimationResult);
        return estimationReport;
    }

    private EstimationResult mockEstimationResult(EstimationBlock... estimatedBlocks) {
        EstimationResult estimationResult = mock(EstimationResult.class);
        when(estimationResult.estimated()).thenReturn(Arrays.asList(estimatedBlocks));
        return estimationResult;
    }

    private EstimationBlock mockEstimationBlock(Channel channel, Estimatable... estimatables) {
        EstimationBlock estimationBlock = mock(EstimationBlock.class);
        when(estimationBlock.getChannel()).thenReturn(channel);
        doReturn(Arrays.asList(estimatables)).when(estimationBlock).estimatables();
        return estimationBlock;
    }

    private Estimatable mockEstimatable(Instant timestamp) {
        Estimatable estimatable = mock(Estimatable.class);
        when(estimatable.getTimestamp()).thenReturn(timestamp);
        return estimatable;
    }

    private Channel mockChannel(ChannelsContainer channelsContainer, ReadingType readingType) {
        Channel channel = mock(Channel.class);
        when(channelsContainer.getChannel(readingType)).thenReturn(Optional.of(channel));
        when(channelsContainer.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel.getMainReadingType()).thenReturn(readingType);
        return channel;
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
    public void testPurposeActivation() {
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
    public void testPurposeDeactivation() {
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
        return createPurposeInfo(metrologyContract, INSTANT);
    }

    private PurposeInfo createPurposeInfo(MetrologyContract metrologyContract, Instant lastChecked) {
        PurposeInfo purposeInfo = new PurposeInfo();
        purposeInfo.id = metrologyContract.getId();
        purposeInfo.version = metrologyContract.getVersion();
        purposeInfo.validationInfo = new UsagePointValidationStatusInfo();
        purposeInfo.validationInfo.lastChecked = lastChecked;
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
