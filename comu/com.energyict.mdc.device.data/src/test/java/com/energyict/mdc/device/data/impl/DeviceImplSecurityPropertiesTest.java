package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.*;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;

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
    private MdcReadingTypeUtilService readingTypeUtilService;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceLifeCycle deviceLifeCycle;
    @Mock
    private FiniteStateMachine finiteStateMachine;
    @Mock
    private SecurityPropertySet securityPropertySet;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private Meter meter;
    @Mock
    private LifecycleDates lifeCycleDates;
    @Mock
    private MultiplierType multiplierType;


    @Before
    public void setup() {
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        when(deviceLifeCycle.getFiniteStateMachine()).thenReturn(finiteStateMachine);
        MeterBuilder meterBuilder = FakeBuilder.initBuilderStub(meter, MeterBuilder.class);
        when(amrSystem.newMeter(anyString())).thenReturn(meterBuilder);
        when(meter.getLifecycleDates()).thenReturn(lifeCycleDates);
        mockDataModelWithNoValidationIssues();
        when(meteringService.findAmrSystem(anyLong())).thenReturn(Optional.of(amrSystem));
        when(meteringService.getMultiplierType("Default")).thenReturn(Optional.of(multiplierType));
        when(multiplierType.getName()).thenReturn("Default");
        when(amrSystem.findMeter(anyString())).thenReturn(Optional.of(meter));
    }

    private DeviceImpl setId(DeviceImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

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
        device.securityPropertiesAreValid();

        // Asserts
        verify(this.securityPropertyService).securityPropertiesAreValid(device);
    }

    @Test
    public void setSecurityProperties() {
        DeviceImpl device = this.getTestInstance();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty("One", BigDecimal.TEN);
        properties.setProperty("Two", "just a string");
        setId(device, 1000L); // fake the device as an already persisted device
        Meter koreMeter = mock(Meter.class);
        when(amrSystem.findMeter("1000")).thenReturn(Optional.of(koreMeter));
        // Business method
        device.setSecurityProperties(this.securityPropertySet, properties);
        device.save();

        // Asserts
        verify(this.securityPropertyService).setSecurityProperties(device, this.securityPropertySet, properties);
    }

    private void mockDataModelWithNoValidationIssues() {
        ValidatorFactory validationFactory = mock(ValidatorFactory.class);
        Validator validator = mock(Validator.class);
        when(validationFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), any())).thenReturn(Collections.emptySet());
        when(dataModel.getValidatorFactory()).thenReturn(validationFactory);
    }

    private DeviceImpl getTestInstance() {
        DeviceImpl device = new DeviceImpl(
                this.dataModel, this.eventService, this.issueService, this.thesaurus, this.clock, this.meteringService,
                this.validationService, this.securityPropertyService,
                this.scheduledConnectionTaskProvider, this.inboundConnectionTaskProvider, this.connectionInitiationTaskProvider,
                this.scheduledComTaskExecutionProvider, this.manuallyScheduledComTaskExecutionProvider,
                this.firmwareComTaskExecutionProvider, this.meteringGroupsService, customPropertySetService, readingTypeUtilService);
        device.initialize(this.deviceConfiguration, "Not persistent", "with all mocked services", null);
        return device;
    }

}