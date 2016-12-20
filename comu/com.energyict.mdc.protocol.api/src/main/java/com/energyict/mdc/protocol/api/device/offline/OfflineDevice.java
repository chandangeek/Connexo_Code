package com.energyict.mdc.protocol.api.device.offline;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.Offline;
import com.energyict.mdc.upl.offline.OfflineRegister;

import java.util.List;

/**
 * Represents an Offline version of a physical device which should contain all
 * necessary information needed to perform protocolTasks without the need to go to the database.
 *
 * @author gna
 * @since 11/04/12 - 10:01
 */
public interface OfflineDevice extends Offline, com.energyict.mdc.upl.offline.OfflineDevice {

    /**
     * Get a list of all offlineLoadProfiles which are valid for the given device MRID
     *
     * @param mrid the mrid of the device
     * @return a list of offlineLoadProfiles
     */
    List<OfflineLoadProfile> getAllOfflineLoadProfilesForMRID(String mrid);

    /**
     * Get a list of all offlineLogBooks which are valid for the given device MRDi
     *
     * @param mrdi the mrid of the device
     * @return a list of offlineLogBooks
     */
    List<OfflineLogBook> getAllOfflineLogBooksForMRID(String mrdi);

    /**
     * Get a list of <b>ALL</b> {@link OfflineRegister}s which are configured on this {@link OfflineDevice}.
     *
     * @return a list of OfflineRegister
     */
    List<OfflineRegister> getAllRegisters();

    /**
     * Get a list of {@link OfflineRegister}s which are configured on this {@link OfflineDevice}
     * <b>AND</b> are included in one of the given RegisterGroup that are specified by ID.
     *
     * @param registerGroupIds the list ID of RegisterGroup
     * @param mrid the mrid of the device
     * @return a list of {@link OfflineRegister}s filtered according to the given RegisterGroup
     */
    List<OfflineRegister> getRegistersForRegisterGroupAndMRID(List<Long> registerGroupIds, String mrid);

    /**
     * Get the list of all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}s
     * that are for some reason no longer valid to be sent to the device.
     *
     * @return the list of pending messages that have become invalid since creation
     */
    List<OfflineDeviceMessage> getAllInvalidPendingDeviceMessages();

    /**
     * Returns the {@link DeviceProtocolPluggableClass} configured for this device.
     *
     * @return The DeviceProtocolPluggableClass
     */
    DeviceProtocolPluggableClass getDeviceProtocolPluggableClass();

    DeviceIdentifier getDeviceIdentifier();

    List<OfflineRegister> getAllRegistersForMRID(String mrid);

    List<OfflineCalendar> getCalendars();

    boolean touCalendarManagementAllowed();

    boolean firmwareVersionManagementAllowed();

}