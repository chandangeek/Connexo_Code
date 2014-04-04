package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.readings.MeterReading;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.DeviceMultiplier;
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
public interface Device extends BaseDevice<Channel, LoadProfile, Register>, HasId {

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

    /**
     * Get the Device which is used for <i>communication</i><br>
     * <i>Note that this can be another device than the physical gateway</i>
     *
     * @return the Device which is used to communicate with the HeadEnd
     */
    public Device getCommunicationGateway();

    /**
     * Set the device which will be used to communicate with the HeadEnd.<br>
     *
     * @param device the communication gateway for this device
     */
    void setCommunicationGateway(Device device);

    /**
     * Remove the current link to the communication gateway
     */
    void clearCommunicationGateway();

    /**
     * Gets the list of Devices which are reference to this Device for Communication.
     * This means that for each returned Device, the {@link #getCommunicationGateway()}
     * will return this Device for the current timestamp.
     *
     * @return the list of Devices which are currently linked to this Device for communication
     */
    List<BaseDevice<Channel, LoadProfile, Register>> getCommunicationReferencingDevices();

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

    LogBook.LogBookUpdater getLogBookUpdaterFor(LogBook logBook);

    LoadProfile.LoadProfileUpdater getLoadProfileUpdaterFor(LoadProfile loadProfile);

    TypedProperties getDeviceProtocolProperties();

    void setProperty(String name, Object value);

    void removeProperty(String name);

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
    BaseChannel getChannel(String name);

    ScheduledConnectionTask createScheduledConnectionTask(PartialOutboundConnectionTask partialConnectionTask);
}