/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyTimeslice;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Merges a list of {@link TopologyTimeslice}s to make sure that none
 * of the {@link Range intervals} of the entries overlap.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-04 (08:58)
 */
public class TopologyTimesliceMerger {

    private List<CompleteTopologyTimesliceImpl> entries = new ArrayList<>();

    public List<CompleteTopologyTimesliceImpl> getEntries() {
        return new ArrayList<>(this.entries);
    }

    public void add(CompleteTopologyTimesliceImpl newEntry) {
        AddEntryCommand command = new DefaultAddEntryCommand();
        for (CompleteTopologyTimesliceImpl existingChild : this.entries) {
            command = this.createCommandsFor(existingChild, command);
        }
        command.execute(newEntry);
    }

    private AddEntryCommand createCommandsFor(CompleteTopologyTimesliceImpl existingEntry, AddEntryCommand defaultCommand) {
        return new EqualsCommand(
                existingEntry,
                new EnvelopCommand(
                        existingEntry,
                        new IncludeCommand(
                                existingEntry,
                                new OverlapCommand(
                                        existingEntry,
                                        defaultCommand))));
    }

    private Instant lowerEndpoint(Range<Instant> period) {
        if (period.hasLowerBound()) {
            return period.lowerEndpoint();
        } else {
            return Instant.MIN;
        }
    }

    private Instant upperEndpoint(Range<Instant> period) {
        if (period.hasUpperBound()) {
            return period.upperEndpoint();
        } else {
            return Instant.MAX;
        }
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
        public boolean execute(CompleteTopologyTimesliceImpl newEntry);
    }

    private abstract class AddEntryCommandImpl implements AddEntryCommand {
        protected void addEntry(CompleteTopologyTimesliceImpl newEntry) {
            TopologyTimesliceMerger.this.entries.add(newEntry);
        }

        protected void removeEntry(CompleteTopologyTimesliceImpl newEntry) {
            TopologyTimesliceMerger.this.entries.remove(newEntry);
        }

        public Range<Instant> fromStartToStart(Range<Instant> first, Range<Instant> second) {
            return Range.closed(lowerEndpoint(first), lowerEndpoint(second));
        }

        public Range<Instant> fromStartToEnd(Range<Instant> first, Range<Instant> second) {
            return Range.closed(lowerEndpoint(first), upperEndpoint(second));
        }

        public Range<Instant> fromEndToEnd(Range<Instant> first, Range<Instant> second) {
            return Range.closed(upperEndpoint(first), upperEndpoint(second));
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
        public boolean execute(CompleteTopologyTimesliceImpl newEntry) {
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
        private final CompleteTopologyTimesliceImpl existingEntry;
        private final AddEntryCommand next;

        protected ChainedAddEntryCommand(CompleteTopologyTimesliceImpl existingEntry, AddEntryCommand next) {
            super();
            this.existingEntry = existingEntry;
            this.next = next;
        }

        protected CompleteTopologyTimesliceImpl myEntry() {
            return existingEntry;
        }

        @Override
        public boolean execute(CompleteTopologyTimesliceImpl newEntry) {
            if (this.matches(newEntry)) {
                this.merge(newEntry);
                return false;
            } else {
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
        protected abstract boolean matches(CompleteTopologyTimesliceImpl newEntry);

        /**
         * Merges the new CompleteCommunicationTopologyEntryImpl entry with this command's entry.
         *
         * @param newEntry The new CompleteCommunicationTopologyEntryImpl entry
         */
        protected abstract void merge(CompleteTopologyTimesliceImpl newEntry);

    }

    private class EqualsCommand extends ChainedAddEntryCommand {
        protected EqualsCommand(CompleteTopologyTimesliceImpl existingEntry, AddEntryCommand next) {
            super(existingEntry, next);
        }

        @Override
        protected boolean matches(CompleteTopologyTimesliceImpl newEntry) {
            return myEntry().getPeriod().equals(newEntry.getPeriod());
        }

        @Override
        protected void merge(CompleteTopologyTimesliceImpl newEntry) {
            myEntry().addAll(newEntry.getDevices());
        }
    }

    /**
     * Uses the {@link Range#encloses(Range)} method to check
     * for matches and splits the existing CompleteCommunicationTopologyEntryImpl entry
     * into multiple entries as a merge strategy.
     * An Range envelops another if the other Interval are completely within its own bounds.
     * In other words: i1.start &lt; i2.start &le; i2.end &lt; i1.end
     * Here is how the entry is split:
     * <ol>
     * <li>from start of this command's entry to start of new entry</li>
     * <li>new entry interval</li>
     * <li>from new entry end to original end</li>
     * </ol>
     */
    private class EnvelopCommand extends ChainedAddEntryCommand {
        protected EnvelopCommand(CompleteTopologyTimesliceImpl existingEntry, AddEntryCommand next) {
            super(existingEntry, next);
        }

        @Override
        protected boolean matches(CompleteTopologyTimesliceImpl newEntry) {
            Range<Instant> myPeriod = myEntry().getPeriod();
            Instant lowerEndpoint = lowerEndpoint(myPeriod);
            Instant upperEndpoint = upperEndpoint(myPeriod);
            if (lowerEndpoint.equals(upperEndpoint)) {
                return Range.closed(lowerEndpoint, upperEndpoint).encloses(newEntry.getPeriod());
            }
            return Range.open(lowerEndpoint, upperEndpoint).encloses(newEntry.getPeriod());
        }

        @Override
        protected void merge(CompleteTopologyTimesliceImpl newEntry) {
            this.removeEntry(myEntry());
            this.addEntry(
                    new CompleteTopologyTimesliceImpl(
                            this.fromStartToStart(myEntry().getPeriod(), newEntry.getPeriod()),
                            myEntry().getDevices()));
            this.addEntry(
                    new CompleteTopologyTimesliceImpl(
                            newEntry.getPeriod(),
                            this.concat(myEntry().getDevices(), newEntry.getDevices())));
            this.addEntry(
                    new CompleteTopologyTimesliceImpl(
                            this.fromEndToEnd(newEntry.getPeriod(), myEntry().getPeriod()),
                            myEntry().getDevices()));
        }

    }

    /**
     * Uses the {@link Range#encloses(Range)} method to check
     * for matches and splits the existing CompleteCommunicationTopologyEntryImpl entry
     * into multiple entries as a merge strategy.
     * A Range includes another Interval under the following conditions:
     * i1.start &le; i2.start &le; i2.end &le; i1.end
     * Here is how the entry is split (assuming the overlap is at the end of the command's entry):
     * <ol>
     * <li>from start of this command's entry to start of new entry</li>
     * <li>new entry interval</li>
     * </ol>
     */
    private class IncludeCommand extends ChainedAddEntryCommand {
        protected IncludeCommand(CompleteTopologyTimesliceImpl existingEntry, AddEntryCommand next) {
            super(existingEntry, next);
        }

        @Override
        protected boolean matches(CompleteTopologyTimesliceImpl newEntry) {
            return myEntry().getPeriod().encloses(newEntry.getPeriod());
        }

        @Override
        protected void merge(CompleteTopologyTimesliceImpl newEntry) {
            this.removeEntry(myEntry());
            if (this.abutsAtStartOfMyEntry(newEntry)) {
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                newEntry.getPeriod(),
                                this.concat(myEntry().getDevices(), newEntry.getDevices())));
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromEndToEnd(newEntry.getPeriod(), myEntry().getPeriod()),
                                myEntry().getDevices()));
            } else {
                // Abut at the end
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromStartToStart(myEntry().getPeriod(), newEntry.getPeriod()),
                                myEntry().getDevices()));
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                newEntry.getPeriod(),
                                this.concat(myEntry().getDevices(), newEntry.getDevices())));
            }
        }

        private boolean abutsAtStartOfMyEntry(CompleteTopologyTimesliceImpl newEntry) {
            return myEntry().getPeriod().lowerEndpoint().equals(newEntry.getPeriod().lowerEndpoint());
        }
    }

    /**
     * Uses the {@link Range#isConnected(Range)} method to check
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
        protected OverlapCommand(CompleteTopologyTimesliceImpl existingEntry, AddEntryCommand next) {
            super(existingEntry, next);
        }

        @Override
        protected boolean matches(CompleteTopologyTimesliceImpl newEntry) {
            return myEntry().getPeriod().isConnected(newEntry.getPeriod())
                    && !this.endPointMatch(myEntry().getPeriod(), newEntry.getPeriod());
        }

        /**
         * Tests if both "connected" periods end points match,
         * i.e. the one's lower end point is the other's upper end point or vice versa.
         *
         * @param period1 The first period
         * @param period2 The second period
         * @return A flag that indicate if both periods abut
         */
        private boolean endPointMatch(Range<Instant> period1, Range<Instant> period2) {
            return this.lowerBoundMatch(period1, period2) || this.upperBoundMatch(period1, period2);
        }

        private boolean lowerBoundMatch(Range<Instant> period1, Range<Instant> period2) {
            return (period1.hasUpperBound() && period2.hasLowerBound() && period1.upperEndpoint().equals(period2.lowerEndpoint()))
                    || (period2.hasUpperBound() && period1.hasLowerBound() && period2.upperEndpoint().equals(period1.lowerEndpoint()));
        }

        private boolean upperBoundMatch(Range<Instant> period1, Range<Instant> period2) {
            return (period1.hasLowerBound() && period2.hasUpperBound() && period1.lowerEndpoint().equals(period2.upperEndpoint()))
                    || (period2.hasLowerBound() && period1.hasUpperBound() && period2.lowerEndpoint().equals(period1.upperEndpoint()));
        }

        @Override
        protected void merge(CompleteTopologyTimesliceImpl newEntry) {
            this.removeEntry(myEntry());
            if (newEntry.getPeriod().encloses(myEntry().getPeriod())) {
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromStartToStart(newEntry.getPeriod(), myEntry().getPeriod()),
                                newEntry.getDevices()));
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromStartToEnd(myEntry().getPeriod(), myEntry().getPeriod()),
                                this.concat(newEntry.getDevices(), myEntry().getDevices())));
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromEndToEnd(myEntry().getPeriod(), newEntry.getPeriod()),
                                newEntry.getDevices()));
            } else if (myEntry().getPeriod().contains(upperEndpoint(newEntry.getPeriod()))) {
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromStartToStart(newEntry.getPeriod(), myEntry().getPeriod()),
                                newEntry.getDevices()));
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromStartToEnd(myEntry().getPeriod(), newEntry.getPeriod()),
                                this.concat(newEntry.getDevices(), myEntry().getDevices())));
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromEndToEnd(newEntry.getPeriod(), myEntry().getPeriod()),
                                myEntry().getDevices()));
            } else {
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromStartToStart(myEntry().getPeriod(), newEntry.getPeriod()),
                                myEntry().getDevices()));
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromStartToEnd(newEntry.getPeriod(), myEntry().getPeriod()),
                                this.concat(newEntry.getDevices(), myEntry().getDevices())));
                this.addEntry(
                        new CompleteTopologyTimesliceImpl(
                                this.fromEndToEnd(myEntry().getPeriod(), newEntry.getPeriod()),
                                newEntry.getDevices()));
            }
        }
    }

}