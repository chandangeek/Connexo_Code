/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterBuilder;
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
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LockService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.MultiplierConfigurationException;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.collect.Range;

import javax.inject.Provider;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
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

    private static final long ID = 9536541L;

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
    private ServerDeviceService deviceService;
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
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private Provider<ScheduledConnectionTaskImpl> scheduledConnectionTaskProvider;
    @Mock
    private Provider<InboundConnectionTaskImpl> inboundConnectionTaskProvider;
    @Mock
    private Provider<ConnectionInitiationTaskImpl> connectionInitiationTaskProvider;
    @Mock
    private Provider<ComTaskExecutionImpl> scheduledComTaskExecutionProvider;
    @Mock
    private Provider<ComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider;
    @Mock
    private Provider<ComTaskExecutionImpl> firmwareComTaskExecutionProvider;
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
    @Mock
    private DeviceLifeCycle deviceLifeCycle;
    @Mock
    private FiniteStateMachine finiteStateMachine;
    @Mock
    private MeterBuilder meterBuilder;
    @Mock
    private LifecycleDates lifecycleDates;
    @Mock
    private DeviceType deviceType;
    @Mock
    private LockService lockService;

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
        when(amrSystem.newMeter(anyString(), anyString())).thenReturn(meterBuilder);

        when(meterBuilder.setAmrId(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setMRID(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setSerialNumber(anyString())).thenReturn(meterBuilder);
        when(meterBuilder.setStateMachine(any(FiniteStateMachine.class))).thenReturn(meterBuilder);
        when(meterBuilder.setReceivedDate(any(Instant.class))).thenReturn(meterBuilder);
        when(meterBuilder.create()).thenReturn(meter);

        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), any())).thenReturn(Collections.emptySet());

        when(meter.getLifecycleDates()).thenReturn(lifecycleDates);
        when(meter.getConfiguration(any(Instant.class))).thenReturn(Optional.empty());

        when(deviceLifeCycle.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        when(finiteStateMachine.getId()).thenReturn(633L);

        when(deviceService.findDefaultMultiplierType()).thenReturn(multiplierType);
        when(meterActivation.getMultiplier(multiplierType)).thenReturn(Optional.empty());
        when(meterActivation.getRange()).thenReturn(Range.atLeast(startOfMeterActivation));
        when(meterActivation.getUsagePoint()).thenReturn(Optional.empty());
        when(meter.getUsagePoint(any())).thenReturn(Optional.empty());
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        when(deviceLifeCycle.getMaximumPastEffectiveTimestamp()).thenReturn(Instant.MIN);
        when(deviceLifeCycle.getMaximumFutureEffectiveTimestamp()).thenReturn(Instant.MAX);
        when(deviceLifeCycle.getFiniteStateMachine()).thenReturn(finiteStateMachine);
    }

    private Device createMockedDevice(Instant startOfMeterActivation) {
        DeviceImpl device = new DeviceImpl(dataModel, eventService, issueService, thesaurus, clock, meteringService, validationService, securityPropertyService,
                scheduledConnectionTaskProvider, inboundConnectionTaskProvider, connectionInitiationTaskProvider, scheduledComTaskExecutionProvider,
                meteringGroupsService, customPropertySetService, readingTypeUtilService, threadPrincipalService, userPreferencesService, deviceConfigurationService, deviceService, lockService);
//        setId(device, ID);
        device.initialize(deviceConfiguration, "Name", startOfMeterActivation);
        device.save();
        return device;
    }

    @Test
    public void getMultiplierWhenNoMultiplierIsDefined() {
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Collections.singletonList(meterActivation)).when(meter).getMeterActivations();

        Device mockedDevice = createMockedDevice(now);

        assertThat(mockedDevice.getMultiplier()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    public void dontCreateNewMeterActivationWhenMultiplierIsOneTest() {
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Collections.singletonList(meterActivation)).when(meter).getMeterActivations();

        Device mockedDevice = createMockedDevice(Instant.now());
        mockedDevice.setMultiplier(BigDecimal.ONE);

        verify(meterActivation, never()).endAt(any(Instant.class));
    }

    @Test
    public void setMultiplierTest() {
        Instant start = Instant.ofEpochSecond(1469540100L);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meterActivation.getStart()).thenReturn(start);
        doReturn(Collections.singletonList(meterActivation)).when(meter).getMeterActivations();

        Device mockedDevice = createMockedDevice(start);

        Instant from = Instant.ofEpochSecond(1469540400L);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(newMeterActivation.getStart()).thenReturn(from);
        when(meter.activate(from)).thenReturn(newMeterActivation);
        doReturn(Arrays.asList(newMeterActivation, meterActivation)).when(meter).getMeterActivations();
        doReturn(Optional.of(meterActivation)).when(meter).getMeterActivation(from);
        when(meter.getConfiguration(from)).thenReturn(Optional.empty());

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, from);
        mockedDevice.save();

        verify(meterActivation).endAt(from);
        verify(meter).activate(from);
        verify(newMeterActivation).setMultiplier(multiplierType, multiplier);
    }

    @Test
    public void setMultiplierInThePastTest() {
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meterActivation.getStart()).thenReturn(startOfMeterActivation);
        doReturn(Collections.singletonList(meterActivation)).when(meter).getMeterActivations();

        Device mockedDevice = createMockedDevice(startOfMeterActivation);

        Instant past = now.minus(1, ChronoUnit.DAYS);
        MeterActivation newMeterActivation = mock(MeterActivation.class, "newMeterActivation");
        when(meter.activate(past)).thenReturn(newMeterActivation);
        doReturn(Optional.of(meterActivation)).when(meter).getMeterActivation(past);
        when(newMeterActivation.getStart()).thenReturn(past);
        when(meter.getConfiguration(past)).thenReturn(Optional.empty());
        doReturn(Arrays.asList(newMeterActivation, meterActivation)).when(meter).getMeterActivations();
        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, past);
        mockedDevice.save();

        verify(meterActivation).endAt(past);
        verify(meter).activate(past);
        verify(newMeterActivation).setMultiplier(multiplierType, multiplier);
    }

    @Test
    @Expected(value = MultiplierConfigurationException.class, message = "You can not configure a multiplier in the past when your device already has data")
    public void setMultiplierInThePastWhenAlreadyDataTest() {
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Collections.singletonList(meterActivation)).when(meter).getMeterActivations();

        Device mockedDevice = createMockedDevice(now);

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
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meterActivation.getStart()).thenReturn(now);
        doReturn(Collections.singletonList(meterActivation)).when(meter).getMeterActivations();


        Device mockedDevice = createMockedDevice(now);

        when(meter.hasData()).thenReturn(true);
        Instant from = Instant.ofEpochSecond(1469540400L);
        doReturn(Optional.of(meterActivation)).when(meter).getMeterActivation(from);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(meter.activate(from)).thenReturn(newMeterActivation);
        when(meter.getConfiguration(from)).thenReturn(Optional.empty());

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, from);
        mockedDevice.save();

        verify(meterActivation).endAt(from);
        verify(meter).activate(from);
        verify(newMeterActivation).setMultiplier(multiplierType, multiplier);
    }

    @Test
    @Expected(value = MultiplierConfigurationException.class, message = "You can not configure a multiplier with a start date which doesn't correspond with a meter activation")
    public void setMultiplierOnMeterActivationThatDoesntExist() {
        MeterActivation futureMeterActivation = mock(MeterActivation.class);
        when(futureMeterActivation.getId()).thenReturn(55432186L);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation, futureMeterActivation)).when(meter).getMeterActivations();

        Device mockedDevice = createMockedDevice(now);

        when(meterActivation.getRange()).thenReturn(Range.openClosed(startOfMeterActivation, now));
        when(meter.hasData()).thenReturn(true);
        Instant from = now.plus(1, ChronoUnit.DAYS);
        doReturn(Optional.empty()).when(meter).getMeterActivation(from);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(meter.activate(from)).thenReturn(newMeterActivation);

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, from);
    }


    @Test
    @Expected(value = MultiplierConfigurationException.class, message = "You can not configure a multiplier with a start date which doesn't correspond with a meter activation")
    public void setMultiplierInPastOutsideRangeOfCurrentMeterActivationTest() {
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Collections.singletonList(meterActivation)).when(meter).getMeterActivations();

        Device mockedDevice = createMockedDevice(startOfMeterActivation);

        when(meterActivation.getRange()).thenReturn(Range.openClosed(startOfMeterActivation, now));
        Instant from = startOfMeterActivation.minus(1, ChronoUnit.DAYS);
        when(meter.getMeterActivation(from)).thenReturn(Optional.empty());
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(meter.activate(from)).thenReturn(newMeterActivation);

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, from);
    }

    @Test
    public void getMultiplierEffectiveTimeStampWhenNoMultiplierIsDefinedTest() {
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meterActivation.getStart()).thenReturn(now);
        doReturn(Collections.singletonList(meterActivation)).when(meter).getMeterActivations();

        Device mockedDevice = createMockedDevice(now);

        assertThat(mockedDevice.getMultiplierEffectiveTimeStamp()).isEqualTo(now);
    }

    @Test
    public void getMultiplierEffectiveTimeStampWhenMultiplierIsDefinedTest() {
        Instant meterActivationStart = Instant.ofEpochSecond(1419465600L);

        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Collections.singletonList(meterActivation)).when(meter).getMeterActivations();
        when(meter.activate(meterActivationStart)).thenReturn(meterActivation);
        when(meterActivation.getStart()).thenReturn(meterActivationStart);
        when(meterActivation.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));

        Device mockedDevice = createMockedDevice(meterActivationStart);

        assertThat(mockedDevice.getMultiplierEffectiveTimeStamp()).isEqualTo(meterActivationStart);
    }

    @Test
    public void getMultiplierEffectiveTimeStampWhenMultiplierIsDefinedAndMultipleMeterActivationsTest() {
        Instant meterActivationStart = Instant.ofEpochSecond(1419465600L);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meterActivation.getStart()).thenReturn(meterActivationStart);
        when(meterActivation.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));

        Instant otherMeterActivationStart = Instant.ofEpochSecond(1387929600L);
        MeterActivation otherMeterActivation = mock(MeterActivation.class);
        when(otherMeterActivation.getStart()).thenReturn(otherMeterActivationStart);
        when(otherMeterActivation.getMultiplier(multiplierType)).thenReturn(Optional.of(BigDecimal.TEN));
        doReturn(Arrays.asList(otherMeterActivation, meterActivation)).when(meter).getMeterActivations();
        when(meter.activate(otherMeterActivationStart)).thenReturn(otherMeterActivation);

        Device mockedDevice = createMockedDevice(otherMeterActivationStart);  // we create on oldest date

        assertThat(mockedDevice.getMultiplierEffectiveTimeStamp()).isEqualTo(otherMeterActivationStart);
    }

    @Test
    public void getMultiplierEffectiveTimeStampWhenMultiplierIsDefinedAndMultipleMeterActivationsWithOtherMultiplierTest() {

        Instant meterActivationStart = Instant.ofEpochSecond(1419465600L);
        Instant otherMeterActivationStart1 = Instant.ofEpochSecond(1387929600L);
        Instant otherMeterActivationStart2 = Instant.ofEpochSecond(1387920600L);
        Instant otherMeterActivationStart3 = Instant.ofEpochSecond(1387909600L);
        MeterActivation otherMeterActivation1 = mock(MeterActivation.class);
        MeterActivation otherMeterActivation2 = mock(MeterActivation.class);
        MeterActivation otherMeterActivation3 = mock(MeterActivation.class);

        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meter.activate(meterActivationStart)).thenReturn(meterActivation);
        when(meter.activate(otherMeterActivationStart1)).thenReturn(otherMeterActivation1);
        when(meter.activate(otherMeterActivationStart2)).thenReturn(otherMeterActivation2);
        when(meter.activate(otherMeterActivationStart3)).thenReturn(otherMeterActivation3);

        Device mockedDevice = createMockedDevice(meterActivationStart);

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

        Instant meterActivationStart = Instant.ofEpochSecond(1419465600L);
        Instant otherMeterActivationStart1 = Instant.ofEpochSecond(1387929600L);
        Instant otherMeterActivationStart2 = Instant.ofEpochSecond(1387920600L);
        Instant otherMeterActivationStart3 = Instant.ofEpochSecond(1387909600L);
        MeterActivation otherMeterActivation1 = mock(MeterActivation.class, "otherMeterActivation1");
        MeterActivation otherMeterActivation2 = mock(MeterActivation.class, "otherMeterActivation2");
        MeterActivation otherMeterActivation3 = mock(MeterActivation.class, "otherMeterActivation3");

        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meter.activate(meterActivationStart)).thenReturn(meterActivation);
        when(meter.activate(otherMeterActivationStart1)).thenReturn(otherMeterActivation1);
        when(meter.activate(otherMeterActivationStart2)).thenReturn(otherMeterActivation2);
        when(meter.activate(otherMeterActivationStart3)).thenReturn(otherMeterActivation3);

        Device mockedDevice = createMockedDevice(meterActivationStart);

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
