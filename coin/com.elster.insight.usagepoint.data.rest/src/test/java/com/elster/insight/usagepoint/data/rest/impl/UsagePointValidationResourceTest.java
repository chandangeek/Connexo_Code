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
    private Thesaurus thesaurus;
    @Mock
    private AmrSystem mdcAmrSystem;
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
    private DataValidationStatus validationStatus1, validationStatus2, validationStatus3, validationStatus4, validationStatus5, validationStatus6;
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
        doModelStubbing();

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


    private void doModelStubbing() {
//        when(usagePoint.getRegisters()).thenReturn(Arrays.asList(register1));
//        when(register1.getReadingType()).thenReturn(regReadingType);
//        when(register1.getRegisterSpec()).thenReturn(registerSpec);
//        when(registerSpec.getReadingType()).thenReturn(regReadingType);
//        when(regReadingType.getMRID()).thenReturn("REG1");
//        when(regReadingType.getAliasName()).thenReturn("Sum+");
//        when(channelReadingType1.getMRID()).thenReturn("CH1");
//        when(channelReadingType2.getMRID()).thenReturn("CH2");

//        when(usagePoint.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1));
//        when(loadProfile1.getChannels()).thenReturn(Arrays.asList(ch1, ch2));
//        when(loadProfile1.getId()).thenReturn(1L);
//        when(loadProfile1.getLoadProfileSpec()).thenReturn(loadProfileSpec1);
//        when(loadProfileSpec1.getLoadProfileType()).thenReturn(loadProfileType1);
//        when(loadProfileType1.getName()).thenReturn("Profile1");
//
//        when(ch1.getReadingType()).thenReturn(channelReadingType1);
//        when(ch2.getReadingType()).thenReturn(channelReadingType2);

        
        ZonedDateTime fromReg = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        ZonedDateTime from = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(2);
        ZonedDateTime to = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusDays(10);
        when(meterActivation.getInterval()).thenReturn(Interval.endAt(from.toInstant()));
        Range<Instant> intervalToNow = Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.now());
        when(meterActivation.getRange()).thenReturn(intervalToNow);
        
//        when(channel1.getMeterActivation()).thenReturn(meterActivation);
//        when(channel2.getMeterActivation()).thenReturn(meterActivation);
//        when(channel3.getMeterActivation()).thenReturn(meterActivation);
//        when(validationService.getLastChecked(meterActivation)).thenReturn(Optional.of(NOW));
//        when(channel1.getMainReadingType()).thenReturn(regReadingType);
//        when(channel2.getMainReadingType()).thenReturn(channelReadingType1);
//        when(channel3.getMainReadingType()).thenReturn(channelReadingType2);
//        when(channel4.getMainReadingType()).thenReturn(regReadingType);
//        when(channel5.getMainReadingType()).thenReturn(channelReadingType1);
//        when(channel6.getMainReadingType()).thenReturn(channelReadingType2);
//        when(channel7.getMainReadingType()).thenReturn(regReadingType);
//        when(channel8.getMainReadingType()).thenReturn(channelReadingType1);
//        when(channel9.getMainReadingType()).thenReturn(channelReadingType2);
//        doReturn(Arrays.asList(regReadingType)).when(channel1).getReadingTypes();
//        doReturn(Arrays.asList(channelReadingType1)).when(channel2).getReadingTypes();
//        doReturn(Arrays.asList(channelReadingType2)).when(channel3).getReadingTypes();
//        doReturn(Arrays.asList(regReadingType)).when(channel4).getReadingTypes();
//        doReturn(Arrays.asList(channelReadingType1)).when(channel5).getReadingTypes();
//        doReturn(Arrays.asList(channelReadingType2)).when(channel6).getReadingTypes();
//        doReturn(Arrays.asList(regReadingType)).when(channel7).getReadingTypes();
//        doReturn(Arrays.asList(channelReadingType1)).when(channel8).getReadingTypes();
//        doReturn(Arrays.asList(channelReadingType2)).when(channel9).getReadingTypes();
        

        Interval regInterval1 = new Interval(Date.from(fromReg.toInstant()), Date.from(to.toInstant()));
        Instant toNow = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant();
        Range<Instant> wholeRegInterval = Range.openClosed(fromReg.toInstant(), toNow);
//        when(deviceValidation.getValidationStatus(eq(register1), anyList(), eq(wholeRegInterval))).thenReturn(Arrays.asList(validationStatus1, validationStatus2, validationStatus3));
        doReturn(Arrays.asList(suspect, suspect)).when(validationStatus1).getReadingQualities();
        doReturn(Arrays.asList(suspect, suspect)).when(validationStatus2).getReadingQualities();
        doReturn(Arrays.asList(notSuspect, suspect)).when(validationStatus3).getReadingQualities();

        ZonedDateTime fromCh = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        Range wholeInterval = Range.openClosed(fromCh.toInstant(), toNow);
//        when(deviceValidation.getValidationStatus(eq(ch1), anyList(), eq(wholeInterval))).thenReturn(Arrays.asList(validationStatus4));
//        when(deviceValidation.getValidationStatus(eq(ch2), anyList(), eq(wholeInterval))).thenReturn(Arrays.asList(validationStatus5, validationStatus6));
        doReturn(Arrays.asList(suspect, suspect)).when(validationStatus4).getReadingQualities();
        doReturn(Arrays.asList(suspect, notSuspect)).when(validationStatus5).getReadingQualities();
        doReturn(Arrays.asList(notSuspect, suspect)).when(validationStatus6).getReadingQualities();

//        when(deviceValidation.getLastChecked()).thenReturn(Optional.<Instant>empty());

        ValidationRuleSet ruleSet = mockValidationRuleSet(1,true);
        doReturn(ruleSet.getRules()).when(validationStatus4).getOffendedRules();
        doReturn(ruleSet.getRules()).when(validationStatus2).getOffendedRules();
//        when(usagePoint.getDeviceType()).thenReturn(deviceType);
//        when(usagePoint.getDeviceConfiguration()).thenReturn(deviceConfiguration);
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

    private ValidationRuleSet mockValidationRuleSet(int id, boolean version) {
        ValidationRuleSet ruleSet = mock(ValidationRuleSet.class);
        when(ruleSet.getId()).thenReturn(Long.valueOf(id));
        when(ruleSet.getName()).thenReturn("MyName");
        when(ruleSet.getDescription()).thenReturn("MyDescription");
        if (version) {
            ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(11, ruleSet);
            List versions = Arrays.asList(ruleSetVersion);
            when(ruleSet.getRuleSetVersions()).thenReturn(versions);

            List rules = Arrays.asList(mockValidationRuleInRuleSetVersion(20, ruleSet, ruleSetVersion));
            when(ruleSetVersion.getRules()).thenReturn(rules);
            when(ruleSet.getRules()).thenReturn(rules);
        }

        doReturn(Optional.of(ruleSet)).when(validationService).getValidationRuleSet(id);

        return ruleSet;
    }

    private ValidationRuleSetVersion mockValidationRuleSetVersion(long id, ValidationRuleSet ruleSet){
        ValidationRuleSetVersion ruleSetVersion = mock(ValidationRuleSetVersion.class);
        when(ruleSetVersion.getDescription()).thenReturn("descriptionOfVersion");
        when(ruleSetVersion.getId()).thenReturn(id);
        when(ruleSetVersion.getStartDate()).thenReturn(ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant());
        when(ruleSetVersion.getRuleSet()).thenReturn(ruleSet);
        return ruleSetVersion;
    }

    private ValidationRule mockValidationRuleInRuleSetVersion(long id, ValidationRuleSet ruleSet, ValidationRuleSetVersion ruleSetVersion) {
        ValidationRule rule = mock(ValidationRule.class);
        when(rule.getName()).thenReturn("MyRule");
        when(rule.getId()).thenReturn(id);
        when(rule.getAction()).thenReturn(ValidationAction.FAIL);
        when(rule.getImplementation()).thenReturn("com.blablabla.Validator");
        when(rule.getDisplayName()).thenReturn("My rule");
        when(rule.isActive()).thenReturn(true);
        when(rule.getRuleSetVersion()).thenReturn(ruleSetVersion);
        when(rule.getRuleSet()).thenReturn(ruleSet);


        Map<String, Object> props = new HashMap<>();
        props.put("number", 13);
        props.put("nullableboolean", true);
        props.put("boolean", false);
        props.put("text", "string");
        when(rule.getProps()).thenReturn(props);

        return rule;
    }
}