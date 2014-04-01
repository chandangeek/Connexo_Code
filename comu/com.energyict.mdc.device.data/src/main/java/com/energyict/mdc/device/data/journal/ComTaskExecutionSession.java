package com.energyict.mdc.device.data.journal;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.Device;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/12
 * Time: 14:25
 */
public interface ComTaskExecutionSession extends IdBusinessObject {

    public enum SuccessIndicator {
        Success,
        Failure,
        Interrupted;

        public static Set<SuccessIndicator> unSuccessful(){
            return EnumSet.of(Failure, Interrupted);
        }

        public int dbValue() {
            return this.ordinal();
        }

        public static SuccessIndicator valueFromDb(int dbValue) {
            for (SuccessIndicator indicator : SuccessIndicator.values()) {
                if (indicator.dbValue() == dbValue) {
                    return indicator;
                }
            }
            throw new ApplicationException("unknown dbValue: " + dbValue);
        }

    }

    public Device getDevice ();

    public ComSession getComSession ();

    public ComStatistics getComStatistics ();

    public ComTaskExecution getComTaskExecution ();

    public List<ComTaskExecutionJournalEntry> getComTaskExecutionJournalEntries ();

    public Date getStartDate ();

    public Date getStopDate ();

    public SuccessIndicator getSuccessIndicator ();

    /**
     * Returns a {@link ComCommandJournalEntryBuilder} that adds a new
     * {@link ComCommandJournalEntry} to this ComTaskExecutionSession.
     *
     * @return The ComCommandJournalEntryBuilder
     */
    public ComCommandJournalEntryBuilder newComCommandJournalEntry();

    /**
     * Returns a {@link ComCommandJournalEntryBuilder} that adds a new
     * {@link ComTaskExecutionMessageJournalEntry} to this ComTaskExecutionSession.
     *
     * @return The ComTaskExecutionMessageJournalEntryBuilder
     */
    public ComTaskExecutionMessageJournalEntryBuilder newComTaskExecutionMessageJournalEntry();

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
        public void timestamp (Date when);

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