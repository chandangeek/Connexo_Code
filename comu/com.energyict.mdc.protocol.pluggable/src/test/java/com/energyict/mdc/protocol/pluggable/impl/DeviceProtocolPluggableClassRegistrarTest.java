/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol;

import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceProtocolPluggableClassRegistrar} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (13:21)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolPluggableClassRegistrarTest {

    @Mock
    private ServerProtocolPluggableService protocolPluggableService;
    @Mock
    private LicensedProtocol licensedProtocol;
    @Mock
    private MeteringService meteringService;

    private TransactionService transactionService = new FakeTransactionService(null);

    @Test
    public void testRegistration() {
        DeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<MockMeterProtocol> expectedDeviceProtocolClass = MockMeterProtocol.class;
        String expectedDeviceProtocolClassName = expectedDeviceProtocolClass.getName();
        when(this.licensedProtocol.getClassName()).thenReturn(expectedDeviceProtocolClassName);
        when(this.licensedProtocol.getName()).thenReturn(expectedDeviceProtocolClassName);
        // Make sure the DeviceProtocolPluggableClass does not exist yet
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName)).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClass expectedCreated = mock(DeviceProtocolPluggableClass.class);
        when(this.protocolPluggableService.newDeviceProtocolPluggableClass(anyString(), anyString())).thenReturn(expectedCreated);

        // Business method
        registrar.registerAll(Collections.singletonList(this.licensedProtocol));

        // Asserts
        verify(this.protocolPluggableService).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName, expectedDeviceProtocolClassName);
        verify(this.protocolPluggableService, never()).registerDeviceProtocolPluggableClassAsCustomPropertySet(expectedDeviceProtocolClassName);
    }

    @Test
    public void testRegistrationWithEndDeviceEventTypeCreation() {
        DeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<MockDeviceProtocol> expectedDeviceProtocolClass = MockDeviceProtocol.class;
        String expectedDeviceProtocolClassName = expectedDeviceProtocolClass.getName();
        when(this.licensedProtocol.getClassName()).thenReturn(expectedDeviceProtocolClassName);
        when(this.licensedProtocol.getName()).thenReturn(expectedDeviceProtocolClassName);
        // Make sure the DeviceProtocolPluggableClass does not exist yet
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName)).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClass expectedCreated = mock(DeviceProtocolPluggableClass.class);
        when(this.protocolPluggableService.newDeviceProtocolPluggableClass(anyString(), anyString())).thenReturn(expectedCreated);
        when(expectedCreated.getDeviceProtocol()).thenReturn(new MockDeviceProtocol());
        when(this.meteringService.getEndDeviceEventType(MockDeviceProtocol.END_DEVICE_EVENT_TYPE_MRID)).thenReturn(Optional.empty());

        // Business method
        registrar.registerAll(Collections.singletonList(this.licensedProtocol));

        // Asserts
        verify(this.protocolPluggableService).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName, expectedDeviceProtocolClassName);
        verify(this.protocolPluggableService, never()).registerDeviceProtocolPluggableClassAsCustomPropertySet(expectedDeviceProtocolClassName);
        verify(this.meteringService).getEndDeviceEventType(MockDeviceProtocol.END_DEVICE_EVENT_TYPE_MRID);
        verify(this.meteringService).createEndDeviceEventType(MockDeviceProtocol.END_DEVICE_EVENT_TYPE_MRID);
    }

    @Test
    public void testRegistrationWithExistingEndDeviceEventType() {
        DeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<MockDeviceProtocol> expectedDeviceProtocolClass = MockDeviceProtocol.class;
        String expectedDeviceProtocolClassName = expectedDeviceProtocolClass.getName();
        when(this.licensedProtocol.getClassName()).thenReturn(expectedDeviceProtocolClassName);
        when(this.licensedProtocol.getName()).thenReturn(expectedDeviceProtocolClassName);
        // Make sure the DeviceProtocolPluggableClass does not exist yet
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName)).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClass expectedCreated = mock(DeviceProtocolPluggableClass.class);
        when(this.protocolPluggableService.newDeviceProtocolPluggableClass(anyString(), anyString())).thenReturn(expectedCreated);
        when(expectedCreated.getDeviceProtocol()).thenReturn(new MockDeviceProtocol());
        when(this.meteringService.getEndDeviceEventType(MockDeviceProtocol.END_DEVICE_EVENT_TYPE_MRID)).thenReturn(Optional.of(mock(EndDeviceEventType.class)));

        // Business method
        registrar.registerAll(Collections.singletonList(this.licensedProtocol));

        // Asserts
        verify(this.protocolPluggableService).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName, expectedDeviceProtocolClassName);
        verify(this.protocolPluggableService, never()).registerDeviceProtocolPluggableClassAsCustomPropertySet(expectedDeviceProtocolClassName);
        verify(this.meteringService).getEndDeviceEventType(MockDeviceProtocol.END_DEVICE_EVENT_TYPE_MRID);
        verify(this.meteringService, never()).createEndDeviceEventType(MockDeviceProtocol.END_DEVICE_EVENT_TYPE_MRID);
    }

    @Test
    public void testNoRegistrationWhenAlreadyExists() {
        DeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<MockMeterProtocol> expectedDeviceProtocolClass = MockMeterProtocol.class;
        String expectedDeviceProtocolClassName = expectedDeviceProtocolClass.getName();
        when(this.licensedProtocol.getClassName()).thenReturn(expectedDeviceProtocolClassName);
        when(this.licensedProtocol.getName()).thenReturn(expectedDeviceProtocolClassName);
        // Mock the existence of the DeviceProtocolPluggableClass
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName)).thenReturn(Collections.singletonList(deviceProtocolPluggableClass));

        // Business method
        registrar.registerAll(Collections.singletonList(this.licensedProtocol));

        // Asserts
        verify(this.protocolPluggableService, never()).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName, expectedDeviceProtocolClassName);
        verify(this.protocolPluggableService).registerDeviceProtocolPluggableClassAsCustomPropertySet(expectedDeviceProtocolClassName);
    }

    private DeviceProtocolPluggableClassRegistrar testRegistrar() {
        return new DeviceProtocolPluggableClassRegistrar(this.protocolPluggableService, this.transactionService, this.meteringService);
    }

}