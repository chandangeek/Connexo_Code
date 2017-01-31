/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dialects;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import java.math.BigDecimal;

public abstract class AbstractDeviceProtocolDialect implements DeviceProtocolDialect {

    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final TimeDuration DEFAULT_TIMEOUT = new TimeDuration(10, TimeDuration.TimeUnit.SECONDS);
    public static final TimeDuration DEFAULT_FORCED_DELAY = new TimeDuration(100, TimeDuration.TimeUnit.MILLISECONDS);
    public static final TimeDuration DEFAULT_DELAY_AFTER_ERROR = new TimeDuration(250, TimeDuration.TimeUnit.MILLISECONDS);

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    protected AbstractDeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

}