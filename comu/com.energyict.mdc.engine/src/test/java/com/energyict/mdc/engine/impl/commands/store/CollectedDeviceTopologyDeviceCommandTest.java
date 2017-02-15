/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.EventType;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.DeviceTopologyChangedEvent;
import com.energyict.mdc.engine.impl.events.UnknownSlaveDeviceEvent;
import com.energyict.mdc.engine.impl.meterdata.DeviceTopology;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LastSeenDateInfo;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 16/11/12 (13:30)
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedDeviceTopologyDeviceCommandTest {
    private static final String SLAVE_1_SERIAL_NUMBER = "Slave_1";
    private static final String SLAVE_2_SERIAL_NUMBER = "Slave_2";

    @Mock
    private DeviceIdentifier slave1Identifier;
    @Mock
    private DeviceIdentifier slave2Identifier;
    @Mock
    public IssueService issueService;
    @Mock
    private BaseDevice device;
    @Mock
    private DeviceIdentifier deviceIdentifier;
    @Mock
    private OfflineDevice offlineDevice;
    @Mock
    private OfflineDevice offlineSlave_1;
    @Mock
    private OfflineDevice offlineSlave_2;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;

    @Before
    public void setUp() {
        when(slave1Identifier.getIdentifier()).thenReturn(SLAVE_1_SERIAL_NUMBER);
        when(slave2Identifier.getIdentifier()).thenReturn(SLAVE_2_SERIAL_NUMBER);
        when(serviceProvider.issueService()).thenReturn(issueService);
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.empty());
        when(comServerDAO.findOfflineDevice(eq(deviceIdentifier), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        when(comServerDAO.findOfflineDevice(deviceIdentifier)).thenReturn(Optional.of(offlineDevice));
        when(deviceIdentifier.findDevice()).thenReturn(device);
        when(comServerDAO.findOfflineDevice(eq(slave1Identifier), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineSlave_1));
        when(comServerDAO.findOfflineDevice(eq(slave2Identifier), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineSlave_2));

        when(offlineSlave_1.getSerialNumber()).thenReturn(SLAVE_1_SERIAL_NUMBER);
        when(offlineSlave_2.getSerialNumber()).thenReturn(SLAVE_2_SERIAL_NUMBER);
    }

    /**
     * Test setup 1
     * EIServer topology:
     * Master
     * - No slave device
     * <p>
     * Actual topology:
     * Master
     * - Slave_1 (id 1)
     * - Slav_2 (id 2)
     * TopologyAction = UPDATE
     * <p>
     * Slave_1 is already known in EIServer
     * Slave_2 is not yet present in EIServer
     *
     * @throws Exception
     */
    @Test
    public void testAddedSlave_UpdateAction() throws Exception {
        when(comServerDAO.findOfflineDevice(eq(slave1Identifier), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineSlave_1));  // Already known in EIServer
        when(comServerDAO.findOfflineDevice(eq(slave2Identifier), any(OfflineDeviceContext.class))).thenReturn(Optional.<OfflineDevice>empty());            // Not yet present in EIServer

        when(offlineDevice.getAllSlaveDevices()).thenReturn(new ArrayList<>());

        Map<DeviceIdentifier, LastSeenDateInfo> slaveDeviceIdentifiers = createSlaveIdentifiers(slave1Identifier, slave2Identifier);

        DeviceTopology deviceTopology = new DeviceTopology(deviceIdentifier, slaveDeviceIdentifiers);
        deviceTopology.setTopologyAction(TopologyAction.UPDATE);
        CollectedDeviceTopologyDeviceCommand command = new CollectedDeviceTopologyDeviceCommand(deviceTopology, comTaskExecution, new MeterDataStoreCommandImpl(this.comTaskExecution, this.serviceProvider), this.serviceProvider);
        DeviceCommand.ExecutionLogger mockedExecutionLogger = mock(DeviceCommand.ExecutionLogger.class);
        command.logExecutionWith(mockedExecutionLogger);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO, times(1)).updateGateway(slave1Identifier, deviceIdentifier);
        verify(comServerDAO, times(1)).signalEvent(eq(EventType.UNKNOWN_SLAVE_DEVICE.topic()), any());
        verify(mockedExecutionLogger).addIssue(eq(CompletionCode.ConfigurationWarning), any(Issue.class), eq(comTaskExecution));
    }

    private Map<DeviceIdentifier, LastSeenDateInfo> createSlaveIdentifiers(DeviceIdentifier... identifier) {
        LastSeenDateInfo lastSeenDateInfo = new LastSeenDateInfo("LastSeenDate", Instant.now());
        Map<DeviceIdentifier, LastSeenDateInfo> slaveDeviceIdentifiers = new HashMap<>();
        Stream.of(identifier).forEach(deviceIdentifier1 -> slaveDeviceIdentifiers.put(deviceIdentifier1, lastSeenDateInfo));
        return slaveDeviceIdentifiers;
    }

    /**
     * Test setup 2
     * EIServer topology:
     * Master
     * - No slave device
     * <p>
     * Actual topology:
     * Master
     * - Slave_1 (id 1)
     * - Slave_2 (id 2)
     * TopologyAction = VERIFY
     * <p>
     * Slave_1 is already known in EIServer
     * Slave_2 is not yet present in EIServer
     *
     * @throws Exception
     */
    @Test
    public void testAddedSlave_VerifyAction() throws Exception {
        when(comServerDAO.findOfflineDevice(eq(slave1Identifier), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineSlave_1));  // Already known in EIServer
        when(comServerDAO.findOfflineDevice(eq(slave2Identifier), any(OfflineDeviceContext.class))).thenReturn(Optional.<OfflineDevice>empty());            // Not yet present in EIServer

        when(offlineDevice.getAllSlaveDevices()).thenReturn(new ArrayList<>());

        Map<DeviceIdentifier, LastSeenDateInfo> slaveDeviceIdentifiers = createSlaveIdentifiers(slave1Identifier, slave2Identifier);

        DeviceTopology deviceTopology = new DeviceTopology(deviceIdentifier, slaveDeviceIdentifiers);
        deviceTopology.setTopologyAction(TopologyAction.VERIFY);
        CollectedDeviceTopologyDeviceCommand command = new CollectedDeviceTopologyDeviceCommand(deviceTopology, comTaskExecution, new MeterDataStoreCommandImpl(this.comTaskExecution, this.serviceProvider), this.serviceProvider);
        DeviceCommand.ExecutionLogger mockedExecutionLogger = mock(DeviceCommand.ExecutionLogger.class);
        command.logExecutionWith(mockedExecutionLogger);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO, never()).updateGateway(any(DeviceIdentifier.class), any(DeviceIdentifier.class));
        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        verify(comServerDAO, times(2)).signalEvent(anyString(), argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues().get(0)).isInstanceOf(UnknownSlaveDeviceEvent.class);
        assertThat(argumentCaptor.getAllValues().get(1)).isInstanceOf(DeviceTopologyChangedEvent.class);
    }

    /**
     * Test setup 2
     * EIServer topology:
     * Master
     * - No slave device
     * <p>
     * Actual topology:
     * Master
     * - Slave_1 (id 1)
     * - Slave_2 (id 2)
     * TopologyAction = VERIFY
     * <p>
     * Slave_1 is already known in EIServer
     * Slave_2 is not yet present in EIServer
     *
     * @throws Exception
     */
    @Test
    public void testAddedSlave_VerifyWithFinderExceptionAction() throws Exception {
        doThrow(CanNotFindForIdentifier.device(slave1Identifier, com.energyict.mdc.device.data.impl.MessageSeeds.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER)).when(comServerDAO)
                .findOfflineDevice(eq(slave1Identifier), any(OfflineDeviceContext.class));

        when(offlineDevice.getAllSlaveDevices()).thenReturn(new ArrayList<>());

        Map<DeviceIdentifier, LastSeenDateInfo> slaveDeviceIdentifiers = createSlaveIdentifiers(slave1Identifier, slave2Identifier);

        DeviceTopology deviceTopology = new DeviceTopology(deviceIdentifier, slaveDeviceIdentifiers);
        deviceTopology.setTopologyAction(TopologyAction.VERIFY);
        CollectedDeviceTopologyDeviceCommand command = new CollectedDeviceTopologyDeviceCommand(deviceTopology, comTaskExecution, new MeterDataStoreCommandImpl(this.comTaskExecution, this.serviceProvider), this.serviceProvider);
        DeviceCommand.ExecutionLogger mockedExecutionLogger = mock(DeviceCommand.ExecutionLogger.class);
        command.logExecutionWith(mockedExecutionLogger);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO, never()).updateGateway(any(DeviceIdentifier.class), any(DeviceIdentifier.class));
        verify(mockedExecutionLogger, atLeastOnce()).addIssue(eq(CompletionCode.ConfigurationWarning), any(Issue.class), eq(comTaskExecution));
    }

    /**
     * Test setup 3
     * EIServer topology:
     * Master
     * - Slave_1 (id 1)
     * - Slave_2 (id 2)
     * <p>
     * Actual topology:
     * Master
     * - Slave_2 (id 2)
     * TopologyAction = UPDATE
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testRemovedSlave_UpdateAction() throws Exception {
        List<OfflineDevice> slaveDevices = new ArrayList<>();
        slaveDevices.add(offlineSlave_1);
        slaveDevices.add(offlineSlave_2);
        when(offlineDevice.getAllSlaveDevices()).thenReturn(slaveDevices);

        Map<DeviceIdentifier, LastSeenDateInfo> slaveDeviceIdentifiers = createSlaveIdentifiers(slave2Identifier);

        DeviceTopology deviceTopology = new DeviceTopology(deviceIdentifier, slaveDeviceIdentifiers);
        deviceTopology.setTopologyAction(TopologyAction.UPDATE);
        CollectedDeviceTopologyDeviceCommand command = new CollectedDeviceTopologyDeviceCommand(deviceTopology, comTaskExecution, new MeterDataStoreCommandImpl(this.comTaskExecution, this.serviceProvider), this.serviceProvider);
        DeviceCommand.ExecutionLogger mockedExecutionLogger = mock(DeviceCommand.ExecutionLogger.class);
        command.logExecutionWith(mockedExecutionLogger);
        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO, times(1)).updateGateway(any(DeviceIdentifier.class), any(DeviceIdentifier.class));
        verify(mockedExecutionLogger).addIssue(eq(CompletionCode.ConfigurationWarning), any(Issue.class), eq(comTaskExecution));
    }

    /**
     * Test setup 4
     * EIServer topology:
     * Master
     * - Slave_1 (id 1)
     * - Slave_2 (id 2)
     * <p>
     * Actual topology:
     * Master
     * - Slave_2 (id 2)
     * TopologyAction = VERIFY
     * <p>
     *
     * @throws Exception
     */
    @Test
    public void testRemovedSlave_VerifyAction() throws Exception {
        List<OfflineDevice> slaveDevices = new ArrayList<>();
        slaveDevices.add(offlineSlave_1);
        slaveDevices.add(offlineSlave_2);
        when(offlineDevice.getAllSlaveDevices()).thenReturn(slaveDevices);

        Map<DeviceIdentifier, LastSeenDateInfo> slaveDeviceIdentifiers = createSlaveIdentifiers(slave2Identifier);

        DeviceTopology deviceTopology = new DeviceTopology(deviceIdentifier, slaveDeviceIdentifiers);
        deviceTopology.setTopologyAction(TopologyAction.VERIFY);
        CollectedDeviceTopologyDeviceCommand command = new CollectedDeviceTopologyDeviceCommand(deviceTopology, comTaskExecution, new MeterDataStoreCommandImpl(this.comTaskExecution, this.serviceProvider), this.serviceProvider);
        DeviceCommand.ExecutionLogger mockedExecutionLogger = mock(DeviceCommand.ExecutionLogger.class);
        command.logExecutionWith(mockedExecutionLogger);
        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO, never()).updateGateway(any(DeviceIdentifier.class), any(DeviceIdentifier.class));
        verify(mockedExecutionLogger).addIssue(eq(CompletionCode.ConfigurationWarning), any(Issue.class), eq(comTaskExecution));
    }

    @Test
    public void testToJournalMessageDescription() {
        List<OfflineDevice> slaveDevices = new ArrayList<>();
        slaveDevices.add(this.offlineSlave_1);
        slaveDevices.add(this.offlineSlave_2);
        when(this.offlineDevice.getAllSlaveDevices()).thenReturn(slaveDevices);

        Map<DeviceIdentifier, LastSeenDateInfo> slaveDeviceIdentifiers = createSlaveIdentifiers(slave2Identifier);

        DeviceTopology deviceTopology = new DeviceTopology(this.deviceIdentifier, slaveDeviceIdentifiers);
        TopologyAction topologyAction = TopologyAction.VERIFY;
        deviceTopology.setTopologyAction(topologyAction);
        CollectedDeviceTopologyDeviceCommand command = new CollectedDeviceTopologyDeviceCommand(deviceTopology, comTaskExecution, new MeterDataStoreCommandImpl(this.comTaskExecution, this.serviceProvider), this.serviceProvider);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("{topologyAction.verify; deviceIdentifier: deviceIdentifier}");
    }

}