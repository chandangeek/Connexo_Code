/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Defines whether a protocol class can fetch meterEvents.
 */
public interface MeterProtocolEventSupport {

    /**
     * Get all the meter events from the device starting from the given date.
     *
     * @param lastLogbookDate the date of the last <CODE>MeterEvent</CODE> stored in the database
     * @return a list of <CODE>MeterEvents</CODE>
     * @throws IOException when a logical error occurred
     */
    List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException;

}
