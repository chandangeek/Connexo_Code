/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock;

import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

/**
 * Unimplemented interface of {@link MeterProtocol} and {@link MessageProtocol} to allow mocking
 * the two interfaces at once ...
 *
 * @author gna
 * @since 5/04/12 - 11:37
 */
public interface MessageSupportedMeterProtocol extends MeterProtocol, MessageProtocol {
}