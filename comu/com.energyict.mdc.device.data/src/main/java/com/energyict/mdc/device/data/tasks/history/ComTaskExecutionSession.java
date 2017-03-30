/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@ProviderType
public interface ComTaskExecutionSession extends HasId {

    enum SuccessIndicator {
        Success,
        Failure,
        Interrupted;

        public static Set<SuccessIndicator> unSuccessful() {
            return EnumSet.of(Failure, Interrupted);
        }

    }

    Device getDevice();

    ComSession getComSession();

    ComStatistics getStatistics();

    ComTaskExecution getComTaskExecution();

    ComTask getComTask();

    List<ComTaskExecutionJournalEntry> getComTaskExecutionJournalEntries();

    Finder<ComTaskExecutionJournalEntry> findComTaskExecutionJournalEntries(Set<ComServer.LogLevel> levels);

    Instant getStartDate();

    Instant getStopDate();

    boolean endsAfter(ComTaskExecutionSession other);

    SuccessIndicator getSuccessIndicator();

    /**
     * Returns a {@link ComCommandJournalEntryBuilder} that adds a new
     * {@link ComCommandJournalEntry} to this ComTaskExecutionSession.
     *
     * @param timestamp The timestamp of which the ComCommandJournalEntry
     * @param completionCode The CompletionCode of the ComCommand
     * @param errorDescription The error description
     * @param commandDescription The ComCommand description
     * @return The ComCommandJournalEntryBuilder
     */
    ComCommandJournalEntry createComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDescription, String commandDescription);

    /**
     * Returns a {@link ComCommandJournalEntryBuilder} that adds a new
     * {@link ComTaskExecutionMessageJournalEntry} to this ComTaskExecutionSession.
     *
     * @param timestamp The timestamp of which the ComCommandJournalEntry
     * @param logLevel The LogLevel
     * @param message The message
     * @param errorDescription The error description
     * @return The ComTaskExecutionMessageJournalEntryBuilder
     */
    ComTaskExecutionMessageJournalEntry createComTaskExecutionMessageJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, String errorDescription);

    /**
     * Gets the completion code with the highest priority
     * @return the completion code
     */
    CompletionCode getHighestPriorityCompletionCode();

    String getHighestPriorityCompletionCodeDisplayName();

    /**
     * Gets the error description of the journal entry with the highest priority completion code
     * @return The error description
     */
    String getHighestPriorityErrorDescription();

    /**
     * Provides building services for {@link ComTaskExecutionJournalEntry ComTaskExecutionJournalEntries}.
     */
    interface ComTaskExecutionJournalEntryBuilder {

        /**
         * Specifies the timestamp of the entry that is under construction.
         * By default, the timestamp is set to the time on which the builder was created.
         *
         * @param when The entry's timestamp
         */
        void timestamp(Instant when);

        void errorDescription(String errorDescription);

    }

    /**
     * Provides building services for {@link ComCommandJournalEntry ComCommandJournalEntries}.
     */
    interface ComCommandJournalEntryBuilder extends ComTaskExecutionJournalEntryBuilder {

        void completionCode(CompletionCode completionCode);

        void comCommandDescription(String comCommandDescription);

        /**
         * Completes the building process by adding the {@link ComCommandJournalEntry}
         * to the {@link ComTaskExecutionSession} from which this builder was created.
         *
         * @return The created ComCommandJournalEntry
         */
        ComCommandJournalEntry add();

    }

    /**
     * Provides building services for {@link ComTaskExecutionMessageJournalEntry ComTaskExecutionMessageJournalEntries}.
     */
    interface ComTaskExecutionMessageJournalEntryBuilder extends ComTaskExecutionJournalEntryBuilder {

        void description(String description);

        /**
         * Completes the building process by adding the {@link ComTaskExecutionMessageJournalEntry}
         * to the {@link ComTaskExecutionSession} from which this builder was created.
         *
         * @return The created ComCommandJournalEntry
         */
        ComTaskExecutionMessageJournalEntry add();

    }

}