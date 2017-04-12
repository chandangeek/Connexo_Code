/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractBaseReadingImplTest {

    private static final Instant DATE = ZonedDateTime.of(2013, 9, 20, 10, 11, 14, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant RECORD_DATE = ZonedDateTime.of(2013, 9, 19, 10, 11, 14, 0, ZoneId.systemDefault()).toInstant();
    private static final BigDecimal VALUE = new BigDecimal("14.15");
    private BaseReadingRecordImpl baseReading;

    @Mock
    private TimeSeriesEntry entry;
    @Mock
    private Meter meter;
    private MeterActivationImpl meterActivation;
    private ChannelImpl channel;
    private IReadingType readingType, readingType1, readingType2, unknownReadingType;
    private Clock clock = Clock.systemDefaultZone();
    @Mock
    private DataModel dataModel;
    @Mock
    private IdsService idsService;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private EventService eventService;
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
        when(meteringService.getClock()).thenReturn(clock);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(dataModel.getInstance(ReadingTypeInChannel.class)).then(invocation -> new ReadingTypeInChannel(dataModel, meteringService));
        when(meter.getConfiguration(any())).thenReturn(Optional.empty());
        when(idsService.getVault(anyString(), anyInt())).thenReturn(Optional.of(vault));
        when(idsService.getRecordSpec(anyString(), anyInt())).thenReturn(Optional.of(recordSpec));
        when(entry.getTimeStamp()).thenReturn(DATE);
        when(entry.getRecordDateTime()).thenReturn(RECORD_DATE);
        when(entry.getBigDecimal(anyInt())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return BigDecimal.valueOf((long) (int) invocationOnMock.getArguments()[0]);
            }
        });
        final Provider<ChannelImpl> channelFactory = () -> new ChannelImpl(dataModel, idsService, meteringService, clock, eventService);
        final Provider<ChannelBuilder> channelBuilder = () -> new ChannelBuilderImpl(dataModel, channelFactory);
        when(dataModel.getInstance(MeterActivationChannelsContainerImpl.class)).then(invocation -> new MeterActivationChannelsContainerImpl(meteringService, eventService, channelBuilder));
        when(meter.getHeadEndInterface()).thenReturn(Optional.empty());
        meterActivation = new MeterActivationImpl(dataModel, eventService, clock, thesaurus).init(meter, null, null, Instant.EPOCH);
        meterActivation.save();
        ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_PRIMARY_METERED)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                .flow(FlowDirection.FORWARD);
        readingType = new ReadingTypeImpl(dataModel, thesaurus).init(builder.code(), "");
        builder.flow(FlowDirection.REVERSE);
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(builder.code(), "");
        builder.flow(FlowDirection.NET);
        readingType2 = new ReadingTypeImpl(dataModel, thesaurus).init(builder.code(), "");
        builder.measure(MeasurementKind.DEMAND).in(MetricMultiplier.KILO, ReadingTypeUnit.WATT);
        unknownReadingType = new ReadingTypeImpl(dataModel, thesaurus).init(builder.code(), "");
        channel = (ChannelImpl) meterActivation.getChannelsContainer().createChannel(readingType1, readingType2, readingType);

        when(entry.getLong(0)).thenReturn(1L << ProcessStatus.Flag.SUSPECT.ordinal());
        baseReading = createInstanceToTest(channel, entry);
        when(entry.size()).thenReturn(3 + baseReading.getReadingTypeOffset());

    }

    @After
    public void tearDown() {

    }

    abstract BaseReadingRecordImpl createInstanceToTest(ChannelImpl channel, TimeSeriesEntry entry);

    @Test
    public void testGetChannel() {
        assertThat(baseReading.getChannel()).isEqualTo(channel);
    }

    @Test
    public void testGetEntry() {
        assertThat(baseReading.getEntry()).isEqualTo(entry);
    }

    @Test
    public void testGetTimeStamp() {
        assertThat(baseReading.getTimeStamp()).isEqualTo(DATE);
    }

    @Test
    public void testGetRecordDate() {
        assertThat(baseReading.getReportedDateTime()).isEqualTo(RECORD_DATE);
    }

    @Test
    public void testGetValue() {
        assertThat(baseReading.getValue()).isEqualTo(BigDecimal.valueOf(baseReading.getReadingTypeOffset()));
    }

    @Test
    public void testGetValues() {
        assertThat(baseReading.getQuantities()).hasSize(3);
    }

    @Test
    public void testGetQuantityForReadingType() {
        assertThat(baseReading.getQuantity(readingType).getValue()).isEqualTo(BigDecimal.valueOf(baseReading.getReadingTypeOffset() + 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetQuantityForUnknownReadingType() {
        baseReading.getQuantity(unknownReadingType);
    }

    @Test
    public void testGetReadingType() {
        assertThat(baseReading.getReadingType()).isEqualTo(readingType1);
    }

    @Test
    public void testGetReadingTypeAtOffset() {
        assertThat(baseReading.getReadingType(1)).isEqualTo(readingType2);
    }

    @Test
    public void testGetReadingTypes() {
        assertThat(baseReading.getReadingTypes()).isEqualTo(Arrays.asList(readingType1, readingType2, readingType));
    }

    @Test
    public void testGetProcessingFlags() {
        assertThat(baseReading.getProcessStatus()).isEqualTo(ProcessStatus.of(ProcessStatus.Flag.SUSPECT));
    }

    ChannelImpl getChannel() {
        return channel;
    }

}
