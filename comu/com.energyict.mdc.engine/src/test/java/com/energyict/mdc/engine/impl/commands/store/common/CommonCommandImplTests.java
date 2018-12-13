/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.AbstractComCommandExecuteTest;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Common methods for ComCommand tests
 *
 * @author gna
 * @since 31/05/12 - 16:28
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class CommonCommandImplTests extends AbstractComCommandExecuteTest {

    public CommandRoot createCommandRoot() {
        return new CommandRootImpl(this.newTestExecutionContext(), this.commandRootServiceProvider);
    }

    public CommandRoot createCommandRoot(final OfflineDevice offlineDevice){
        return new CommandRootImpl(this.newTestExecutionContext(), this.commandRootServiceProvider);
    }

}