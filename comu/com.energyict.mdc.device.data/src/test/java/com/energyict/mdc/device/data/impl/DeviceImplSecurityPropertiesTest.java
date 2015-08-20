package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.FirmwareComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ManuallyScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the security properties of the {@link DeviceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-28 (12:46)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceImplSecurityPropertiesTest {

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
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private Provider<ManuallyScheduledComTaskExecutionImpl> manuallyScheduledComTaskExecutionProvider;
    @Mock
    private Provider<FirmwareComTaskExecutionImpl> firmwareComTaskExecutionProvider;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private SecurityPropertySet securityPropertySet;

    @Test
    public void getSecurityPropertiesUsesClock() {
        DeviceImpl device = this.getTestInstance();
        reset(this.clock);

        // Business method
        device.getSecurityProperties(this.securityPropertySet);

        // Asserts
        verify(this.clock).instant();
    }

    @Test
    public void getSecurityPropertiesDelegatesToServiceWithCorrectParameters() {
        Instant now = Instant.ofEpochSecond(97L);
        when(this.clock.instant()).thenReturn(now);
        DeviceImpl device = this.getTestInstance();

        // Business method
        device.getSecurityProperties(this.securityPropertySet);

        // Asserts
        verify(this.securityPropertyService).getSecurityProperties(device, now, this.securityPropertySet);
    }

    @Test
    public void hasSecurityPropertiesUsesClock() {
        DeviceImpl device = this.getTestInstance();
        reset(this.clock);

        // Business method
        device.hasSecurityProperties(this.securityPropertySet);

        // Asserts
        verify(this.clock).instant();
    }

    @Test
    public void hasSecurityPropertiesDelegatesToServiceWithCorrectParameters() {
        Instant now = Instant.ofEpochSecond(97L);
        when(this.clock.instant()).thenReturn(now);
        DeviceImpl device = this.getTestInstance();

        // Business method
        device.hasSecurityProperties(this.securityPropertySet);

        // Asserts
        verify(this.securityPropertyService).hasSecurityProperties(device, now, this.securityPropertySet);
    }

    @Test
    public void securityPropertiesAreValidDelegatesToServiceWithCorrectParameters() {
        DeviceImpl device = this.getTestInstance();

        // Business method
        device.securityPropertiesAreValid("");

        // Asserts
        verify(this.securityPropertyService).securityPropertiesAreValid(device);
    }

    @Test
    public void setSecurityProperties() {
        DeviceImpl device = this.getTestInstance();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty("One", BigDecimal.TEN);
        properties.setProperty("Two", "just a string");

        // Business method
        device.setSecurityProperties(this.securityPropertySet, properties);

        // Asserts
        verify(this.securityPropertyService).setSecurityProperties(device, this.securityPropertySet, properties);
    }

    private DeviceImpl getTestInstance() {
        DeviceImpl device = new DeviceImpl(
                this.dataModel, this.eventService, this.issueService, this.thesaurus, this.clock, this.meteringService,
                this.validationService, this.connectionTaskService, this.communicationTaskService, this.securityPropertyService,
                this.scheduledConnectionTaskProvider, this.inboundConnectionTaskProvider, this.connectionInitiationTaskProvider,
                this.scheduledComTaskExecutionProvider, this.protocolPluggableService, this.manuallyScheduledComTaskExecutionProvider,
                this.firmwareComTaskExecutionProvider, this.meteringGroupsService);
        device.initialize(this.deviceConfiguration, "Not persistent", "with all mocked services");
        return device;
    }

}