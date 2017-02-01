/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.exceptions.VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceProtocolPluggableClassDeletionEventHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-19 (13:14)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolPluggableClassDeletionEventHandlerTest {

    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceType deviceType;

    @Before
    public void initializeMocks () {
        when(this.nlsService.getThesaurus(DeviceConfigurationService.COMPONENTNAME, Layer.DOMAIN)).thenReturn(this.thesaurus);
        when(this.deviceProtocolPluggableClass.getName()).thenReturn(DeviceProtocolPluggableClassDeletionEventHandlerTest.class.getSimpleName());
        when(this.deviceType.getName()).thenReturn(DeviceProtocolPluggableClassDeletionEventHandlerTest.class.getSimpleName());
    }

    @Test
    public void testDeleteEventForDeviceProtocolPluggableClassThatIsNotUsed () {
        LocalEvent event = this.mockDeleteEvent(this.deviceProtocolPluggableClass);

        // Business method
        this.newTestHandler().handle(event);

        // Asserts
        verify(event).getSource();
        verify(this.deviceConfigurationService).findDeviceTypesWithDeviceProtocol(this.deviceProtocolPluggableClass);
        // Should not throw VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException
    }

    @Test(expected = VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException.class)
    public void testDeleteEventForDeviceProtocolPluggableClassThatIsStillInUse () {
        List<DeviceType> deviceTypes = Arrays.asList(this.deviceType);
        when(this.deviceConfigurationService.findDeviceTypesWithDeviceProtocol(this.deviceProtocolPluggableClass)).thenReturn(deviceTypes);

        LocalEvent event = this.mockDeleteEvent(this.deviceProtocolPluggableClass);

        // Business method
        this.newTestHandler().handle(event);

        // Asserts: expected VetoDeviceProtocolPluggableClassDeletionBecauseStillUsedByDeviceTypesException
    }

    private LocalEvent mockDeleteEvent(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        LocalEvent event = mock(LocalEvent.class);
        when(event.getSource()).thenReturn(deviceProtocolPluggableClass);
        EventType eventType = mock(EventType.class);
        when(eventType.getTopic()).thenReturn("com/energyict/mdc/protocol/pluggable/deviceprotocol/DELETED");
        when(event.getType()).thenReturn(eventType);
        return event;
    }

    private DeviceProtocolPluggableClassDeletionEventHandler newTestHandler () {
        DeviceProtocolPluggableClassDeletionEventHandler handler = new DeviceProtocolPluggableClassDeletionEventHandler();
        handler.setNlsService(this.nlsService);
        handler.setDeviceConfigurationService(this.deviceConfigurationService);
        return handler;
    }

}