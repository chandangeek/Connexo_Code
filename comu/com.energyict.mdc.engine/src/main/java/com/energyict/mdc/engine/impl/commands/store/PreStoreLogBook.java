/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.util.Pair;
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

/**
 * Performs several actions on the given LogBook which are required before storing.
 * <p>
 *
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
     * <li>Filter future dates</li>
     * <li>Filter duplicates</li>
     * <li>Calculate lastlogbook date</li>
     * </ul>
     *
     * @param deviceLogBook the collected events from the device
     * @return the preStored logbook
     */
    public Optional<Pair<DeviceIdentifier, LocalLogBook>> preStore(CollectedLogBook deviceLogBook) {
        Set<UniqueTrio<String, String, Instant>> uniqueCheck = new HashSet<>();
        Optional<OfflineLogBook> offlineLogBook = this.comServerDAO.findOfflineLogBook(deviceLogBook.getLogBookIdentifier());
        if (offlineLogBook.isPresent()) {
            List<EndDeviceEvent> filteredEndDeviceEvents = new ArrayList<>();
            Instant lastLogbook = null;
            Instant currentDate = this.clock.instant();
            for (EndDeviceEvent endDeviceEvent : MeterDataFactory.createEndDeviceEventsFor(deviceLogBook, offlineLogBook.get().getLogBookId())) {
                if(uniqueCheck.add(new UniqueTrio<>(endDeviceEvent.getEventTypeCode(), endDeviceEvent.getType(), endDeviceEvent.getCreatedDateTime()))) {
                    if (!endDeviceEvent.getCreatedDateTime().isAfter(currentDate.plus(1, ChronoUnit.DAYS))) {
                        filteredEndDeviceEvents.add(endDeviceEvent);
                        if (lastLogbook == null || endDeviceEvent.getCreatedDateTime().isAfter(lastLogbook)) {
                            lastLogbook = endDeviceEvent.getCreatedDateTime();
                        }
                    }
                }
            }

            DeviceIdentifier deviceIdentifier = comServerDAO.getDeviceIdentifierFor(deviceLogBook.getLogBookIdentifier());
            return Optional.of(Pair.of(deviceIdentifier, new LocalLogBook(filteredEndDeviceEvents, lastLogbook)));
        } else {
            return Optional.empty();
        }
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

    private class UniqueTrio<F,S,T>{
        final F first;
        final S second;
        final T third;

        private UniqueTrio(F first, S second, T third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UniqueTrio)) {
                return false;
            }

            UniqueTrio<F,S, T> uniqueDuo = (UniqueTrio<F,S,T>) o;

            return first.equals(uniqueDuo.first) && second.equals(uniqueDuo.second) && third.equals(uniqueDuo.third);

        }

        @Override
        public int hashCode() {
            int result = first.hashCode();
            result = 31 * result + second.hashCode();
            return result;
        }
    }


}