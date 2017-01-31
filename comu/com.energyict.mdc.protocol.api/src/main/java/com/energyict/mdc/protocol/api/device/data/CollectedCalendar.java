/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

/**
 * A CollectedCalendar identifies a specific Active/Passive Calendar object on a device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-24 (09:43)
 */
public interface CollectedCalendar extends CollectedData, CollectedCalendarInformation {

    /**
     * Gets the {@link DeviceIdentifier} that identifies the device
     * for which calendar information was collected.
     *
     * @return The DeviceIdentifier
     */
    DeviceIdentifier getDeviceIdentifier();

    void setActiveCalendar(String calendarName);

    void setPassiveCalendar(String calendarName);

    void setDataCollectionConfiguration(DataCollectionConfiguration configuration);

}
