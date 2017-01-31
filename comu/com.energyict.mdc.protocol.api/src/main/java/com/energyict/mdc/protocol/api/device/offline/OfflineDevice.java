/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.offline;

import com.elster.jupiter.orm.MacException;
import com.energyict.mdc.common.Offline;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Represents an Offline version of a physical device which should contain all
 * necessary information needed to perform protocolTasks without the need to go to the database.
 *
 * @author gna
 * @since 11/04/12 - 10:01
 */
public interface OfflineDevice extends Offline {

    /**
     * Get the ID of the device.
     *
     * @return the ID
     */
    long getId();

    /**
     * Get the device TimeZone.
     * @return the device TimeZone
     */
    TimeZone getTimeZone();

    /**
     * Returns the SerialNumber of the device.
     *
     * @return the SerialNumber
     */
    String getSerialNumber();

    /**
     * Get all the properties of an device and his {@link DeviceProtocolPluggableClass}.
     *
     * @return all properties
     */
    TypedProperties getAllProperties();

    /**
     * Get a list of all the slave devices of this device in their {@link Offline} representation.
     *
     * @return a list of slave devices
     */
    List<OfflineDevice> getAllSlaveDevices();

    /**
     * Get a list of {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice}.<br/>
     * <b>Be aware that the returned list will NOT contain {@link OfflineLoadProfile offlineLoadProfiles} which are owned by slave devices</b>,
     * for this you should use {@link #getAllOfflineLoadProfiles}
     *
     * @return a list of {@link OfflineLoadProfile offlineLoadProfiles}
     */
    List<OfflineLoadProfile> getMasterOfflineLoadProfiles();

    /**
     * Get a list of <b>ALL</b> {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice} <b>AND</b>
     * his slave devices.
     *
     * @return a list of {@link OfflineLoadProfile offlineLoadProfiles}
     */
    List<OfflineLoadProfile> getAllOfflineLoadProfiles();

    /**
     * Get a list of all offlineLoadProfiles which are valid for the given device MRID
     *
     * @param mrid the mrid of the device
     * @return a list of offlineLoadProfiles
     */
    List<OfflineLoadProfile> getAllOfflineLoadProfilesForMRID(String mrid);

    /**
     * Get a list of <b>ALL</b> {@link OfflineLogBook offlineLoagBooks} which are owned by this {@link OfflineDevice}.
     *
     * @return a list of {@link OfflineLogBook offlineLoagBooks}
     */
    List<OfflineLogBook> getAllOfflineLogBooks();

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
     * Get the list of all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}s.
     *
     * @return the list of pending messages
     */
    List<OfflineDeviceMessage> getAllPendingDeviceMessages();

    /**
     * Get the list of all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}s
     * that are for some reason no longer valid to be sent to the device.
     *
     * @return the list of pending messages that have become invalid since creation
     */
    List<OfflineDeviceMessage> getAllInvalidPendingDeviceMessages();

    /**
     * Get the list of all {@link DeviceMessageStatus#SENT sent} {@link OfflineDeviceMessage}s.
     *
     * @return the list of sent messages
     */
    List<OfflineDeviceMessage> getAllSentDeviceMessages();

    /**
     * Returns the {@link DeviceProtocolPluggableClass} configured for this device.
     *
     * @return The DeviceProtocolPluggableClass
     */
    DeviceProtocolPluggableClass getDeviceProtocolPluggableClass();

    /**
     * Get the {@link DeviceProtocolCache} linked to this Device.
     *
     * @return the used DeviceProtocolCache
     */
    DeviceProtocolCache getDeviceProtocolCache();

    DeviceIdentifier<?> getDeviceIdentifier();

    List<OfflineRegister> getAllRegistersForMRID(String mrid);

    List<OfflineCalendar> getCalendars();

    boolean touCalendarManagementAllowed();

    boolean firmwareVersionManagementAllowed();

    Optional<MacException> getMacException();

}