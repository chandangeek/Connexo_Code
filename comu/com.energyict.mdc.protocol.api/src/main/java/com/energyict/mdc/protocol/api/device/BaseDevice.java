package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.CanGoPartiallyOffline;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;

import java.util.List;

/**
 * Device represents a data logger or energy meter.
 * Each Device has a number of channels to store load profile data.
 * The number of channels is defined by its DeviceType.
 */
public interface BaseDevice<C extends Channel, LP extends LoadProfile<C>, R extends BaseRegister> extends CanGoPartiallyOffline<OfflineDevice, OfflineDeviceContext> {

    /**
     * Returns number that uniquely identifies this DeviceType.
     *
     * @return the id
     */
    long getId();

    /**
     * Returns the receiver's Channels
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects in ordinal order
     */
    List<C> getChannels();

    /**
     * Returns the device serial number.
     *
     * @return the serial number.
     */
    String getSerialNumber();

    /**
     * Returns the {@link BaseRegister}s defined for this device.
     *
     * @return a List of Register objects
     */
    List<R> getRegisters();

    /**
     * returns the {@link BaseRegister} with the given obis code which is known by the Device
     *
     * @param code Obis code to match
     * @return the register or null.
     */
    R getRegisterWithDeviceObisCode(ObisCode code);

    /**
     * Returns this device's gateway device.
     *
     * @return the device used as a gateway or null if none is assigned
     */
    BaseDevice getPhysicalGateway();

    /**
     * update the gateway for this device to the specified gateway
     *
     * @param gateway the new gateway device
     */
    void setPhysicalGateway(BaseDevice gateway);

    /**
     * Clears the current physical gateway, if any
     */
    void clearPhysicalGateway();

    /**
     * Returns the list of Devices which are <i>Physically</i> connected to this device.
     *
     * @return the list of physically connected Devices
     */
    List<BaseDevice> getDownstreamDevices();

    /**
     * returns the {@link LoadProfile}s defined for this device.
     *
     * @return the LoadProfiles
     */
    List<LP> getLoadProfiles();

    /**
     * Checks if this device is a logical Slave (depends on settings in its device type)
     * @return
     */
    public boolean isLogicalSlave();

    /**
     * return the Logbooks defined for this rtu
     *
     * @return the LogBooks
     */
    List<LogBook> getLogBooks();

}