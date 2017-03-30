/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock;

import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

/**
 * Unimplemented interface of {@link MeterProtocol} and {@link RegisterProtocol} to allow mocking
 * the two interfaces at once ...
 *
 * @author gna
 * @since 4/04/12 - 16:19
 */
public interface RegisterSupportedMeterProtocol extends MeterProtocol, RegisterProtocol {
}