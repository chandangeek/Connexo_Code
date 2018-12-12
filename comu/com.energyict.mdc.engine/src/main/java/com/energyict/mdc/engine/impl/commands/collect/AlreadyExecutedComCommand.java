/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

/**
 * @author sva
 * @since 9/06/2015 - 16:08
 */
public interface AlreadyExecutedComCommand extends NoopComCommand {

    /**
     * Links this AlreadyExecutedComCommand to the ComCommand which will do actual execution of the ComCommands.<br/>
     * By doing so, we indicate all operations of type comCommandType will be executed as part of the specified ComCommand.
     *
     * @param comCommandType the type of ComCommands
     * @param comCommand the ComCommand who will actually execute the ComCommands
     */
    public void linkToComCommandDoingActualExecution(ComCommandTypes comCommandType, ComCommand comCommand);
}