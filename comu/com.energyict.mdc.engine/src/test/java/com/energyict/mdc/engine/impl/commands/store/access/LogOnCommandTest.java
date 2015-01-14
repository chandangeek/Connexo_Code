package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;

import java.time.Clock;

import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link LogOnCommand} component
 *
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 16:30
 */
public class LogOnCommandTest extends AbstractComCommandExecuteTest {

    @Before
    public void setUp() {
        when(executionContextServiceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(commandRootServiceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void commandTypeTest(){
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, this.newTestExecutionContext(), this.commandRootServiceProvider);
        LogOnCommand logOnCommand = new LogOnCommand(commandRoot);

        assertEquals(ComCommandTypes.LOGON, logOnCommand.getCommandType());
    }

    @Test
    public void validateAdapterCallForMeterProtocol () {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, this.commandRootServiceProvider);
        CommandFactory.createLogOnCommand(commandRoot, null);
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);

        // business method
        commandRoot.execute(meterProtocolAdapter, executionContext);

        // validate that the connect on the adapter is called
        verify(meterProtocolAdapter).logOn();
    }

    @Test
    public void validateAdapterCallForSmartMeterProtocol () {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = this.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, this.commandRootServiceProvider);
        CommandFactory.createLogOnCommand(commandRoot, null);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);

        // business method
        commandRoot.execute(smartMeterProtocolAdapter, executionContext);

        // validate that the connect on the adapter is called
        verify(smartMeterProtocolAdapter).logOn();
    }

}