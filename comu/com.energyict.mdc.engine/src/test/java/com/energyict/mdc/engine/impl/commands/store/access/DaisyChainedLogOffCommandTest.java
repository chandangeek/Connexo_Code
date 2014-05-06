package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolAdapter;
import com.energyict.comserver.commands.AbstractComCommandExecuteTest;
import com.energyict.comserver.commands.core.CommandRootImpl;
import com.energyict.comserver.core.CommandFactory;
import com.energyict.comserver.core.JobExecution;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import org.junit.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link DaisyChainedLogOffCommand}
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 16:37
 */
public class DaisyChainedLogOffCommandTest extends AbstractComCommandExecuteTest {

    @Test
    public void testCommandType(){
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), issueService);
        DaisyChainedLogOffCommand daisyChainedLogOffCommand = new DaisyChainedLogOffCommand(commandRoot);

        assertEquals(ComCommandTypes.DAISY_CHAINED_LOGOFF, daisyChainedLogOffCommand.getCommandType());
    }

    @Test
    public void validateAdapterCallForMeterProtocol () {
        JobExecution.ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, issueService);
        CommandFactory.createDaisyChainedLogOffCommand(commandRoot, null);
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);

        // business method
        commandRoot.execute(meterProtocolAdapter, executionContext);

        // validate that the connect on the adapter is called
        verify(meterProtocolAdapter).daisyChainedLogOff();

    }

    @Test
    public void validateAdapterCallForSmartMeterProtocol () {
        JobExecution.ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, issueService);
        CommandFactory.createDaisyChainedLogOffCommand(commandRoot, null);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);

        // business method
        commandRoot.execute(smartMeterProtocolAdapter, executionContext);

        // validate that the connect on the adapter is called
        verify(smartMeterProtocolAdapter).daisyChainedLogOff();
    }

}