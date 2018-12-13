/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link ComTaskExecutionJournalEntry} for a command that was executed
 * against the device in the context of a ComTaskExecution.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (09:13)
 */
@ProviderType
public interface ComCommandJournalEntry extends ComTaskExecutionJournalEntry {

    /**
     * Gets the {@link CompletionCode} of the command that was executed.
     *
     * @return The CompletionCode
     */
    CompletionCode getCompletionCode();

    /**
     * Gets the human readable description of the command that was executed.
     *
     * @return The human readable description of the command that was executed
     */
    String getCommandDescription();

}