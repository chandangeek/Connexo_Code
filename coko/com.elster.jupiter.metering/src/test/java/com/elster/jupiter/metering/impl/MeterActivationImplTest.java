/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterAlreadyLinkedToUsagePoint;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Provider;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationImplTest extends EqualsContractTest {

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    private MeterActivationImpl instanceA;

    private static final String MRID1 = "13.2.2.4.0.8.12.8.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID2 = "13.2.2.1.0.8.12.9.16.9.11.12.13.14.128.3.72.124";
    private static final String MRID3 = "13.2.3.4.0.8.12.10.16.9.11.12.13.14.128.3.72.124";
    private static final ZonedDateTime ACTIVATION_TIME_BASE = ZonedDateTime.of(1984, 11, 5, 13, 37, 3, 14_000_000, TimeZoneNeutral.getMcMurdo());
    private static final Instant ACTIVATION_TIME = ACTIVATION_TIME_BASE.toInstant();
    private static final long USAGEPOINT_ID = 6546L;
    private static final long METER_ID = 46335L;
    private static final Instant END = ZonedDateTime.of(2166, 8, 6, 8, 35, 0, 0, TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final long ID = 154177L;

    private MeterActivationImpl meterActivation;

    @Mock
    private UsagePoint usagePoint, otherUsagePoint;
    @Mock
    private Meter meter, otherMeter;
    @Mock
    private MeterRole meterRole;
    private ChannelImpl channel1, channel2;
    private ReadingTypeImpl readingType1, readingType2, readingType3;
    @Mock
    private IntervalReadingRecord reading1, reading2;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private Clock clock;
    private Provider<ChannelBuilder> channelBuilder;
    @Mock
    private IdsService idsService;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private Vault vault;
    @Mock
    private RecordSpec recordSpec;
    private TimeZone timeZone = TimeZone.getTimeZone("Asia/Calcutta");
    @Mock
    private TimeSeries timeSeries;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;

    @Before
    public void setUp() {
        when(meteringService.getClock()).thenReturn(clock);
        when(meteringService.getDataModel()).thenReturn(dataModel);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        readingType1 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID1, "readingType1");
        readingType2 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID2, "readingType2");
        readingType3 = new ReadingTypeImpl(dataModel, thesaurus).init(MRID3, "readingType3");

        final Provider<ChannelImpl> channelFactory = () -> new ChannelImpl(dataModel, idsService, meteringService, clock, eventService);
        channelBuilder = () -> new ChannelBuilderImpl(dataModel, channelFactory);
        MeterActivationContraintValidatorFactory contraintValidatorFactory = new MeterActivationContraintValidatorFactory(dataModel, thesaurus);
        ValidatorFactory validatorFactory = Validation.byDefaultProvider()
                .configure()
                .constraintValidatorFactory(contraintValidatorFactory)
                .messageInterpolator(thesaurus)
                .buildValidatorFactory();
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(meter.getUsagePoint(any())).thenReturn(Optional.empty());
        when(usagePoint.getId()).thenReturn(USAGEPOINT_ID);
        when(meter.getId()).thenReturn(METER_ID);
        when(meter.getHeadEndInterface()).thenReturn(Optional.empty());
        when(idsService.getVault(anyString(), anyInt())).thenReturn(Optional.of(vault));
        when(idsService.getRecordSpec(anyString(), anyInt())).thenReturn(Optional.of(recordSpec));
        when(clock.getZone()).thenReturn(timeZone.toZoneId());
        when(meter.getConfiguration(any())).thenReturn(Optional.empty());
        when(usagePoint.getConfiguration(any())).thenReturn(Optional.empty());
        when(dataModel.getInstance(ReadingTypeInChannel.class)).thenAnswer(invocation -> new ReadingTypeInChannel(dataModel, meteringService));
        when(dataModel.getInstance(MeterActivationChannelsContainerImpl.class)).then(invocation -> new MeterActivationChannelsContainerImpl(meteringService, eventService, channelBuilder));

        //make sure the meteractivation is valid
        EffectiveMetrologyConfigurationOnUsagePoint effMetrologyConf = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(usagePoint.getEffectiveMetrologyConfigurations(any())).thenReturn(Collections.singletonList(effMetrologyConf));
        UsagePointMetrologyConfiguration metrologyConfigiruation = mock(UsagePointMetrologyConfiguration.class);
        when(effMetrologyConf.getMetrologyConfiguration()).thenReturn(metrologyConfigiruation);
        when(metrologyConfigiruation.getMeterRoles()).thenReturn(Collections.singletonList(meterRole));

        meterActivation = getTestInstanceAndInitWithActivationTime();
    }

    private MeterActivationImpl getTestInstanceAndInitWithActivationTime() {
        MeterActivationImpl meterActivation = getTestInstance().init(meter, meterRole, usagePoint, ACTIVATION_TIME);
        meterActivation.save();
        return meterActivation;
    }

    private MeterActivationImpl getTestInstance() {
        return new MeterActivationImpl(dataModel, eventService, clock, thesaurus);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreationRemembersUsagePoint() {
        assertThat(meterActivation.getUsagePoint().get()).isEqualTo(usagePoint);
    }

    @Test
    public void testCreationRemembersMeter() {
        assertThat(meterActivation.getMeter().get()).isEqualTo(meter);
    }

    @Test
    public void testCreationRemembersStartDate() {
        assertThat(meterActivation.getRange().lowerEndpoint()).isEqualTo(ACTIVATION_TIME);
    }

    @Test
    public void testGetEnd() {
        simulateSavedMeterActivation();

        meterActivation.endAt(END);

        verify(dataModel).update(meterActivation);

        assertThat(meterActivation.getRange().upperEndpoint()).isEqualTo(END);
    }


    @Test
    public void testCreateChannel() {
        when(vault.createRegularTimeSeries(eq(recordSpec), eq(timeZone), any(), anyInt())).thenReturn(timeSeries);

        Channel channel = meterActivation.getChannelsContainer().createChannel(readingType1, readingType3);

        assertThat(channel.getChannelsContainer()).isInstanceOf(MeterActivationChannelsContainerImpl.class);
        assertThat(channel.getReadingTypes()).isEqualTo(Arrays.asList(readingType1, readingType3));
    }

    @Test
    public void testSetUsagePoint() {
        meterActivation = getTestInstance().init(meter, ACTIVATION_TIME);

        assertThat(meterActivation.getUsagePoint()).isEmpty();

        meterActivation.setUsagePoint(usagePoint);

        assertThat(meterActivation.getUsagePoint()).contains(usagePoint);
    }

    @Test(expected = MeterAlreadyLinkedToUsagePoint.class)
    public void testSetUsagePointWhenAlreadySet() {
        meterActivation = getTestInstanceAndInitWithActivationTime();

        meterActivation.setUsagePoint(usagePoint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdvanceStartDateMustNotBeLater() {
        meterActivation = getTestInstanceAndInitWithActivationTime();

        meterActivation.advanceStartDate(ACTIVATION_TIME_BASE.plusSeconds(1).toInstant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdvanceStartDateMustBeEarlier() {
        meterActivation = getTestInstanceAndInitWithActivationTime();

        meterActivation.advanceStartDate(ACTIVATION_TIME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdvanceStartDateMustNotOverlapWithMeterActivationOfMeter() {
        meterActivation = getTestInstanceAndInitWithActivationTime();

        IMeterActivation earlier = mock(IMeterActivation.class);

        doReturn(Arrays.asList(earlier, meterActivation)).when(meter).getMeterActivations();
        when(earlier.getId()).thenReturn(516501L);
        when(earlier.getRange()).thenReturn(Range.closedOpen(ACTIVATION_TIME_BASE.minusYears(1).toInstant(), ACTIVATION_TIME));
        when(earlier.getUsagePoint()).thenReturn(Optional.of(otherUsagePoint));
        when(earlier.getMeter()).thenReturn(Optional.of(meter));

        meterActivation.advanceStartDate(ACTIVATION_TIME_BASE.minusDays(5).toInstant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdvanceStartDateMustNotOverlapWithMeterActivationOfUsagePoint() {
        meterActivation = getTestInstanceAndInitWithActivationTime();

        IMeterActivation earlier = mock(IMeterActivation.class);

        doReturn(Arrays.asList(earlier, meterActivation)).when(usagePoint).getMeterActivations();
        when(earlier.getId()).thenReturn(516501L);
        when(earlier.getRange()).thenReturn(Range.closedOpen(ACTIVATION_TIME_BASE.minusYears(1).toInstant(), ACTIVATION_TIME));
        when(earlier.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(earlier.getMeter()).thenReturn(Optional.of(otherMeter));

        meterActivation.advanceStartDate(ACTIVATION_TIME_BASE.minusDays(5).toInstant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdvanceStartDateMustNotOverlapWithIncompatibleMeterActivations() {
        meterActivation = getTestInstanceAndInitWithActivationTime();

        IMeterActivation earlier = mock(IMeterActivation.class);

        doReturn(Arrays.asList(earlier, meterActivation)).when(meter).getMeterActivations();
        when(earlier.getId()).thenReturn(516501L);
        when(earlier.getRange()).thenReturn(Range.closedOpen(ACTIVATION_TIME_BASE.minusYears(1).toInstant(), ACTIVATION_TIME));
        when(earlier.getUsagePoint()).thenReturn(Optional.empty());
        when(earlier.getMeter()).thenReturn(Optional.of(meter));

        IMeterActivation otherEarlier = mock(IMeterActivation.class);
        doReturn(Arrays.asList(otherEarlier, meterActivation)).when(usagePoint).getMeterActivations();
        when(otherEarlier.getId()).thenReturn(516502L);
        when(otherEarlier.getRange()).thenReturn(Range.closedOpen(ACTIVATION_TIME_BASE.minusYears(1).toInstant(), ACTIVATION_TIME));
        when(otherEarlier.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(otherEarlier.getMeter()).thenReturn(Optional.empty());

        meterActivation.advanceStartDate(ACTIVATION_TIME_BASE.minusDays(5).toInstant());
    }

    public void testAdvanceStartDateSuccess() {
        meterActivation = getTestInstanceAndInitWithActivationTime();
        field("id").ofType(Long.TYPE).in(meterActivation).set(987987L);

        MeterActivation earlier = mock(MeterActivation.class);

        doReturn(Arrays.asList(earlier, meterActivation)).when(usagePoint).getMeterActivations();
        when(earlier.getId()).thenReturn(516501L);
        when(earlier.getRange()).thenReturn(Range.closedOpen(ACTIVATION_TIME_BASE.minusYears(1).toInstant(), ACTIVATION_TIME_BASE.minusMonths(8).toInstant()));

        meterActivation.advanceStartDate(ACTIVATION_TIME_BASE.minusDays(5).toInstant());

        verify(dataModel.mapper(MeterActivation.class)).update(meterActivation);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(ACTIVATION_TIME_BASE.minusDays(5).toInstant()));
        ArgumentCaptor<EventType.MeterActivationAdvancedEvent> captor = ArgumentCaptor.forClass(EventType.MeterActivationAdvancedEvent.class);
        verify(eventService).postEvent(eq(EventType.METER_ACTIVATION_ADVANCED.topic()), captor.capture());

        EventType.MeterActivationAdvancedEvent event = captor.getValue();
        assertThat(event.getAdvanced()).isEqualTo(meterActivation);
        assertThat(event.getShrunk()).isEqualTo(earlier);
    }

    public void testAdvanceStartDateSuccessOnClosedPeriod() {
        meterActivation = getTestInstanceAndInitWithActivationTime();
        field("id").ofType(Long.TYPE).in(meterActivation).set(987987L);
        meterActivation.endAt(ACTIVATION_TIME_BASE.plusYears(1).toInstant());

        MeterActivation earlier = mock(MeterActivation.class);

        doReturn(Arrays.asList(earlier, meterActivation)).when(usagePoint).getMeterActivations();
        when(earlier.getId()).thenReturn(516501L);
        when(earlier.getRange()).thenReturn(Range.closedOpen(ACTIVATION_TIME_BASE.minusYears(1).toInstant(), ACTIVATION_TIME_BASE.minusMonths(8).toInstant()));

        meterActivation.advanceStartDate(ACTIVATION_TIME_BASE.minusDays(5).toInstant());

        verify(dataModel.mapper(MeterActivation.class)).update(meterActivation);
        assertThat(meterActivation.getRange()).isEqualTo(Range.closedOpen(ACTIVATION_TIME_BASE.minusDays(5).toInstant(), ACTIVATION_TIME_BASE.plusYears(1).toInstant()));
    }


    private void simulateSavedMeterActivation() {
        field("id").ofType(Long.TYPE).in(meterActivation).set(ID);
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = getTestInstance();
            field("id").ofType(Long.TYPE).in(instanceA).set(45L);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        MeterActivationImpl meterActivation = getTestInstance();
        field("id").ofType(Long.TYPE).in(meterActivation).set(45L);
        return meterActivation;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        MeterActivationImpl meterActivation1 = getTestInstance();
        field("id").ofType(Long.TYPE).in(meterActivation1).set(43L);
        MeterActivationImpl meterActivation2 = getTestInstance();
        return ImmutableList.of(meterActivation1, meterActivation2);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }


}
