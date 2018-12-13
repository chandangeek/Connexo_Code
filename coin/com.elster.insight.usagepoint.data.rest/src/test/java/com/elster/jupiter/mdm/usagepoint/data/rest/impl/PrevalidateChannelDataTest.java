/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PrevalidateChannelDataTest extends UsagePointDataRestApplicationJerseyTest {

    private static final String USAGEPOINT_NAME = "UP001";
    private static final Long CONTRACT_ID = 1L;
    private static final Long OUTPUT_ID = 2L;
    private static final String URL = "/usagepoints/" + USAGEPOINT_NAME + "/purposes/" + CONTRACT_ID + "/outputs/" + OUTPUT_ID + "/channelData/prevalidate";

    private static final Instant TIMESTAMP = ZonedDateTime.of(2017, 4, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    @Mock
    private TransactionContext transactionContext;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private ReadingType readingType;
    @Mock
    private Channel channel;
    @Mock
    private ValidationEvaluator validationEvaluator;

    @Captor
    private ArgumentCaptor<List<BaseReading>> readingsArgumentCaptor;
    @Captor
    private ArgumentCaptor<Range<Instant>> rangeArgumentCaptor;
    @Captor
    private ArgumentCaptor<ValidationContext> validationContextArgumentCaptor;

    private DataAggregationService.MetrologyContractDataEditor editor = FakeBuilder.initBuilderStub(null, DataAggregationService.MetrologyContractDataEditor.class);

    @Before
    public void before() {
        when(transactionService.getContext()).thenReturn(transactionContext);
        when(meteringService.findReadingQualityComment(anyLong())).thenReturn(Optional.empty());
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);

        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(CONTRACT_ID, readingType, OUTPUT_ID);
        UsagePoint usagePoint = mockUsagePoint(USAGEPOINT_NAME, metrologyConfiguration);
        ChannelsContainer channelsContainer = mockChannelsContainer(usagePoint);
        when(dataAggregationService.edit(eq(usagePoint), any(MetrologyContract.class), any(ReadingTypeDeliverable.class), eq(QualityCodeSystem.MDM))).thenReturn(editor);

        when(channelsContainer.getChannel(readingType)).thenReturn(Optional.of(channel));
    }

    private ChannelsContainer mockChannelsContainer(UsagePoint usagePoint) {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = usagePoint.getCurrentEffectiveMetrologyConfiguration().get();
        MetrologyContract metrologyContract = effectiveMC.getMetrologyConfiguration().getContracts().get(0);
        when(effectiveMC.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.atLeast(TIMESTAMP)));
        return channelsContainer;
    }

    private UsagePointMetrologyConfiguration mockMetrologyConfiguration(long contractId, ReadingType readingType, long outputId) {
        ReadingTypeDeliverable readingTypeDeliverable = mock(ReadingTypeDeliverable.class);
        when(readingTypeDeliverable.getId()).thenReturn(outputId);
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);
        when(readingType.isRegular()).thenReturn(true);

        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        when(metrologyContract.getId()).thenReturn(contractId);
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(readingTypeDeliverable));
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);

        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        return metrologyConfiguration;
    }

    private UsagePoint mockUsagePoint(String name, UsagePointMetrologyConfiguration metrologyConfiguration) {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getName()).thenReturn(name);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC));
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Collections.singletonList(effectiveMC));
        when(effectiveMC.getUsagePoint()).thenReturn(usagePoint);
        when(meteringService.findUsagePointByName(name)).thenReturn(Optional.of(usagePoint));
        return usagePoint;
    }

    private DataValidationStatus mockDataValidationStatus(Instant readingTime, ValidationResult validationResult, ValidationRule... offendedRules) {
        DataValidationStatus dataValidationStatus = mock(DataValidationStatus.class);
        when(dataValidationStatus.getReadingTimestamp()).thenReturn(readingTime);
        when(dataValidationStatus.getValidationResult()).thenReturn(validationResult);
        when(dataValidationStatus.getOffendedRules()).thenReturn(Arrays.asList(offendedRules));
        return dataValidationStatus;
    }

    private ValidationRule mockValidationRule(String name) {
        ValidationRuleSet validationRuleSet = mock(ValidationRuleSet.class);
        ValidationRuleSetVersion ruleSetVersion = mock(ValidationRuleSetVersion.class);
        when(ruleSetVersion.getId()).thenReturn(1L);
        when(ruleSetVersion.getVersion()).thenReturn(1L);
        when(ruleSetVersion.getRuleSet()).thenReturn(validationRuleSet);
        ValidationRule validationRule = mock(ValidationRule.class);
        when(validationRule.getName()).thenReturn(name);
        when(validationRule.getRuleSetVersion()).thenReturn(ruleSetVersion);
        when(validationRule.getRuleSet()).thenReturn(validationRuleSet);
        return validationRule;
    }

    @Test
    public void prevalidateEditedChannelData() throws IOException {
        // prepare payload
        PrevalidateChannelDataRequestInfo info = new PrevalidateChannelDataRequestInfo();
        info.validateUntil = TIMESTAMP.plus(5, ChronoUnit.DAYS);
        OutputChannelDataInfo editedReading = new OutputChannelDataInfo();
        editedReading.interval = IntervalInfo.from(Range.openClosed(TIMESTAMP, TIMESTAMP.plus(1, ChronoUnit.DAYS)));
        editedReading.value = BigDecimal.ONE;
        OutputChannelDataInfo estimatedReading = new OutputChannelDataInfo();
        estimatedReading.interval = IntervalInfo.from(Range.openClosed(TIMESTAMP.plus(3, ChronoUnit.DAYS), TIMESTAMP.plus(4, ChronoUnit.DAYS)));
        estimatedReading.value = BigDecimal.TEN;
        estimatedReading.ruleId = 1L;
        estimatedReading.isConfirmed = false;
        info.editedReadings = Arrays.asList(editedReading, estimatedReading);

        // mock validation result
        List<DataValidationStatus> dataValidationStatuses = Arrays.asList(
                mockDataValidationStatus(TIMESTAMP.plus(1, ChronoUnit.DAYS), ValidationResult.SUSPECT, mockValidationRule("MinMax")),
                mockDataValidationStatus(TIMESTAMP.plus(2, ChronoUnit.DAYS), ValidationResult.VALID),
                mockDataValidationStatus(TIMESTAMP.plus(3, ChronoUnit.DAYS), ValidationResult.SUSPECT, mockValidationRule("Missing")),
                mockDataValidationStatus(TIMESTAMP.plus(4, ChronoUnit.DAYS), ValidationResult.VALID)
        );
        Range<Instant> expectedValidationRange = Range.closed(Instant.ofEpochMilli(editedReading.interval.end), info.validateUntil);
        when(validationEvaluator.getValidationStatus(eq(ImmutableSet.of(QualityCodeSystem.MDM)), eq(channel), anyList(), eq(expectedValidationRange))).thenReturn(dataValidationStatuses);

        // Business method
        Response response = target(URL).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // verify that transaction is not committed
        verify(transactionContext).close();
        verify(transactionContext, never()).commit();

        // verify that readings are saved
        verify(editor).updateAll(readingsArgumentCaptor.capture());
        assertThat(readingsArgumentCaptor.getValue()).hasSize(1);
        assertThat(readingsArgumentCaptor.getValue().get(0).getValue()).isEqualTo(BigDecimal.ONE);

        verify(editor).estimateAll(readingsArgumentCaptor.capture());
        assertThat(readingsArgumentCaptor.getValue()).hasSize(1);
        assertThat(readingsArgumentCaptor.getValue().get(0).getValue()).isEqualTo(BigDecimal.TEN);
        verify(editor).save();

        // verify that validation has been performed on a correct range
        verify(validationService).validate(validationContextArgumentCaptor.capture(), rangeArgumentCaptor.capture());
        assertThat(validationContextArgumentCaptor.getValue().getChannelsContainer()).isEqualTo(channelsContainer);
        assertThat(validationContextArgumentCaptor.getValue().getReadingType()).isEqualTo(Optional.of(readingType));
        assertThat(validationContextArgumentCaptor.getValue().getQualityCodeSystems()).isEqualTo(ImmutableSet.of(QualityCodeSystem.MDM));
        assertThat(rangeArgumentCaptor.getValue()).isEqualTo(expectedValidationRange);

        // verify returned payload
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<?>>get("$.potentialSuspects")).hasSize(2);
        assertThat(jsonModel.<List<Number>>get("$.potentialSuspects[*].readingTime"))
                .containsExactly(TIMESTAMP.plus(1, ChronoUnit.DAYS).toEpochMilli(), TIMESTAMP.plus(3, ChronoUnit.DAYS).toEpochMilli());
        assertThat(jsonModel.<List<String>>get("$.potentialSuspects[*].validationRules[*].name")).containsExactly("MinMax", "Missing");
    }
}
