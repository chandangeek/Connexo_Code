/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.tasks.support;

import com.energyict.mdc.io.ComChannel;

/**
 * Provides functionality to do LogOn/LogOff with a device.
 * In some situations multiple Logical or Physical devices are read one after the other.
 * These can require other behavior then the normal LogOn/LogOff
 */
public interface DeviceAccessSupport {

    /**
     * Perform a Protocol 'logOn'.<br/>
     * For certain meters this means a SignOn/Connect has to take place, for others nothing needs to be done.
     * The idea is that after this call, data can be fetched from a specific device using this ProtocolType.
     */
    void logOn();

    /**
     * Performs a Protocol 'logOn' <b>after</b> a previous device has been read out. This method is only
     * called by the ComServer if daisy-chained devices are called at the same time and another device
     * has previously been handled. If this is the first device, then the normal {@link #logOn()} will be called.<br/>
     * <p/>
     * Protocol Implementers should be aware that the other {@link #logOn()} will not be called for this device
     * by the ComServer framework. If that needs to be done, you should do this in your own implementation.
     */
    void daisyChainedLogOn();


    /**
     * Perform a Protocol 'logOff'.<br/>
     * For certain meters this means a SignOff/Disconnect has to take place, for others nothing needs to be done.
     * The idea is that after his call, the meter is aware that we will not make any new request and later on the
     * {@link ComChannel} can close the actual connection with the device.
     */
    void logOff();

    /**
     * Performs a Protocol 'logOff' <b>before</b> a next device will be read out. This method is only
     * called by the ComServer if daisy-chained devices are called at the same time and another device
     * is yet to be handled. If this is the last device, then the normal {@link #logOff()} will be called.<br/>
     * <p/>
     * Protocol Implementers should be aware that the other {@link #logOff()} will not be called for this device
     * by the ComServer framework. If that needs to be done, you should do this in your own implementation.
     */
    void daisyChainedLogOff();

}
