/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.energyict.mdc.device.data.Device;

import java.math.BigDecimal;

public interface TimeOfUseItem {
    Device getDevice();

    BigDecimal getParentServiceCallId();
}
