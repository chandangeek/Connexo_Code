/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.common.protocol.DeviceSecuritySupport;
import com.energyict.mdc.common.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceProtocolSecuritySupportAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

/**
 * Provides a mapping for the legacy {@link SmartMeterProtocol}s
 * and the new {@link DeviceSecuritySupport}
 * <p>
 * Date: 14/01/13
 * Time: 14:45
 */
public class SmartMeterProtocolSecuritySupportAdapter extends AbstractDeviceProtocolSecuritySupportAdapter {

    public SmartMeterProtocolSecuritySupportAdapter(SmartMeterProtocol smartMeterProtocol, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, PropertiesAdapter propertiesAdapter, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory) {
        super(propertySpecService, protocolPluggableService, propertiesAdapter, securitySupportAdapterMappingFactory);

        String javaClassName;
        if (smartMeterProtocol instanceof UPLProtocolAdapter) {
            javaClassName = ((UPLProtocolAdapter) smartMeterProtocol).getActualClass().getName();
        } else {
            javaClassName = smartMeterProtocol.getClass().getName();
        }

        Object securityInstance = createNewSecurityInstance(getDeviceSecuritySupportMappingFor(javaClassName));
        if (DeviceProtocolSecurityCapabilities.class.isAssignableFrom(securityInstance.getClass())) {
            setLegacySecuritySupport((DeviceProtocolSecurityCapabilities) securityInstance);
        }
        if (LegacySecurityPropertyConverter.class.isAssignableFrom(securityInstance.getClass())) {
            setLegacySecurityPropertyConverter((LegacySecurityPropertyConverter) securityInstance);
        }
    }

}