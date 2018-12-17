/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import java.math.BigDecimal;

public interface TimeOfUseItem {
    String getDeviceName();

    BigDecimal getParentServiceCallId();
}
