package com.energyict.mdc.engine.impl.commands.store.access;

import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.aspects.ComServerEventServiceProviderAdapter;
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
 * Tests for the {@link LogOffCommand} component
 *
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 16:34
 */
public class LogOffCommandTest extends AbstractComCommandExecuteTest {

    private FakeServiceProvider serviceProvider = new FakeServiceProvider();

    @Before
    public void setUp() {
        EventPublisherImpl eventPublisher = mock(EventPublisherImpl.class);
        when(eventPublisher.serviceProvider()).thenReturn(new ComServerEventServiceProviderAdapter());
        EventPublisherImpl.setInstance(eventPublisher);
        ServiceProvider.instance.set(serviceProvider);
        serviceProvider.setClock(new ProgrammableClock());
    }

    @After
    public void tearDown() {
        EventPublisherImpl.setInstance(null);
        ServiceProvider.instance.set(null);
    }

    @Test
    public void commandTypeTest(){
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), serviceProvider);
        LogOffCommand logOffCommand = new LogOffCommand(commandRoot);

        assertEquals(ComCommandTypes.LOGOFF, logOffCommand.getCommandType());
    }

    @Test
    public void validateAdapterCallForMeterProtocol () {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, serviceProvider);
        CommandFactory.createLogOffCommand(commandRoot, null);
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);

        // business method
        commandRoot.execute(meterProtocolAdapter, executionContext);

        // validate that the connect on the adapter is called
        verify(meterProtocolAdapter).logOff();

    }

    @Test
    public void validateAdapterCallForSmartMeterProtocol () {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);

        ExecutionContext executionContext = AbstractComCommandExecuteTest.newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, executionContext, serviceProvider);
        CommandFactory.createLogOffCommand(commandRoot, null);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);

        // business method
        commandRoot.execute(smartMeterProtocolAdapter, executionContext);

        // validate that the connect on the adapter is called
        verify(smartMeterProtocolAdapter).logOff();
    }

}