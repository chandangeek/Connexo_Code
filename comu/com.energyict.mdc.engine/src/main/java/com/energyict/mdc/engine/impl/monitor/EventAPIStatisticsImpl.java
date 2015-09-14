package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.List;

/**
 * Provides an implementation for the {@link EventAPIStatistics} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-05 (13:16)
 */
public class EventAPIStatisticsImpl extends CanConvertToCompositeDataSupport implements EventAPIStatistics {

    public static final String NUMBER_OF_CLIENTS_ITEM_NAME = "numberOfClients";
    private static final String NUMBER_OF_CLIENTS_ITEM_DESCRIPTION = "number of clients";
    public static final String NUMBER_OF_EVENTS_ITEM_NAME = "numberOfEvents";
    private static final String NUMBER_OF_EVENTS_ITEM_DESCRIPTION = "number of events";

    private int numberOfClients;
    private long numberOfEvents;

    @Override
    public synchronized void reset () {
        this.numberOfEvents = 0;
    }

    @Override
    public int getNumberOfClients () {
        return numberOfClients;
    }

    public void setNumberOfClients (int numberOfClients) {
        this.numberOfClients = numberOfClients;
    }

    @Override
    public synchronized void clientRegistered () {
        /* Make sure this is properly guarded because clients
         * register via the internal jetty server
         * and that uses or may use different threads
         * for every http request. */
        this.numberOfClients++;
    }

    @Override
    public synchronized void clientUnregistered () {
        /* Make sure this is properly guarded because clients
         * register via the internal jetty server
         * and that uses or may use different threads
         * for every http request. */
        this.numberOfClients--;
    }

    @Override
    public long getNumberOfEvents () {
        return numberOfEvents;
    }

    public void setNumberOfEvents (long numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    @Override
    public synchronized void eventWasPublished () {
        /* Make sure this is properly guarded because event
         * are being published from many different threads. */
        this.numberOfEvents++;
    }

    public CompositeType getCompositeType () {
        try {
            return new CompositeType(
                    this.getClass().getSimpleName(),
                    "Event API statistics",
                    new String[]{NUMBER_OF_CLIENTS_ITEM_NAME, NUMBER_OF_EVENTS_ITEM_NAME},
                    new String[]{NUMBER_OF_CLIENTS_ITEM_DESCRIPTION, NUMBER_OF_EVENTS_ITEM_DESCRIPTION},
                    new OpenType[]{SimpleType.INTEGER, SimpleType.LONG});
        }
        catch (OpenDataException e) {
            throw CodingException.compositeTypeCreation(this.getClass(), e, MessageSeeds.COMPOSITE_TYPE_CREATION);
        }
    }

    @Override
    protected void initializeAccessors (List<CompositeDataItemAccessor> accessors) {
        accessors.add(new CompositeDataItemAccessor(NUMBER_OF_CLIENTS_ITEM_NAME, this::getNumberOfClients));
        accessors.add(new CompositeDataItemAccessor(NUMBER_OF_EVENTS_ITEM_NAME, this::getNumberOfEvents));
    }

}