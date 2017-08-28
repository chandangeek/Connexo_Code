/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineLogBook;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Performs several actions on the given LogBook which are required before storing.
 * <p>
 * <p>
 * Date: 9/18/14
 * Time: 11:46 AM
 */
public class PreStoreLogBook {

    private final Clock clock;
    private final ComServerDAO comServerDAO;

    public PreStoreLogBook(Clock clock, ComServerDAO comServerDAO) {
        this.clock = clock;
        this.comServerDAO = comServerDAO;
    }

    /**
     * Tasks:
     * <ul>
     * <li>Filter out events having a data more than 1 day in the future</li>
     * <li>Filter out duplicates (of which all fields are the same) </li>
     * <li>Avoid entries having same eventTypeCode and createdDateTime (which acts as primary key of EndDeviceEventRecord)</li>
     * <li>Calculate lastLogbook date</li>
     * </ul>
     *
     * @param deviceLogBook the collected events from the device
     * @return the preStored logbook
     */
    public Optional<Pair<DeviceIdentifier, LocalLogBook>> preStore(CollectedLogBook deviceLogBook) {
        Set<UniquePair<String, Instant>> uniqueCheck = new HashSet<>();
        Optional<OfflineLogBook> offlineLogBook = this.comServerDAO.findOfflineLogBook(deviceLogBook.getLogBookIdentifier());
        if (offlineLogBook.isPresent()) {
            List<EndDeviceEvent> filteredEndDeviceEvents = new ArrayList<>();
            Instant lastLogbook = null;
            Instant currentDate = this.clock.instant();
            List<EndDeviceEvent> endDeviceEvents =
                    DecoratedStream.decorate(MeterDataFactory.createEndDeviceEventsFor(deviceLogBook, offlineLogBook.get().getLogBookId()).stream())
                            .distinct(this::calculateEndDeviceEventHashCode) // Filter out all duplicates (by looking at the hashcode of the EndDeviceEvent which is calculated across all fields of the EndDeviceEvent)
                            .collect(Collectors.toList());
            for (EndDeviceEvent endDeviceEvent : endDeviceEvents) {
                while (!uniqueCheck.add(new UniquePair<>(endDeviceEvent.getEventTypeCode(), endDeviceEvent.getCreatedDateTime()))) { // If not unique, add 1 millisecond and try to re-add
                    endDeviceEvent = EndDeviceEventImpl.copyOf(endDeviceEvent, endDeviceEvent.getCreatedDateTime().plusMillis(1));
                }

                if (!endDeviceEvent.getCreatedDateTime().isAfter(currentDate.plus(1, ChronoUnit.DAYS))) {
                    filteredEndDeviceEvents.add(endDeviceEvent);
                    if (lastLogbook == null || endDeviceEvent.getCreatedDateTime().isAfter(lastLogbook)) {
                        lastLogbook = endDeviceEvent.getCreatedDateTime();
                    }
                }
            }

            DeviceIdentifier deviceIdentifier = comServerDAO.getDeviceIdentifierFor(deviceLogBook.getLogBookIdentifier());
            return Optional.of(Pair.of(deviceIdentifier, new LocalLogBook(filteredEndDeviceEvents, lastLogbook)));
        } else {
            return Optional.empty();
        }
    }

    private int calculateEndDeviceEventHashCode(EndDeviceEvent endDeviceEvent) {
        int result = endDeviceEvent.getCreatedDateTime().hashCode();
        result = 31 * result + endDeviceEvent.getEventTypeCode().hashCode();
        result = 31 * result + (endDeviceEvent.getMRID() != null ? endDeviceEvent.getMRID().hashCode() : 0);
        result = 31 * result + (endDeviceEvent.getReason() != null ? endDeviceEvent.getReason().hashCode() : 0);
        result = 31 * result + (endDeviceEvent.getSeverity() != null ? endDeviceEvent.getSeverity().hashCode() : 0);
        result = 31 * result + (endDeviceEvent.getStatus() != null ? endDeviceEvent.getStatus().hashCode() : 0);
        result = 31 * result + (endDeviceEvent.getType() != null ? endDeviceEvent.getType().hashCode() : 0);
        result = 31 * result + (endDeviceEvent.getIssuerID() != null ? endDeviceEvent.getIssuerID().hashCode() : 0);
        result = 31 * result + (endDeviceEvent.getIssuerTrackingID() != null ? endDeviceEvent.getIssuerTrackingID().hashCode() : 0);
        result = 31 * result + (endDeviceEvent.getUserID() != null ? endDeviceEvent.getUserID().hashCode() : 0);
        result = 31 * result + (endDeviceEvent.getEventData() != null ? endDeviceEvent.getEventData().hashCode() : 0);
        result = 31 * result + (int) (endDeviceEvent.getLogBookId() ^ (endDeviceEvent.getLogBookId() >>> 32));
        result = 31 * result + endDeviceEvent.getLogBookPosition();
        result = 31 * result + (endDeviceEvent.getAliasName() != null ? endDeviceEvent.getAliasName().hashCode() : 0);
        result = 31 * result + (endDeviceEvent.getDescription() != null ? endDeviceEvent.getDescription().hashCode() : 0);
        result = 31 * result + (endDeviceEvent.getName() != null ? endDeviceEvent.getName().hashCode() : 0);
        return result;
    }

    class LocalLogBook {

        private final List<EndDeviceEvent> endDeviceEvents;
        private final Instant lastLogbook;

        private LocalLogBook(List<EndDeviceEvent> endDeviceEvents, Instant lastLogBook) {
            this.endDeviceEvents = endDeviceEvents;
            this.lastLogbook = lastLogBook;
        }

        public List<EndDeviceEvent> getEndDeviceEvents() {
            return endDeviceEvents;
        }

        public Instant getLastLogbook() {
            return lastLogbook;
        }

    }

    private class UniquePair<F, S> {
        final F first;
        final S second;

        private UniquePair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UniquePair)) {
                return false;
            }

            UniquePair<F, S> uniqueDuo = (UniquePair<F, S>) o;

            return first.equals(uniqueDuo.first) && second.equals(uniqueDuo.second);
        }

        @Override
        public int hashCode() {
            int result = first.hashCode();
            result = 31 * result + second.hashCode();
            return result;
        }
    }

}