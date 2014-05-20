package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.StatusInformationCommand;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
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
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), serviceProvider);
        StatusInformationCommand statusInformationCommand = new StatusInformationCommandImpl(offlineDevice, commandRoot, null);

        // asserts
        Assert.assertEquals(ComCommandTypes.STATUS_INFORMATION_COMMAND, statusInformationCommand.getCommandType());
    }

}