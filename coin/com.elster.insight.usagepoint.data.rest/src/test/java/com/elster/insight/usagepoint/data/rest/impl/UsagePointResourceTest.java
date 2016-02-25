package com.elster.insight.usagepoint.data.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.SecurityContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.google.common.collect.Range;

public class UsagePointResourceTest extends UsagePointDataRestApplicationJerseyTest {

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    public static final Instant NOW = ZonedDateTime.of(2015, 12, 10, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();
    public static final Instant LAST_READING = ZonedDateTime.of(2015, 12, 9, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();
    private static long intervalStart = 1410774630000L;

    @Mock
    private ValidationEvaluator evaluator;

    @Mock
    private User principal;
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
        when(usagePoint.getServiceLocation()).thenReturn(Optional.empty());
        when(usagePoint.getMRID()).thenReturn("MRID");
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
        
        when(usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(any())).thenReturn(Optional.empty());
        
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
    public void testGetUsagePointInfo() {
        
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.hasPrivilege(any(String.class), any(String.class))).thenReturn(true);
        UsagePointInfo response = target("usagepoints/MRID").request().get(UsagePointInfo.class);

        assertThat(response.mRID).isEqualTo("MRID");
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