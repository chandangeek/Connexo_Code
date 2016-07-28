package com.energyict.mdc.protocol.pluggable;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolAdapter;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

/**
 * Adapter between a {@link SmartMeterProtocol} and a {@link DeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-01 (13:05)
 */
@ProviderType
public interface SmartMeterProtocolAdapter extends DeviceProtocol, DeviceProtocolAdapter {

    SmartMeterProtocol getSmartMeterProtocol();

}