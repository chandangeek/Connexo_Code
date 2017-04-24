/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock;

import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocol.HHUEnabler;

public interface HhuEnabledMeterProtocol extends MeterProtocol, HHUEnabler {
}