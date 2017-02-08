/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.TopologyCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolProperty;
import com.energyict.mdc.engine.impl.meterdata.DeviceTopology;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LastSeenDateInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.TopologyTask;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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


    private Map<DeviceIdentifier, LastSeenDateInfo> createSlaveIdentifiers(DeviceIdentifier... identifier) {
        LastSeenDateInfo lastSeenDateInfo = new LastSeenDateInfo("LastSeenDate", Instant.now());
        Map<DeviceIdentifier, LastSeenDateInfo> slaveDeviceIdentifiers = new HashMap<>();
        Stream.of(identifier).forEach(deviceIdentifier1 -> slaveDeviceIdentifiers.put(deviceIdentifier1, lastSeenDateInfo));
        return slaveDeviceIdentifiers;
    }

    @Test
    public void doExecuteTest() {
        DeviceIdentifierById masterDevice = new DeviceIdentifierById(1L, mock(DeviceService.class));
        DeviceIdentifierById slaveDevice1 = new DeviceIdentifierById(2L, mock(DeviceService.class));
        DeviceIdentifierById slaveDevice2 = new DeviceIdentifierById(3L, mock(DeviceService.class));
        Map<DeviceIdentifier, LastSeenDateInfo> slaveDeviceIdentifiers = createSlaveIdentifiers(slaveDevice1, slaveDevice2);

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
        assertThat(description).isEqualTo(
                ComCommandDescriptionTitle.TopologyCommandImpl.getDescription() +
                        " {topologyAction: UPDATE; updatedTopologyMaster: device having id 1; originalSlaves: None; receivedSlaves: device having id 2, device having id 3;" +
                        " additionalDeviceInfo: DeviceProtocolProperty {deviceIdentifier: device having id 1; property: myProperty; value: myPropertyValue}}"
        );
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
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(propertySpec.getName()).thenReturn("myProperty");
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        String propertyValue = "myPropertyValue";
        when(valueFactory.toStringValue(propertyValue)).thenReturn(propertyValue);
        return new DeviceProtocolProperty(masterDevice, propertySpec, propertyValue);
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