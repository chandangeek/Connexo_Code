/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntervalReadingRecordImplTest {

    private ChannelImpl channel;
    @Mock
    private TimeSeriesEntry timeSeriesEntry;
    private ReadingTypeImpl readingType1, readingType2;
    @Mock
    private DataModel dataModel;
    @Mock
    private IdsService idsService;
    @Mock
    private MeteringService meteringService;
    private Clock clock = Clock.system(ZoneId.systemDefault());
    @Mock
    private EventService eventService;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;
    @Mock
    private Vault vault;
    @Mock
    private RecordSpec recordSpec;

    @Before
    public void setUp() {
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        doReturn(Optional.of(vault)).when(idsService).getVault("MTR", 1);
        doReturn(Optional.of(recordSpec)).when(idsService).getRecordSpec("MTR", 2);
        doReturn(ZoneId.systemDefault()).when(channelsContainer).getZoneId();
        Object[] values = {0L, 0L, BigDecimal.ONE, BigDecimal.TEN};
        doReturn(values).when(timeSeriesEntry).getValues();
        doAnswer(invocation -> values[(int) invocation.getArguments()[0]]).when(timeSeriesEntry).getBigDecimal(anyInt());

        String bulkCode = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .flow(FlowDirection.FORWARD)
                .period(TimeAttribute.MINUTE15)
                .accumulate(Accumulation.BULKQUANTITY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                .code();
        readingType2 = new ReadingTypeImpl(dataModel, thesaurus).init(bulkCode, "bulk");
        String deltaCode = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .flow(FlowDirection.FORWARD)
                .period(TimeAttribute.MINUTE15)
                .accumulate(Accumulation.DELTADELTA)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                .code();
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(deltaCode, "delta");

        channel = new ChannelImpl(dataModel, idsService, meteringService, clock, eventService)
                .init(channelsContainer, Arrays.asList(readingType1, readingType2));
    }

    @Test
    public void testFilterBulk() {
        IntervalReadingRecordImpl intervalReadingRecord = new IntervalReadingRecordImpl(channel, timeSeriesEntry);

        IntervalReadingRecord filtered = intervalReadingRecord.filter(readingType2);

        assertThat((List<ReadingType>) filtered.getReadingTypes()).containsExactly(readingType2);
        assertThat(filtered.getReadingType()).isEqualTo(readingType2);
        assertThat(filtered.getReadingType(0)).isEqualTo(readingType2);
        assertThat(filtered.getValue()).isEqualTo(BigDecimal.TEN);
        assertThat(filtered.getQuantities()).containsExactly(ReadingTypeUnit.WATTHOUR.getUnit().amount(BigDecimal.TEN, 3));
    }

    @Test
    public void testFilterDelta() {
        IntervalReadingRecordImpl intervalReadingRecord = new IntervalReadingRecordImpl(channel, timeSeriesEntry);

        IntervalReadingRecord filtered = intervalReadingRecord.filter(readingType1);

        assertThat((List<ReadingType>) filtered.getReadingTypes()).containsExactly(readingType1);
        assertThat(filtered.getReadingType()).isEqualTo(readingType1);
        assertThat(filtered.getReadingType(0)).isEqualTo(readingType1);
        assertThat(filtered.getValue()).isEqualTo(BigDecimal.ONE);
        assertThat(filtered.getQuantities()).containsExactly(ReadingTypeUnit.WATTHOUR.getUnit().amount(BigDecimal.ONE, 3));
    }

}