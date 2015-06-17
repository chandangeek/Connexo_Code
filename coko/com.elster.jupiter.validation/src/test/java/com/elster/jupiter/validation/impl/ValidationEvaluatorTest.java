package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.*;
import com.google.common.collect.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

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

    private final static ReadingQualityType suspect = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT);

    @Before
    public void setUp() {
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2, channel3));
        when(meterActivation.getRange()).thenReturn(Range.all());
    }
    @Test
    public void isAllDataValidTestWithNoSuspects(){
        //No suspects
        when(channel1.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(Collections.emptyList());
        when(channel2.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(Collections.emptyList());
        when(channel3.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(Collections.emptyList());

        assertThat(new ValidationEvaluatorImpl(validationService).isAllDataValid(meterActivation)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsForEachChannel(){
        //No suspects
        List<ReadingQualityRecord> records1 = initRecordsChannel(1);
        List<ReadingQualityRecord> records2 = initRecordsChannel(2);
        List<ReadingQualityRecord> records3 = initRecordsChannel(3);
        when(channel1.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(records1);
        when(channel2.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(records2);
        when(channel3.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(records3);

        assertThat(new ValidationEvaluatorImpl(validationService).isAllDataValid(meterActivation)).isFalse();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel1(){
        //No suspects
        List<ReadingQualityRecord> records1 = initRecordsChannel(123);

        when(channel1.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(records1);
        when(channel2.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(Collections.emptyList());
        when(channel3.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(Collections.emptyList());

        assertThat(new ValidationEvaluatorImpl(validationService).isAllDataValid(meterActivation)).isFalse();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel2(){
        //No suspects
        List<ReadingQualityRecord> records2 = initRecordsChannel(985);

        when(channel1.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(Collections.emptyList());
        when(channel2.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(records2);
        when(channel3.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(Collections.emptyList());

        assertThat(new ValidationEvaluatorImpl(validationService).isAllDataValid(meterActivation)).isFalse();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel3(){
        //No suspects
        List<ReadingQualityRecord> records3 = initRecordsChannel(365);

        when(channel1.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(Collections.emptyList());
        when(channel2.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(Collections.emptyList());
        when(channel3.findActualReadingQuality(suspect, meterActivation.getRange())).thenReturn(records3);

        assertThat(new ValidationEvaluatorImpl(validationService).isAllDataValid(meterActivation)).isFalse();
    }


    private List<ReadingQualityRecord> initRecordsChannel(int numberOfSusptects){
        List<ReadingQualityRecord> records= new ArrayList<>();
        for (int i=0; i < numberOfSusptects; i++){
            ReadingQualityRecord mocked = mock(ReadingQualityRecord.class);
            when(mocked.getTypeCode()).thenReturn(QualityCodeSystem.MDM.name());
            when(mocked.getType()).thenReturn(suspect);
            records.add(mocked);
        }
        return records;
    }

}
