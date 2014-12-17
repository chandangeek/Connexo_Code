package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.time.Clock;
import java.time.Instant;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Performs several actions on the given LogBook which are required before storing
 *
 * Copyrights EnergyICT
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
     *     <li>Filter future dates</li>
     *     <li>Filter duplicates</li>
     *     <li>Calculate lastlogbook date</li>
     * </ul>
     *
     * @param deviceLogBook the collected events from the device
     * @return the preStored logbook
     */
    public LocalLogBook preStore(CollectedLogBook deviceLogBook) {
        Set<UniqueDuo<String, Date>> uniqueCheck = new HashSet<>();
        OfflineLogBook offlineLogBook = this.comServerDAO.findOfflineLogBook(deviceLogBook.getLogBookIdentifier());

        List<EndDeviceEvent> filteredEndDeviceEvents = new ArrayList<>();
        Instant lastLogbook = null;
        Instant currentDate = this.clock.instant();
        for (EndDeviceEvent endDeviceEvent : MeterDataFactory.createEndDeviceEventsFor(deviceLogBook, offlineLogBook.getLogBookId())) {
            if(uniqueCheck.add(new UniqueDuo<>(endDeviceEvent.getMRID(), Date.from(endDeviceEvent.getCreatedDateTime())))) {
                if (!endDeviceEvent.getCreatedDateTime().isAfter(currentDate)) {
                    filteredEndDeviceEvents.add(endDeviceEvent);
                    if (lastLogbook == null || endDeviceEvent.getCreatedDateTime().isAfter(lastLogbook)) {
                        lastLogbook = endDeviceEvent.getCreatedDateTime();
                    }
                }
            }
        }
        return new LocalLogBook(filteredEndDeviceEvents, lastLogbook == null ? null : Date.from(lastLogbook));
    }

    class LocalLogBook {

        private final List<EndDeviceEvent> endDeviceEvents;
        private final Date lastLogbook;

        private LocalLogBook(List<EndDeviceEvent> endDeviceEvents, Date lastLogBook) {
            this.endDeviceEvents = endDeviceEvents;
            this.lastLogbook = lastLogBook;
        }

        public List<EndDeviceEvent> getEndDeviceEvents() {
            return endDeviceEvents;
        }

        public Date getLastLogbook() {
            return lastLogbook;
        }

    }

    private class UniqueDuo<F,S>{
        final F first;
        final S second;

        private UniqueDuo(F first, S second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UniqueDuo)) {
                return false;
            }

            UniqueDuo<F,S> uniqueDuo = (UniqueDuo<F,S>) o;

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
