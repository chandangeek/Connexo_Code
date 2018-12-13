/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

/**
 * Provides functionality to lock a specific Business object
 */
public interface LockService {

    /**
     * Locks and returns the DeviceType based on the given deviceTpeId.
     * The method will block until a lock is provided.
     *
     * @param deviceTypeId the ID of the DeviceType to lock
     * @return the locked DeviceType
     */
    DeviceType lockDeviceType(long deviceTypeId);
}
