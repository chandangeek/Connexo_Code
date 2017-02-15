/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

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
 * Models a DeviceProtocolDialect for a Serial connection type.
 *
 * @author khe
 * @since 16/10/12 (113:25)
 */
public class SerialDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final BigDecimal DEFAULT_RETRIES = new BigDecimal(3);
    public static final BigDecimal DEFAULT_INFORMATION_FIELD_SIZE = BigDecimal.valueOf(128L);
    public static final BigDecimal DEFAULT_ADDRESSING_MODE = BigDecimal.valueOf(2L);
    public static final BigDecimal DEFAULT_ROUND_TRIP_CORRECTION = BigDecimal.ZERO;
    public static final TimeDuration DEFAULT_TIMEOUT = new TimeDuration(10, TimeDuration.TimeUnit.SECONDS);
    public static final TimeDuration DEFAULT_FORCED_DELAY = new TimeDuration(100, TimeDuration.TimeUnit.MILLISECONDS);
    public static final TimeDuration DEFAULT_DELAY_AFTER_ERROR = new TimeDuration(100, TimeDuration.TimeUnit.MILLISECONDS);

    public static final BigDecimal[] PREDEFINED_VALUES_ADDRESSING_MODE = {BigDecimal.ONE, DEFAULT_ADDRESSING_MODE, BigDecimal.valueOf(4L)};

    public SerialDeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new SerialDeviceProtocolDialectCustomPropertySet(this.getThesaurus(), this.getPropertySpecService()));
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.SERIAL.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.SERIAL).format();
    }

}