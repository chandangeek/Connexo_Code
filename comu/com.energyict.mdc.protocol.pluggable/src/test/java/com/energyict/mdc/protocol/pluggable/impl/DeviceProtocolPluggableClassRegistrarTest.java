package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.protocolimplv2.sdksample.SDKDeviceProtocol;
import com.energyict.protocols.mdc.channels.ip.OutboundIpConnectionType;
import com.google.common.base.Optional;

import java.util.Arrays;
import java.util.Collections;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
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
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private LicensedProtocol licensedProtocol;

    private TransactionService transactionService = new FakeTransactionService();

    @Test
    public void testRegistration() {
        DeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<SDKDeviceProtocol> expectedDeviceProtocolClass = SDKDeviceProtocol.class;
        String expectedDeviceProtocolClassName = expectedDeviceProtocolClass.getName();
        when(this.licensedProtocol.getClassName()).thenReturn(expectedDeviceProtocolClassName);
        when(this.licensedProtocol.getName()).thenReturn(expectedDeviceProtocolClassName);
        // Make sure the DeviceProtocolPluggableClass does not exist yet
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName)).thenReturn(Collections.emptyList());
        DeviceProtocolPluggableClass expectedCreated = mock(DeviceProtocolPluggableClass.class);
        when(this.protocolPluggableService.newDeviceProtocolPluggableClass(anyString(), anyString())).thenReturn(expectedCreated);

        // Business method
        registrar.registerAll(Arrays.asList(this.licensedProtocol));

        // Asserts
        verify(this.protocolPluggableService).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName, expectedDeviceProtocolClassName);
    }

    @Test
    public void testNoRegistrationWhenAlreadyExists() {
        DeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<SDKDeviceProtocol> expectedDeviceProtocolClass = SDKDeviceProtocol.class;
        String expectedDeviceProtocolClassName = expectedDeviceProtocolClass.getName();
        when(this.licensedProtocol.getClassName()).thenReturn(expectedDeviceProtocolClassName);
        when(this.licensedProtocol.getName()).thenReturn(expectedDeviceProtocolClassName);
        // Mock the existence of the DeviceProtocolPluggableClass
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(expectedDeviceProtocolClassName)).thenReturn(Arrays.asList(deviceProtocolPluggableClass));

        // Business method
        registrar.registerAll(Arrays.asList(this.licensedProtocol));

        // Asserts
        verify(this.protocolPluggableService, never()).newDeviceProtocolPluggableClass(expectedDeviceProtocolClassName, expectedDeviceProtocolClassName);
    }

    private DeviceProtocolPluggableClassRegistrar testRegistrar() {
        return new DeviceProtocolPluggableClassRegistrar(this.protocolPluggableService, this.transactionService);
    }

}