/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceProtocolSecuritySupportAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;

public class MeterProtocolSecuritySupportAdapter extends AbstractDeviceProtocolSecuritySupportAdapter {

    public MeterProtocolSecuritySupportAdapter(MeterProtocol meterProtocol, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, PropertiesAdapter propertiesAdapter, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory) {
        super(propertySpecService, protocolPluggableService, propertiesAdapter, securitySupportAdapterMappingFactory);
        boolean matchingTypeFound = false;
        Object securityInstance = createNewSecurityInstance(getDeviceSecuritySupportMappingFor(meterProtocol.getClass().getName()));
        if (DeviceProtocolSecurityCapabilities.class.isAssignableFrom(securityInstance.getClass())) {
            setLegacySecuritySupport((DeviceProtocolSecurityCapabilities) securityInstance);
            matchingTypeFound=true;
        }
        if (LegacySecurityPropertyConverter.class.isAssignableFrom(securityInstance.getClass())) {
            setLegacySecurityPropertyConverter((LegacySecurityPropertyConverter) securityInstance);
            matchingTypeFound=true;
        }
        assert matchingTypeFound: "Expected "+securityInstance.getClass()+" to implement at least one of "
                +DeviceProtocolSecurityCapabilities.class.getSimpleName()+" or "
                +LegacySecurityPropertyConverter.class.getSimpleName();
    }

}