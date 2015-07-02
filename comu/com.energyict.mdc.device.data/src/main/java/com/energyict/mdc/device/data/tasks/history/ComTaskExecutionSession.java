package com.energyict.mdc.device.data.tasks.history;

import com.energyict.mdc.common.HasId;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/12
 * Time: 14:25
 */
@ProviderType
public interface ComTaskExecutionSession extends HasId {

    public enum SuccessIndicator {
        Success,
        Failure,
        Interrupted;

        public static Set<SuccessIndicator> unSuccessful(){
            return EnumSet.of(Failure, Interrupted);
        }

    }

    public Device getDevice ();

    public ComSession getComSession ();

    public ComStatistics getStatistics();

    public ComTaskExecution getComTaskExecution ();

    public ComTask getComTask();

    public List<ComTaskExecutionJournalEntry> getComTaskExecutionJournalEntries ();

    public Finder<ComTaskExecutionJournalEntry> findComTaskExecutionJournalEntries(Set<ComServer.LogLevel> levels);

    public Instant getStartDate ();

    public Instant getStopDate ();

    public boolean endsAfter (ComTaskExecutionSession other);

    public SuccessIndicator getSuccessIndicator ();

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
    public ComCommandJournalEntry createComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDescription, String commandDescription);

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
    public ComTaskExecutionMessageJournalEntry createComTaskExecutionMessageJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, String errorDescription);

    /**
     * Gets the completion code with the highest priority
     * @return the completion code
     */
    public CompletionCode getHighestPriorityCompletionCode();

    /**
     * Gets the error description of the journal entry with the highest priority completion code
     * @return The error description
     */
    public String getHighestPriorityErrorDescription();

    /**
     * Provides building services for {@link ComTaskExecutionJournalEntry ComTaskExecutionJournalEntries}.
     */
    public interface ComTaskExecutionJournalEntryBuilder {

        /**
         * Specifies the timestamp of the entry that is under construction.
         * By default, the timestamp is set to the time on which the builder was created.
         *
         * @param when The entry's timestamp
         */
        public void timestamp (Instant when);

        public void errorDescription (String errorDescription);

    }

    /**
     * Provides building services for {@link ComCommandJournalEntry ComCommandJournalEntries}.
     */
    public interface ComCommandJournalEntryBuilder extends ComTaskExecutionJournalEntryBuilder {

        public void completionCode (CompletionCode completionCode);

        public void comCommandDescription (String comCommandDescription);

        /**
         * Completes the building process by adding the {@link ComCommandJournalEntry}
         * to the {@link ComTaskExecutionSession} from which this builder was created.
         *
         * @return The created ComCommandJournalEntry
         */
        public ComCommandJournalEntry add ();

    }

    /**
     * Provides building services for {@link ComTaskExecutionMessageJournalEntry ComTaskExecutionMessageJournalEntries}.
     */
    public interface ComTaskExecutionMessageJournalEntryBuilder extends ComTaskExecutionJournalEntryBuilder {

        public void description (String description);

        /**
         * Completes the building process by adding the {@link ComTaskExecutionMessageJournalEntry}
         * to the {@link ComTaskExecutionSession} from which this builder was created.
         *
         * @return The created ComCommandJournalEntry
         */
        public ComTaskExecutionMessageJournalEntry add ();

    }

}