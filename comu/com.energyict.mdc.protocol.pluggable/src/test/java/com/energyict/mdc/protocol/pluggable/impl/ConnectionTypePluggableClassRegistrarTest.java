package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionType;

import java.util.Arrays;
import java.util.Collections;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ConnectionTypePluggableClassRegistrar} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (14:12)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTypePluggableClassRegistrarTest {

    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private ConnectionTypeService connectionTypeService;

    private TransactionService transactionService = new FakeTransactionService(null);

    @Test
    public void testRegistration() {
        ConnectionTypePluggableClassRegistrar registrar = this.testRegistrar();
        PluggableClassDefinition pluggableClassDefinition = mock(PluggableClassDefinition.class);
        Class<OutboundIpConnectionType> expectedConnectionTypeClass = OutboundIpConnectionType.class;
        String expectedConnectionTypeClassName = expectedConnectionTypeClass.getName();
        when(pluggableClassDefinition.getProtocolTypeClass()).thenReturn(expectedConnectionTypeClass);
        String expectedPluggableClassName = expectedConnectionTypeClass.getSimpleName();
        when(pluggableClassDefinition.getName()).thenReturn(expectedPluggableClassName);
        when(this.connectionTypeService.getExistingConnectionTypePluggableClasses()).thenReturn(Arrays.asList(pluggableClassDefinition));
        // Make sure the ConnectionTypePluggableClass does not exist yet
        when(this.protocolPluggableService.findConnectionTypePluggableClassByClassName(expectedConnectionTypeClassName)).thenReturn(Collections.emptyList());
        ConnectionTypePluggableClass expectedCreated = mock(ConnectionTypePluggableClass.class);
        when(this.protocolPluggableService.newConnectionTypePluggableClass(anyString(), anyString())).thenReturn(expectedCreated);

        // Business method
        registrar.registerAll(Arrays.asList(this.connectionTypeService));

        // Asserts
        verify(this.connectionTypeService).getExistingConnectionTypePluggableClasses();
        verify(this.protocolPluggableService).newConnectionTypePluggableClass(expectedPluggableClassName, expectedConnectionTypeClassName);
        verify(expectedCreated).save();
    }

    @Test
    public void testNoRegistrationWhenAlreadyExists() {
        ConnectionTypePluggableClassRegistrar registrar = this.testRegistrar();
        PluggableClassDefinition pluggableClassDefinition = mock(PluggableClassDefinition.class);
        Class<OutboundIpConnectionType> expectedConnectionTypeClass = OutboundIpConnectionType.class;
        String expectedConnectionTypeClassName = expectedConnectionTypeClass.getName();
        when(pluggableClassDefinition.getProtocolTypeClass()).thenReturn(expectedConnectionTypeClass);
        String expectedPluggableClassName = expectedConnectionTypeClass.getSimpleName();
        when(pluggableClassDefinition.getName()).thenReturn(expectedPluggableClassName);
        when(this.connectionTypeService.getExistingConnectionTypePluggableClasses()).thenReturn(Arrays.asList(pluggableClassDefinition));
        // Mock that the ConnectionTypePluggableClass already exists
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(this.protocolPluggableService.findConnectionTypePluggableClassByClassName(expectedConnectionTypeClassName)).thenReturn(Arrays.asList(connectionTypePluggableClass));

        // Business method
        registrar.registerAll(Arrays.asList(this.connectionTypeService));

        // Asserts
        verify(this.connectionTypeService).getExistingConnectionTypePluggableClasses();
        verify(this.protocolPluggableService, never()).newConnectionTypePluggableClass(expectedPluggableClassName, expectedConnectionTypeClassName);
    }

    private ConnectionTypePluggableClassRegistrar testRegistrar() {
        return new ConnectionTypePluggableClassRegistrar(this.protocolPluggableService, this.transactionService);
    }

}