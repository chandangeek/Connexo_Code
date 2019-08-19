/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the methods of the {@link ComServerDAOImpl}
 * that relate to the {@link com.energyict.mdc.upl.InboundDAO} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-17 (10:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComServerDAOImplInboundTest {

    @Mock
    private DeviceService deviceService;
    @Mock
    private Device device;
    @Mock
    private DeviceIdentifier deviceIdentifier;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private ComServerDAOImpl.ServiceProvider serviceProvider;
    @Mock
    private User comServerUser;

    private ComServerDAO comServerDAO;

    @Before
    public void initializeMocksAndFactories() throws SQLException {
        TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
        when(this.serviceProvider.transactionService()).thenReturn(transactionService);
        when(this.serviceProvider.communicationTaskService()).thenReturn(this.communicationTaskService);
        when(this.serviceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.deviceService.findDeviceByIdentifier(this.deviceIdentifier)).thenReturn(Optional.of(this.device));
        this.comServerDAO = new ComServerDAOImpl(this.serviceProvider, comServerUser);
    }

    /**
     * Tests the situation where the Device does not exist.
     */
    @Ignore // Enable when messages are being implemented
    @Test(expected = DataAccessException.class)
    public void testConfirmSentMessagesAndGetPendingForNonExistingDevice() {
        when(this.deviceService.findDeviceByIdentifier(this.deviceIdentifier)).thenReturn(Optional.empty());
        comServerDAO.confirmSentMessagesAndGetPending(this.deviceIdentifier, 10);
    }

    /**
     * Tests the situation where the Device does not have any
     * sent or pending messages.
     */
    @Ignore // Enable when messages are being implemented
    @Test
    public void testConfirmSentMessagesAndGetPendingWithNoMessages() {
        // Business method
        List<OfflineDeviceMessage> pendingMessages = comServerDAO.confirmSentMessagesAndGetPending(this.deviceIdentifier, 10);

        // Asserts
        verify(this.device).getMessagesByState(DeviceMessageStatus.SENT);
        verify(this.device).getMessagesByState(DeviceMessageStatus.PENDING);
        assertThat(pendingMessages).isNotNull();
        assertThat(pendingMessages).isEmpty();
    }

    @Test(expected = CanNotFindForIdentifier.class)
    public void testGetDeviceProtocolSecurityPropertySetWhenDeviceDoesNotExist() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.empty());

        // Business method
        this.comServerDAO.getDeviceProtocolSecurityPropertySet(deviceIdentifier, mock(InboundComPort.class));

        // Asserts: expect a NotFoundException do be thrown
    }

    @Test
    public void testGetDeviceProtocolSecurityPropertySetWithoutConnectionTasks() {
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(new ArrayList<>(0));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.of(device));
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(mock(InboundComPortPool.class));

        // Business method
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = this.comServerDAO.getDeviceProtocolSecurityPropertySet(deviceIdentifier, comPort);

        // Asserts
        assertThat(deviceProtocolSecurityPropertySet).isNull();
    }

    @Test
    public void testGetDeviceProtocolSecurityPropertySetWithoutMatchingConnectionTask() {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        InboundComPortPool otherComPortPool = mock(InboundComPortPool.class);
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.of(device));
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(otherComPortPool);

        // Business method
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = this.comServerDAO.getDeviceProtocolSecurityPropertySet(deviceIdentifier, comPort);

        // Asserts
        assertThat(deviceProtocolSecurityPropertySet).isNull();
    }

    @Test
    public void testGetDeviceProtocolSecurityPropertiesWithoutComTasks() {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.of(device));
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        Finder<ComTaskExecution> comTaskExecutionFinder = mockFinder(Collections.emptyList());
        when(communicationTaskService.findComTaskExecutionsByConnectionTask(connectionTask)).thenReturn(comTaskExecutionFinder);

        // Business method
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = this.comServerDAO.getDeviceProtocolSecurityPropertySet(deviceIdentifier, comPort);

        // Asserts
        assertThat(deviceProtocolSecurityPropertySet).isNull();
    }

    @Test
    public void testGetDeviceProtocolSecurityPropertySet() {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);

        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(1);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(2);
        ConfigurationSecurityProperty expectedSecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(expectedSecurityProperty.getName()).thenReturn("Password");
        com.energyict.mdc.upl.TypedProperties expectedSecurityProperties = com.energyict.mdc.upl.TypedProperties.empty();
        expectedSecurityProperties.setProperty("Password", "MyPassword");

        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getClient()).thenReturn("client");
        when(securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        when(securityPropertySet.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(expectedSecurityProperty));

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSecurityProperties(securityPropertySet)).thenReturn(expectedSecurityProperties);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.of(device));
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        Finder<ComTaskExecution> comTaskExecutionFinder = mockFinder(Collections.singletonList(comTaskExecution));
        when(this.communicationTaskService.findComTaskExecutionsByConnectionTask(connectionTask)).thenReturn(comTaskExecutionFinder);

        // Business method
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = this.comServerDAO.getDeviceProtocolSecurityPropertySet(deviceIdentifier, comPort);

        // Asserts
        assertThat(deviceProtocolSecurityPropertySet.getClient()).isEqualTo("client");
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(1);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(2);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().size()).isEqualTo(1);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty("Password")).isEqualTo("MyPassword");
        verify(device).getSecurityProperties(securityPropertySet);
    }

    @Test(expected = CanNotFindForIdentifier.class)
    public void testGetDeviceConnectionTypePropertiesWhenDeviceDoesNotExist() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.empty());

        // Business method
        this.comServerDAO.getDeviceConnectionTypeProperties(deviceIdentifier, mock(InboundComPort.class));

        // Asserts: expect a NotFoundException do be thrown
    }

    @Test
    public void testGetDeviceConnectionTypePropertiesWithoutConnectionTasks() {
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(new ArrayList<>(0));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.of(device));
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(mock(InboundComPortPool.class));

        // Business method
        TypedProperties connectionTypeProperties = this.comServerDAO.getDeviceConnectionTypeProperties(deviceIdentifier, comPort);

        // Asserts
        assertThat(connectionTypeProperties).isNull();
    }

    @Test
    public void testGetDeviceConnectionTypePropertiesWithoutMatchingConnectionTask() {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        InboundComPortPool otherComPortPool = mock(InboundComPortPool.class);
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.of(device));
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(otherComPortPool);

        // Business method
        TypedProperties connectionTypeProperties = this.comServerDAO.getDeviceConnectionTypeProperties(deviceIdentifier, comPort);

        // Asserts
        assertThat(connectionTypeProperties).isNull();
    }

    @Test
    public void testGetDeviceConnectionTypeProperties() {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getTypedProperties()).thenReturn(com.energyict.mdc.upl.TypedProperties.empty());
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.of(device));
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);

        // Business method
        this.comServerDAO.getDeviceConnectionTypeProperties(deviceIdentifier, comPort);

        // Asserts
        verify(connectionTask).getTypedProperties();
    }

    @Test
    public void testGetDeviceProtocolPropertiesForDeviceIdentifierThatReturnsNullWhenDeviceDoesNotExist() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.empty());

        // Business method
        TypedProperties protocolProperties = this.comServerDAO.getDeviceProtocolProperties(deviceIdentifier);

        // Asserts
        assertThat(protocolProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolPropertiesForDeviceIdentifierThatThrowsNotFoundExceptionWhenDeviceDoesNotExist() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.empty());

        // Business method
        TypedProperties protocolProperties = this.comServerDAO.getDeviceProtocolProperties(deviceIdentifier);

        // Asserts
        assertThat(protocolProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolProperties() {
        Device device = mock(Device.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(this.deviceService.findDeviceByIdentifier(deviceIdentifier)).thenReturn(Optional.of(device));

        // Business method
        this.comServerDAO.getDeviceProtocolProperties(deviceIdentifier);

        // Asserts
        verify(device).getDeviceProtocolProperties();
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }

    /* Todo: copy remaining tests from mdc-all::com.energyict.comserver.core.impl.online.ComServerDAOImplInboundTest
     *       once all message related code has been moved to the new bundles too. */
}
