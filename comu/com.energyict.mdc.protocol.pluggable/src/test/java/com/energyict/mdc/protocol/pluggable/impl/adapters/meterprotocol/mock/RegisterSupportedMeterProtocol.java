package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock;

import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;

/**
 * Unimplemented interface of {@link MeterProtocol} and {@link RegisterProtocol} to allow mocking
 * the two interfaces at once ...
 *
 * @author gna
 * @since 4/04/12 - 16:19
 */
public interface RegisterSupportedMeterProtocol extends MeterProtocol, RegisterProtocol {
}