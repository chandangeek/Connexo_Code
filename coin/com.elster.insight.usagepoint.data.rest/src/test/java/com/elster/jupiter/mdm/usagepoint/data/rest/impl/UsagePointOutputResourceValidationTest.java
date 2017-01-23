package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverablesInfo;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import com.jayway.jsonpath.JsonModel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceValidationTest extends UsagePointDataRestApplicationJerseyTest {
    private static final Instant NOW = ZonedDateTime.of(2016, 6, 1, 12, 40, 30, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant DAY_BEFORE = NOW.minus(1, ChronoUnit.DAYS);
    private static final String READING_TYPE_MRID = "13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String EXPECTED_FORMULA_DESCRIPTION = "15min A+ Wh / 1000";

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    @Mock
    private ValidationEvaluator validationEvaluator;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private MetrologyPurpose metrologyPurpose;
    @Mock
    private DataValidationTask validationTask;
    @Mock
    private Query<DataValidationTask> dataValidationTaskQuery;
    @Mock
    private ChannelsContainer channelsContainer;
    private ReadingType readingType;
    @Mock
    private ReadingTypeDeliverable deliverable;
    @Mock
    private Channel channel;
    @Mock
    private CimChannel cimChannel;
    @Mock
    private MetrologyContract.Status status;
    @Mock
    private ReadingQualityWithTypeFetcher readingQualityFetcher;
    @Mock
    private ValidationRuleSet validationRuleSet;
    @Mock
    private ValidationRuleSetVersion validationRuleSetVersion;
    @Mock
    private ValidationRule validationRule;
    @Mock
    private DataValidationStatus dataValidationStatus;

    @Before
    public void setStubs() {
        when(clock.instant()).thenReturn(NOW);
        when(meteringService.findUsagePointByName("UP")).thenReturn(Optional.of(usagePoint));
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(validationRuleSet));
        readingType = mockReadingType(READING_TYPE_MRID);
        when(readingType.isRegular()).thenReturn(true);
        setDataValidationTaskStub();
        when(usagePoint.getId()).thenReturn(1L);
        when(usagePoint.getName()).thenReturn("UP");
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMetrologyConfiguration.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(effectiveMetrologyConfiguration.getChannelsContainer(eq(metrologyContract), any(Instant.class))).thenReturn(Optional
                .of(channelsContainer));
        setMetrologyContractStub();
        setUsagePointGroupStub();
        setChannelStub();
        setDeliverableStub();
        setValidationRuleStub();
        when(dataValidationStatus.getOffendedRules()).thenReturn(Collections.singletonList(validationRule));
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationEvaluator.getLastChecked(channelsContainer, readingType)).thenReturn(Optional.of(DAY_BEFORE));
        when(validationEvaluator.isAllDataValidated(channelsContainer)).thenReturn(false);
        when(dataValidationStatus.completelyValidated()).thenReturn(false);
        when(validationEvaluator.getValidationStatus(anySetOf(QualityCodeSystem.class), eq(Collections.singletonList(cimChannel)), eq(Collections.emptyList()), any()))
                .thenReturn(Collections.singletonList(dataValidationStatus));
        ReadingTypeDeliverablesInfo readingTypeDeliverablesInfo = new ReadingTypeDeliverablesInfo();
        readingTypeDeliverablesInfo.formula = new com.elster.jupiter.mdm.usagepoint.config.rest.FormulaInfo();
        readingTypeDeliverablesInfo.formula.description = EXPECTED_FORMULA_DESCRIPTION;
        when(readingTypeDeliverableFactory.asInfo(any(ReadingTypeDeliverable.class))).thenReturn(readingTypeDeliverablesInfo);
    }

    private void setDataValidationTaskStub() {
        when(validationService.findValidationTasksQuery()).thenReturn(dataValidationTaskQuery);
        when(dataValidationTaskQuery.select(any(Condition.class))).thenReturn(Collections.singletonList(validationTask));
        when(validationTask.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(validationTask.getEndDeviceGroup()).thenReturn(Optional.empty());
        when(validationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));
        when(validationTask.getId()).thenReturn(1L);
        when(validationTask.getName()).thenReturn("Validation Task");
        when(validationTask.getScheduleExpression()).thenReturn(new TemporalExpression(TimeDuration.days(5)));
        when(validationTask.getLastRun()).thenReturn(Optional.empty());
        when(validationTask.getLastOccurrence()).thenReturn(Optional.empty());
    }

    private void setUsagePointGroupStub() {
        when(usagePointGroup.getId()).thenReturn(1L);
    }

    private void setMetrologyContractStub() {
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(metrologyPurpose.getId()).thenReturn(1L);
        when(metrologyPurpose.getName()).thenReturn(DefaultMetrologyPurpose.BILLING.getName().getDefaultMessage());
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(metrologyContract.getId()).thenReturn(1L);
        when(metrologyContract.getStatus(usagePoint)).thenReturn(status);
        when(metrologyContract.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(status.isComplete()).thenReturn(true);
        when(status.getKey()).thenReturn("COMPLETE");
        when(status.getName()).thenReturn("Complete");
    }

    private void setChannelStub() {
        when(channelsContainer.getChannels()).thenReturn(Collections.singletonList(channel));
        when(channelsContainer.getChannel(readingType)).thenReturn(Optional.of(channel));
        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(channel.getCimChannel(readingType)).thenReturn(Optional.of(cimChannel));
        setReadingQualityFilterStub();
    }

    private void setReadingQualityFilterStub() {
        when(channel.findReadingQualities()).thenReturn(readingQualityFetcher);
        when(readingQualityFetcher.ofQualitySystems(anySetOf(QualityCodeSystem.class))).thenReturn(readingQualityFetcher);
        when(readingQualityFetcher.ofQualityIndex(any(QualityCodeIndex.class))).thenReturn(readingQualityFetcher);
        when(readingQualityFetcher.inTimeInterval(any())).thenReturn(readingQualityFetcher);
        when(readingQualityFetcher.actual()).thenReturn(readingQualityFetcher);
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityFetcher.findFirst()).thenReturn(Optional.of(readingQualityRecord));
    }

    private void setDeliverableStub() {
        Formula formula = mock(Formula.class);
        when(deliverable.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(deliverable.getReadingType()).thenReturn(readingType);
        when(deliverable.getName()).thenReturn("Deliverable");
        when(deliverable.getFormula()).thenReturn(formula);
        when(deliverable.getId()).thenReturn(1L);
    }

    private void setValidationRuleStub() {
        when(validationRule.getId()).thenReturn(1L);
        when(validationRule.getDisplayName()).thenReturn("Validation Rule");
        when(validationRule.getRuleSetVersion()).thenReturn(validationRuleSetVersion);
        when(validationRule.getRuleSet()).thenReturn(validationRuleSet);
        when(validationRule.isActive()).thenReturn(true);
        when(validationRule.appliesTo(readingType)).thenReturn(true);
        when(validationRuleSetVersion.getId()).thenReturn(1L);
        when(validationRuleSetVersion.getRuleSet()).thenReturn(validationRuleSet);
        when(validationRuleSet.getId()).thenReturn(1L);
        doReturn(Collections.singletonList(validationRule)).when(validationRuleSet).getRules();
    }

    @Test
    public void testValidationStatusInfoOnMetrologyContract() {
        String json = target("/usagepoints/UP/purposes").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Boolean>get("$.purposes[0].validationInfo.validationActive")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.purposes[0].validationInfo.allDataValidated")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.purposes[0].validationInfo.hasSuspects")).isTrue();
        assertThat(jsonModel.<Number>get("$.purposes[0].validationInfo.lastChecked")).isEqualTo(DAY_BEFORE.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.purposes[0].validationInfo.suspectReason[0].key.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.purposes[0].validationInfo.suspectReason[0].key.displayName")).isEqualTo("Validation Rule");
        assertThat(jsonModel.<Number>get("$.purposes[0].validationInfo.suspectReason[0].key.ruleSetVersion.id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.purposes[0].validationInfo.suspectReason[0].key.ruleSetVersion.ruleSet.id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.purposes[0].validationInfo.suspectReason[0].value")).isEqualTo(1);
    }

    @Test
    public void testUsagePointDeliverablesInfo() {
        String json = target("/usagepoints/UP/purposes/1/outputs").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.outputs[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.outputs[0].name")).isEqualTo("Deliverable");
        assertThat(jsonModel.<Number>get("$.outputs[0].interval.count")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.outputs[0].interval.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.outputs[0].readingType.mRID")).isEqualTo(READING_TYPE_MRID);
        assertThat(jsonModel.<String>get("$.outputs[0].formula.description")).isEqualTo(EXPECTED_FORMULA_DESCRIPTION);
        assertThat(jsonModel.<Boolean>get("$.outputs[0].validationInfo.hasSuspects")).isTrue();
    }

    @Test
    public void testUsagePointSingleDeliverableInfo() {
        String json = target("/usagepoints/UP/purposes/1/outputs/1").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("Deliverable");
        assertThat(jsonModel.<Number>get("$.interval.count")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.interval.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.readingType.mRID")).isEqualTo(READING_TYPE_MRID);
        assertThat(jsonModel.<String>get("$.formula.description")).isEqualTo(EXPECTED_FORMULA_DESCRIPTION);
        assertThat(jsonModel.<Boolean>get("$.validationInfo.validationActive")).isTrue();
        assertThat(jsonModel.<Boolean>get("$.validationInfo.allDataValidated")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.validationInfo.hasSuspects")).isTrue();
        assertThat(jsonModel.<Number>get("$.validationInfo.lastChecked")).isEqualTo(DAY_BEFORE.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.validationInfo.suspectReason[0].key.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.validationInfo.suspectReason[0].key.displayName")).isEqualTo("Validation Rule");
        assertThat(jsonModel.<Number>get("$.validationInfo.suspectReason[0].key.ruleSetVersion.id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.validationInfo.suspectReason[0].key.ruleSetVersion.ruleSet.id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.validationInfo.suspectReason[0].value")).isEqualTo(1);
    }
}
