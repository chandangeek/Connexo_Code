package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock;

import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

/**
 * Unimplemented interface of {@link SmartMeterProtocol} and {@link HHUEnabler} to allow mocking
 * the two interfaces at once ...
 *
 * Copyrights EnergyICT
 * Date: 24/08/12
 * Time: 13:10
 */
public interface HhuEnabledSmartMeterProtocol extends SmartMeterProtocol, HHUEnabler {

}
