package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.energyict.mdc.engine.exceptions.ComCommandException;
import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;

import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link InitializeLoggerCommand} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 15:57
 */
@RunWith(MockitoJUnitRunner.class)
public class InitializeLoggerCommandTest extends AbstractComCommandExecuteTest {

    @Mock
    private EventPublisherImpl eventPublisher;

    @Before
    public void setupEventPublisher () {
        super.setupEventPublisher();
        EventPublisherImpl.setInstance(this.eventPublisher);
        when(this.eventPublisher.serviceProvider()).thenReturn(this.comServerEventServiceProviderAdapter());
    }

    @After
    public void resetEventPublisher () {
        super.resetEventPublisher();
        EventPublisherImpl.setInstance(null);
    }

    @Test
    public void commandTypeTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), commandRootServiceProvider);
        InitializeLoggerCommand initializeLoggerCommand = new InitializeLoggerCommand(commandRoot);

        assertEquals(ComCommandTypes.INIT_LOGGER_COMMAND, initializeLoggerCommand.getCommandType());
    }

    @Test
    public void validateAdapterCallForMeterProtocol () {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext(logger);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, commandRootServiceProvider);
        CommandFactory.createLegacyInitLoggerCommand(commandRoot, null);
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);

        // business method
        commandRoot.execute(meterProtocolAdapter, executionContext);

        // validate the initializeLogger is called on the DeviceProtocolAdapter class
        verify(meterProtocolAdapter).initializeLogger(any(Logger.class));
    }

    @Test
    public void validateAdapterCallForSmartMeterProtocol () {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext(logger);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, commandRootServiceProvider);
        CommandFactory.createLegacyInitLoggerCommand(commandRoot, null);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);

        // business method
        commandRoot.execute(smartMeterProtocolAdapter, executionContext);

        // validate the initializeLogger is called on the DeviceProtocolAdapter class
        verify(smartMeterProtocolAdapter).initializeLogger(any(Logger.class));
    }

    @Test
    public void validateIllegalDeviceProtocolTest() {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext(logger);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, commandRootServiceProvider);
        CommandFactory.createLegacyInitLoggerCommand(commandRoot, null);
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

}