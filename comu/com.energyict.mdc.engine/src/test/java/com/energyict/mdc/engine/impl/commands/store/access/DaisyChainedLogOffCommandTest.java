package com.energyict.mdc.engine.impl.commands.store.access;

import java.time.Clock;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DaisyChainedLogOffCommand}
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 16:37
 */
public class DaisyChainedLogOffCommandTest extends AbstractComCommandExecuteTest {

    @Before
    public void setUp() {
        EventPublisherImpl eventPublisher = mock(EventPublisherImpl.class);
        serviceProvider.setClock(Clock.systemDefaultZone());
        serviceProvider.setEventPublisher(eventPublisher);
    }

    @After
    public void tearDown() {
        ServiceProvider.instance.set(null);
    }

    @Test
    public void testCommandType(){
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), (ServiceProvider) serviceProvider);
        DaisyChainedLogOffCommand daisyChainedLogOffCommand = new DaisyChainedLogOffCommand(commandRoot);

        assertEquals(ComCommandTypes.DAISY_CHAINED_LOGOFF, daisyChainedLogOffCommand.getCommandType());
    }

    @Test
    public void validateAdapterCallForMeterProtocol () {
        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, (ServiceProvider) serviceProvider);
        CommandFactory.createDaisyChainedLogOffCommand(commandRoot, null);
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);

        // business method
        commandRoot.execute(meterProtocolAdapter, executionContext);

        // validate that the connect on the adapter is called
        verify(meterProtocolAdapter).daisyChainedLogOff();

    }

    @Test
    public void validateAdapterCallForSmartMeterProtocol () {
        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, (ServiceProvider) serviceProvider);
        CommandFactory.createDaisyChainedLogOffCommand(commandRoot, null);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);

        // business method
        commandRoot.execute(smartMeterProtocolAdapter, executionContext);

        // validate that the connect on the adapter is called
        verify(smartMeterProtocolAdapter).daisyChainedLogOff();
    }

}