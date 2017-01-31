/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.StatusInformationCommand;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Tests for the StatusInformationCommandImpl component
 *
 * @author gna
 * @since 18/06/12 - 14:46
 */
public class StatusInformationCommandImplTest extends AbstractComCommandExecuteTest {

    @Test
    public void commandTypeTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        GroupedDeviceCommand groupedDeviceCommand = getGroupedDeviceCommand();
        StatusInformationCommand statusInformationCommand = new StatusInformationCommandImpl(device, groupedDeviceCommand, null);

        // asserts
        Assert.assertEquals(ComCommandTypes.STATUS_INFORMATION_COMMAND, statusInformationCommand.getCommandType());
    }

}