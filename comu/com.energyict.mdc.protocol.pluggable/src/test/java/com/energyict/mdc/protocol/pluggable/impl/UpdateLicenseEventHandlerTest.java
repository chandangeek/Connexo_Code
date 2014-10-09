package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.event.Event;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link UpdateLicenseEventHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (13:04)
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateLicenseEventHandlerTest {

    @Mock
    private TransactionService transactionService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    @Mock
    private ConnectionTypeService connectionTypeService;

    @Test
    public void nonMDCEventDoesNotTriggerRegistrationProcess() {
        UpdateLicenseEventHandler eventHandler = this.testEventHandler();

        // Business method
        eventHandler.handle(this.mockedEvent(UpdateLicenseEventHandlerTest.class.getSimpleName()));

        // Asserts
        verify(this.protocolPluggableService, never()).getAllLicensedProtocols();
    }

    @Test
    public void mdcEventTriggersRegistrationProcess() {
        UpdateLicenseEventHandler eventHandler = this.testEventHandler();
        eventHandler.addConnectionTypeService(this.connectionTypeService);
        eventHandler.addInboundDeviceProtocolService(this.inboundDeviceProtocolService);
        reset(this.inboundDeviceProtocolService);
        reset(this.connectionTypeService);

        // Business method
        eventHandler.handle(this.mockedEvent("MDC"));

        // Asserts
        verify(this.protocolPluggableService).getAllLicensedProtocols();
        verify(this.inboundDeviceProtocolService).getExistingInboundDeviceProtocolPluggableClasses();
        verify(this.connectionTypeService).getExistingConnectionTypePluggableClasses();
    }

    @Test
    public void addConnectionTypeServiceTriggersRegistration() {
        UpdateLicenseEventHandler eventHandler = this.testEventHandler();

        // Business method
        eventHandler.addConnectionTypeService(this.connectionTypeService);

        // Asserts
        verify(this.connectionTypeService).getExistingConnectionTypePluggableClasses();
    }

    @Test
    public void addInboundDeviceProtocolServiceTriggersRegistration() {
        UpdateLicenseEventHandler eventHandler = this.testEventHandler();

        // Business method
        eventHandler.addInboundDeviceProtocolService(this.inboundDeviceProtocolService);

        // Asserts
        verify(this.inboundDeviceProtocolService).getExistingInboundDeviceProtocolPluggableClasses();
    }

    private UpdateLicenseEventHandler testEventHandler() {
        UpdateLicenseEventHandler eventHandler = new UpdateLicenseEventHandler();
        eventHandler.setProtocolPluggableService(this.protocolPluggableService);
        eventHandler.setTransactionService(this.transactionService);
        return eventHandler;
    }

    private LocalEvent mockedEvent(String applicationKey) {
        LocalEvent event = mock(LocalEvent.class);
        Map<String, Object> osgiEventProperties = new HashMap<>();
        osgiEventProperties.put("appKey", applicationKey);
        Event osgiEvent = new Event(UpdateLicenseEventHandlerTest.class.getSimpleName(), osgiEventProperties);
        when(event.toOsgiEvent()).thenReturn(osgiEvent);
        return event;
    }

}