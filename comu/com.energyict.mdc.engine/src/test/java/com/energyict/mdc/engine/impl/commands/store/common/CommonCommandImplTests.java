package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.comserver.commands.AbstractComCommandExecuteTest;
import com.energyict.comserver.commands.core.CommandRootImpl;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import static org.mockito.Mockito.mock;

/**
 * Common methods for {@link com.energyict.mdc.commands.ComCommand} tests
 *
 * @author gna
 * @since 31/05/12 - 16:28
 */
public abstract class CommonCommandImplTests extends AbstractComCommandExecuteTest {

    public static CommandRoot createCommandRoot() {
        return new CommandRootImpl(mock(OfflineDevice.class), AbstractComCommandExecuteTest.newTestExecutionContext(), issueService);
    }

    public static CommandRoot createCommandRoot(final OfflineDevice offlineDevice){
        return new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), issueService);
    }
}
