package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.*;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests regarding the DeviceMultiplier
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMultiplierTest {

    private final long ID = 9536541L;

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

    private Instant now = Instant.ofEpochMilli(1448460000000L);

    @Before
    public void setup() {
        when(clock.instant()).thenReturn(now);
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter(String.valueOf(ID))).thenReturn(Optional.of(meter));
        when(meteringService.getMultiplierType(any())).thenReturn(Optional.of(multiplierType));
        when(meterActivation.getMultiplier(multiplierType)).thenReturn(Optional.empty());
        when(meter.getUsagePoint(any())).thenReturn(Optional.empty());
    }

    private Device createMockedDevice() {
        DeviceImpl device = new DeviceImpl(dataModel, eventService, issueService, thesaurus, clock, meteringService, validationService,
                connectionTaskService, communicationTaskService, securityPropertyService, scheduledConnectionTaskProvider, inboundConnectionTaskProvider,
                connectionInitiationTaskProvider, scheduledComTaskExecutionProvider, protocolPluggableService, manuallyScheduledComTaskExecutionProvider,
                firmwareComTaskExecutionProvider, meteringGroupsService, readingTypeUtilService);
        setId(device, ID);
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
    public void setMultiplierTest() {
        Device mockedDevice = createMockedDevice();
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        Instant from = Instant.ofEpochSecond(1448466879L);
        MeterActivation newMeterActivation = mock(MeterActivation.class);
        when(meter.activate(from)).thenReturn(newMeterActivation);

        // business method
        BigDecimal multiplier = BigDecimal.TEN;
        mockedDevice.setMultiplier(multiplier, from);

        verify(meterActivation).endAt(from);
        verify(meter).activate(from);
        verify(newMeterActivation).setMultiplier(multiplierType, multiplier);
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