package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.comserver.commands.AbstractComCommandExecuteTest;
import com.energyict.comserver.commands.core.CommandRootImpl;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.commands.StatusInformationCommand;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link com.energyict.comserver.commands.deviceactions.StatusInformationCommandImpl} component
 *
 * @author gna
 * @since 18/06/12 - 14:46
 */
public class StatusInformationCommandImplTest extends AbstractComCommandExecuteTest {

    @Test
    public void commandTypeTest() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        CommandRoot commandRoot = new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), issueService);
        StatusInformationCommand statusInformationCommand = new StatusInformationCommandImpl(offlineDevice, commandRoot, null);

        // asserts
        Assert.assertEquals(ComCommandTypes.STATUS_INFORMATION_COMMAND, statusInformationCommand.getCommandType());
    }

}