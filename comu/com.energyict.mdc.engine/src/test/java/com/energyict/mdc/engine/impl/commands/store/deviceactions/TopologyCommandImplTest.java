package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.TopologyCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.tasks.TopologyAction;
import com.energyict.mdc.tasks.TopologyTask;

import org.junit.*;
import org.junit.runner.*;
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

    @Test
    public void doExecuteTest() {
        CollectedTopology collectedTopology = mock(CollectedTopology.class);
        when(collectedTopology.getTopologyAction()).thenReturn(TopologyAction.UPDATE);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceTopology()).thenReturn(collectedTopology);
        CommandRoot commandRoot = createCommandRoot();
        when(this.topologyTask.getTopologyAction()).thenReturn(TopologyAction.UPDATE);
        TopologyCommand topologyCommand = commandRoot.findOrCreateTopologyCommand(topologyTask, commandRoot, comTaskExecution);

        // Business method
        topologyCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertThat(topologyCommand.getCollectedData()).isNotNull();
        assertThat(topologyCommand.getCollectedData()).hasSize(1);
        CollectedData collectedData = topologyCommand.getCollectedData().get(0);
        assertThat(collectedData).isInstanceOf(CollectedTopology.class);
        assertThat(topologyCommand.toJournalMessageDescription(LogLevel.ERROR)).contains("{topologyAction: UPDATE}");
    }

    @Test
    public void testJournalMessageDescription() {
        CollectedTopology collectedTopology = mock(CollectedTopology.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceTopology()).thenReturn(collectedTopology);
        CommandRoot commandRoot = createCommandRoot();
        when(this.topologyTask.getTopologyAction()).thenReturn(TopologyAction.UPDATE);
        TopologyCommand topologyCommand = commandRoot.findOrCreateTopologyCommand(topologyTask, commandRoot, comTaskExecution);

        // Business method
        String description = topologyCommand.toJournalMessageDescription(LogLevel.TRACE);

        // Asserts
        assertThat(description).isNotNull();
        assertThat(description).contains("{executionState: NOT_EXECUTED; completionCode: Ok; nrOfWarnings: 0; nrOfProblems: 0; topologyAction: UPDATE}");
    }

}