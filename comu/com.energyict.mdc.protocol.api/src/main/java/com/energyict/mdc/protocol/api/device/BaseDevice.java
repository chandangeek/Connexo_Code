/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.ObisCode;

import java.util.List;

/**
 * Device represents a data logger or energy meter.
 * Each Device has a number of channels to store load profile data.
 * The number of channels is defined by its DeviceType.
 */
public interface BaseDevice<C extends BaseChannel, LP extends BaseLoadProfile<C>, R extends BaseRegister> {

    /**
     * Gets the number that uniquely identifies this Device.
     *
     * @return the id
     */
    long getId();

    /**
     * Gets the receiver's Channels.
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects in ordinal order
     */
    List<C> getChannels();

    /**
     * Gets the device serial number.
     *
     * @return the serial number.
     */
    String getSerialNumber();

    /**
     * Gets the {@link BaseRegister}s defined for this device.
     *
     * @return a List of Register objects
     */
    List<R> getRegisters();

    /**
     * Gets the {@link BaseRegister} with the given obis code which is known by the Device.
     *
     * @param code Obis code to match
     * @return the register or null.
     */
    R getRegisterWithDeviceObisCode(ObisCode code);

    /**
     * Gets the {@link BaseLoadProfile}s defined for this device.
     *
     * @return the LoadProfiles
     */
    List<LP> getLoadProfiles();

    /**
     * Checks if this device is a logical Slave (depends on settings in its device type).
     *
     * @return A flag that indicates if this Device is a logical slave
     */
    public boolean isLogicalSlave();

    /**
     * Gets the Logbooks defined for this Device.
     *
     * @return the LogBooks
     */
    List<? extends BaseLogBook> getLogBooks();

}