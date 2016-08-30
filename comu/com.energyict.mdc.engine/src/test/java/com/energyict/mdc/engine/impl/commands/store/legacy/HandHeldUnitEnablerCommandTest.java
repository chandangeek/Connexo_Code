package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.exceptions.ComCommandException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.ServerSerialPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.OpticalDriver;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link HandHeldUnitEnablerCommand} component
 * <p>
 * Copyrights EnergyICT
 * Date: 24/08/12
 * Time: 11:50
 */
@RunWith(MockitoJUnitRunner.class)
public class HandHeldUnitEnablerCommandTest extends AbstractComCommandExecuteTest {

    @Mock
    private ServerSerialPort serverSerialPort;
    @Mock
    private SerialComChannel serialComChannel;
    @Mock
    private ComPortRelatedComChannel comPortRelatedComChannel;
    @Mock
    private InputStream inputStream;

    private ComChannelPlaceHolder comChannelPlaceHolder;

    @Before
    public void initMocks() {
        when(serialComChannel.getSerialPort()).thenReturn(serverSerialPort);
        when(serverSerialPort.getInputStream()).thenReturn(inputStream);
        this.comChannelPlaceHolder = ComChannelPlaceHolder.forKnownComChannel(this.comPortRelatedComChannel);
        when(this.comPortRelatedComChannel.getActualComChannel()).thenReturn(serialComChannel);
        when(this.comPortRelatedComChannel.getSerialPort()).thenReturn(serverSerialPort);
    }

    @Test
    public void commandTypeTest() {
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        HandHeldUnitEnablerCommand handHeldUnitEnablerCommand = new HandHeldUnitEnablerCommand(groupedDeviceCommand, this.comChannelPlaceHolder);

        assertEquals(ComCommandTypes.HAND_HELD_UNIT_ENABLER, handHeldUnitEnablerCommand.getCommandType());
    }

    @Test
    public void validateAdapterCallForMeterProtocolUsingOpticalConnection() throws ConnectionException {
        Logger logger = Logger.getLogger("MyTestLogger");
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);
        ExecutionContext executionContext = newTestExecutionContext(logger);
        ConnectionTask<?, ?> connectionTask = executionContext.getConnectionTask();
        when(connectionTask.getConnectionType()).thenReturn(new OpticalConnectionType());

        executionContext.connecting = new StopWatch();
        executionContext.executing = new StopWatch(false);  // Do not auto start but start it manually as soon as execution starts.
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, meterProtocolAdapter, null);
        CommandFactory.createHandHeldUnitEnabler(groupedDeviceCommand, comTaskExecution, this.comChannelPlaceHolder);

        // business method
        groupedDeviceCommand.execute(executionContext);
        ComCommand hhuCommand = groupedDeviceCommand.getComCommand(ComCommandTypes.HAND_HELD_UNIT_ENABLER);
        String journalMessage = hhuCommand.toJournalMessageDescription(LogLevel.DEBUG);

        // verify that the method call has been made
        verify(meterProtocolAdapter).enableHHUSignOn(any(SerialCommunicationChannelAdapter.class));
        assertEquals("Hand-held unit sign-on {Enabling sign-on}", journalMessage);
    }

    @Test
    public void validateAdapterCallForMeterProtocolUsingNonOpticalConnection() throws ConnectionException {
        Logger logger = Logger.getLogger("MyTestLogger");
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getConnectionType()).thenReturn(mock(NoPropertiesConnectionType.class));
        ExecutionContext executionContext = newTestExecutionContext(logger);

        executionContext.connecting = new StopWatch();
        executionContext.executing = new StopWatch(false);  // Do not auto start but start it manually as soon as execution starts.
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, meterProtocolAdapter, null);
        CommandFactory.createHandHeldUnitEnabler(groupedDeviceCommand, comTaskExecution, this.comChannelPlaceHolder);

        // business method
        groupedDeviceCommand.execute(executionContext);
        ComCommand hhuCommand = groupedDeviceCommand.getComCommand(ComCommandTypes.HAND_HELD_UNIT_ENABLER);
        String journalMessage = hhuCommand.toJournalMessageDescription(LogLevel.DEBUG);

        // verify that the method call has been made
        verify(meterProtocolAdapter, never()).enableHHUSignOn(any(SerialCommunicationChannelAdapter.class));
        assertEquals("Hand-held unit sign-on {Not needed}", journalMessage);
    }

    @Test
    public void validateAdapterCallForSmartMeterProtocolUsingOpticalConnection() throws ConnectionException {
        Logger logger = Logger.getLogger("MyTestLogger");
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);
        ExecutionContext executionContext = newTestExecutionContext(logger);
        ConnectionTask<?, ?> connectionTask = executionContext.getConnectionTask();
        when(connectionTask.getConnectionType()).thenReturn(new OpticalConnectionType());

        executionContext.connecting = new StopWatch();
        executionContext.executing = new StopWatch(false);  // Do not auto start but start it manually as soon as execution starts.
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, smartMeterProtocolAdapter, null);
        CommandFactory.createHandHeldUnitEnabler(groupedDeviceCommand, comTaskExecution, this.comChannelPlaceHolder);

        // business method
        groupedDeviceCommand.execute(executionContext);

        // verify that the method call has been made
        verify(smartMeterProtocolAdapter).enableHHUSignOn(any(SerialCommunicationChannelAdapter.class));
    }

    @Test
    public void validateAdapterCallForSmartMeterProtocolUsingNonOpticalConnection() throws ConnectionException {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);
        ExecutionContext executionContext = newTestExecutionContext(logger);
        when(executionContext.getConnectionTask().getConnectionType()).thenReturn(mock(NoPropertiesConnectionType.class));
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, smartMeterProtocolAdapter, null);
        CommandFactory.createHandHeldUnitEnabler(groupedDeviceCommand, comTaskExecution, this.comChannelPlaceHolder);

        // business method
        groupedDeviceCommand.execute(executionContext);

        // verify that the method call has been made
        verify(smartMeterProtocolAdapter, never()).enableHHUSignOn(any(SerialCommunicationChannelAdapter.class));
    }

    @Test
    public void validateConnectionExceptionDuringHHUEnabledSignOn() throws ConnectionException {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);
        ExecutionContext executionContext = newTestExecutionContext(logger);
        ConnectionTask<?, ?> connectionTask = executionContext.getConnectionTask();
        when(connectionTask.getConnectionType()).thenReturn(new OpticalConnectionType());
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, meterProtocolAdapter, null);
        CommandFactory.createHandHeldUnitEnabler(groupedDeviceCommand, comTaskExecution, this.comChannelPlaceHolder);
        doThrow(ConnectionException.class).when(meterProtocolAdapter).enableHHUSignOn(any(SerialCommunicationChannelAdapter.class));

        groupedDeviceCommand.execute(executionContext);

        assertEquals(commandRoot.hasConnectionErrorOccurred(), true);
        assertEquals(groupedDeviceCommand.getComTaskRoot(comTaskExecution).getIssues().size(), 1);
    }

    @Test(expected = ComCommandException.class)
    public void validateIllegalDeviceProtocolTest() {
        Logger logger = Logger.getLogger("MyTestLogger");
        ExecutionContext executionContext = newTestExecutionContext(logger);
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        CommandFactory.createHandHeldUnitEnabler(groupedDeviceCommand, comTaskExecution, this.comChannelPlaceHolder);

        try {
            // business method
            groupedDeviceCommand.execute(executionContext);
        } catch (ComCommandException e) {
            if (e.getMessageSeed().equals(com.energyict.mdc.engine.impl.MessageSeeds.ILLEGAL_COMMAND)) {
                throw e;
            }
        }
    }

    private class OpticalConnectionType extends NoPropertiesConnectionType implements OpticalDriver {
    }

    private class NoPropertiesConnectionType implements ConnectionType {

        @Override
        public boolean allowsSimultaneousConnections() {
            return false;
        }

        @Override
        public boolean supportsComWindow() {
            return false;
        }

        @Override
        public Set<ComPortType> getSupportedComPortTypes() {
            return EnumSet.allOf(ComPortType.class);
        }

        @Override
        public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
            return Optional.empty();
        }

        @Override
        public ComChannel connect(List<ConnectionProperty> properties) throws com.energyict.mdc.protocol.api.ConnectionException {
            return null;
        }

        @Override
        public void disconnect(ComChannel comChannel) throws com.energyict.mdc.protocol.api.ConnectionException {
        }

        @Override
        public Direction getDirection() {
            return Direction.OUTBOUND;
        }

        @Override
        public String getVersion() {
            return "For Unit test purposes only";
        }

        @Override
        public void copyProperties(TypedProperties properties) {
        }

    }

}