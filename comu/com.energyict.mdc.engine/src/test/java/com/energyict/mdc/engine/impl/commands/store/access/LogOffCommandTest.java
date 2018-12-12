/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
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

public class LogOffCommandTest extends AbstractComCommandExecuteTest {

    @Before
    public void setUp() {
        EventPublisherImpl eventPublisher = mock(EventPublisherImpl.class);
        when(executionContextServiceProvider.eventPublisher()).thenReturn(eventPublisher);
        when(executionContextServiceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(commandRootServiceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void commandTypeTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = createCommandRoot();
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, deviceProtocol, null);
        LogOffCommand logOffCommand = new LogOffCommand(groupedDeviceCommand);

        assertEquals(ComCommandTypes.LOGOFF, logOffCommand.getCommandType());
    }

    @Test
    public void validateAdapterCallForMeterProtocol() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, meterProtocolAdapter, null);
        CommandFactory.createLogOffCommand(groupedDeviceCommand, comTaskExecution);

        // business method
        groupedDeviceCommand.execute(executionContext);

        // validate that the connect on the adapter is called
        verify(meterProtocolAdapter).logOff();

    }

    @Test
    public void validateAdapterCallForSmartMeterProtocol() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);

        ExecutionContext executionContext = newTestExecutionContext();
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, smartMeterProtocolAdapter, null);
        CommandFactory.createLogOffCommand(groupedDeviceCommand, comTaskExecution);

        // business method
        groupedDeviceCommand.execute(executionContext);

        // validate that the connect on the adapter is called
        verify(smartMeterProtocolAdapter).logOff();
    }
}