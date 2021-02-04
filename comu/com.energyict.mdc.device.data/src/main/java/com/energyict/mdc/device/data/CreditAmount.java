/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.common.device.data.Device;

import java.math.BigDecimal;
import java.time.Instant;

public interface CreditAmount {
    long getId();

    Device getDevice();

    String getCreditType();

    BigDecimal getCreditAmount();

    Instant getLastChecked();

    void setLastChecked(Instant lastChecked);

    void save();
}
