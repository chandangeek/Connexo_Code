package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.Optional;

/**
 * A CollectedCalendar identifies a specific Active/Passive Calendar object on a device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-24 (09:43)
 */
public interface CollectedCalendar extends CollectedData {

    /**
     * Gets the {@link DeviceIdentifier} that identifies the device
     * for which calendar information was collected.
     *
     * @return The DeviceIdentifier
     */
    DeviceIdentifier getDeviceIdentifier();

    /**
     * Gets the name of the active calendar, if that information was collected.
     *
     * @return The name of the active calendar that was collected and if it was collected
     */
    Optional<String> getActiveCalendar();
    void setActiveCalendar(String calendarName);

    /**
     * Gets the name of the passive calendar, if that information was collected.
     *
     * @return The name of the passive calendar that was collected and if it was collected
     */
    Optional<String> getPassiveCalendar();
    void setPassiveCalendar(String calendarName);

    /**
     * Tests if data was collected or not. If that is the case,
     * either {@link #getActiveCalendar()} or {@link #getPassiveCalendar()}
     * will return an non empty Optional.
     *
     * @return <code>false</code> iff data was data collected.
     */
    boolean isEmpty();

    void setDataCollectionConfiguration(DataCollectionConfiguration configuration);

}
