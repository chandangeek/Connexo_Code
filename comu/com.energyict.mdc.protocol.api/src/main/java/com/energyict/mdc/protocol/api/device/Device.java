package com.energyict.mdc.protocol.api.device;

import com.elster.jupiter.metering.readings.MeterReading;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.CanGoPartiallyOffline;
import com.energyict.mdc.common.NamedPropertyBusinessObject;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Protectable;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.relation.DefaultRelationParticipant;
import com.energyict.mdc.protocol.api.device.data.MeterData;
import com.energyict.mdc.protocol.api.device.data.MeterReadingData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Device represents a data logger or energy meter.
 * Each Device has a number of channels to store load profile data.
 * The number of channels is defined by its DeviceType.
 */
public interface Device<C extends Channel, LP extends LoadProfile<C>, R extends Register> extends NamedPropertyBusinessObject, Protectable, DefaultRelationParticipant, CanGoPartiallyOffline<OfflineDevice, OfflineDeviceContext> {

    /**
     * Returns the id of the Device's type object.
     *
     * @return the DeviceType id.
     */
    int getDeviceTypeId();

    /**
     * Returns the receiver's collection TimeZone.
     * This is the timeZone in which interval data is stored
     * in the database. All eiserver protocols and import modules
     * convert between device time and the configured collection TimeZone
     *
     * @return the receiver's collection TimeZone
     */
    TimeZone getTimeZone();

    /**
     * Returns the receiver's Channels
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects in ordinal order
     */
    List<C> getChannels();

    /**
     * Returns all receiver's Channels that are not (yet) assigned to a {@link LoadProfile}.
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects
     */
    List<C> getNonAssignedChannels();

    /**
     * Returns the device serial number.
     *
     * @return the serial number.
     */
    String getSerialNumber();

    /**
     * return the date of the last logbook entry (meter event)
     * for this device.
     *
     * @return the date of the last logbook entry
     */
    java.util.Date getLastLogbook();

    /**
     * return the end time of the last interval read from the device.
     * An device has a number of LoadProfiles, each Load profile has its own lastreading.
     * This method return the lastreading of the LoadProfile referenced by the first channel, otherwise null
     *
     * @return end time of the last interval.
     * @deprecated
     */
    @Deprecated
    java.util.Date getLastReading();


    /**
     * Updates the last event date if the argument is later than
     * the current last event date.
     *
     * @param execDate the new last event date
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    void updateLastLogbookIfLater(java.util.Date execDate) throws SQLException, BusinessException;

    /**
     * Updates the last event date.
     *
     * @param execDate the new last event date
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    void updateLastLogbook(java.util.Date execDate) throws SQLException, BusinessException;

    /**
     * Updates the total number of suspects register readings on this device
     *
     * @throws SQLException
     */
    void updateNumberOfSuspectReadings() throws BusinessException, SQLException;

    /**
     * Updates the total number of suspect intervals on this device
     *
     * @throws SQLException
     */
    void updateNumberOfSuspectIntervals() throws BusinessException, SQLException;

    /**
     * Updates the number of failed schedules on this device
     *
     * @throws SQLException
     */
    void updateNumberOfFailedSchedules() throws SQLException;

    /**
     * Updates the number of active alarms on this device
     *
     * @throws SQLException
     */
    void updateNumberOfActiveAlarms() throws SQLException, BusinessException;

    /**
     * Stores the argument's interval data
     *
     * @param profileData interval data container
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    void store(ProfileData profileData) throws SQLException, BusinessException;

    /**
     * Stores the argument's interval data
     *
     * @param profileData      interval data container
     * @param checkLastReading if true, do not store intervals before the device's lastReading
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    void store(ProfileData profileData, boolean checkLastReading) throws SQLException, BusinessException;

    /**
     * stores the argument's meter readings
     *
     * @param meterReadingData meter readings container
     * @throws SQLException      if a database exception occurred
     * @throws BusinessException if a business exception occurred
     */
    void store(MeterReadingData meterReadingData) throws SQLException, BusinessException;

    /**
     * Stores the interval data and meterreading data contained in the meterdata argument
     *
     * @param meterData        meterData data container
     * @param checkLastReading boolean checkLastReading
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    void store(MeterData meterData, boolean checkLastReading) throws SQLException, BusinessException;

    /**
     * Stores the interval data and meterreading data contained in the meterdata argument
     *
     * @param meterData meterData data container
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    void store(MeterData meterData) throws SQLException, BusinessException;

    /**
     * Returns the version of the receiver's interval storage schema
     *
     * @return storage version number
     */
    int getStoreVersion();

    /**
     * Returns a boolean indicating whether the receiver's time zone
     * using daylight saving time.
     *
     * @return true if the receiver's time zone uses DST, false otherwise.
     */
    boolean useDaylightTime();

    /**
     * Returns the receiver's full name.
     * Equivalent to getFolder().getFullName() + "/" + getName().
     *
     * @return the full name.
     */
    String getFullName();

    /**
     * Returns the channel with the given name or null.
     *
     * @param name the channel name.
     * @return the channel or null.
     */
    C getChannel(String name);

    /**
     * Returns the channel with the given index.
     *
     * @param index the zero based index.
     * @return the Channel.
     */
    C getChannel(int index);

    /**
     * Returns a list of devices that reference this device
     *
     * @return a List of devices
     */
    List<Device> getReferencingDevices();

    /**
     * Returns a list of devices that use the receiver as a gateway
     */
    List<Device> getDownstreamDevices();

    /**
     * Returns the {@link Register}s defined for this device.
     *
     * @return a List of Register objects
     */
    List<R> getRegisters();

    /**
     * returns the {@link Register} with the given obis code.
     *
     * @param code Obis code to match
     * @return the register or null.
     */
    R getRegister(ObisCode code);

    /**
     * Returns the receiver's last modification date
     *
     * @return the last modification timestamp.
     */
    Date getModDate();

    /**
     * Returns true if the receiver is a gateway device.
     *
     * @return true if the receiver is a gateway device, false if not
     */
    boolean isGateway();

    /**
     * Returns this device's gateway device.
     *
     * @return the device used as a gateway or null if none is assigned
     */
    Device getGateway();

    /**
     * Returns this device's gateway device on a certain date.
     *
     * @return the device used as a gateway or null if none is assigned
     */
    Device getGateway(Date when);

    /**
     * update the gateway for this device to the specified gateway
     *
     * @param gateway the new gateway device
     * @throws BusinessException if a business exception occurred
     * @throws SQLException      if a database error occurred
     */
    void updateGateway(Device gateway) throws SQLException, BusinessException;

    void updateGateway(Device gateway, Date from, Date to) throws BusinessException, SQLException;

    /**
     * returns the {@link LoadProfile}s defined for this device.
     *
     * @return the LoadProfiles
     */
    List<LP> getLoadProfiles();

    /**
     * Updates the last reading if the argument is later than
     * the current last reading.
     *
     * @param execDate the new last reading.
     * @throws SQLException      if a database exception occurred
     * @throws BusinessException if a business exception occurred
     * @deprecated
     */
    @Deprecated
    void updateLastReadingIfLater(java.util.Date execDate) throws SQLException, BusinessException;

    /**
     * Updates the last reading.
     *
     * @param execDate the new last reading
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     * @deprecated
     */
    @Deprecated
    void updateLastReading(java.util.Date execDate) throws SQLException, BusinessException;

    public int getNumberOfSuspectIntervals();

    public int getNumberOfSuspectReadings();

    public int getNumberOfAlarms();

    /**
     * Notification method to signal that something on the device's load profile(s) changed
     */
    void loadProfilesChanged() throws BusinessException, SQLException;

    TypedProperties getProtocolProperties();

    /*
     * Checks if this device can be used as gateway device (depends on settings in its device type)
     */
    public boolean canBeGateway();

    public boolean isLogicalSlave();

    /**
     * Stores the given MeterReadings
     *
     * @param meterReading the meterReadings which will be stored
     */
    public void store(MeterReading meterReading);

    /**
     * Gets a list of all device multipliers that were active for a device
     *
     * @return a list of device multipliers
     */
    List<DeviceMultiplier> getDeviceMultipliers();

    /**
     * Gets the active device multiplier for a certain date.
     *
     * @param date
     * @return a device multiplier
     */
    DeviceMultiplier getDeviceMultiplier(Date date);

    /**
     * Gets the active device multiplier at the moment the method is called
     *
     * @return a device multiplier
     */
    DeviceMultiplier getDeviceMultiplier();

    /**
     * return the Logbooks defined for this rtu
     *
     * @return the LogBooks
     */
    List<LogBook> getLogBooks();

}