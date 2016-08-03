/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.DeviceEstimation;

/**
 * Adds behavior to {@link DeviceEstimation} that is reserved
 * for server-side components only.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-25 (09:25)
 */
interface ServerDeviceEstimation extends DeviceEstimation {
    void delete();
}