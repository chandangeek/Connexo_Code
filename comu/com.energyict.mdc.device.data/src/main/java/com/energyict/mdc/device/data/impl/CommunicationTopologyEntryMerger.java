package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.CommunicationTopologyEntry;
import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.util.time.Interval;

import java.util.ArrayList;
import java.util.List;

/**
 * Merges a list of {@link CommunicationTopologyEntry} to make sure that none
 * of the {@link Interval}s of the entries overlap.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-04 (08:58)
 */
public class CommunicationTopologyEntryMerger {

    private List<CompleteCommunicationTopologyEntryImpl> entries = new ArrayList<>();

    public List<CompleteCommunicationTopologyEntryImpl> getEntries() {
        return new ArrayList<>(this.entries);
    }

    public void add (CompleteCommunicationTopologyEntryImpl newEntry) {
        AddEntryCommand command = new DefaultAddEntryCommand();
        for (CompleteCommunicationTopologyEntryImpl existingChild : this.entries) {
            command = this.createCommandsFor(existingChild, command);
        }
        command.execute(newEntry);
    }

    private AddEntryCommand createCommandsFor(CompleteCommunicationTopologyEntryImpl existingEntry, AddEntryCommand nextCommand) {
        return new EqualsCommand(
                existingEntry,
                new EnvelopCommand(
                    existingEntry,
                    new IncludeCommand(
                            existingEntry,
                            new OverlapCommand(
                                    existingEntry,
                                    nextCommand))));
    }

    /**
     * Models a command that will attempt to add a new CompleteCommunicationTopologyEntryImpl.
     * The intend is that all existing entries are wrapped into an AddEntryCommand to check
     * for matches and finally add it to the list of entries with a default command if no matches were found.
     */
    private interface AddEntryCommand {
        /**
         * Executes this command and returns true if the entry was
         * effectively added or merged into an existing entry.
         *
         * @param newEntry The new CompleteCommunicationTopologyEntryImpl entry that needs to be added
         * @return <code>true</code> iff the entry was really added or <code>false</code> if it was merged into an existing entry
         */
        public boolean execute(CompleteCommunicationTopologyEntryImpl newEntry);
    }

    private abstract class AddEntryCommandImpl implements AddEntryCommand {
        protected void addEntry(CompleteCommunicationTopologyEntryImpl newEntry) {
            CommunicationTopologyEntryMerger.this.entries.add(newEntry);
        }

        protected void removeEntry(CompleteCommunicationTopologyEntryImpl newEntry) {
            CommunicationTopologyEntryMerger.this.entries.remove(newEntry);
        }

        public Interval fromStartToStart(Interval first, Interval second) {
            return new Interval(first.getStart(), second.getStart());
        }

        public Interval fromStartToEnd(Interval first, Interval second) {
            return new Interval(first.getStart(), second.getEnd());
        }

        public Interval fromEndToEnd(Interval first, Interval second) {
            return new Interval(first.getEnd(), second.getEnd());
        }

        public List<Device> concat(List<Device> devices, List<Device> moreDevices) {
            List<Device> concatenated = new ArrayList<>(devices);
            concatenated.addAll(moreDevices);
            return concatenated;
        }
    }
    /**
     * Provides an implementation for the {@link AddEntryCommand} interface
     * that will simply add the new CompleteCommunicationTopologyEntryImpl entry.
     */
    private class DefaultAddEntryCommand extends AddEntryCommandImpl {
        @Override
        public boolean execute(CompleteCommunicationTopologyEntryImpl newEntry) {
            this.addEntry(newEntry);
            return true;
        }
    }

    /**
     * Allows chaining AddEntryCommands to pass control onto the next
     * if this command's existing CompleteCommunicationTopologyEntryImpl does not match
     * the new CompleteCommunicationTopologyEntryImpl entry.
     */
    private abstract class ChainedAddEntryCommand extends AddEntryCommandImpl {
        private final CompleteCommunicationTopologyEntryImpl existingEntry;
        private final AddEntryCommand next;

        protected ChainedAddEntryCommand(CompleteCommunicationTopologyEntryImpl existingEntry, AddEntryCommand next) {
            super();
            this.existingEntry = existingEntry;
            this.next = next;
        }

        protected CompleteCommunicationTopologyEntryImpl myEntry() {
            return existingEntry;
        }

        @Override
        public boolean execute(CompleteCommunicationTopologyEntryImpl newEntry) {
            if (this.matches(newEntry)) {
                this.merge(newEntry);
                return false;
            }
            else {
                return this.next.execute(newEntry);
            }
        }

        /**
         * Tests if the new CompleteCommunicationTopologyEntryImpl entry matches
         * this command's existing entry.
         * If that is the case, the command will effectively be executed.
         * Otherwise, control will be passed to the next command.
         *
         * @param newEntry The new CompleteCommunicationTopologyEntryImpl entry
         * @return <code>true</code> iff the new entry matches this command's entry
         */
        protected abstract boolean matches (CompleteCommunicationTopologyEntryImpl newEntry);

        /**
         * Merges the new CompleteCommunicationTopologyEntryImpl entry with this command's entry.
         *
         * @param newEntry The new CompleteCommunicationTopologyEntryImpl entry
         */
        protected abstract void merge(CompleteCommunicationTopologyEntryImpl newEntry);

    }

    private class EqualsCommand extends ChainedAddEntryCommand {
        protected EqualsCommand(CompleteCommunicationTopologyEntryImpl existingEntry, AddEntryCommand next) {
            super(existingEntry, next);
        }

        @Override
        protected boolean matches(CompleteCommunicationTopologyEntryImpl newEntry) {
            return myEntry().getInterval().equals(newEntry.getInterval());
        }

        @Override
        protected void merge(CompleteCommunicationTopologyEntryImpl newEntry) {
            myEntry().addAll(newEntry.getDevices());
        }
    }

    /**
     * Uses the {@link Interval#envelops(Interval)} method to check
     * for matches and splits the existing CompleteCommunicationTopologyEntryImpl entry
     * into multiple entries as a merge strategy.
     * An Interval envelops another if the other Interval are completely within its own bounds.
     * In other words: i1.start &lt; i2.start &le; i2.end &lt; i1.end
     * Here is how the entry is split:
     * <ol>
     * <li>from start of this command's entry to start of new entry</li>
     * <li>new entry interval</li>
     * <li>from new entry end to original end</li>
     * </ol>
     */
    private class EnvelopCommand extends ChainedAddEntryCommand {
        protected EnvelopCommand(CompleteCommunicationTopologyEntryImpl existingEntry, AddEntryCommand next) {
            super(existingEntry, next);
        }

        @Override
        protected boolean matches(CompleteCommunicationTopologyEntryImpl newEntry) {
            return myEntry().getInterval().envelops(newEntry.getInterval());
        }

        @Override
        protected void merge(CompleteCommunicationTopologyEntryImpl newEntry) {
            this.removeEntry(myEntry());
            this.addEntry(
                    new CompleteCommunicationTopologyEntryImpl(
                            this.fromStartToStart(myEntry().getInterval(), newEntry.getInterval()),
                            myEntry().getDevices()));
            this.addEntry(
                    new CompleteCommunicationTopologyEntryImpl(
                            newEntry.getInterval(),
                            this.concat(myEntry().getDevices(), newEntry.getDevices())));
            this.addEntry(
                    new CompleteCommunicationTopologyEntryImpl(
                            this.fromEndToEnd(newEntry.getInterval(), myEntry().getInterval()),
                            myEntry().getDevices()));
        }

    }

    /**
     * Uses the {@link Interval#includes(Interval)} method to check
     * for matches and splits the existing CompleteCommunicationTopologyEntryImpl entry
     * into multiple entries as a merge strategy.
     * An Interval includes another if the other Interval under the following conditions:
     * i1.start &le; i2.start &le; i2.end &le; i1.end
     * Here is how the entry is split (assuming the overlap is at the end of the command's entry):
     * <ol>
     * <li>from start of this command's entry to start of new entry</li>
     * <li>new entry interval</li>
     * </ol>
     */
    private class IncludeCommand extends ChainedAddEntryCommand {
        protected IncludeCommand(CompleteCommunicationTopologyEntryImpl existingEntry, AddEntryCommand next) {
            super(existingEntry, next);
        }

        @Override
        protected boolean matches(CompleteCommunicationTopologyEntryImpl newEntry) {
            return myEntry().getInterval().includes(newEntry.getInterval());
        }

        @Override
        protected void merge(CompleteCommunicationTopologyEntryImpl newEntry) {
            this.removeEntry(myEntry());
            if (this.abutsAtStartOfMyEntry(newEntry)) {
                this.addEntry(
                        new CompleteCommunicationTopologyEntryImpl(
                                newEntry.getInterval(),
                                this.concat(myEntry().getDevices(), newEntry.getDevices())));
                this.addEntry(
                        new CompleteCommunicationTopologyEntryImpl(
                                this.fromEndToEnd(newEntry.getInterval(), myEntry().getInterval()),
                                myEntry().getDevices()));
            }
            else {
                // Abut at the end
                this.addEntry(
                        new CompleteCommunicationTopologyEntryImpl(
                                this.fromStartToStart(myEntry().getInterval(), newEntry.getInterval()),
                                myEntry().getDevices()));
                this.addEntry(
                        new CompleteCommunicationTopologyEntryImpl(
                                newEntry.getInterval(),
                                this.concat(myEntry().getDevices(), newEntry.getDevices())));
            }
        }

        private boolean abutsAtStartOfMyEntry(CompleteCommunicationTopologyEntryImpl newEntry) {
            return myEntry().getInterval().getStart().equals(newEntry.getInterval().getStart());
        }
    }

    /**
     * Uses the {@link Interval#overlaps(Interval)} method to check
     * for matches and splits the existing CompleteCommunicationTopologyEntryImpl entry
     * into multiple entries as a merge strategy.
     * Here is how the entry is split (assuming the overlap is at the end of the command's entry):
     * <ol>
     * <li>from start of this command's entry to start of new entry</li>
     * <li>from start of the new entry to end of this command's entry</li>
     * <li>from end of this command's entry to end of the new entry</li>
     * </ol>
     */
    private class OverlapCommand extends ChainedAddEntryCommand {
        protected OverlapCommand(CompleteCommunicationTopologyEntryImpl existingEntry, AddEntryCommand next) {
            super(existingEntry, next);
        }

        @Override
        protected boolean matches(CompleteCommunicationTopologyEntryImpl newEntry) {
            return myEntry().getInterval().overlaps(newEntry.getInterval());
        }

        @Override
        protected void merge(CompleteCommunicationTopologyEntryImpl newEntry) {
            this.removeEntry(myEntry());
            if (myEntry().getInterval().contains(newEntry.getInterval().getEnd(), Interval.EndpointBehavior.OPEN_CLOSED)) {
                this.addEntry(
                        new CompleteCommunicationTopologyEntryImpl(
                                this.fromStartToStart(newEntry.getInterval(), myEntry().getInterval()),
                                newEntry.getDevices()));
                this.addEntry(
                        new CompleteCommunicationTopologyEntryImpl(
                                this.fromStartToEnd(myEntry().getInterval(), newEntry.getInterval()),
                                this.concat(newEntry.getDevices(), myEntry().getDevices())));
                this.addEntry(
                        new CompleteCommunicationTopologyEntryImpl(
                                this.fromEndToEnd(newEntry.getInterval(), myEntry().getInterval()),
                                myEntry().getDevices()));
            }
            else {
                this.addEntry(
                        new CompleteCommunicationTopologyEntryImpl(
                                this.fromStartToStart(myEntry().getInterval(), newEntry.getInterval()),
                                myEntry().getDevices()));
                this.addEntry(
                        new CompleteCommunicationTopologyEntryImpl(
                                this.fromStartToEnd(newEntry.getInterval(), myEntry().getInterval()),
                                this.concat(newEntry.getDevices(), myEntry().getDevices())));
                this.addEntry(
                        new CompleteCommunicationTopologyEntryImpl(
                                this.fromEndToEnd(myEntry().getInterval(), newEntry.getInterval()),
                                newEntry.getDevices()));
            }
        }
    }

}