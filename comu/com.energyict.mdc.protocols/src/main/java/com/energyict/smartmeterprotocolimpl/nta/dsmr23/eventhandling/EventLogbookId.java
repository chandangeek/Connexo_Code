package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling;

/**
 * Defines a summary of eventLogbook ID's
 */
public enum EventLogbookId {

    StandardEventLogbook(1),
    DisconnectControlLogbook(2),
    PowerFailureEventLogbook(3),
    FraudDetectionEventLogbook(4),
    MbusEventLogbook(5),
    MbusControlLogbook(6),
    UnknownEventLogbook(0);

    /**
     * The logbook ID of the different EventLogBooks
     */
    private final int logbookId;

    EventLogbookId(final int logbookId) {
        this.logbookId = logbookId;
    }

    /**
     * @return the eventLogId for this Logbook
     */
    public int eventLogId(){
        return this.logbookId;
    }
}
