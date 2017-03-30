/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.mocks.MockOutboundConnectionType;

import java.util.Collections;

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
 * Tests the {@link ConnectionTypePluggableClassRegistrar} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (14:12)
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTypePluggableClassRegistrarTest {

    @Mock
    private ServerProtocolPluggableService protocolPluggableService;
    @Mock
    private ConnectionTypeService connectionTypeService;

    private TransactionService transactionService = new FakeTransactionService(null);

    @Test
    public void testRegistration() {
        ConnectionTypePluggableClassRegistrar registrar = this.testRegistrar();
        PluggableClassDefinition pluggableClassDefinition = mock(PluggableClassDefinition.class);
        String expectedConnectionTypeClassName = MockOutboundConnectionType.class.getName();
        String expectedPluggableClassName = MockOutboundConnectionType.class.getSimpleName();
        when(pluggableClassDefinition.getName()).thenReturn(expectedPluggableClassName);
        when(pluggableClassDefinition.getProtocolTypeClass()).thenReturn(MockOutboundConnectionType.class);
        when(this.connectionTypeService.getExistingConnectionTypePluggableClasses()).thenReturn(Collections.singletonList(pluggableClassDefinition));
        // Make sure the ConnectionTypePluggableClass does not exist yet
        when(this.protocolPluggableService.findConnectionTypePluggableClassByClassName(expectedConnectionTypeClassName)).thenReturn(Collections.emptyList());
        ConnectionTypePluggableClass expectedCreated = mock(ConnectionTypePluggableClass.class);
        when(this.protocolPluggableService.newConnectionTypePluggableClass(anyString(), anyString())).thenReturn(expectedCreated);

        // Business method
        registrar.registerAll(Collections.singletonList(this.connectionTypeService));

        // Asserts
        verify(this.connectionTypeService).getExistingConnectionTypePluggableClasses();
        verify(this.protocolPluggableService).newConnectionTypePluggableClass(expectedPluggableClassName, expectedConnectionTypeClassName);
        verify(this.protocolPluggableService, never()).registerConnectionTypePluggableClassAsCustomPropertySet(expectedConnectionTypeClassName);
    }

    @Test
    public void testNoRegistrationWhenAlreadyExists() {
        ConnectionTypePluggableClassRegistrar registrar = this.testRegistrar();
        PluggableClassDefinition pluggableClassDefinition = mock(PluggableClassDefinition.class);
        String expectedConnectionTypeClassName = MockOutboundConnectionType.class.getName();
        String expectedPluggableClassName = MockOutboundConnectionType.class.getSimpleName();
        when(pluggableClassDefinition.getName()).thenReturn(expectedPluggableClassName);
        when(pluggableClassDefinition.getProtocolTypeClass()).thenReturn(MockOutboundConnectionType.class);
        when(this.connectionTypeService.getExistingConnectionTypePluggableClasses()).thenReturn(Collections.singletonList(pluggableClassDefinition));
        // Mock that the ConnectionTypePluggableClass already exists
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(this.protocolPluggableService.findConnectionTypePluggableClassByClassName(expectedConnectionTypeClassName)).thenReturn(Collections.singletonList(connectionTypePluggableClass));

        // Business method
        registrar.registerAll(Collections.singletonList(this.connectionTypeService));

        // Asserts
        verify(this.connectionTypeService).getExistingConnectionTypePluggableClasses();
        verify(this.protocolPluggableService, never()).newConnectionTypePluggableClass(expectedPluggableClassName, expectedConnectionTypeClassName);
        verify(this.protocolPluggableService).registerConnectionTypePluggableClassAsCustomPropertySet(expectedConnectionTypeClassName);
    }

    private ConnectionTypePluggableClassRegistrar testRegistrar() {
        return new ConnectionTypePluggableClassRegistrar(this.protocolPluggableService, this.transactionService);
    }

}