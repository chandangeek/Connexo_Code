/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.inbound.InboundDAO;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.tasks.ComTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the methods of the {@link ComServerDAOImpl}
 * that relate to the {@link InboundDAO} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-17 (10:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComServerDAOImplInboundTest {

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
        TransactionService transactionService = new FakeTransactionService();
        when(this.serviceProvider.transactionService()).thenReturn(transactionService);
        when(this.serviceProvider.communicationTaskService()).thenReturn(this.communicationTaskService);
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        this.comServerDAO = new ComServerDAOImpl(this.serviceProvider, comServerUser);
    }

    /**
     * Tests the situation where the Device does not exist.
     */
    @Ignore // Enable when messages are being implemented
    @Test(expected = DataAccessException.class)
    public void testConfirmSentMessagesAndGetPendingForNonExistingDevice() {
        doThrow(CanNotFindForIdentifier.class).when(this.deviceIdentifier).findDevice();
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
    public void testGetDeviceProtocolSecurityPropertiesWhenDeviceDoesNotExist() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(CanNotFindForIdentifier.class).when(deviceIdentifier).findDevice();

        // Business method
        this.comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, mock(InboundComPort.class));

        // Asserts: expect a NotFoundException do be thrown
    }

    @Test
    public void testGetDeviceProtocolSecurityPropertiesWithoutConnectionTasks() {
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(new ArrayList<>(0));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(mock(InboundComPortPool.class));

        // Business method
        List<SecurityProperty> securityProperties = this.comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort);

        // Asserts
        assertThat(securityProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolSecurityPropertiesWithoutMatchingConnectionTask() {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        InboundComPortPool otherComPortPool = mock(InboundComPortPool.class);
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(otherComPortPool);

        // Business method
        List<SecurityProperty> securityProperties = this.comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort);

        // Asserts
        assertThat(securityProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolSecurityPropertiesWithoutComTasks() {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        Finder<ComTaskExecution> comTaskExecutionFinder = mockFinder(Collections.emptyList());
        when(communicationTaskService.findComTaskExecutionsByConnectionTask(connectionTask)).thenReturn(comTaskExecutionFinder);

        // Business method
        List<SecurityProperty> securityProperties = this.comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort);

        // Asserts
        assertThat(securityProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolSecurityProperties() {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        ComTask comTask = mock(ComTask.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement));
        SecurityProperty expectedSecurityProperty = mock(SecurityProperty.class);
        List<SecurityProperty> expectedSecurityProperties = Arrays.asList(expectedSecurityProperty);
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSecurityProperties(securityPropertySet)).thenReturn(expectedSecurityProperties);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        Finder<ComTaskExecution> comTaskExecutionFinder = mockFinder(Arrays.asList(comTaskExecution));
        when(this.communicationTaskService.findComTaskExecutionsByConnectionTask(connectionTask)).thenReturn(comTaskExecutionFinder);

        // Business method
        this.comServerDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort);

        // Asserts
        verify(device).getSecurityProperties(securityPropertySet);
    }

    @Test(expected = CanNotFindForIdentifier.class)
    public void testGetDeviceConnectionTypePropertiesWhenDeviceDoesNotExist() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(CanNotFindForIdentifier.class).when(deviceIdentifier).findDevice();

        // Business method
        this.comServerDAO.getDeviceConnectionTypeProperties(deviceIdentifier, mock(InboundComPort.class));

        // Asserts: expect a NotFoundException do be thrown
    }

    @Test
    public void testGetDeviceConnectionTypePropertiesWithoutConnectionTasks() {
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(new ArrayList<>(0));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
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
        when(device.getInboundConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
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
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        Device device = mock(Device.class);
        when(device.getInboundConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
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
        when(deviceIdentifier.findDevice()).thenReturn(null);

        // Business method
        TypedProperties protocolProperties = this.comServerDAO.getDeviceProtocolProperties(deviceIdentifier);

        // Asserts
        assertThat(protocolProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolPropertiesForDeviceIdentifierThatThrowsNotFoundExceptionWhenDeviceDoesNotExist() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(CanNotFindForIdentifier.class).when(deviceIdentifier).findDevice();

        // Business method
        TypedProperties protocolProperties = this.comServerDAO.getDeviceProtocolProperties(deviceIdentifier);

        // Asserts
        assertThat(protocolProperties).isNull();
    }

    @Test
    public void testGetDeviceProtocolProperties() {
        Device device = mock(Device.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);

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