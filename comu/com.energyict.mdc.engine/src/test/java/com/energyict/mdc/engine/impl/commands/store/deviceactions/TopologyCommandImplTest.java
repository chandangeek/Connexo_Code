/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.cbo.ObservationDateProperty;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.TopologyTask;
import com.energyict.mdc.engine.impl.commands.collect.TopologyCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolProperty;
import com.energyict.mdc.engine.impl.meterdata.DeviceTopology;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.TopologyAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the TopologyCommandImpl component
 *
 * @author gna
 * @since 31/05/12 - 11:53
 */
@RunWith(MockitoJUnitRunner.class)
public class TopologyCommandImplTest extends CommonCommandImplTests {

    @Mock
    private TopologyTask topologyTask;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private OfflineDevice offlineDevice;

    @Test
    public void doExecuteTest() {
        DeviceIdentifierById masterDevice = new DeviceIdentifierById(1L);
        DeviceIdentifierById slaveDevice1 = new DeviceIdentifierById(2L);
        DeviceIdentifierById slaveDevice2 = new DeviceIdentifierById(3L);

        Map<DeviceIdentifier, ObservationDateProperty> slaveDeviceIdentifiers = new HashMap<>();
        slaveDeviceIdentifiers.put(slaveDevice1, mock(ObservationDateProperty.class));
        slaveDeviceIdentifiers.put(slaveDevice2, mock(ObservationDateProperty.class));

        CollectedTopology collectedTopology = new DeviceTopology(masterDevice, slaveDeviceIdentifiers);
        CollectedDeviceInfo additionalDeviceInfoOfMaster = getAdditionalDeviceInfoOfMaster(masterDevice);
        collectedTopology.addAdditionalCollectedDeviceInfo(additionalDeviceInfoOfMaster);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceTopology()).thenReturn(collectedTopology);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        when(this.topologyTask.getTopologyAction()).thenReturn(TopologyAction.UPDATE);
        TopologyCommand topologyCommand = groupedDeviceCommand.getTopologyCommand(topologyTask, groupedDeviceCommand, comTaskExecution);

        // Business method
        topologyCommand.execute(deviceProtocol, newTestExecutionContext());
        String description = topologyCommand.toJournalMessageDescription(LogLevel.TRACE);

        // asserts
        assertThat(topologyCommand.getCollectedData()).isNotNull();
        assertThat(topologyCommand.getCollectedData()).hasSize(1);
        CollectedData collectedData = topologyCommand.getCollectedData().get(0);
        assertThat(collectedData).isInstanceOf(CollectedTopology.class);
        assertThat(description).isEqualTo("Executed topology protocol task" +
                " {topologyAction: UPDATE; updatedTopologyMaster: id 1; originalSlaves: None; receivedSlaves: id 2, id 3;" +
                " additionalDeviceInfo: DeviceProtocolProperty {deviceIdentifier: id 1; property: myProperty; value: myPropertyValue}}"
        );
    }

    @Test
    public void doExecuteWithoutAdditionalInfoTest() {
        DeviceIdentifierById masterDevice = new DeviceIdentifierById(1L);
        DeviceIdentifierById slaveDevice1 = new DeviceIdentifierById(2L);
        DeviceIdentifierById slaveDevice2 = new DeviceIdentifierById(3L);

        Map<DeviceIdentifier, ObservationDateProperty> slaveDeviceIdentifiers = new HashMap<>();
        slaveDeviceIdentifiers.put(slaveDevice1, mock(ObservationDateProperty.class));
        slaveDeviceIdentifiers.put(slaveDevice2, mock(ObservationDateProperty.class));

        CollectedTopology collectedTopology = new DeviceTopology(masterDevice, slaveDeviceIdentifiers);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceTopology()).thenReturn(collectedTopology);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        when(this.topologyTask.getTopologyAction()).thenReturn(TopologyAction.UPDATE);
        TopologyCommand topologyCommand = groupedDeviceCommand.getTopologyCommand(topologyTask, groupedDeviceCommand, comTaskExecution);

        // Business method
        topologyCommand.execute(deviceProtocol, newTestExecutionContext());
        String description = topologyCommand.toJournalMessageDescription(LogLevel.TRACE);

        // asserts
        assertThat(topologyCommand.getCollectedData()).isNotNull();
        assertThat(topologyCommand.getCollectedData()).hasSize(1);
        CollectedData collectedData = topologyCommand.getCollectedData().get(0);
        assertThat(collectedData).isInstanceOf(CollectedTopology.class);
        assertThat(description).isEqualTo("Executed topology protocol task {topologyAction: UPDATE; updatedTopologyMaster: id 1; originalSlaves: None; receivedSlaves: id 2, id 3}");
    }

    @Test
    public void testUpdateAccordingToUpdateAction() throws Exception {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        when(topologyTask.getTopologyAction()).thenReturn(TopologyAction.VERIFY);
        TopologyCommand topologyCommand = groupedDeviceCommand.getTopologyCommand(topologyTask, groupedDeviceCommand, comTaskExecution);

        assertThat(topologyCommand.getTopologyAction()).isEqualTo(TopologyAction.VERIFY);

        // Business method
        TopologyTask topologyTask_B = mock(TopologyTask.class);
        when(topologyTask_B.getTopologyAction()).thenReturn(TopologyAction.UPDATE);
        topologyCommand.updateAccordingTo(topologyTask_B, groupedDeviceCommand, comTaskExecution);

        // Asserts
        assertThat(topologyCommand.getTopologyAction()).isEqualTo(TopologyAction.UPDATE);
    }

    @Test
    public void testUpdateAccordingToVerifyAction() throws Exception {
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);
        when(topologyTask.getTopologyAction()).thenReturn(TopologyAction.UPDATE);
        TopologyCommand topologyCommand = groupedDeviceCommand.getTopologyCommand(topologyTask, groupedDeviceCommand, comTaskExecution);

        assertThat(topologyCommand.getTopologyAction()).isEqualTo(TopologyAction.UPDATE);

        // Business method
        TopologyTask topologyTask_B = mock(TopologyTask.class);
        when(topologyTask_B.getTopologyAction()).thenReturn(TopologyAction.VERIFY);
        topologyCommand.updateAccordingTo(topologyTask_B, groupedDeviceCommand, comTaskExecution);

        // Asserts
        assertThat(topologyCommand.getTopologyAction()).isEqualTo(TopologyAction.UPDATE);
    }

    private CollectedDeviceInfo getAdditionalDeviceInfoOfMaster(DeviceIdentifierById masterDevice) {
        String propertyValue = "myPropertyValue";
        return new DeviceProtocolProperty(masterDevice, "myProperty", propertyValue);
    }

    @Test
    public void testJournalMessageDescription() {
        CollectedTopology collectedTopology = mock(CollectedTopology.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceTopology()).thenReturn(collectedTopology);
        when(this.topologyTask.getTopologyAction()).thenReturn(TopologyAction.UPDATE);
        GroupedDeviceCommand groupedDeviceCommand = createGroupedDeviceCommand(offlineDevice, deviceProtocol);

        TopologyCommand topologyCommand = groupedDeviceCommand.getTopologyCommand(topologyTask, groupedDeviceCommand, comTaskExecution);

        // Business method
        String description = topologyCommand.toJournalMessageDescription(LogLevel.TRACE);

        // Asserts
        assertThat(description).isNotNull();
        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok; topologyAction: UPDATE}");
    }

}