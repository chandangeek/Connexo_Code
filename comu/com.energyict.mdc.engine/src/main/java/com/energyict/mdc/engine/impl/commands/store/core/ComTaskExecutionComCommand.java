/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;

/**
 * Models a {@link CompositeComCommand} that contains all the ComCommands
 * that relate to the same ComTaskExecution.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (14:15)
 */
public interface ComTaskExecutionComCommand extends CompositeComCommand {

    boolean contains(ComCommand comCommand);

    ComTaskExecution getComTaskExecution();

}