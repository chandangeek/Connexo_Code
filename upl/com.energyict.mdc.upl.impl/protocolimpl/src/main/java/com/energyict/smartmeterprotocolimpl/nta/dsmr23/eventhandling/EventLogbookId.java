package com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling;

/**
 * Defines a summary of eventLogbook ID's
 */
public enum EventLogbookId {

    StandardEventLogbook(0),
    DisconnectControlLogbook(1),
    PowerFailureEventLogbook(2),
    FraudDetectionEventLogbook(3),
    MbusEventLogbook(4),
    MbusControlLogbook(5),
    UnknownEventLogook(-1);

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
