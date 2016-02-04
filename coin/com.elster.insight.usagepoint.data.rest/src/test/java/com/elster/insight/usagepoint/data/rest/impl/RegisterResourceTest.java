package com.elster.insight.usagepoint.data.rest.impl;


import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.elster.insight.common.rest.IntervalInfo;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

public class RegisterResourceTest extends UsagePointDataRestApplicationJerseyTest {

    public static final String REGISTER_MRID1 = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";

    public static final Instant LAST_READING = Instant.ofEpochMilli(1410786196000L);
    public static final Date LAST_CHECKED = new Date(1409570229000L);
    private static long intervalStart = 1410774630000L;
    private static long intervalEnd = 1410828630000L;

    @Mock
    private Meter meter;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Channel channel;
    @Mock
    private BaseReadingRecord lastReading;
    @Mock
    private ReadingRecord readingRecord, readingRecord2, readingRecord3, readingRecord4;
    @Mock
    private RegisterResourceHelper registerHelper;
    @Mock
    private ValidationEvaluator evaluator;

    public RegisterResourceTest() {
    }

    @Before
    public void setUpStubs() {
        when(meteringService.findMeter("1")).thenReturn(Optional.of(meter));
        when(meteringService.findUsagePoint("1")).thenReturn(Optional.of(usagePoint));

        when(usagePoint.getCurrentMeterActivation()).thenReturn(Optional.of(meterActivation));
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel));
        ReadingType readingType = mockReadingType(REGISTER_MRID1);
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(meteringService.getReadingType(REGISTER_MRID1)).thenReturn(Optional.of(readingType));

        when(channel.getLastDateTime()).thenReturn(LAST_READING);
        when(channel.getReading(LAST_READING)).thenReturn(Optional.of(lastReading));
        when(channel.getMainReadingType()).thenReturn(readingType);
        when(readingType.isCumulative()).thenReturn(false);

        Range<Instant> interval = Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.ofEpochMilli(intervalEnd));
        when(channel.getReadings(interval)).thenReturn(asList(readingRecord, readingRecord2, readingRecord3, readingRecord4));
        when(channel.getReading(Instant.ofEpochMilli(intervalEnd))).thenReturn(Optional.of(readingRecord));
        List myList = asList(readingRecord, readingRecord2, readingRecord3, readingRecord4);
        when(usagePoint.getReadings(interval, readingType)).thenReturn(myList);

        when(readingRecord.getValue()).thenReturn(BigDecimal.valueOf(200, 0));
        when(readingRecord.getTimeStamp()).thenReturn(LAST_READING);
        when(readingRecord.getReadingType()).thenReturn(readingType);
        when(readingRecord2.getValue()).thenReturn(BigDecimal.valueOf(201, 0));
        when(readingRecord2.getTimeStamp()).thenReturn(LAST_READING);
        when(readingRecord2.getReadingType()).thenReturn(readingType);
        when(readingRecord3.getValue()).thenReturn(BigDecimal.valueOf(202, 0));
        when(readingRecord3.getTimeStamp()).thenReturn(LAST_READING);
        when(readingRecord3.getReadingType()).thenReturn(readingType);
        when(readingRecord4.getValue()).thenReturn(BigDecimal.valueOf(203, 0));
        when(readingRecord4.getTimeStamp()).thenReturn(LAST_READING);
        when(readingRecord4.getReadingType()).thenReturn(readingType);
        
        when(channel.getMeterActivation()).thenReturn(meterActivation);
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        
        when(usagePoint.getMeter(any())).thenReturn(Optional.of(meter));
        when(clock.instant()).thenReturn(Instant.now());
        when(channel.getMeterActivation()).thenReturn(meterActivation);
        Range<Instant> intervalToNow = Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.now());
        when(meterActivation.getRange()).thenReturn(intervalToNow);
//        when(evaluator.getValidationStatus(channel, myList, channel.getMeterActivation().getRange().intersection(interval))).thenReturn(new ArrayList());
//        when(evaluator.getValidationStatus(eq(channel), any(), any(Range.class))).thenReturn(new ArrayList());
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(validationService.getEvaluator(eq(meter), any(Range.class))).thenReturn(evaluator);
        when(evaluator.isValidationEnabled(channel)).thenReturn(false);
        when(evaluator.getLastChecked(meter, readingType)).thenReturn(Optional.empty());
        when(evaluator.isAllDataValidated(meterActivation)).thenReturn(false);
        
    }

    @Test
    public void testRegisterData() {
        String filter = URLEncoder.encode("[{\"property\":\"intervalStart\",\"value\":1410774630000},{\"property\":\"intervalEnd\",\"value\":1410828630000}]");

        String json = target("usagepoints/1/registers/" + REGISTER_MRID1 + "/data")
                .queryParam("filter", filter)
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>> get("$.data")).hasSize(4);
        assertThat(jsonModel.<Long> get("$.data[0].readingTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<String> get("$.data[0].value")).isEqualTo("200");

        assertThat(jsonModel.<Long> get("$.data[1].readingTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<String> get("$.data[1].value")).isEqualTo("201");

        assertThat(jsonModel.<Long> get("$.data[2].readingTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<String> get("$.data[2].value")).isEqualTo("202");

        assertThat(jsonModel.<Long> get("$.data[3].readingTime")).isEqualTo(LAST_READING.toEpochMilli());
        assertThat(jsonModel.<String> get("$.data[3].value")).isEqualTo("203");
    }

    @Test
    public void testRegisterInfo() {
        String json = target("usagepoints/1/registers/" + REGISTER_MRID1).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<String> get("$.readingType.mRID")).isEqualTo(REGISTER_MRID1);
        assertThat(jsonModel.<Number> get("$.lastValueTimestamp")).isEqualTo(LAST_READING.toEpochMilli());
    }
    
    @Test
    public void testPutRegisterData() {
        
        when(channel.getReading(Instant.ofEpochMilli(intervalEnd+900000))).thenReturn(Optional.of(lastReading));
        
        RegisterDataInfo registerDataInfo = new RegisterDataInfo();
        registerDataInfo.value = BigDecimal.TEN;
        registerDataInfo.interval = new IntervalInfo();
        registerDataInfo.readingTime = Instant.ofEpochMilli(intervalEnd);

        Response response = target("usagepoints/1/registers/" + REGISTER_MRID1 + "/data/" + intervalEnd).request().put(Entity.json(registerDataInfo));
        verify(channel).editReadings(any());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testChannelDataFiltered() throws UnsupportedEncodingException {
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.VALID);
//        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.VALID);

        String filter = ExtjsFilter.filter().property("intervalStart", 1410774630000L).property("intervalEnd", 1410828630000L).property("suspect", "suspect").create();
        String json = target("usagepoints/1/registers/" + REGISTER_MRID1 + "/data")
                .queryParam("filter", filter)
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }

    @Test
    public void testChannelDataFilteredMatches() throws UnsupportedEncodingException {
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);
        ReadingQualityType readingQualitySuspect = new ReadingQualityType("3.5.258");
        DataValidationStatus statusForSuspect = mockDataValidationStatus(readingQualitySuspect, false);
        when(evaluator.getValidationStatus(eq(channel), any(), any())).thenReturn(Arrays.asList(statusForSuspect));       
        String filter = ExtjsFilter.filter().property("intervalStart", 1410774630000L).property("intervalEnd", 1410828630000L).property("suspect","suspect").create();
        String json = target("usagepoints/1/registers/" + REGISTER_MRID1 + "/data")
                .queryParam("filter", filter)
                .request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(4);
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

    @Test
    public void testValidate() {
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        RegisterTriggerValidationInfo info = new RegisterTriggerValidationInfo();
        info.version = channel.getVersion();
        info.id = REGISTER_MRID1;

        Response response = target("usagepoints/1/registers/" + REGISTER_MRID1 + "/validate")
                .request()
                .put(Entity.json(info));

        assertThat(response.getEntity()).isNotNull();
        verify(validationService).validate(eq(meterActivation), any());
    }
//
    @Test
    public void testValidateWithDate() {
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        UsagePointTriggerValidationInfo info = new UsagePointTriggerValidationInfo();
        info.version = channel.getVersion();
        info.id = REGISTER_MRID1;
        info.lastChecked = LAST_CHECKED.getTime();
        Response response = target("usagepoints/1/channels/" + REGISTER_MRID1 + "/validate")
                .request()
                .put(Entity.json(info));

        assertThat(response.getEntity()).isNotNull();
        verify(validationService).updateLastChecked(channel, LAST_CHECKED.toInstant());
        verify(validationService).validate(eq(meterActivation), any());
    }


}