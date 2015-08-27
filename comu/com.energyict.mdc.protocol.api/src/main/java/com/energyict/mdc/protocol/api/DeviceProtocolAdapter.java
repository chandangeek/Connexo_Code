package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexEnabler;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.legacy.DeviceCachingSupport;
import com.energyict.mdc.protocol.api.legacy.CachingProtocol;
import com.energyict.mdc.protocol.api.tasks.support.UsesLegacyMessageConverter;

import java.util.logging.Logger;

/**
 * Provides additional functionality for protocol adapters to smoothly integrate a
 * {@link DeviceProtocol}
 * with a {@link MeterProtocol} or
 * {@link SmartMeterProtocol}
 * <p/>
 * This adapter will also provide functionality to forward the
 * {@link HalfDuplexEnabler} and {@link HHUEnabler}
 * functionality to the corresponding Legacy protocol.
 * <p/>
 * <p>
 * As it is meant to support Legacy protocols, the DeviceProtocolAdapters needs
 * to implement the UsesLegacyMessageConverter
 * </p>
 *
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 8:41
 */
public interface DeviceProtocolAdapter extends HHUEnabler, CachingProtocol, DeviceCachingSupport, DeviceSecuritySupport, UsesLegacyMessageConverter {

    /**
     * Initialize the logger which will be used by the legacy protocols
     *
     * @param logger the given logger
     */
    public void initializeLogger(final Logger logger);

    public DeviceProtocolSecurityPropertySet getLegacyTypedPropertiesAsSecurityPropertySet(TypedProperties typedProperties);

}