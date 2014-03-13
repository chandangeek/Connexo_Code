package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.readings.MeterReading;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.Channel;
import com.energyict.mdc.protocol.api.device.DeviceMultiplier;
import com.energyict.mdc.protocol.api.device.LoadProfile;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 19/12/12
 * Time: 10:35
 */
public interface Device extends BaseDevice<Channel, LoadProfile<Channel>, Register> {

    void save();

    void delete();

    /**
     * Gets the name of the Device
     *
     * @return the name of the Device
     */
    String getName();

    void setName(String name);

    /**
     * Returns the receiver's DeviceType
     *
     * @return the receiver's DeviceType
     */
    public DeviceType getDeviceType();

    @Override
    public Device getPhysicalGateway();

    public Device getCommunicationGateway();

    void setCommunicationGateway(Device device);

    void clearCommunicationGateway();

    List<DeviceMessage> getMessages();

    /**
     * returns The released pending messages for this device
     *
     * @return a List of all messages of this device
     */
    List<DeviceMessage> getMessagesByState(DeviceMessageStatus status);


    /**
     * Returns the {@link DeviceProtocolPluggableClass} configured for this device
     *
     * @return the used {@link DeviceProtocolPluggableClass}
     */
    public DeviceProtocolPluggableClass getDeviceProtocolPluggableClass();

    /**
     * Returns the device configuration of a device
     *
     * @return a device configuration
     */
    DeviceConfiguration getDeviceConfiguration();

    /**
     * Returns the receiver's collection TimeZone.
     * This is the timeZone in which interval data is stored
     * in the database. All eiserver protocols and import modules
     * convert between device time and the configured collection TimeZone
     *
     * @return the receiver's collection TimeZone
     */
    TimeZone getTimeZone();

    void setTimeZone(TimeZone timeZone);

    void setSerialNumber(String serialNumber);

    /**
     * Returns the receiver's last modification date
     *
     * @return the last modification timestamp.
     */
    Date getModDate();

    /**
     * Notification method to signal that something on the device's load profile(s) changed
     */
    void loadProfilesChanged();

    TypedProperties getProtocolProperties();

    /**
     * Stores the given MeterReadings
     *
     * @param meterReading the meterReadings which will be stored
     */
    void store(MeterReading meterReading);

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

    String getExternalName();

    void setExternalName(String externalName);

    /**
     * Returns the channel with the given name or null.
     *
     * @param name the channel name.
     * @return the channel or null.
     */
    Channel getChannel(String name);

    /**
     * Returns the channel with the given index.
     *
     * @param index the zero based index.
     * @return the Channel.
     */
    Channel getChannel(int index);
}