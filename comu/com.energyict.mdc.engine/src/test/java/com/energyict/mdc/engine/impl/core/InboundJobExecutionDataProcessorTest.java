package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.CreateComTaskExecutionSessionCommand;
import com.energyict.mdc.engine.impl.commands.collect.CreateComTaskExecutionSessionCommandType;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.ComPortDiscoveryLogger;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.tasks.ComTask;
import org.fest.assertions.core.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 04.08.15
 * Time: 08:47
 */
@RunWith(MockitoJUnitRunner.class)
public class InboundJobExecutionDataProcessorTest {

    @Mock
    private ComPort comPort;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private InboundDiscoveryContextImpl inboundDiscoveryContext;
    @Mock
    private InboundDeviceProtocol inboundDeviceProtocol;
    @Mock
    private OfflineDevice offlineDevice;
    @Mock
    private JobExecution.ServiceProvider serviceProvider;
    @Mock
    private InboundCommunicationHandler inboundCommunicationHandler;
    @Mock(extraInterfaces = ServerCollectedData.class)
    private CollectedData collectedData;
    @Mock
    private ComTask comTask;
    @Mock
    private ComSessionBuilder comSessionBuilder;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private ComPortDiscoveryLogger testComPortDiscoveryLogger;

    @Before
    public void setup() {
        when(inboundDiscoveryContext.getComSessionBuilder()).thenReturn(comSessionBuilder);
        when(inboundDeviceProtocol.getCollectedData(offlineDevice)).thenReturn(Collections.singletonList(collectedData));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(offlineDevice.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
    }

    @Test
    public void comTaskExecutionSessionCreatedTest() {
        InboundJobExecutionDataProcessor inboundJobExecutionDataProcessor = getTestInstance();
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));
        when(collectedData.isConfiguredIn(comTaskExecution)).thenReturn(true);
        List<? extends ComTaskExecution> comTaskExecutions = Collections.singletonList(comTaskExecution);
        List<JobExecution.PreparedComTaskExecution> preparedComTaskExecutions = inboundJobExecutionDataProcessor.prepareAll(comTaskExecutions);

        assertThat(preparedComTaskExecutions).hasSize(1);
        assertThat(preparedComTaskExecutions.get(0).getCommandRoot().getCommands()).hasSize(1);
        assertThat(preparedComTaskExecutions.get(0).getCommandRoot().getCommands()).has(new Condition<Map<ComCommandType, ComCommand>>() {
            @Override
            public boolean matches(Map<ComCommandType, ComCommand> comCommandTypeComCommandMap) {
                return comCommandTypeComCommandMap.entrySet().stream()
                        .filter(comCommandTypeComCommandEntry -> comCommandTypeComCommandEntry.getKey() instanceof CreateComTaskExecutionSessionCommandType)
                        .filter(comCommandTypeComCommandEntry -> comCommandTypeComCommandEntry.getValue() instanceof CreateComTaskExecutionSessionCommand)
                        .findFirst().isPresent();
            }
        });
    }

    private InboundJobExecutionDataProcessor getTestInstance() {
        return new InboundJobExecutionDataProcessor(comPort, comServerDAO, deviceCommandExecutor, inboundDiscoveryContext, inboundDeviceProtocol, offlineDevice, serviceProvider, inboundCommunicationHandler, testComPortDiscoveryLogger);
    }

}