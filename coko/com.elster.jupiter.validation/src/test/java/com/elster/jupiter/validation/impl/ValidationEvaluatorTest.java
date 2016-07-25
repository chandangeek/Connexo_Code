package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;

import com.google.common.collect.Range;

import java.util.Arrays;
import java.util.Collections;
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
    ChannelsContainer channelsContainer;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Channel channel1, channel2, channel3;

    private final static Set<QualityCodeSystem> SYSTEMS = Collections.singleton(QualityCodeSystem.OTHER);


    @Before
    public void setUp() {
        when(channelsContainer.getChannels()).thenReturn(Arrays.asList(channel1, channel2, channel3));
        when(channelsContainer.getRange()).thenReturn(Range.all());
    }
    @Test
    public void isAllDataValidTestWithNoSuspects(){
        //No suspects
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, channelsContainer)).isFalse();
    }

    @Test
    public void isAllDataValidTestWithSuspectsForEachChannel(){
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, channelsContainer)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel1(){
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, channelsContainer)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel2(){
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, channelsContainer)).isTrue();
    }

    @Test
    public void isAllDataValidTestWithSuspectsInChannel3(){
        when(channel1.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel2.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(false);
        when(channel3.findReadingQualities()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndex(QualityCodeIndex.SUSPECT)
                .inTimeInterval(channelsContainer.getRange())
                .actual()
                .anyMatch()).thenReturn(true);

        assertThat(new ValidationEvaluatorImpl(validationService).areSuspectsPresent(SYSTEMS, channelsContainer)).isTrue();
    }
}
