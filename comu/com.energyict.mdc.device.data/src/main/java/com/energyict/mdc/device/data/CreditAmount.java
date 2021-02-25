/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.common.device.data.Device;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.time.Instant;

@ProviderType
public interface CreditAmount {
    Device getDevice();

    String getCreditType();

    BigDecimal getCreditAmount();

    Instant getFirstChecked();

    Instant getLastChecked();

    void setLastChecked(Instant lastChecked);

    boolean matches(String type, BigDecimal amount);

    void save();
}
