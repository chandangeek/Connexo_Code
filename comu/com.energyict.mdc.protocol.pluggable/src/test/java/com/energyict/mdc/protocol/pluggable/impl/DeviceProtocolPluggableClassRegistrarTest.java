/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.LicensedProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.mocks.MockMeterProtocol;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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
    private LicensedProtocol licensedProtocol, licensedProtocol2, licensedProtocol3;
    @Mock
    private MeteringService meteringService;

    private final TransactionService actualTransactionService = mock(TransactionService.class);

    private TransactionService transactionService = new FakeTransactionService(actualTransactionService);

    @Before
    public void before() {
        when(actualTransactionService.isInTransaction()).thenReturn(false);
    }
    @Test
    public void testRegistration() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.supportedEventTypes()).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<MockMeterProtocol> expectedDeviceProtocolClass = MockMeterProtocol.class;
        String expectedDeviceProtocolClassName = expectedDeviceProtocolClass.getName();
        when(this.licensedProtocol.getClassName()).thenReturn(expectedDeviceProtocolClassName);
        when(this.licensedProtocol.getName()).thenReturn(expectedDeviceProtocolClassName);
        // Make sure the DeviceProtocolPluggableClass does not exist yet
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName)).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClass expectedCreated = mock(DeviceProtocolPluggableClass.class);
        when(expectedCreated.getDeviceProtocol()).thenReturn(deviceProtocol);
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
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.supportedEventTypes()).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<MockMeterProtocol> expectedDeviceProtocolClass = MockMeterProtocol.class;
        String expectedDeviceProtocolClassName = expectedDeviceProtocolClass.getName();
        when(this.licensedProtocol.getClassName()).thenReturn(expectedDeviceProtocolClassName);
        when(this.licensedProtocol.getName()).thenReturn(expectedDeviceProtocolClassName);
        // Mock the existence of the DeviceProtocolPluggableClass
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName)).thenReturn(Collections.singletonList(deviceProtocolPluggableClass));

        // Business method
        registrar.registerAll(Collections.singletonList(this.licensedProtocol));

        // Asserts
        verify(this.protocolPluggableService, never()).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName, expectedDeviceProtocolClassName);
        verify(this.protocolPluggableService).registerDeviceProtocolPluggableClassAsCustomPropertySet(expectedDeviceProtocolClassName);
    }

    @Test
    public void testMultipleRegistration() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.supportedEventTypes()).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<MockMeterProtocol> expectedDeviceProtocolClass = MockMeterProtocol.class;
        String expectedDeviceProtocolClassName = expectedDeviceProtocolClass.getName();
        when(this.licensedProtocol.getClassName()).thenReturn(expectedDeviceProtocolClassName + "1");
        when(this.licensedProtocol.getName()).thenReturn(expectedDeviceProtocolClassName + "1");
        when(this.licensedProtocol2.getClassName()).thenReturn(expectedDeviceProtocolClassName + "2");
        when(this.licensedProtocol2.getName()).thenReturn(expectedDeviceProtocolClassName + "2");
        when(this.licensedProtocol3.getClassName()).thenReturn(expectedDeviceProtocolClassName+"3");
        when(this.licensedProtocol3.getName()).thenReturn(expectedDeviceProtocolClassName + "3");

        // Make sure the DeviceProtocolPluggableClasses do not exist yet
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName+"1")).thenReturn(Collections.emptyList());
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName+"2")).thenReturn(Collections.emptyList());
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName+"3")).thenReturn(Collections.emptyList());

        DeviceProtocolPluggableClass expectedCreated = mock(DeviceProtocolPluggableClass.class);
        when(expectedCreated.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.protocolPluggableService.newDeviceProtocolPluggableClass(anyString(), anyString())).thenReturn(expectedCreated);

        // Business method
        registrar.registerAll(Arrays.asList(this.licensedProtocol, this.licensedProtocol2, this.licensedProtocol3));

        // Asserts
        verify(this.protocolPluggableService).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName+"1", expectedDeviceProtocolClassName+"1");
        verify(this.protocolPluggableService).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName+"2", expectedDeviceProtocolClassName+"2");
        verify(this.protocolPluggableService).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName+"3", expectedDeviceProtocolClassName+"3");
    }

    @Test
    public void testMultipleRegistrationWithFailure() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.supportedEventTypes()).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<MockMeterProtocol> expectedDeviceProtocolClass = MockMeterProtocol.class;
        String expectedDeviceProtocolClassName = expectedDeviceProtocolClass.getName();
        when(this.licensedProtocol.getClassName()).thenReturn(expectedDeviceProtocolClassName + "1");
        when(this.licensedProtocol.getName()).thenReturn(expectedDeviceProtocolClassName + "1");
        when(this.licensedProtocol2.getClassName()).thenReturn(expectedDeviceProtocolClassName + "2");
        when(this.licensedProtocol2.getName()).thenReturn(expectedDeviceProtocolClassName + "2");
        when(this.licensedProtocol3.getClassName()).thenReturn(expectedDeviceProtocolClassName+"3");
        when(this.licensedProtocol3.getName()).thenReturn(expectedDeviceProtocolClassName + "3");

        // Make sure the DeviceProtocolPluggableClasses do not exist yet
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName+"1")).thenReturn(Collections.emptyList());
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName+"2")).thenReturn(Collections.emptyList());
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName+"3")).thenReturn(Collections.emptyList());

        DeviceProtocolPluggableClass expectedCreated = mock(DeviceProtocolPluggableClass.class);
        when(expectedCreated.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(protocolPluggableService.createProtocol(expectedDeviceProtocolClassName + "2")).thenThrow(NoServiceFoundThatCanLoadTheJavaClass.class);
        when(this.protocolPluggableService.newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName + "1",expectedDeviceProtocolClassName + "1")).thenReturn(expectedCreated);
        when(this.protocolPluggableService.newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName + "3",expectedDeviceProtocolClassName + "3")).thenReturn(expectedCreated);


        // Business method
        registrar.registerAll(Arrays.asList(this.licensedProtocol, this.licensedProtocol2, this.licensedProtocol3));

        // Asserts
        verify(this.protocolPluggableService).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName+"1", expectedDeviceProtocolClassName+"1");
        verify(this.protocolPluggableService).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName+"3", expectedDeviceProtocolClassName+"3");
    }

    private DeviceProtocolPluggableClassRegistrar testRegistrar() {
        return new DeviceProtocolPluggableClassRegistrar(this.protocolPluggableService, this.transactionService, this.meteringService);
    }

}