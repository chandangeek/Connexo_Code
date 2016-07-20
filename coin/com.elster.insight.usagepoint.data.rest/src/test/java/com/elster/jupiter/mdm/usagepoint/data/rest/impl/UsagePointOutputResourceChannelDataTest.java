package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceChannelDataTest extends UsagePointDataRestApplicationJerseyTest {

    @Mock
    private UsagePoint usagePoint;

    @Test
    public void testChannelReadingsOfOutput() throws Exception {
        when(meteringService.findUsagePoint("MRID")).thenReturn(Optional.of(usagePoint));

        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfigurationWithContract(42L, "Test mc");
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.of(metrologyConfiguration));
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(usagePoint.getEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfiguration));
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(effectiveMetrologyConfiguration.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer));
        Channel channel = mock(Channel.class);
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        mockIntervalReadingWithValidationResult(channel);

        // Business method
        String filter = ExtjsFilter.filter().property("intervalStart", 1468846440000L).property("intervalEnd", 1500382440000L).create();
        String json = target("usagepoints/MRID/purposes/1/outputs/1/channelData").queryParam("filter", filter).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Long>get("$.channelData[0].interval.start")).isEqualTo(1468875600000L);
        assertThat(jsonModel.<Long>get("$.channelData[0].interval.end")).isEqualTo(1468962000000L);
        assertThat(jsonModel.<Long>get("$.channelData[0].readingTime")).isEqualTo(1468962000000L);
        assertThat(jsonModel.<String>get("$.channelData[0].value")).isEqualTo("10");
        assertThat(jsonModel.<Boolean>get("$.channelData[0].dataValidated")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.channelData[0].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<String>get("$.channelData[0].action")).isEqualTo("FAIL");
    }

    private void mockIntervalReadingWithValidationResult(Channel channel) {
        IntervalReadingRecord intervalReadingRecord = mock(IntervalReadingRecord.class);
        ReadingQualityRecord quality = mock(ReadingQualityRecord.class);
        ValidationEvaluator evaluator = mock(ValidationEvaluator.class);
        DataValidationStatus validationStatus = mock(DataValidationStatus.class);
        ValidationRule validationRule = mock(ValidationRule.class);
        ValidationRuleSet validationRuleSet = mock(ValidationRuleSet.class);
        ValidationRuleSetVersion ruleSetVersion = mock(ValidationRuleSetVersion.class);
        ReadingQualityType qualityType = new ReadingQualityType("3.5.258");

        when(intervalReadingRecord.getTimePeriod()).thenReturn(Optional.of(Range.openClosed(Instant.ofEpochMilli(1468875600000L), Instant.ofEpochMilli(1468962000000L))));
        when(intervalReadingRecord.getTimeStamp()).thenReturn(Instant.ofEpochMilli((1468962000000L)));
        when(intervalReadingRecord.getValue()).thenReturn(BigDecimal.valueOf(10L));
        when(quality.getType()).thenReturn(qualityType);
        doReturn(Collections.singletonList(quality)).when(validationStatus).getReadingQualities();
        List<IntervalReadingRecord> intervalReadings = Collections.singletonList(intervalReadingRecord);
        when(channel.getIntervalReadings(any(Range.class))).thenReturn(intervalReadings);
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(validationStatus.getReadingTimestamp()).thenReturn(Instant.ofEpochMilli((1468962000000L)));
        when(validationStatus.completelyValidated()).thenReturn(true);
        when(validationStatus.getValidationResult()).thenReturn(ValidationResult.SUSPECT);
        when(validationStatus.getOffendedRules()).thenReturn(Collections.singletonList(validationRule));
        when(validationRule.getId()).thenReturn(1L);
        when(validationRule.getDisplayName()).thenReturn("testRule");
        when(validationRule.getRuleSet()).thenReturn(validationRuleSet);
        when(validationRule.getRuleSetVersion()).thenReturn(ruleSetVersion);
        when(ruleSetVersion.getRuleSet()).thenReturn(validationRuleSet);
        when(evaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM, QualityCodeSystem.MDC), channel, intervalReadings,
                Range.openClosed(Instant.ofEpochMilli(1468846440000L), Instant.ofEpochMilli(1500382440000L))))
                .thenReturn(Collections.singletonList(validationStatus));
    }
}
