/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.ChannelDataUpdater;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;

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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PrevalidateChannelDataTest extends DeviceDataRestApplicationJerseyTest {

    private static final String DEVICE_NAME = "SPE001";
    private static final Long CHANNEL_ID = 2L;
    private static final String URL = "/devices/" + DEVICE_NAME + "/channels/" + CHANNEL_ID + "/data/prevalidate";

    private static final Instant TIMESTAMP = ZonedDateTime.of(2017, 4, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    @Mock
    private TransactionContext transactionContext;
    @Mock
    private Device device;
    @Mock
    private DeviceValidation deviceValidation;
    @Mock
    private Channel channel;

    @Captor
    private ArgumentCaptor<List<BaseReading>> readingsArgumentCaptor;
    @Captor
    private ArgumentCaptor<Range<Instant>> rangeArgumentCaptor;

    private ChannelDataUpdater channelDataUpdater = FakeBuilder.initBuilderStub(null, ChannelDataUpdater.class);

    @Before
    public void before() {
        when(transactionService.getContext()).thenReturn(transactionContext);
        when(meteringService.findReadingQualityComment(anyLong())).thenReturn(Optional.empty());

        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        when(device.getChannels()).thenReturn(Collections.singletonList(channel));
        when(device.forValidation()).thenReturn(deviceValidation);

        when(channel.getId()).thenReturn(CHANNEL_ID);
        when(channel.startEditingData()).thenReturn(channelDataUpdater);
    }

    @Test
    public void prevalidateEditedChannelData() throws IOException {
        // prepare payload
        PrevalidateChannelDataRequestInfo info = new PrevalidateChannelDataRequestInfo();
        info.validateUntil = TIMESTAMP.plus(5, ChronoUnit.DAYS);
        ChannelDataInfo editedReading = new ChannelDataInfo();
        editedReading.interval = IntervalInfo.from(Range.openClosed(TIMESTAMP, TIMESTAMP.plus(1, ChronoUnit.DAYS)));
        editedReading.value = BigDecimal.ONE;
        ChannelDataInfo estimatedReading = new ChannelDataInfo();
        estimatedReading.interval = IntervalInfo.from(Range.openClosed(TIMESTAMP.plus(3, ChronoUnit.DAYS), TIMESTAMP.plus(4, ChronoUnit.DAYS)));
        estimatedReading.value = BigDecimal.TEN;
        estimatedReading.mainValidationInfo = new MinimalVeeReadingValueInfo();
        estimatedReading.mainValidationInfo.ruleId = 1L;
        estimatedReading.mainValidationInfo.isConfirmed = false;
        info.editedReadings = Arrays.asList(editedReading, estimatedReading);

        // mock validation result
        List<DataValidationStatus> dataValidationStatuses = Arrays.asList(
                mockDataValidationStatus(TIMESTAMP.plus(1, ChronoUnit.DAYS), ValidationResult.SUSPECT, ValidationResult.VALID, mockValidationRule("MinMax")),
                mockDataValidationStatus(TIMESTAMP.plus(2, ChronoUnit.DAYS), ValidationResult.VALID, ValidationResult.VALID),
                mockDataValidationStatus(TIMESTAMP.plus(3, ChronoUnit.DAYS), ValidationResult.VALID, ValidationResult.SUSPECT, mockValidationRule("Missing")),
                mockDataValidationStatus(TIMESTAMP.plus(4, ChronoUnit.DAYS), ValidationResult.SUSPECT, ValidationResult.SUSPECT, mockValidationRule("Consecutive zero values"))
        );
        Range<Instant> expectedValidationRange = Range.closed(Instant.ofEpochMilli(editedReading.interval.end), info.validateUntil);
        when(deviceValidation.getValidationStatus(eq(channel), eq(Collections.emptyList()), eq(expectedValidationRange))).thenReturn(dataValidationStatuses);

        // Business method
        Response response = target(URL).request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // verify that transaction is not committed
        verify(transactionContext).close();
        verify(transactionContext, never()).commit();

        // verify that readings are saved
        verify(channelDataUpdater).editChannelData(readingsArgumentCaptor.capture());
        assertThat(readingsArgumentCaptor.getValue()).hasSize(1);
        assertThat(readingsArgumentCaptor.getValue().get(0).getValue()).isEqualTo(BigDecimal.ONE);
        verify(channelDataUpdater).estimateChannelData(readingsArgumentCaptor.capture());
        assertThat(readingsArgumentCaptor.getValue()).hasSize(1);
        assertThat(readingsArgumentCaptor.getValue().get(0).getValue()).isEqualTo(BigDecimal.TEN);
        verify(channelDataUpdater).complete();

        // verify that validation has been performed on a correct range
        verify(deviceValidation).validateChannel(eq(channel), rangeArgumentCaptor.capture());
        assertThat(rangeArgumentCaptor.getValue()).isEqualTo(expectedValidationRange);

        // verify returned payload
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<?>>get("$.potentialSuspects")).hasSize(3);

        assertThat(jsonModel.<Number>get("$.potentialSuspects[0].readingTime")).isEqualTo(TIMESTAMP.plus(1, ChronoUnit.DAYS).toEpochMilli());
        assertThat(jsonModel.<List<String>>get("$.potentialSuspects[0].validationRules[*].name")).containsExactly("MinMax");
        assertThat(jsonModel.<List<String>>get("$.potentialSuspects[0].bulkValidationRules[*].name")).isEmpty();

        assertThat(jsonModel.<Number>get("$.potentialSuspects[1].readingTime")).isEqualTo(TIMESTAMP.plus(3, ChronoUnit.DAYS).toEpochMilli());
        assertThat(jsonModel.<List<String>>get("$.potentialSuspects[1].validationRules[*].name")).isEmpty();
        assertThat(jsonModel.<List<String>>get("$.potentialSuspects[1].bulkValidationRules[*].name")).containsExactly("Missing");

        assertThat(jsonModel.<Number>get("$.potentialSuspects[2].readingTime")).isEqualTo(TIMESTAMP.plus(4, ChronoUnit.DAYS).toEpochMilli());
        assertThat(jsonModel.<List<String>>get("$.potentialSuspects[2].validationRules[*].name")).containsExactly("Consecutive zero values");
        assertThat(jsonModel.<List<String>>get("$.potentialSuspects[2].bulkValidationRules[*].name")).containsExactly("Consecutive zero values");
    }

    private DataValidationStatus mockDataValidationStatus(Instant readingTime, ValidationResult validationResult, ValidationResult bulkValidationResult, ValidationRule... offendedRules) {
        DataValidationStatus dataValidationStatus = mock(DataValidationStatus.class);
        when(dataValidationStatus.getReadingTimestamp()).thenReturn(readingTime);
        when(dataValidationStatus.getValidationResult()).thenReturn(validationResult);
        when(dataValidationStatus.getBulkValidationResult()).thenReturn(bulkValidationResult);
        if (ValidationResult.SUSPECT == validationResult) {
            when(dataValidationStatus.getOffendedRules()).thenReturn(Arrays.asList(offendedRules));
        }
        if (ValidationResult.SUSPECT == bulkValidationResult) {
            when(dataValidationStatus.getBulkOffendedRules()).thenReturn(Arrays.asList(offendedRules));
        }
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
}
