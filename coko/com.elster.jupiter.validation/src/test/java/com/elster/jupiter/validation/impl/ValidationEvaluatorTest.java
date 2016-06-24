package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;

import com.google.common.collect.Range;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationEvaluatorTest {
    @Mock
    ValidationServiceImpl validationService;
    @Mock
    MeterActivation meterActivation;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Channel channel1, channel2, channel3;
    @Mock
    ReadingQualityRecord record;

    private final static Set<QualityCodeSystem> SYSTEMS = Collections.singleton(QualityCodeSystem.OTHER);


    @Before
    public void setUp() {
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2, channel3));
        when(meterActivation.getRange()).thenReturn(Range.all());
    }
    @Test
    public void isAllDataValidTestWithNoSuspects(){
        //No suspects
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.empty());
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.empty());
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.empty());

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, meterActivation)).isFalse();
    }

    @Test
    public void isAllDataValidTestWithSuspectsForEachChannel(){
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.of(record));
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.of(record));
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.of(record));

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, meterActivation)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel1(){
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.of(record));
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.empty());
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.empty());

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, meterActivation)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel2(){
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.empty());
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.of(record));
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.empty());

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, meterActivation)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel3(){
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.empty());
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.empty());
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(meterActivation.getRange())
                .actual()
                .findFirst()).thenReturn(Optional.of(record));

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, meterActivation)).isTrue();
    }
}
