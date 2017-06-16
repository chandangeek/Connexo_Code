/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.tasks.LogBooksTask;

import com.energyict.protocol.LogBookReader;

import java.util.List;

/**
 * The {@link ComCommand} which can perform the necessary actions to read all the logBooks from the device
 *
 * @author sva
 * @since 07/12/12 - 11:33
 */
public interface LogBooksCommand extends CompositeComCommand {

    List<LogBookReader> getLogBookReaders();

    void removeLogBookReader(LogBookReader logBookReader);

    void updateAccordingTo(LogBooksTask logBooksTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution);

}
