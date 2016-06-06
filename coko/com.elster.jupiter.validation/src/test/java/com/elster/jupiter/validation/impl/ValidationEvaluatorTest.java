package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;

import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationEvaluatorTest {
    @Mock
    ValidationServiceImpl validationService;
    @Mock
    MeterActivation meterActivation;
    @Mock
    Channel channel1, channel2, channel3;

    private final static Set<QualityCodeSystem> SYSTEM = Collections.singleton(QualityCodeSystem.OTHER);


    @Before
    public void setUp() {
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2, channel3));
        when(meterActivation.getRange()).thenReturn(Range.all());
    }
    @Test
    public void isAllDataValidTestWithNoSuspects(){
        //No suspects
        when(channel1.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(Collections.emptyList());
        when(channel2.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(Collections.emptyList());
        when(channel3.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(Collections.emptyList());

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEM, meterActivation)).isFalse();
    }

    @Test
    public void isAllDataValidTestWithSuspectsForEachChannel(){
        //No suspects
        List<ReadingQualityRecord> records1 = initRecordsChannel(1);
        List<ReadingQualityRecord> records2 = initRecordsChannel(2);
        List<ReadingQualityRecord> records3 = initRecordsChannel(3);
        when(channel1.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(records1);
        when(channel2.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(records2);
        when(channel3.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(records3);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEM, meterActivation)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel1(){
        //No suspects
        List<ReadingQualityRecord> records1 = initRecordsChannel(123);

        when(channel1.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(records1);
        when(channel2.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(Collections.emptyList());
        when(channel3.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(Collections.emptyList());

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEM, meterActivation)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel2(){
        //No suspects
        List<ReadingQualityRecord> records2 = initRecordsChannel(985);

        when(channel1.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(Collections.emptyList());
        when(channel2.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(records2);
        when(channel3.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(Collections.emptyList());

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEM, meterActivation)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel3(){
        //No suspects
        List<ReadingQualityRecord> records3 = initRecordsChannel(365);

        when(channel1.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(Collections.emptyList());
        when(channel2.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(Collections.emptyList());
        when(channel3.findReadingQualities(SYSTEM, QualityCodeIndex.SUSPECT, meterActivation.getRange(), true, false)).thenReturn(records3);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEM, meterActivation)).isTrue();
    }

    private List<ReadingQualityRecord> initRecordsChannel(int numberOfSuspects) {
        List<ReadingQualityRecord> records = new ArrayList<>(numberOfSuspects);
        for (int i = 0; i < numberOfSuspects; i++) {
            records.add(mock(ReadingQualityRecord.class));
        }
        return records;
    }
}
