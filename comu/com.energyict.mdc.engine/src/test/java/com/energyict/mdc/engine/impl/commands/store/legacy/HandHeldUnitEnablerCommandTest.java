package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.exceptions.ComCommandException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.ServerSerialPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.OpticalDriver;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;

import com.elster.jupiter.properties.PropertySpec;

import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link HandHeldUnitEnablerCommand} component
 * <p/>
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
    }

    @Test
    public void commandTypeTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, this.newTestExecutionContext(), commandRootServiceProvider);
        HandHeldUnitEnablerCommand handHeldUnitEnablerCommand = new HandHeldUnitEnablerCommand(commandRoot, this.comChannelPlaceHolder);

        assertEquals(ComCommandTypes.HAND_HELD_UNIT_ENABLER, handHeldUnitEnablerCommand.getCommandType());
    }

    @Test
    public void validateAdapterCallForMeterProtocolUsingOpticalConnection() throws ConnectionException {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext(logger);
        when(executionContext.getConnectionTask().getConnectionType()).thenReturn(mock(OpticalConnectionType.class));
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, commandRootServiceProvider);
        CommandFactory.createHandHeldUnitEnabler(commandRoot, null, this.comChannelPlaceHolder);
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);

        // business method
        commandRoot.execute(meterProtocolAdapter, executionContext);

        // verify that the method call has been made
        verify(meterProtocolAdapter).enableHHUSignOn(any(SerialCommunicationChannelAdapter.class));
    }

    @Test
    public void validateAdapterCallForMeterProtocolUsingNonOpticalConnection() throws ConnectionException {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext(logger);
        when(executionContext.getConnectionTask().getConnectionType()).thenReturn(mock(NoPropertiesConnectionType.class));
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, commandRootServiceProvider);
        CommandFactory.createHandHeldUnitEnabler(commandRoot, null, this.comChannelPlaceHolder);
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);

        // business method
        commandRoot.execute(meterProtocolAdapter, executionContext);

        // verify that the method call has been made
        verify(meterProtocolAdapter, never()).enableHHUSignOn(any(SerialCommunicationChannelAdapter.class));
    }

    @Test
    public void validateAdapterCallForSmartMeterProtocolUsingOpticalConnection() throws ConnectionException {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext(logger);
        when(executionContext.getConnectionTask().getConnectionType()).thenReturn(mock(OpticalConnectionType.class));
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, commandRootServiceProvider);
        CommandFactory.createHandHeldUnitEnabler(commandRoot, null, this.comChannelPlaceHolder);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);

        // business method
        commandRoot.execute(smartMeterProtocolAdapter, executionContext);

        // verify that the method call has been made
        verify(smartMeterProtocolAdapter).enableHHUSignOn(any(SerialCommunicationChannelAdapter.class));
    }

    @Test
    public void validateAdapterCallForSmartMeterProtocolUsingNonOpticalConnection() throws ConnectionException {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext(logger);
        when(executionContext.getConnectionTask().getConnectionType()).thenReturn(mock(NoPropertiesConnectionType.class));
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, commandRootServiceProvider);
        CommandFactory.createHandHeldUnitEnabler(commandRoot, null, this.comChannelPlaceHolder);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);

        // business method
        commandRoot.execute(smartMeterProtocolAdapter, executionContext);

        // verify that the method call has been made
        verify(smartMeterProtocolAdapter, never()).enableHHUSignOn(any(SerialCommunicationChannelAdapter.class));
    }

    @Test
    public void validateConnectionExceptionDuringHHUEnabledSignOn() throws ConnectionException {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext(logger);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, commandRootServiceProvider);
        CommandFactory.createHandHeldUnitEnabler(commandRoot, null, this.comChannelPlaceHolder);
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);
        Mockito.doThrow(ConnectionException.class).when(meterProtocolAdapter).enableHHUSignOn(any(SerialCommunicationChannelAdapter.class));

        try {
            // business method
            commandRoot.execute(meterProtocolAdapter, executionContext);
        } catch (CommunicationException e) {
            if (!(e.getCause() instanceof ConnectionException)) {
                throw e;
            }
            // It is OK, we needed that CommunicationException, wrapped around a ConnectionException
        }
    }

    @Test
    public void validateIllegalDeviceProtocolTest() {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext(logger);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, commandRootServiceProvider);
        CommandFactory.createHandHeldUnitEnabler(commandRoot, null, this.comChannelPlaceHolder);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);


        try {
            // business method
            commandRoot.execute(deviceProtocol, executionContext);
        } catch (ComCommandException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.ILLEGAL_COMMAND)) {
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
        public PropertySpec getPropertySpec(String name) {
            return null;
        }

        @Override
        public List<PropertySpec> getPropertySpecs () {
            return Collections.emptyList();
        }

        @Override
        public ComChannel connect (List<ConnectionProperty> properties) throws com.energyict.mdc.protocol.api.ConnectionException {
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
        public void copyProperties (TypedProperties properties) {
        }

    }

}