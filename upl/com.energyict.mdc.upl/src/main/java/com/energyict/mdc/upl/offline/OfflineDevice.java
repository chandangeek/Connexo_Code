package com.energyict.mdc.upl.offline;

import com.energyict.mdc.identifiers.*;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.LoadProfileType;
import com.energyict.mdc.upl.meterdata.RegisterGroup;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.TimeZone;

/**
 * Represents an Offline version of a Device which should contain all necessary information needed to perform
 * protocol tasks} without the need to go to the database.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-14 (16:42)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface OfflineDevice extends Offline {

    /**
     * Get the ID of the Device
     *
     * @return the ID
     */
    @XmlAttribute
    long getId();

    /**
     * Get the device TimeZone
     *
     * @return the device TimeZone
     */
    @XmlAttribute
    TimeZone getTimeZone();

    /**
     * Returns the SerialNumber of the device
     *
     * @return the SerialNumber
     */
    @XmlAttribute
    String getSerialNumber();

    @XmlElement(name = "mRID")
    String getmRID();

    /**
     * Returns the external name of the device
     *
     * @return the external name
     */
    String getExternalName();

    /**
     * Returns the location of the device
     *
     * @return the location
     */
    @XmlAttribute
    String getLocation();

    /**
     * Returns the usage point of the device
     *
     * @return the usage point
     */
    @XmlAttribute
    String getUsagePoint();

    /**
     * Get all the properties of the Device and the protocol to communicate with the device.
     *
     * @return all properties
     */
    @XmlElement
    TypedProperties getAllProperties();

    /**
     * Get a list of all the slave devices of this Device in their {@link Offline} representation
     *
     * @return a list of slave devices
     */
    @XmlAttribute
    List<? extends OfflineDevice> getAllSlaveDevices();

    /**
     * Get a list of {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice}.<br/>
     * <b>Be aware that the returned list will NOT contain {@link OfflineLoadProfile offlineLoadProfiles} which are owned by slave devices</b>,
     * for this you should use {@link #getAllOfflineLoadProfiles}
     *
     * @return a list of {@link OfflineLoadProfile offlineLoadProfiles}
     */
    @XmlAttribute
    List<OfflineLoadProfile> getMasterOfflineLoadProfiles();

    /**
     * Get a list of <b>ALL</b> {@link OfflineLoadProfile offlineLoadProfiles} which are owned by this {@link OfflineDevice} <b>AND</b>
     * his slave devices.
     *
     * @return a list of {@link OfflineLoadProfile offlineLoadProfiles}
     */
    @XmlAttribute
    List<OfflineLoadProfile> getAllOfflineLoadProfiles();

    /**
     * Get a list of <b>ALL</b> {@link OfflineLogBook}s which are owned by this {@link OfflineDevice}.
     *
     * @return a list of OfflineLogBook
     */
    @XmlAttribute
    List<OfflineLogBook> getAllOfflineLogBooks();

    /**
     * Get a list of <b>ALL</b> {@link OfflineRegister}s which are configured on this {@link OfflineDevice}.
     *
     * @return a list of Register
     */
    @XmlAttribute
    List<OfflineRegister> getAllOfflineRegisters();

    /**
     * Get a list of {@link OfflineRegister}s which are configured on this {@link OfflineDevice} <b>AND</b> are included in
     * one of the specified {@link com.energyict.mdc.upl.meterdata.RegisterGroup}s.
     *
     * @param rtuRegisterGroups the list of RegisterGroup
     * @return The List of OfflineRegister
     */
    List<OfflineRegister> getRegistersForRegisterGroup(List<RegisterGroup> rtuRegisterGroups);

    /**
     * Get the list of all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}
     *
     * @return the list of pending messages
     */
    @XmlAttribute
    List<OfflineDeviceMessage> getAllPendingDeviceMessages();

    /**
     * Get the list of all {@link DeviceMessageStatus#SENT sent} {@link OfflineDeviceMessage}
     *
     * @return the list of sent messages
     */
    @XmlAttribute
    List<OfflineDeviceMessage> getAllSentDeviceMessages();

    /**
     * Get a list of {@link com.energyict.mdc.upl.meterdata.LoadProfile}s which are configured on this {@link OfflineDevice} <b>AND</b> are included in
     * one of the specified LoadProfileType s.
     *
     * @param loadProfileTypes the list of LoadProfileType
     * @return The list of OfflineLoadProfile
     */
    List<OfflineLoadProfile> getLoadProfilesForLoadProfileTypes(List<LoadProfileType> loadProfileTypes);

    /**
     * Get the {@link DeviceProtocolCache} linked to this Device
     *
     * @return the used {@link DeviceProtocolCache}
     */
    DeviceProtocolCache getDeviceProtocolCache();

    @XmlElements(
            {@XmlElement(type = DeviceIdentifierById.class), @XmlElement(type = DeviceIdentifierBySerialNumber.class), @XmlElement(type = DeviceIdentifierForAlreadyKnownDevice.class)}
    )
    DeviceIdentifier getDeviceIdentifier();

    List<OfflineCalendar> getCalendars();

    boolean touCalendarManagementAllowed();

    boolean firmwareVersionManagementAllowed();

    @XmlElement(name = "type")
    default String getXmlType() {
        return getClass().getName();
    }

    default void setXmlType(String ignore) {
    }

    /**
     * Get the list of all {@link DeviceMessageStatus#PENDING pending} {@link OfflineDeviceMessage}s
     * that are for some reason no longer valid to be sent to the device.
     *
     * @return the list of pending messages that have become invalid since creation
     */
    List<OfflineDeviceMessage> getAllInvalidPendingDeviceMessages();

}