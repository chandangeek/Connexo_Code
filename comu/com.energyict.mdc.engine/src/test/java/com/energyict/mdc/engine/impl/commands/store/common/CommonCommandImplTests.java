package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import static org.mockito.Mockito.mock;

/**
 * Common methods for ComCommand tests
 *
 * @author gna
 * @since 31/05/12 - 16:28
 */
public abstract class CommonCommandImplTests extends AbstractComCommandExecuteTest {

    public static CommandRoot createCommandRoot() {
        return new CommandRootImpl(mock(OfflineDevice.class), AbstractComCommandExecuteTest.newTestExecutionContext(), serviceProvider);
    }

    public static CommandRoot createCommandRoot(final OfflineDevice offlineDevice){
        return new CommandRootImpl(offlineDevice, AbstractComCommandExecuteTest.newTestExecutionContext(), serviceProvider);
    }
}
