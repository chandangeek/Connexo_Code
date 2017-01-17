package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceProtocolSecuritySupportAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

/**
 * Provides a mapping for the legacy {@link SmartMeterProtocol}s
 * and the new {@link DeviceSecuritySupport}
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 14:45
 */
public class SmartMeterProtocolSecuritySupportAdapter extends AbstractDeviceProtocolSecuritySupportAdapter {

    public SmartMeterProtocolSecuritySupportAdapter(SmartMeterProtocol smartMeterProtocol, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, PropertiesAdapter propertiesAdapter, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory) {
        super(propertySpecService, protocolPluggableService, propertiesAdapter, securitySupportAdapterMappingFactory);
        Object securityInstance = createNewSecurityInstance(getDeviceSecuritySupportMappingFor(smartMeterProtocol.getClass().getName()));
        if (DeviceProtocolSecurityCapabilities.class.isAssignableFrom(securityInstance.getClass())) {
            setLegacySecuritySupport((DeviceProtocolSecurityCapabilities) securityInstance);
        }
        if (LegacySecurityPropertyConverter.class.isAssignableFrom(securityInstance.getClass())) {
            setLegacySecurityPropertyConverter((LegacySecurityPropertyConverter) securityInstance);
        }
    }

}