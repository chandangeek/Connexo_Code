package com.elster.insight.usagepoint.data.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.google.common.collect.Range;

public class UsagePointValidationResourceTest extends UsagePointDataRestApplicationJerseyTest {

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    public static final long DEVICE_ID = 56854L;
    public static final String CHANNEL_MRID1 = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String REGISTER_MRID1 = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final Instant NOW = ZonedDateTime.of(2015, 12, 10, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();
    public static final Instant LAST_READING = ZonedDateTime.of(2015, 12, 9, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();
    private static long intervalStart = 1410774630000L;

    @Mock
    private ValidationEvaluator evaluator;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private Meter meter;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Channel channel, register;
    @Mock
    private ReadingQuality suspect, notSuspect;
    @Mock
    private IntervalReadingRecord irr1, irr2, irr3, irr4;

    @Before
    public void setUp1() {
        when(meteringService.findUsagePoint("MRID")).thenReturn(Optional.of(usagePoint));

        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        
        when(usagePoint.getMeter(any())).thenReturn(Optional.of(meter));
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getCreateDate()).thenReturn(Instant.now().minusSeconds(60*60*24));
        when(usagePoint.getModificationDate()).thenReturn(Instant.now().minusSeconds(60*60*5));
        when(usagePoint.getDetail(any(Instant.class))).thenReturn(Optional.empty());
        when(usagePoint.getCurrentMeterActivation()).thenReturn(Optional.of(meterActivation));
        when(usagePoint.getInstallationTime()).thenReturn(Instant.EPOCH);
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.empty());

        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        when(extension.getAllCustomPropertySets()).thenReturn(Collections.emptyList());
        when(usagePoint.forCustomProperties()).thenReturn(extension);
      
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        
        Range<Instant> intervalToNow = Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.now());
        when(meterActivation.getRange()).thenReturn(intervalToNow);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel, register));
        
        when(register.isRegular()).thenReturn(false);
        when(channel.isRegular()).thenReturn(true);
        when(register.getMeterActivation()).thenReturn(meterActivation);
        when(channel.getMeterActivation()).thenReturn(meterActivation);
        when(channel.getReadings(any())).thenReturn(Arrays.asList(irr1, irr2, irr3, irr4));
        ReadingType channelReadingType = mockReadingType(CHANNEL_MRID1);
        
        when(irr1.getValue()).thenReturn(BigDecimal.valueOf(200, 0));
        when(irr1.getTimeStamp()).thenReturn(LAST_READING);
        when(irr1.getReadingType()).thenReturn(channelReadingType);
        when(irr2.getValue()).thenReturn(BigDecimal.valueOf(202, 0));
        when(irr2.getTimeStamp()).thenReturn(LAST_READING);
        when(irr2.getReadingType()).thenReturn(channelReadingType);
        when(irr3.getValue()).thenReturn(BigDecimal.valueOf(204, 0));
        when(irr3.getTimeStamp()).thenReturn(LAST_READING);
        when(irr3.getReadingType()).thenReturn(channelReadingType);
        when(irr4.getValue()).thenReturn(BigDecimal.valueOf(205, 0));
        when(irr4.getTimeStamp()).thenReturn(LAST_READING);
        when(irr4.getReadingType()).thenReturn(channelReadingType);
        
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(validationService.getEvaluator(eq(meter), any(Range.class))).thenReturn(evaluator);
        when(validationService.getLastChecked(any(MeterActivation.class))).thenReturn(Optional.of(NOW));
        when(suspect.getTypeCode()).thenReturn("3.5.258");
        when(notSuspect.getTypeCode()).thenReturn("0.0.0");
        when(suspect.getType()).thenReturn(new ReadingQualityType("3.5.258"));
        when(notSuspect.getType()).thenReturn(new ReadingQualityType("0.0.0"));
        
        ReadingQualityType readingQualitySuspect = new ReadingQualityType("3.5.258");
        DataValidationStatus statusForSuspect = mockDataValidationStatus(readingQualitySuspect, false);
        when(evaluator.getValidationStatus(eq(channel), any(), any())).thenReturn(Arrays.asList(statusForSuspect, statusForSuspect, statusForSuspect, statusForSuspect));
        when(evaluator.getValidationStatus(eq(register), any(), any())).thenReturn(Arrays.asList(statusForSuspect, statusForSuspect, statusForSuspect, statusForSuspect, statusForSuspect));
    }

    @Test
    public void testGetValidationFeatureStatusCheckRegisterCount() {

        UsagePointValidationStatusInfo response = target("usagepoints/MRID/validationrulesets/validationstatus").request().get(UsagePointValidationStatusInfo.class);

        assertThat(response.registerSuspectCount).isEqualTo(5);
    }

    @Test
    public void testGetValidationFeatureStatusCheckChannelCount() {

        UsagePointValidationStatusInfo response = target("usagepoints/MRID/validationrulesets/validationstatus").request().get(UsagePointValidationStatusInfo.class);

        assertThat(response.channelSuspectCount).isEqualTo(4);
    }

    private DataValidationStatus mockDataValidationStatus(ReadingQualityType readingQualityType, boolean isBulk) {
        DataValidationStatus status = mock(DataValidationStatus.class);
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.getType()).thenReturn(readingQualityType);
        List<? extends ReadingQualityRecord> readingQualities = Arrays.asList(readingQualityRecord);
        if (isBulk) {
            doReturn(readingQualities).when(status).getBulkReadingQualities();
        } else {
            doReturn(readingQualities).when(status).getReadingQualities();
        }
        return status;
    }
}