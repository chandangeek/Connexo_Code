package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.TransactionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
 * Tests the {@link InboundDeviceProtocolPluggableClassRegistrar} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (13:56)
 */
@RunWith(MockitoJUnitRunner.class)
public class InboundDeviceProtocolPluggableClassRegistrarTest {

    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private InboundDeviceProtocolService inboundDeviceProtocolService;

    private TransactionService transactionService = new FakeTransactionService(null);

    @Test
    public void testRegistration() {
        InboundDeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<TestInboundDeviceProtocol> expectedInboundDeviceProtocolClass = TestInboundDeviceProtocol.class;
        String expectedInboundDeviceProtocolClassName = expectedInboundDeviceProtocolClass.getName();
        PluggableClassDefinition pluggableClassDefinition = mock(PluggableClassDefinition.class);
        when(pluggableClassDefinition.getProtocolTypeClass()).thenReturn(expectedInboundDeviceProtocolClass);
        String expectedPluggableClassName = expectedInboundDeviceProtocolClass.getSimpleName();
        when(pluggableClassDefinition.getName()).thenReturn(expectedPluggableClassName);
        when(this.inboundDeviceProtocolService.getExistingInboundDeviceProtocolPluggableClasses()).thenReturn(Arrays.asList(pluggableClassDefinition));
        // Make sure the InboundDeviceProtocolPluggableClass does not exist yet
        when(this.protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(anyString())).thenReturn(Collections.emptyList());
        InboundDeviceProtocolPluggableClass expectedCreated = mock(InboundDeviceProtocolPluggableClass.class);
        when(this.protocolPluggableService.newInboundDeviceProtocolPluggableClass(anyString(), anyString())).thenReturn(expectedCreated);

        // Business method
        registrar.registerAll(Arrays.asList(this.inboundDeviceProtocolService));

        // Asserts
        verify(this.inboundDeviceProtocolService).getExistingInboundDeviceProtocolPluggableClasses();
        verify(this.protocolPluggableService).newInboundDeviceProtocolPluggableClass(expectedPluggableClassName, expectedInboundDeviceProtocolClassName);
        verify(expectedCreated).save();
    }

    @Test
    public void testNoRegistrationWhenAlreadyExists() {
        InboundDeviceProtocolPluggableClassRegistrar registrar = this.testRegistrar();
        Class<TestInboundDeviceProtocol> expectedInboundDeviceProtocolClass = TestInboundDeviceProtocol.class;
        String expectedInboundDeviceProtocolClassName = expectedInboundDeviceProtocolClass.getName();
        PluggableClassDefinition pluggableClassDefinition = mock(PluggableClassDefinition.class);
        when(pluggableClassDefinition.getProtocolTypeClass()).thenReturn(expectedInboundDeviceProtocolClass);
        String expectedPluggableClassName = expectedInboundDeviceProtocolClass.getSimpleName();
        when(pluggableClassDefinition.getName()).thenReturn(expectedPluggableClassName);
        when(this.inboundDeviceProtocolService.getExistingInboundDeviceProtocolPluggableClasses()).thenReturn(Arrays.asList(pluggableClassDefinition));
        // Mock the existence of the InboundDeviceProtocolPluggableClass
        InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(this.protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(expectedInboundDeviceProtocolClassName)).thenReturn(Arrays.asList(inboundDeviceProtocolPluggableClass));

        // Business method
        registrar.registerAll(Arrays.asList(this.inboundDeviceProtocolService));

        // Asserts
        verify(this.inboundDeviceProtocolService).getExistingInboundDeviceProtocolPluggableClasses();
        verify(this.protocolPluggableService, never()).newInboundDeviceProtocolPluggableClass(expectedPluggableClassName, expectedInboundDeviceProtocolClassName);
    }

    private InboundDeviceProtocolPluggableClassRegistrar testRegistrar() {
        return new InboundDeviceProtocolPluggableClassRegistrar(this.protocolPluggableService, this.transactionService);
    }

    private class TestInboundDeviceProtocol implements InboundDeviceProtocol {

        @Override
        public void initializeDiscoveryContext(InboundDiscoveryContext context) {

        }

        @Override
        public InboundDiscoveryContext getContext() {
            return null;
        }

        @Override
        public DiscoverResultType doDiscovery() {
            return null;
        }

        @Override
        public void provideResponse(DiscoverResponseType responseType) {

        }

        @Override
        public DeviceIdentifier getDeviceIdentifier() {
            return null;
        }

        @Override
        public List<CollectedData> getCollectedData(OfflineDevice device) {
            return null;
        }

        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public void copyProperties(TypedProperties properties) {

        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return null;
        }

        @Override
        public PropertySpec getPropertySpec(String s) {
            return null;
        }
    }
}