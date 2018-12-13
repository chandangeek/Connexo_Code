/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.engine.exceptions.ComCommandException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.CommandFactory;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;

import java.util.logging.Logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InitializeLoggerCommandTest extends AbstractComCommandExecuteTest {

    @Test
    public void commandTypeTest() {
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        InitializeLoggerCommand initializeLoggerCommand = new InitializeLoggerCommand(groupedDeviceCommand);

        assertEquals(ComCommandTypes.INIT_LOGGER_COMMAND, initializeLoggerCommand.getCommandType());
    }

    @Test
    public void validateAdapterCallForMeterProtocol() {
        Logger logger = Logger.getLogger("MyTestLogger");
        MeterProtocolAdapter meterProtocolAdapter = mock(MeterProtocolAdapter.class);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = newTestExecutionContext(logger);

        executionContext.connecting = new StopWatch();
        executionContext.executing = new StopWatch(false);  // Do not auto start but start it manually as soon as execution starts.
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, meterProtocolAdapter, null);
        CommandFactory.createLegacyInitLoggerCommand(groupedDeviceCommand, comTaskExecution);

        // business method
        groupedDeviceCommand.execute(executionContext);

        // validate the initializeLogger is called on the DeviceProtocolAdapter class
        verify(meterProtocolAdapter).initializeLogger(any(Logger.class));
    }

    @Test
    public void validateAdapterCallForSmartMeterProtocol() {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        SmartMeterProtocolAdapter smartMeterProtocolAdapter = mock(SmartMeterProtocolAdapter.class);
        ExecutionContext executionContext = newTestExecutionContext(logger);

        executionContext.connecting = new StopWatch();
        executionContext.executing = new StopWatch(false);  // Do not auto start but start it manually as soon as execution starts.
        CommandRoot commandRoot = new CommandRootImpl(executionContext, commandRootServiceProvider);
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(commandRoot, offlineDevice, smartMeterProtocolAdapter, null);
        CommandFactory.createLegacyInitLoggerCommand(groupedDeviceCommand, comTaskExecution);

        // business method
        groupedDeviceCommand.execute(executionContext);

        // validate the initializeLogger is called on the DeviceProtocolAdapter class
        verify(smartMeterProtocolAdapter).initializeLogger(any(Logger.class));
    }

    @Test(expected = ComCommandException.class)
    public void validateIllegalDeviceProtocolTest() {
        Logger logger = Logger.getLogger("MyTestLogger");
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        ExecutionContext executionContext = newTestExecutionContext(logger);
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        CommandFactory.createLegacyInitLoggerCommand(groupedDeviceCommand, comTaskExecution);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        try {
            // business method
            groupedDeviceCommand.execute(executionContext);
        } catch (ComCommandException e) {
            if (e.getMessageSeed().equals(MessageSeeds.ILLEGAL_COMMAND)) {
                throw e;
            }
        }
    }

}