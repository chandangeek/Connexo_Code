package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceProtocolSecuritySupportAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

/**
 * Provides a mapping for the legacy {@link MeterProtocol meterProtocols}
 * and the new {@link DeviceSecuritySupport}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 10:15
 */
public class MeterProtocolSecuritySupportAdapter extends AbstractDeviceProtocolSecuritySupportAdapter {

    public MeterProtocolSecuritySupportAdapter(MeterProtocol meterProtocol, PropertiesAdapter propertiesAdapter) {
        super(propertiesAdapter);
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