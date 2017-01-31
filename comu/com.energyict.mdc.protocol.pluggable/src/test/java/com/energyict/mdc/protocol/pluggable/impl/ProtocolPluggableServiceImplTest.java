/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingException;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.api.services.NotAppropriateDeviceCacheMarshallingTargetException;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListener;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListenerRegistration;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ProtocolPluggableServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-11 (10:23)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProtocolPluggableServiceImplTest {

    private static final String PROTOCOL_JAVA_CLASS_NAME = "testing.com.energyict.mdc.protocol.pluggable.impl.DeviceProtocol";
    private static final String JSON_CACHE = "Whatever";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrmService ormService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EventService eventService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NlsService nlsService;
    @Mock
    private IssueService issueService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private PluggableService pluggableService;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private LicenseService licenseService;
    @Mock
    private UserService userService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private TransactionService transactionService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;

    @Before
    public void setUp() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.getInstance(Installer.class)).thenAnswer(invocation -> {
            return new Installer(dataModel, eventService);
        });
        when(licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.empty());
    }

    @After
    public void tearDown() {

    }
    @Test(expected = NoServiceFoundThatCanLoadTheJavaClass.class)
    public void createProtocolWithoutDeviceProtocolService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();

        // Business method
        service.createProtocol(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts: see expected exception rule
    }

    @Test
    public void createProtocolDelegatesToProtocolService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
        service.addDeviceProtocolService(deviceProtocolService);

        // Business method
        service.createProtocol(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts
        verify(deviceProtocolService).createProtocol(PROTOCOL_JAVA_CLASS_NAME);
    }

    @Test
    public void createProtocolDelegatesToAllServices() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolService deviceProtocolService1 = mock(DeviceProtocolService.class);
        when(deviceProtocolService1.createProtocol(anyString())).thenThrow(new ProtocolCreationException(mock(MessageSeed.class), "Whatever"));
        DeviceProtocolService deviceProtocolService2 = mock(DeviceProtocolService.class);
        service.addDeviceProtocolService(deviceProtocolService1);
        service.addDeviceProtocolService(deviceProtocolService2);

        // Business method
        service.createProtocol(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts
        verify(deviceProtocolService1).createProtocol(PROTOCOL_JAVA_CLASS_NAME);
        verify(deviceProtocolService2).createProtocol(PROTOCOL_JAVA_CLASS_NAME);
    }

    @Test
    public void createProtocolReturnsResultFromFirstService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocol expectedDeviceProtocol = mock(DeviceProtocol.class);
        DeviceProtocolService deviceProtocolService1 = mock(DeviceProtocolService.class);
        when(deviceProtocolService1.createProtocol(anyString())).thenReturn(expectedDeviceProtocol);
        DeviceProtocolService deviceProtocolService2 = mock(DeviceProtocolService.class);
        when(deviceProtocolService2.createProtocol(anyString())).thenReturn(mock(DeviceProtocol.class));
        service.addDeviceProtocolService(deviceProtocolService1);
        service.addDeviceProtocolService(deviceProtocolService2);

        // Business method
        Object protocol = service.createProtocol(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts
        verify(deviceProtocolService1).createProtocol(PROTOCOL_JAVA_CLASS_NAME);
        verify(deviceProtocolService2, never()).createProtocol(PROTOCOL_JAVA_CLASS_NAME);
        assertThat(protocol).isEqualTo(expectedDeviceProtocol);
    }

    @Test(expected = NoServiceFoundThatCanLoadTheJavaClass.class)
    public void createProtocolThatDoesNotExist() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
        when(deviceProtocolService.createProtocol(anyString())).thenThrow(new ProtocolCreationException(mock(MessageSeed.class), "Whatever"));
        service.addDeviceProtocolService(deviceProtocolService);

        // Business method
        service.createProtocol(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts: see expected exception rule
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void createInboundDeviceProtocolWithoutInboundDeviceProtocolService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();

        // Business method
        service.createInboundDeviceProtocolFor(mock(PluggableClass.class));

        // Asserts: see expected exception rule
    }

    @Test
    public void createInboundDeviceProtocolDelegatesToProtocolService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        PluggableClass pluggableClass = mock(PluggableClass.class);
        InboundDeviceProtocol expectedInboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        InboundDeviceProtocolService inboundDeviceProtocolService = mock(InboundDeviceProtocolService.class);
        when(inboundDeviceProtocolService.createInboundDeviceProtocolFor(pluggableClass)).thenReturn(expectedInboundDeviceProtocol);
        service.addInboundDeviceProtocolService(inboundDeviceProtocolService);

        // Business method
        InboundDeviceProtocol inboundDeviceProtocol = service.createInboundDeviceProtocolFor(pluggableClass);

        // Asserts
        verify(inboundDeviceProtocolService).createInboundDeviceProtocolFor(pluggableClass);
        assertThat(inboundDeviceProtocol).isEqualTo(expectedInboundDeviceProtocol);
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void createInboundDeviceProtocolDelegatesToAllProtocolServices() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        PluggableClass pluggableClass = mock(PluggableClass.class);
        InboundDeviceProtocolService inboundDeviceProtocolService1 = mock(InboundDeviceProtocolService.class);
        when(inboundDeviceProtocolService1.createInboundDeviceProtocolFor(pluggableClass)).thenReturn(null);
        InboundDeviceProtocolService inboundDeviceProtocolService2 = mock(InboundDeviceProtocolService.class);
        when(inboundDeviceProtocolService2.createInboundDeviceProtocolFor(pluggableClass)).thenReturn(null);
        service.addInboundDeviceProtocolService(inboundDeviceProtocolService1);
        service.addInboundDeviceProtocolService(inboundDeviceProtocolService2);

        // Business method
        try {
            service.createInboundDeviceProtocolFor(pluggableClass);
        }
        catch (DeviceProtocolAdapterCodingExceptions e) {
            // Asserts
            verify(inboundDeviceProtocolService1).createInboundDeviceProtocolFor(pluggableClass);
            verify(inboundDeviceProtocolService2).createInboundDeviceProtocolFor(pluggableClass);
            throw e;
        }
    }

    @Test
    public void createInboundDeviceProtocolReturnsResultOfFirstService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        PluggableClass pluggableClass = mock(PluggableClass.class);
        InboundDeviceProtocolService inboundDeviceProtocolService1 = mock(InboundDeviceProtocolService.class);
        InboundDeviceProtocol expectedInboundDeviceProtocol = mock(InboundDeviceProtocol.class);
        when(inboundDeviceProtocolService1.createInboundDeviceProtocolFor(pluggableClass)).thenReturn(expectedInboundDeviceProtocol);
        InboundDeviceProtocolService inboundDeviceProtocolService2 = mock(InboundDeviceProtocolService.class);
        when(inboundDeviceProtocolService2.createInboundDeviceProtocolFor(pluggableClass)).thenReturn(mock(InboundDeviceProtocol.class));
        service.addInboundDeviceProtocolService(inboundDeviceProtocolService1);
        service.addInboundDeviceProtocolService(inboundDeviceProtocolService2);

        // Business method
        InboundDeviceProtocol inboundDeviceProtocol = service.createInboundDeviceProtocolFor(pluggableClass);
        // Asserts
        verify(inboundDeviceProtocolService1).createInboundDeviceProtocolFor(pluggableClass);
        verify(inboundDeviceProtocolService2, never()).createInboundDeviceProtocolFor(pluggableClass);
        assertThat(inboundDeviceProtocol).isEqualTo(expectedInboundDeviceProtocol);
    }

    @Test(expected = DeviceProtocolAdapterCodingExceptions.class)
    public void createInboundDeviceProtocolThatDoesNotExist() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        InboundDeviceProtocolService inboundDeviceProtocolService = mock(InboundDeviceProtocolService.class);
        PluggableClass pluggableClass = mock(PluggableClass.class);
        when(inboundDeviceProtocolService.createInboundDeviceProtocolFor(pluggableClass)).thenThrow(new ProtocolCreationException(mock(MessageSeed.class), "Whatever"));
        service.addInboundDeviceProtocolService(inboundDeviceProtocolService);

        // Business method
        service.createInboundDeviceProtocolFor(pluggableClass);

        // Asserts: see expected exception rule
    }

    @Test(expected = NoServiceFoundThatCanLoadTheJavaClass.class)
    public void createDeviceProtocolMessagesForWithoutDeviceProtocolService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();

        // Business method
        service.createDeviceProtocolMessagesFor(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts: see expected exception rule
    }

    @Test
    public void createDeviceProtocolMessagesForDelegatesToProtocolService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolMessageService deviceProtocolMessageService = mock(DeviceProtocolMessageService.class);
        service.addDeviceProtocolMessageService(deviceProtocolMessageService);

        // Business method
        service.createDeviceProtocolMessagesFor(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts
        verify(deviceProtocolMessageService).createDeviceProtocolMessagesFor(PROTOCOL_JAVA_CLASS_NAME);
    }

    @Test
    public void createDeviceProtocolMessagesForDelegatesToAllProtocolServices() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolMessageService deviceProtocolMessageService1 = mock(DeviceProtocolMessageService.class);
        when(deviceProtocolMessageService1.createDeviceProtocolMessagesFor(anyString())).thenThrow(new ProtocolCreationException(mock(MessageSeed.class), "Whatever"));
        DeviceProtocolMessageService deviceProtocolMessageService2 = mock(DeviceProtocolMessageService.class);
        service.addDeviceProtocolMessageService(deviceProtocolMessageService1);
        service.addDeviceProtocolMessageService(deviceProtocolMessageService2);

        // Business method
        service.createDeviceProtocolMessagesFor(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts
        verify(deviceProtocolMessageService1).createDeviceProtocolMessagesFor(PROTOCOL_JAVA_CLASS_NAME);
        verify(deviceProtocolMessageService2).createDeviceProtocolMessagesFor(PROTOCOL_JAVA_CLASS_NAME);
    }

    @Test
    public void createDeviceProtocolMessagesForReturnsResultFromFirstService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolMessageService deviceProtocolMessageService1 = mock(DeviceProtocolMessageService.class);
        Object expectedResult = new Object();
        when(deviceProtocolMessageService1.createDeviceProtocolMessagesFor(anyString())).thenReturn(expectedResult);
        DeviceProtocolMessageService deviceProtocolMessageService2 = mock(DeviceProtocolMessageService.class);
        when(deviceProtocolMessageService2.createDeviceProtocolMessagesFor(anyString())).thenReturn(new Object());
        service.addDeviceProtocolMessageService(deviceProtocolMessageService1);
        service.addDeviceProtocolMessageService(deviceProtocolMessageService2);

        // Business method
        Object result = service.createDeviceProtocolMessagesFor(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts
        verify(deviceProtocolMessageService1).createDeviceProtocolMessagesFor(PROTOCOL_JAVA_CLASS_NAME);
        verify(deviceProtocolMessageService2, never()).createDeviceProtocolMessagesFor(PROTOCOL_JAVA_CLASS_NAME);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test(expected = NoServiceFoundThatCanLoadTheJavaClass.class)
    public void createDeviceProtocolMessagesThatDoesNotExist() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolMessageService deviceProtocolMessageService = mock(DeviceProtocolMessageService.class);
        when(deviceProtocolMessageService.createDeviceProtocolMessagesFor(anyString())).thenThrow(new ProtocolCreationException(mock(MessageSeed.class), "Whatever"));
        service.addDeviceProtocolMessageService(deviceProtocolMessageService);

        // Business method
        service.createDeviceProtocolMessagesFor(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts: see expected exception rule
    }

    @Test(expected = NoServiceFoundThatCanLoadTheJavaClass.class)
    public void createDeviceProtocolSecurityForWithoutDeviceProtocolService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();

        // Business method
        service.createDeviceProtocolSecurityFor(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts: see expected exception rule
    }

    @Test
    public void createDeviceProtocolSecurityForDelegatesToProtocolService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolSecurityService deviceProtocolSecurityService = mock(DeviceProtocolSecurityService.class);
        service.addDeviceProtocolSecurityService(deviceProtocolSecurityService);

        // Business method
        service.createDeviceProtocolSecurityFor(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts
        verify(deviceProtocolSecurityService).createDeviceProtocolSecurityFor(PROTOCOL_JAVA_CLASS_NAME);
    }

    @Test
    public void createDeviceProtocolSecurityForDelegatesToAllProtocolServices() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolSecurityService deviceProtocolSecurityService1 = mock(DeviceProtocolSecurityService.class);
        when(deviceProtocolSecurityService1.createDeviceProtocolSecurityFor(anyString()))
                .thenThrow(DeviceProtocolAdapterCodingExceptions.unsupportedMethod(mock(MessageSeed.class), ProtocolPluggableServiceImplTest.class, "createDeviceProtocolSecurityForDelegatesToAllProtocolServices"));
        DeviceProtocolSecurityService deviceProtocolSecurityService2 = mock(DeviceProtocolSecurityService.class);
        service.addDeviceProtocolSecurityService(deviceProtocolSecurityService1);
        service.addDeviceProtocolSecurityService(deviceProtocolSecurityService2);

        // Business method
        service.createDeviceProtocolSecurityFor(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts
        verify(deviceProtocolSecurityService1).createDeviceProtocolSecurityFor(PROTOCOL_JAVA_CLASS_NAME);
        verify(deviceProtocolSecurityService2).createDeviceProtocolSecurityFor(PROTOCOL_JAVA_CLASS_NAME);
    }

    @Test
    public void createDeviceProtocolSecurityReturnsResultOfFirstService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolSecurityService deviceProtocolSecurityService1 = mock(DeviceProtocolSecurityService.class);
        Object expectedResult = new Object();
        when(deviceProtocolSecurityService1.createDeviceProtocolSecurityFor(anyString())).thenReturn(expectedResult);
        DeviceProtocolSecurityService deviceProtocolSecurityService2 = mock(DeviceProtocolSecurityService.class);
        when(deviceProtocolSecurityService2.createDeviceProtocolSecurityFor(anyString())).thenReturn(new Object());
        service.addDeviceProtocolSecurityService(deviceProtocolSecurityService1);
        service.addDeviceProtocolSecurityService(deviceProtocolSecurityService2);

        // Business method
        Object result = service.createDeviceProtocolSecurityFor(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts
        verify(deviceProtocolSecurityService1).createDeviceProtocolSecurityFor(PROTOCOL_JAVA_CLASS_NAME);
        verify(deviceProtocolSecurityService2, never()).createDeviceProtocolSecurityFor(PROTOCOL_JAVA_CLASS_NAME);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test(expected = NoServiceFoundThatCanLoadTheJavaClass.class)
    public void createDeviceProtocolSecuritysThatDoesNotExist() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolSecurityService deviceProtocolSecurityService = mock(DeviceProtocolSecurityService.class);
        when(deviceProtocolSecurityService.createDeviceProtocolSecurityFor(anyString()))
                .thenThrow(DeviceProtocolAdapterCodingExceptions.unsupportedMethod(mock(MessageSeed.class), ProtocolPluggableServiceImplTest.class, "createDeviceProtocolSecuritysThatDoesNotExist"));
        service.addDeviceProtocolSecurityService(deviceProtocolSecurityService);

        // Business method
        service.createDeviceProtocolSecurityFor(PROTOCOL_JAVA_CLASS_NAME);

        // Asserts: see expected exception rule
    }

    @Test
    public void findLicensedProtocolWithoutService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);

        // Business method
        LicensedProtocol licensedProtocol = service.findLicensedProtocolFor(deviceProtocolPluggableClass);

        // Asserts
        assertThat(licensedProtocol).isNull();
    }

    @Test
    public void findLicensedProtocolDelegatesToService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        LicensedProtocolService licensedProtocolService = mock(LicensedProtocolService.class);
        when(licensedProtocolService.findLicensedProtocolFor(any(DeviceProtocolPluggableClass.class))).thenReturn(null);
        service.addLicensedProtocolService(licensedProtocolService);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);

        // Business method
        service.findLicensedProtocolFor(deviceProtocolPluggableClass);

        // Asserts
        verify(licensedProtocolService).findLicensedProtocolFor(deviceProtocolPluggableClass);
    }

    @Test
    public void findLicensedProtocolDelegatesToAllServices() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        LicensedProtocolService licensedProtocolService1 = mock(LicensedProtocolService.class);
        when(licensedProtocolService1.findLicensedProtocolFor(any(DeviceProtocolPluggableClass.class))).thenReturn(null);
        service.addLicensedProtocolService(licensedProtocolService1);
        LicensedProtocolService licensedProtocolService2 = mock(LicensedProtocolService.class);
        when(licensedProtocolService2.findLicensedProtocolFor(any(DeviceProtocolPluggableClass.class))).thenReturn(null);
        service.addLicensedProtocolService(licensedProtocolService2);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);

        // Business method
        service.findLicensedProtocolFor(deviceProtocolPluggableClass);

        // Asserts
        verify(licensedProtocolService1).findLicensedProtocolFor(deviceProtocolPluggableClass);
        verify(licensedProtocolService2).findLicensedProtocolFor(deviceProtocolPluggableClass);
    }

    @Test
    public void findLicensedProtocolReturnsResultOfFirstService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        LicensedProtocolService licensedProtocolService1 = mock(LicensedProtocolService.class);
        LicensedProtocol expectedLicensedProtocol = mock(LicensedProtocol.class);
        when(licensedProtocolService1.findLicensedProtocolFor(any(DeviceProtocolPluggableClass.class))).thenReturn(expectedLicensedProtocol);
        service.addLicensedProtocolService(licensedProtocolService1);
        LicensedProtocolService licensedProtocolService2 = mock(LicensedProtocolService.class);
        when(licensedProtocolService2.findLicensedProtocolFor(any(DeviceProtocolPluggableClass.class))).thenReturn(mock(LicensedProtocol.class));
        service.addLicensedProtocolService(licensedProtocolService2);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);

        // Business method
        LicensedProtocol licensedProtocol = service.findLicensedProtocolFor(deviceProtocolPluggableClass);

        // Asserts
        verify(licensedProtocolService1).findLicensedProtocolFor(deviceProtocolPluggableClass);
        verify(licensedProtocolService2, never()).findLicensedProtocolFor(deviceProtocolPluggableClass);
        assertThat(licensedProtocol).isEqualTo(expectedLicensedProtocol);
    }

    @Test
    public void findLicensedProtocol() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        LicensedProtocol expectedLicensedProtocol = mock(LicensedProtocol.class);
        LicensedProtocolService licensedProtocolService = mock(LicensedProtocolService.class);
        when(licensedProtocolService.findLicensedProtocolFor(any(DeviceProtocolPluggableClass.class))).thenReturn(expectedLicensedProtocol);
        service.addLicensedProtocolService(licensedProtocolService);

        // Business method
        LicensedProtocol licensedProtocol = service.findLicensedProtocolFor(deviceProtocolPluggableClass);

        // Asserts
        assertThat(licensedProtocol).isEqualTo(expectedLicensedProtocol);
    }

    @Test
    public void unMarshallDeviceProtocolCacheWithoutService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();

        // Business method
        Optional<Object> protocolCache = service.unMarshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        assertThat(protocolCache.isPresent()).isFalse();
    }

    @Test
    public void unMarshallDeviceProtocolCacheDelegatesToProtocolService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceCacheMarshallingService deviceCacheMarshallingService = mock(DeviceCacheMarshallingService.class);
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService);

        // Business method
        service.unMarshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        verify(deviceCacheMarshallingService).unMarshallCache(JSON_CACHE);
    }

    @Test
    public void unMarshallDeviceProtocolCacheDelegatesToAllServices() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceCacheMarshallingService deviceCacheMarshallingService1 = mock(DeviceCacheMarshallingService.class);
        when(deviceCacheMarshallingService1.unMarshallCache(JSON_CACHE)).thenThrow(new NotAppropriateDeviceCacheMarshallingTargetException());
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService1);
        DeviceCacheMarshallingService deviceCacheMarshallingService2 = mock(DeviceCacheMarshallingService.class);
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService2);

        // Business method
        service.unMarshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        verify(deviceCacheMarshallingService1).unMarshallCache(JSON_CACHE);
        verify(deviceCacheMarshallingService2).unMarshallCache(JSON_CACHE);
    }

    @Test
    public void unMarshallDeviceProtocolCacheReturnsResultOfFirstService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceCacheMarshallingService deviceCacheMarshallingService1 = mock(DeviceCacheMarshallingService.class);
        Optional<Object> expectedResult = Optional.of(new Object());
        when(deviceCacheMarshallingService1.unMarshallCache(JSON_CACHE)).thenReturn(expectedResult);
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService1);
        DeviceCacheMarshallingService deviceCacheMarshallingService2 = mock(DeviceCacheMarshallingService.class);
        when(deviceCacheMarshallingService2.unMarshallCache(JSON_CACHE)).thenReturn(Optional.empty());
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService2);

        // Business method
        Optional<Object> result = service.unMarshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        verify(deviceCacheMarshallingService1).unMarshallCache(JSON_CACHE);
        verify(deviceCacheMarshallingService2, never()).unMarshallCache(JSON_CACHE);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void unMarshallDeviceProtocolCacheWithIntendedMarshallingError() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceCacheMarshallingService deviceCacheMarshallingService = mock(DeviceCacheMarshallingService.class);
        when(deviceCacheMarshallingService.unMarshallCache(JSON_CACHE)).thenThrow(new NotAppropriateDeviceCacheMarshallingTargetException());
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService);

        // Business method
        Optional<Object> protocolCache = service.unMarshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        assertThat(protocolCache.isPresent()).isFalse();
    }

    @Test
    public void unMarshallDeviceProtocolCacheWithHighLevelMarshallingError() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceCacheMarshallingService deviceCacheMarshallingService = mock(DeviceCacheMarshallingService.class);
        when(deviceCacheMarshallingService.unMarshallCache(JSON_CACHE)).thenThrow(new DeviceCacheMarshallingException("Whatever", new NullPointerException()));
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService);

        // Business method
        Optional<Object> protocolCache = service.unMarshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        assertThat(protocolCache.isPresent()).isFalse();
    }

    @Test
    public void marshallDeviceProtocolCacheWithoutService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();

        // Business method
        String marshalled = service.marshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        assertThat(marshalled).isEmpty();
    }

    @Test
    public void marshallDeviceProtocolCacheDelegatesToProtocolService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceCacheMarshallingService deviceCacheMarshallingService = mock(DeviceCacheMarshallingService.class);
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService);

        // Business method
        service.marshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        verify(deviceCacheMarshallingService).marshall(JSON_CACHE);
    }

    @Test
    public void marshallDeviceProtocolCacheDelegatesToAllServices() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceCacheMarshallingService deviceCacheMarshallingService1 = mock(DeviceCacheMarshallingService.class);
        when(deviceCacheMarshallingService1.marshall(JSON_CACHE)).thenThrow(new NotAppropriateDeviceCacheMarshallingTargetException());
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService1);
        DeviceCacheMarshallingService deviceCacheMarshallingService2 = mock(DeviceCacheMarshallingService.class);
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService2);

        // Business method
        service.marshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        verify(deviceCacheMarshallingService1).marshall(JSON_CACHE);
        verify(deviceCacheMarshallingService2).marshall(JSON_CACHE);
    }

    @Test
    public void marshallDeviceProtocolCacheReturnsResultOfFirstService() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceCacheMarshallingService deviceCacheMarshallingService1 = mock(DeviceCacheMarshallingService.class);
        when(deviceCacheMarshallingService1.marshall(JSON_CACHE)).thenReturn("Result from first service");
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService1);
        DeviceCacheMarshallingService deviceCacheMarshallingService2 = mock(DeviceCacheMarshallingService.class);
        when(deviceCacheMarshallingService2.marshall(JSON_CACHE)).thenReturn("Result from second service");
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService2);

        // Business method
        String result = service.marshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        verify(deviceCacheMarshallingService1).marshall(JSON_CACHE);
        verify(deviceCacheMarshallingService2, never()).marshall(JSON_CACHE);
        assertThat(result).isEqualTo("Result from first service");
    }

    @Test
    public void marshallDeviceProtocolCacheWithIntendedMarshallingError() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceCacheMarshallingService deviceCacheMarshallingService = mock(DeviceCacheMarshallingService.class);
        when(deviceCacheMarshallingService.marshall(JSON_CACHE)).thenThrow(new NotAppropriateDeviceCacheMarshallingTargetException());
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService);

        // Business method
        String marshalled = service.marshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        assertThat(marshalled).isEmpty();
    }

    @Test
    public void marshallDeviceProtocolCacheWithHighLevelMarshallingError() {
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        DeviceCacheMarshallingService deviceCacheMarshallingService = mock(DeviceCacheMarshallingService.class);
        when(deviceCacheMarshallingService.marshall(JSON_CACHE)).thenThrow(new DeviceCacheMarshallingException("Whatever", new NullPointerException()));
        service.addDeviceCacheMarshallingService(deviceCacheMarshallingService);

        // Business method
        String marshalled = service.marshallDeviceProtocolCache(JSON_CACHE);

        // Asserts
        assertThat(marshalled).isEmpty();
    }

    @Test
    public void registeredListenerReceivesPreviouslyAddedServices() {
        DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
        InboundDeviceProtocolService inboundDeviceProtocolService = mock(InboundDeviceProtocolService.class);
        ConnectionTypeService connectionTypeService = mock(ConnectionTypeService.class);
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        service.addDeviceProtocolService(deviceProtocolService);
        service.addInboundDeviceProtocolService(inboundDeviceProtocolService);
        service.addConnectionTypeService(connectionTypeService);
        ProtocolDeploymentListener listener = mock(ProtocolDeploymentListener.class);

        // Business method
        service.register(listener);

        // Asserts
        verify(listener).deviceProtocolServiceDeployed(deviceProtocolService);
        verify(listener).inboundDeviceProtocolServiceDeployed(inboundDeviceProtocolService);
        verify(listener).connectionTypeServiceDeployed(connectionTypeService);
    }

    @Test
    public void registeredListenerReceivesAddedDeviceProtocolService() {
        DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        ProtocolDeploymentListener listener = mock(ProtocolDeploymentListener.class);
        service.register(listener);

        // Business method
        service.addDeviceProtocolService(deviceProtocolService);

        // Asserts
        verify(listener).deviceProtocolServiceDeployed(deviceProtocolService);
    }

    @Test
    public void unregisteredListenerNoLongerReceivesAddedDeviceProtocolService() {
        DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        ProtocolDeploymentListener listener = mock(ProtocolDeploymentListener.class);
        ProtocolDeploymentListenerRegistration registration = service.register(listener);
        registration.unregister();

        // Business method
        service.addDeviceProtocolService(deviceProtocolService);

        // Asserts
        verify(listener, never()).deviceProtocolServiceDeployed(deviceProtocolService);
    }

    @Test
    public void registeredListenerReceivesAddedInboundDeviceProtocolService() {
        InboundDeviceProtocolService inboundDeviceProtocolService = mock(InboundDeviceProtocolService.class);
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        ProtocolDeploymentListener listener = mock(ProtocolDeploymentListener.class);
        service.register(listener);

        // Business method
        service.addInboundDeviceProtocolService(inboundDeviceProtocolService);

        // Asserts
        verify(listener).inboundDeviceProtocolServiceDeployed(inboundDeviceProtocolService);
    }

    @Test
    public void unregisteredListenerNoLongerReceivesAddedInboundDeviceProtocolService() {
        InboundDeviceProtocolService inboundDeviceProtocolService = mock(InboundDeviceProtocolService.class);
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        ProtocolDeploymentListener listener = mock(ProtocolDeploymentListener.class);
        ProtocolDeploymentListenerRegistration registration = service.register(listener);
        registration.unregister();

        // Business method
        service.addInboundDeviceProtocolService(inboundDeviceProtocolService);

        // Asserts
        verify(listener, never()).inboundDeviceProtocolServiceDeployed(inboundDeviceProtocolService);
    }

    @Test
    public void registeredListenerReceivesAddedConnectionTypeService() {
        ConnectionTypeService connectionTypeService = mock(ConnectionTypeService.class);
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        ProtocolDeploymentListener listener = mock(ProtocolDeploymentListener.class);
        service.register(listener);

        // Business method
        service.addConnectionTypeService(connectionTypeService);

        // Asserts
        verify(listener).connectionTypeServiceDeployed(connectionTypeService);
    }

    @Test
    public void unregisteredListenerNoLongerReceivesAddedConnectionTypeService() {
        ConnectionTypeService connectionTypeService = mock(ConnectionTypeService.class);
        ProtocolPluggableServiceImpl service = this.newTestInstance();
        ProtocolDeploymentListener listener = mock(ProtocolDeploymentListener.class);
        ProtocolDeploymentListenerRegistration registration = service.register(listener);
        registration.unregister();

        // Business method
        service.addConnectionTypeService(connectionTypeService);

        // Asserts
        verify(listener, never()).connectionTypeServiceDeployed(connectionTypeService);
    }

    private ProtocolPluggableServiceImpl newTestInstance() {
        return new ProtocolPluggableServiceImpl(this.ormService, this.threadPrincipalService, this.eventService, this.nlsService, this.issueService, this.userService, this.meteringService, this.propertySpecService, this.pluggableService, this.customPropertySetService, this.licenseService, this.dataVaultService, this.transactionService, UpgradeModule.FakeUpgradeService.getInstance());
    }

}