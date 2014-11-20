package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ProtocolTask;

import java.util.List;

/**
 * Models the type of a {@link ComCommand}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-20 (15:25)
 */
public interface ComCommandType {

    public void createLegacyCommandsFromProtocolTask(CommandRoot root, List<? extends ProtocolTask> protocolTasks, ProtocolTask protocolTask, ComTaskExecution comTaskExecution);

    public void createCommandsFromTask(CommandRoot root, ProtocolTask protocolTask, ComTaskExecution comTaskExecution);

}