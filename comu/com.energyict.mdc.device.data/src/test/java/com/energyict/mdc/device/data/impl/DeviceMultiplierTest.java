package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.MultiplierConfigurationException;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.FirmwareComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ManuallyScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.collect.Range;

import javax.inject.Provider;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests regarding the DeviceMultiplier
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMultiplierTest {

    private final long ID = 9536541L;

    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private IssueService issueService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Clock clock;
    @Mock
    private MeteringService meteringService;
    @Mock
    private MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private ValidationService validationService;
    @Mock
    private ServerConnectionTaskService connectionTaskService;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;
    @Mock
    private SecurityPropertyService securityPropertyService;
    @Mock
    private Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    @Mock
    private Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    @Mock
    private Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider;
    @Mock
    private Provider<ScheduledComTaskExecutionImpl> scheduledComTaskExecutionProvider;
    @Mock
    private Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider;
    @Mock
    private Provider<FirmwareComTaskExecutionImpl> firmwareComTaskExecutionProvider;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private MdcReadingTypeUtilService readingTypeUtilService;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private Meter meter;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private MultiplierType multiplierType;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserPreferencesService userPreferencesService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;

    private Instant now = Instant.ofEpochSecond(1448460000L); //25-11-2015
    private Instant startOfMeterActivation = Instant.ofEpochSecond(1447977600L); // 20-11-2015


    @Before
    public void setup() {
        when(thesaurus.getFormat(any(TranslationKey.class))).thenAnswer(invocationOnMock -> {
            TranslationKey translationKey = (TranslationKey) invocationOnMock.getArguments()[0];
            return new NlsMessageFormat() {
                @Override
                public String format(Object... args) {
                    return MessageFormat.format(translationKey.getDefaultFormat(), args);
                }

                @Override
                public String format(Locale locale, Object... args) {
                    return MessageFormat.format(translationKey.getDefaultFormat(), args);
                }
            };
        });
        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocationOnMock -> {
            MessageSeed messageSeed = (MessageSeed) invocationOnMock.getArguments()[0];
            return new NlsMessageFormat() {
                @Override
                public String format(Object... args) {
                    return MessageFormat.format(messageSeed.getDefaultFormat(), args);
                }

                @Override
                public String format(Locale locale, Object... args) {
                    return MessageFormat.format(messageSeed.getDefaultFormat(), args);
                }
            };
        });
        when(clock.instant()).thenReturn(now);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter(String.valueOf(ID))).thenReturn(Optional.of(meter));
        when(meteringService.getMultiplierType(anyString())).thenReturn(Optional.of(multiplierType));
        when(meterActivation.getMultiplier(multiplierType)).thenReturn(Optional.empty());
        when(meterActivation.getRange()).thenReturn(Range.atLeast(startOfMeterActivation));
        when(meter.getUsagePoint(any())).thenReturn(Optional.empty());
    }

    private Device createMockedDevice() {
        DeviceImpl device = new DeviceImpl(dataModel, eventService, issueService, thesaurus, clock, meteringService, metrologyConfigurationService, validationService, securityPropertyService,
                scheduledConnectionTaskProvider, inboundConnectionTaskProvider, connectionInitiationTaskProvider, scheduledComTaskExecutionProvider, manuallyScheduledComTaskExecutionProvider,
                firmwareComTaskExecutionProvider, meteringGroupsService, customPropertySetService, readingTypeUtilService, threadPrincipalService, userPreferencesService, deviceConfigurationService);
        setId(device, ID);
        device.initialize(deviceConfiguration, "Name", "Mrid");
        return device;
    }

    private void setId(Object entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Test
    public void getMultiplierWhenNoMultiplierIsDefined() {
        Device mockedDevice = createMockedDevice();

        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();

        assertThat(mockedDevice.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    public void dontCreateNewMeterActivationWhenMultiplierIsOneTest() {
        Device mockedDevice = createMockedDevice();

        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();

        mockedDevice.setMultiplier(BigDecimal.ONE);

        verify(meterActivation, never()).endAt(any(Instant.class));
    }

    @Test
    public void setMultiplierTest() {
        Device mockedDevice = createMockedDevice();
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        Instant from = Instant.ofEpochSecond(1448466879L);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(meter.activate(from)).thenReturn(newMeterActivation);
        when(meter.getConfiguration(from)).thenReturn(Optional.empty());

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, from);

        verify(meterActivation).endAt(from);
        verify(meter).activate(from);
        verify(newMeterActivation).setMultiplier(multiplierType, multiplier);
    }

    @Test
    public void setMultiplierInThePastTest() {
        Device mockedDevice = createMockedDevice();
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        Instant past = now.minus(1, ChronoUnit.DAYS);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(meter.activate(past)).thenReturn(newMeterActivation);
        when(meter.getConfiguration(past)).thenReturn(Optional.empty());

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, past);

        verify(meterActivation).endAt(past);
        verify(meter).activate(past);
        verify(newMeterActivation).setMultiplier(multiplierType, multiplier);
    }

    @Test
    @Expected(value = MultiplierConfigurationException.class, message = "You can not configure a multiplier in the past when your device already has data")
    public void setMultiplierInThePastWhenAlreadyDataTest() {
        Device mockedDevice = createMockedDevice();
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        when(meter.hasData()).thenReturn(true);
        Instant past = now.minus(1, ChronoUnit.DAYS);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(meter.activate(past)).thenReturn(newMeterActivation);

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, past);
    }

    @Test
    public void setMultiplierInFutureWithAlreadyDataTest() {
        Device mockedDevice = createMockedDevice();
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        when(meter.hasData()).thenReturn(true);
        Instant from = Instant.ofEpochSecond(1448466879L);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(meter.activate(from)).thenReturn(newMeterActivation);
        when(meter.getConfiguration(from)).thenReturn(Optional.empty());

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, from);

        verify(meterActivation).endAt(from);
        verify(meter).activate(from);
        verify(newMeterActivation).setMultiplier(multiplierType, multiplier);
    }

    @Test
    @Expected(value = MultiplierConfigurationException.class, message = "You can not configure a multiplier with a start date outside of the current meter activation")
    public void setMultiplierInFutureOutsideRangeOfCurrentMeterActivationTest() {
        Device mockedDevice = createMockedDevice();
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        when(meterActivation.getRange()).thenReturn(Range.openClosed(startOfMeterActivation, now));
        when(meter.hasData()).thenReturn(true);
        Instant from = now.plus(1, ChronoUnit.DAYS);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(meter.activate(from)).thenReturn(newMeterActivation);

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, from);
    }


    @Test
    @Expected(value = MultiplierConfigurationException.class, message = "You can not configure a multiplier with a start date outside of the current meter activation")
    public void setMultiplierInPastOutsideRangeOfCurrentMeterActivationTest() {
        Device mockedDevice = createMockedDevice();
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        when(meterActivation.getRange()).thenReturn(Range.openClosed(startOfMeterActivation, now));
        Instant from = startOfMeterActivation.minus(1, ChronoUnit.DAYS);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(meter.activate(from)).thenReturn(newMeterActivation);

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, from);
    }

    @Test
    public void getMultiplierEffectiveTimeStampWhenNoMultiplierIsDefinedTest() {
        Device mockedDevice = createMockedDevice();

        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();

        assertThat(mockedDevice.getMultiplierEffectiveTimeStamp()).isEqualTo(now);
    }

    @Test
    public void getMultiplierEffectiveTimeStampWhenMultiplierIsDefinedTest() {
        Device mockedDevice = createMockedDevice();
        Instant meterActivationStart = Instant.ofEpochSecond(1419465600L);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        when(meterActivation.getStart()).thenReturn(meterActivationStart);
        when(meterActivation.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));

        assertThat(mockedDevice.getMultiplierEffectiveTimeStamp()).isEqualTo(meterActivationStart);
    }

    @Test
    public void getMultiplierEffectiveTimeStampWhenMultiplierIsDefinedAndMultipleMeterActivationsTest() {
        Device mockedDevice = createMockedDevice();
        Instant meterActivationStart = Instant.ofEpochSecond(1419465600L);
        Instant otherMeterActivationStart = Instant.ofEpochSecond(1387929600L);
        MeterActivation otherMeterActivation = mock(MeterActivation.class);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(otherMeterActivation, meterActivation)).when(meter).getMeterActivations();
        when(meterActivation.getStart()).thenReturn(meterActivationStart);
        when(meterActivation.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        when(otherMeterActivation.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        when(otherMeterActivation.getStart()).thenReturn(otherMeterActivationStart);

        assertThat(mockedDevice.getMultiplierEffectiveTimeStamp()).isEqualTo(otherMeterActivationStart);
    }

    @Test
    public void getMultiplierEffectiveTimeStampWhenMultiplierIsDefinedAndMultipleMeterActivationsWithOtherMultiplierTest() {
        Device mockedDevice = createMockedDevice();
        Instant meterActivationStart = Instant.ofEpochSecond(1419465600L);
        Instant otherMeterActivationStart1 = Instant.ofEpochSecond(1387929600L);
        Instant otherMeterActivationStart2 = Instant.ofEpochSecond(1387920600L);
        Instant otherMeterActivationStart3 = Instant.ofEpochSecond(1387909600L);
        MeterActivation otherMeterActivation1 = mock(MeterActivation.class);
        MeterActivation otherMeterActivation2 = mock(MeterActivation.class);
        MeterActivation otherMeterActivation3 = mock(MeterActivation.class);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(otherMeterActivation3,otherMeterActivation2,otherMeterActivation1, meterActivation)).when(meter).getMeterActivations();
        when(meterActivation.getStart()).thenReturn(meterActivationStart);
        when(meterActivation.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        when(otherMeterActivation1.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        when(otherMeterActivation1.getStart()).thenReturn(otherMeterActivationStart1);
        when(otherMeterActivation2.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        when(otherMeterActivation2.getStart()).thenReturn(otherMeterActivationStart2);
        when(otherMeterActivation3.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.valueOf(321L)));
        when(otherMeterActivation3.getStart()).thenReturn(otherMeterActivationStart3);

        assertThat(mockedDevice.getMultiplierEffectiveTimeStamp()).isEqualTo(otherMeterActivationStart2);
    }

    @Test
    public void getMultiplierEffectiveTimeStampWhenMultiplierIsDefinedAndMultipleMeterActivationsWithOtherMultiplierAndSameInPastTest() {
        Device mockedDevice = createMockedDevice();
        Instant meterActivationStart = Instant.ofEpochSecond(1419465600L);
        Instant otherMeterActivationStart1 = Instant.ofEpochSecond(1387929600L);
        Instant otherMeterActivationStart2 = Instant.ofEpochSecond(1387920600L);
        Instant otherMeterActivationStart3 = Instant.ofEpochSecond(1387909600L);
        MeterActivation otherMeterActivation1 = mock(MeterActivation.class);
        MeterActivation otherMeterActivation2 = mock(MeterActivation.class);
        MeterActivation otherMeterActivation3 = mock(MeterActivation.class);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(otherMeterActivation3,otherMeterActivation2,otherMeterActivation1, meterActivation)).when(meter).getMeterActivations();
        when(meterActivation.getStart()).thenReturn(meterActivationStart);
        when(meterActivation.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        when(otherMeterActivation1.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        when(otherMeterActivation1.getStart()).thenReturn(otherMeterActivationStart1);
        when(otherMeterActivation2.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.valueOf(654645L)));
        when(otherMeterActivation2.getStart()).thenReturn(otherMeterActivationStart2);
        when(otherMeterActivation3.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        when(otherMeterActivation3.getStart()).thenReturn(otherMeterActivationStart3);

        assertThat(mockedDevice.getMultiplierEffectiveTimeStamp()).isEqualTo(otherMeterActivationStart1);
    }

}