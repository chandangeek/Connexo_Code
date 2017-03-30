/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.iskra.mt880.events;

/**
 * Defines a summary of eventLogbook ID's
 */
public enum EventLogbookId {

    StandardEventLog(1),
    FraudDetectionLog(2),
    PowerQualityLog(3),
    PowerDownEventLog(4),
    CommunicationEventLog(5),
    McoTcoEventLog(6),
    MagneticTamperEventLog(7),
    PowerFailureEventLog(8),
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
