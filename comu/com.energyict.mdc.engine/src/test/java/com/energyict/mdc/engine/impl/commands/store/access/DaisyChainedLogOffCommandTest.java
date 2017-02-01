/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;

import java.time.Clock;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DaisyChainedLogOffCommandTest extends AbstractComCommandExecuteTest {

    @Before
    public void setUp() {
        EventPublisherImpl eventPublisher = mock(EventPublisherImpl.class);
        when(executionContextServiceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(executionContextServiceProvider.eventPublisher()).thenReturn(eventPublisher);
        when(commandRootServiceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCommandType() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        DaisyChainedLogOffCommand daisyChainedLogOffCommand = new DaisyChainedLogOffCommand(groupedDeviceCommand);

        assertEquals(ComCommandTypes.DAISY_CHAINED_LOGOFF, daisyChainedLogOffCommand.getCommandType());
    }

    @Test
    public void validateAdapterCallForMeterProtocol() {
        ExecutionContext executionContext = this.newTestExecutionContext();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = createCommandRoot();
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, meterProtocolAdapter, null);
        CommandFactory.createDaisyChainedLogOffCommand(groupedDeviceCommand, comTaskExecution);

        // business method
        groupedDeviceCommand.execute(executionContext);

        // validate that the connect on the adapter is called
        verify(meterProtocolAdapter).daisyChainedLogOff();

    }

    @Test
    public void validateAdapterCallForSmartMeterProtocol() {
        ExecutionContext executionContext = this.newTestExecutionContext();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = createCommandRoot();
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, smartMeterProtocolAdapter, null);
        CommandFactory.createDaisyChainedLogOffCommand(groupedDeviceCommand, comTaskExecution);

        // business method
        groupedDeviceCommand.execute(executionContext);

        // validate that the connect on the adapter is called
        verify(smartMeterProtocolAdapter).daisyChainedLogOff();
    }

}