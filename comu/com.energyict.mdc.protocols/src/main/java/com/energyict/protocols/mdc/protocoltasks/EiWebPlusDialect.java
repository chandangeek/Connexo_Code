/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Optional;

/**
 * Models a DeviceProtocolDialect for usage with the RTU+Server and EIWebPlus.
 */
public class EiWebPlusDialect extends AbstractDeviceProtocolDialect {

    public static final String SERVER_LOG_LEVER_PROPERTY = "ServerLogLevel";
    public static final String PORT_LOG_LEVEL_PROPERTY = "PortLogLevel";
    public static final String DEFAULT_LOG_LEVEL = "INFO";

    public EiWebPlusDialect(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectName.EIWEBPLUS.getName();
    }

    @Override
    public String getDisplayName() {
        return this.getThesaurus().getFormat(DeviceProtocolDialectName.EIWEBPLUS).format();
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new EiWebPlusDialectCustomPropertySet(this.getThesaurus(), this.getPropertySpecService()));
    }

}