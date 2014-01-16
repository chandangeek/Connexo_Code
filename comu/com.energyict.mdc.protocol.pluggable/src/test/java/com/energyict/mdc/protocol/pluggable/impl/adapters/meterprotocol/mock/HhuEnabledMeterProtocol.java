package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock;

import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

/**
 * Unimplemented interface of {@link MeterProtocol} and {@link HHUEnabler} to allow mocking
 * the two interfaces at once ...
 *
 * Copyrights EnergyICT
 * Date: 24/08/12
 * Time: 12:58
 */
public interface HhuEnabledMeterProtocol extends MeterProtocol, HHUEnabler {
}