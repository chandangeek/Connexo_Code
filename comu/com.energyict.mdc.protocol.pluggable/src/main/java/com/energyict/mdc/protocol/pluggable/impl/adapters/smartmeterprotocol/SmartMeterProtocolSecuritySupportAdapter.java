package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceProtocolSecuritySupportAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

/**
 * Provides a mapping for the legacy {@link SmartMeterProtocol}s
 * and the new {@link DeviceSecuritySupport}
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 14:45
 */
public class SmartMeterProtocolSecuritySupportAdapter extends AbstractDeviceProtocolSecuritySupportAdapter {

    public SmartMeterProtocolSecuritySupportAdapter(SmartMeterProtocol smartMeterProtocol, PropertiesAdapter propertiesAdapter) {
        super(propertiesAdapter);
        Object securityInstance = createNewSecurityInstance(getDeviceSecuritySupportMappingFor(smartMeterProtocol.getClass().getName()));
        if (DeviceProtocolSecurityCapabilities.class.isAssignableFrom(securityInstance.getClass())) {
            setLegacySecuritySupport((DeviceProtocolSecurityCapabilities) securityInstance);
        }
        if (LegacySecurityPropertyConverter.class.isAssignableFrom(securityInstance.getClass())) {
            setLegacySecurityPropertyConverter((LegacySecurityPropertyConverter) securityInstance);
        }
    }

}