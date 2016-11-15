package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.UsagePointReadingTypeConfiguration;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bbl on 7/06/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterActivationChannelCreationTest {

    private static final ZonedDateTime ACTIVATION_TIME_BASE = ZonedDateTime.of(1984, 11, 5, 13, 37, 3, 14_000_000, TimeZoneNeutral.getMcMurdo());
    private static final Instant ACTIVATION_TIME = ACTIVATION_TIME_BASE.toInstant();

    private static final String BULK_RT = "13.2.2.1.0.8.12.8.16.9.11.12.13.14.128.3.72.124";
    private static final String DELTADELTA_RT = "13.2.2.4.0.8.12.8.16.9.11.12.13.14.128.3.72.124";
    private static final String RT2 = "13.2.3.4.0.8.12.8.16.9.11.12.13.14.128.3.72.124";
    private static final String RT3 = "13.2.4.4.0.8.12.8.16.9.11.12.13.14.128.3.72.124";
    private static final Long USAGEPOINT_ID = 5L;
    private static final Long METER_ID = 7L;

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;
    @Mock
    private IdsService idsService;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private Clock clock;
    @Mock
    private EventService eventService;
    @Mock
    private Meter meter;
    @Mock
    private MeterRole meterRole;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private Vault vault;
    @Mock
    private RecordSpec recordSpec;
    @Mock
    private TimeSeries timeSeries;
    @Mock
    private MeterConfiguration meterConfig;
    @Mock
    private MeterReadingTypeConfiguration readingTypeConfig;
    @Mock
    private UsagePointConfiguration usagepointConfig;
    @Mock
    private UsagePointReadingTypeConfiguration usagePointReadingTypeConfig;
    @Mock
    private HeadEndInterface headEndInterface;
    private Provider<ChannelBuilder> channelBuilder;
    private ReadingTypeImpl bulkRT1, deltaRt1, readingType2, readingType3;


    @Before
    public void setUp() {
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        bulkRT1 = new ReadingTypeImpl(dataModel, thesaurus).init(BULK_RT, "bulkRT1");
        deltaRt1 = new ReadingTypeImpl(dataModel, thesaurus).init(DELTADELTA_RT, "deltaRT1");
        readingType2 = new ReadingTypeImpl(dataModel, thesaurus).init(RT2, "readingType2");
        readingType3 = new ReadingTypeImpl(dataModel, thesaurus).init(RT3, "readingType3");

        Provider<ChannelImpl> channelFactory = () -> new ChannelImpl(dataModel, idsService, meteringService, clock, eventService);
        channelBuilder = () -> new ChannelBuilderImpl(dataModel, channelFactory);
        when(dataModel.getInstance(MeterActivationChannelsContainerImpl.class)).then(invocation -> new MeterActivationChannelsContainerImpl(meteringService, eventService, channelBuilder));
        when(usagePoint.getId()).thenReturn(USAGEPOINT_ID);
        when(meter.getId()).thenReturn(METER_ID);
        when(meter.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));
        when(meter.getConfiguration(any())).thenReturn(Optional.empty());
        when(idsService.getVault(anyString(), anyInt())).thenReturn(Optional.of(vault));
        when(idsService.getRecordSpec(anyString(), anyInt())).thenReturn(Optional.of(recordSpec));
        when(vault.createRegularTimeSeries(eq(recordSpec), any(TimeZone.class), any(TemporalAmount.class), anyInt())).thenReturn(timeSeries);
        when(vault.createIrregularTimeSeries(eq(recordSpec), eq(ACTIVATION_TIME_BASE.getZone()))).thenReturn(timeSeries);
        when(timeSeries.getZoneId()).thenReturn(ACTIVATION_TIME_BASE.getZone());
        when(clock.getZone()).thenReturn(ACTIVATION_TIME_BASE.getZone());
        when(clock.instant()).thenReturn(ACTIVATION_TIME);
        when(usagePoint.getConfiguration(any())).thenReturn(Optional.empty());
        when(dataModel.getInstance(ReadingTypeInChannel.class)).thenAnswer(invocation -> new ReadingTypeInChannel(dataModel, meteringService));
        when(meteringService.getClock()).thenReturn(clock);
    }

    private MeterActivationImpl createMeterActivationOnMeter() {
        MeterActivationImpl meterActivation = getTestInstance().init(meter, meterRole, usagePoint, ACTIVATION_TIME);
        meterActivation.save();
        return meterActivation;
    }

    private MeterActivationImpl getTestInstance() {
        return new MeterActivationImpl(dataModel, eventService, clock, thesaurus);
    }

    @Test
    public void testCreateChannels() {
        when(headEndInterface.getCapabilities(meter)).thenAnswer(invocationOnMock -> new EndDeviceCapabilities(Arrays.asList(deltaRt1, readingType2, readingType3), Collections.emptyList()));
        MeterActivationImpl meterActivation = createMeterActivationOnMeter();

        assertThat(meterActivation.getChannelsContainer().getChannels()).hasSize(3);
        assertThat(meterActivation.getChannelsContainer().getChannels().stream().map(Channel::getMainReadingType).collect(Collectors.toList())).contains(deltaRt1, readingType2, readingType3);
    }

    @Test
    public void testCreateChannelsFilterDuplicates() {
        when(headEndInterface.getCapabilities(eq(meter))).thenAnswer(invocationOnMock -> new EndDeviceCapabilities(Arrays.asList(deltaRt1, readingType2, deltaRt1), Collections.emptyList()));
        MeterActivationImpl meterActivation = createMeterActivationOnMeter();

        assertThat(meterActivation.getChannelsContainer().getChannels()).hasSize(2);
        assertThat(meterActivation.getChannelsContainer().getChannels().stream().map(Channel::getMainReadingType).collect(Collectors.toList())).contains(deltaRt1, readingType2);
    }

    @Test
    public void testFilterDeltaDeltas() {
        DataMapper dataMapperMock = mock(DataMapper.class);
        when(dataModel.mapper(eq(IReadingType.class))).thenReturn(dataMapperMock);
        when(dataMapperMock.getOptional(eq(DELTADELTA_RT))).thenReturn(Optional.of(deltaRt1));

        when(headEndInterface.getCapabilities(eq(meter))).thenAnswer(invocationOnMock -> new EndDeviceCapabilities(Arrays.asList(deltaRt1, bulkRT1), Collections.emptyList()));
        MeterActivationImpl meterActivation = createMeterActivationOnMeter();

        assertThat(meterActivation.getChannelsContainer().getChannels()).hasSize(1);
        assertThat(meterActivation.getChannelsContainer().getChannels().stream().map(Channel::getMainReadingType).collect(Collectors.toList())).contains(deltaRt1);
    }

    @Test
    public void testFilterCalculatedMeterReadingTypes() {
        when(meter.getConfiguration(ACTIVATION_TIME)).thenReturn(Optional.of(meterConfig));
        when(meterConfig.getReadingTypeConfigs()).thenReturn(Collections.singletonList(readingTypeConfig));
        when(readingTypeConfig.getMeasured()).thenReturn(readingType2);
        when(readingTypeConfig.getCalculated()).thenReturn(Optional.of(readingType3));

        when(headEndInterface.getCapabilities(eq(meter))).thenAnswer(invocationOnMock -> new EndDeviceCapabilities(Arrays.asList(readingType2, readingType3), Collections.emptyList()));
        MeterActivationImpl meterActivation = createMeterActivationOnMeter();

        assertThat(meterActivation.getChannelsContainer().getChannels()).hasSize(1);
        assertThat(meterActivation.getChannelsContainer().getChannels().stream().map(Channel::getMainReadingType).collect(Collectors.toList())).contains(readingType3);
    }

    @Test
    public void testFilterCalculatedUsagePointReadingTypes() {
        when(usagePoint.getConfiguration(ACTIVATION_TIME)).thenReturn(Optional.of(usagepointConfig));
        when(usagepointConfig.getReadingTypeConfigs()).thenReturn(Collections.singletonList(usagePointReadingTypeConfig));
        when(usagePointReadingTypeConfig.getMeasured()).thenReturn(readingType3);
        when(usagePointReadingTypeConfig.getCalculated()).thenReturn(Optional.of(readingType2));

        when(headEndInterface.getCapabilities(eq(meter))).thenAnswer(invocationOnMock -> new EndDeviceCapabilities(Arrays.asList(readingType2, readingType3), Collections.emptyList()));
        MeterActivationImpl meterActivation = createMeterActivationOnMeter();

        assertThat(meterActivation.getChannelsContainer().getChannels()).hasSize(1);
        assertThat(meterActivation.getChannelsContainer().getChannels().stream().map(Channel::getMainReadingType).collect(Collectors.toList())).contains(readingType2);
    }

    @Test
    public void testFilterDeltaDeltaCalculatedUsagePointReadingTypes() {
        DataMapper dataMapperMock = mock(DataMapper.class);
        when(dataModel.mapper(eq(IReadingType.class))).thenReturn(dataMapperMock);
        when(dataMapperMock.getOptional(eq(DELTADELTA_RT))).thenReturn(Optional.of(deltaRt1));
        when(usagePoint.getConfiguration(ACTIVATION_TIME)).thenReturn(Optional.of(usagepointConfig));
        when(usagepointConfig.getReadingTypeConfigs()).thenReturn(Collections.singletonList(usagePointReadingTypeConfig));
        when(usagePointReadingTypeConfig.getMeasured()).thenReturn(readingType2);
        when(usagePointReadingTypeConfig.getCalculated()).thenReturn(Optional.of(bulkRT1));

        when(headEndInterface.getCapabilities(eq(meter))).thenAnswer(invocationOnMock -> new EndDeviceCapabilities(Arrays.asList(readingType2, deltaRt1), Collections.emptyList()));
        MeterActivationImpl meterActivation = createMeterActivationOnMeter();

        assertThat(meterActivation.getChannelsContainer().getChannels()).hasSize(1);
        assertThat(meterActivation.getChannelsContainer().getChannels().stream().map(Channel::getMainReadingType).collect(Collectors.toList())).contains(deltaRt1);
    }

    @Test
    public void testMeterActivationCreationWithChannels() {
        HeadEndInterface headEndInterface = mock(HeadEndInterface.class);
        when(meter.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));
        EndDeviceCapabilities endDeviceCapabilities = new EndDeviceCapabilities(Collections.singletonList(bulkRT1), Collections.emptyList());
        when(headEndInterface.getCapabilities(any(Meter.class))).thenReturn(endDeviceCapabilities);
        DataMapper dataMapperMock = mock(DataMapper.class);
        when(dataModel.mapper(eq(IReadingType.class))).thenReturn(dataMapperMock);
        when(dataMapperMock.getOptional(eq(DELTADELTA_RT))).thenReturn(Optional.of(deltaRt1));

        MeterActivationImpl meterActivation = createMeterActivationOnMeter();
        assertThat(meterActivation.getChannelsContainer().getChannels()).hasSize(1);
        assertThat(meterActivation.getChannelsContainer().getChannels().stream().map(Channel::getMainReadingType).collect(Collectors.toList())).contains(deltaRt1);

    }
}
