/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import java.math.BigDecimal;

public interface SyncDeviceWithKoreMeter {

    String MULTIPLIER_TYPE = "Default";
    BigDecimal MULTIPLIER_ONE = BigDecimal.ONE;

    /**
     * Actions to execute to synchronize the 'Kore' Meter with the configuration of the given DeviceImpl
     *
     * @param device to synchronize its Alter Ego 'Kore Meter' with
     */
    void syncWithKore(DeviceImpl device);

    /**
     * @return true if updating a current Meter Activation is possible, false if
     * the current MeterActivation needs to be ended and a new MeterActivation needs to be created
     */
    boolean canUpdateCurrentMeterActivation();

}