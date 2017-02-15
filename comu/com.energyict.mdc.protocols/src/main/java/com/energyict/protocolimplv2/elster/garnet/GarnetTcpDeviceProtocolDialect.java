/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Models a DeviceProtocolDialect for the TCP connection type of a Garnet protocol.
 *
 * author: khe
 * since: 16/10/12 (113:25)
 */
public class GarnetTcpDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final BigDecimal DEFAULT_RETRIES = BigDecimal.ZERO;
    public static final TimeDuration DEFAULT_TIMEOUT = new TimeDuration(10, TimeDuration.TimeUnit.SECONDS);
    public static final TimeDuration DEFAULT_FORCED_DELAY = new TimeDuration(0, TimeDuration.TimeUnit.MILLISECONDS);
    public static final TimeDuration DEFAULT_DELAY_AFTER_ERROR = new TimeDuration(100, TimeDuration.TimeUnit.MILLISECONDS);

    public GarnetTcpDeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.GARNET_TCP.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.GARNET_TCP).format();
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new GarnetTcpDeviceProtocolDialectCustomPropertySet(this.getThesaurus(), this.getPropertySpecService()));
    }

}