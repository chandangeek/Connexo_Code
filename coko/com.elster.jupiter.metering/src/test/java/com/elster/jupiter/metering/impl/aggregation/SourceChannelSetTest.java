package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityFetcher;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SourceChannelSetTest {

    @Mock
    private MeteringService meteringService;

    private SourceChannelSetFactory sourceChannelSetFactory;

    @Before
    public void before() {
        this.sourceChannelSetFactory = new SourceChannelSetFactory(meteringService);
    }

    @Test
    public void parseFromString() {
        // Business method
        SourceChannelSet sourceChannelSet = sourceChannelSetFactory.parseFromString("1001,1250,1350");

        // Asserts
        assertThat(sourceChannelSet.getSourceChannelIds()).containsOnly(1001L, 1250L, 1350L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseFromStringUnexpectedFormat() {
        // Business method
        sourceChannelSetFactory.parseFromString("long");

        // Asserts
        // exception is thrown
    }

    @Test
    public void mergeSourceChannelSets() {
        SourceChannelSet sourceChannelSetA = sourceChannelSetFactory.parseFromString("1001,1250,1350");
        SourceChannelSet sourceChannelSetB = sourceChannelSetFactory.parseFromString("1002,1251,1351");

        // Business method
        SourceChannelSet merged = sourceChannelSetFactory.merge(sourceChannelSetA, sourceChannelSetB);

        // Asserts
        assertThat(merged.getSourceChannelIds()).containsOnly(1001L, 1250L, 1350L, 1002L, 1251L, 1351L);
    }

    @Test
    public void fetchReadingQualities() {
        Range<Instant> now = Range.singleton(Instant.now());

        long channelId_1 = 1001L;
        long channelId_2 = 1002L;
        Channel channel_1 = mock(Channel.class);
        Channel channel_2 = mock(Channel.class);
        when(meteringService.findChannel(channelId_1)).thenReturn(Optional.of(channel_1));
        when(meteringService.findChannel(channelId_2)).thenReturn(Optional.of(channel_2));

        ReadingQualityRecord rqr_1 = mockReadingQualityRecord(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT));
        ReadingQualityRecord rqr_2 = mockReadingQualityRecord(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 1001));
        ReadingQualityFetcher readingQualityFetcher_1 = FakeBuilder.initBuilderStub(Arrays.asList(rqr_1, rqr_2), ReadingQualityFetcher.class);
        when(channel_1.findReadingQualities()).thenReturn(readingQualityFetcher_1);

        ReadingQualityRecord rqr_3 = mockReadingQualityRecord(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT));
        ReadingQualityRecord rqr_4 = mockReadingQualityRecord(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1003));
        ReadingQualityFetcher readingQualityFetcher_2 = FakeBuilder.initBuilderStub(Arrays.asList(rqr_3, rqr_4), ReadingQualityFetcher.class);
        when(channel_2.findReadingQualities()).thenReturn(readingQualityFetcher_2);

        SourceChannelSet sourceChannelSet = sourceChannelSetFactory.parseFromString(channelId_1 + "," + channelId_2);

        // Business method
        List<ReadingQualityRecord> readingQualities = sourceChannelSet.fetchReadingQualities(now).collect(Collectors.toList());

        // Asserts
        assertThat(readingQualities).hasSize(4);
        assertThat(readingQualities).containsOnly(rqr_1, rqr_2, rqr_3, rqr_4);
    }

    private ReadingQualityRecord mockReadingQualityRecord(ReadingQualityType readingQualityType) {
        ReadingQualityRecord record = mock(ReadingQualityRecord.class);
        when(record.getType()).thenReturn(readingQualityType);
        return record;
    }
}
