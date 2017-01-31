/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolAdapter;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

import aQute.bnd.annotation.ProviderType;

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