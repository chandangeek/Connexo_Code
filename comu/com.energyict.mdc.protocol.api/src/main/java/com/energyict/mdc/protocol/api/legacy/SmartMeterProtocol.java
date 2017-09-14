/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy;

import com.energyict.mdc.protocol.api.legacy.dynamic.Pluggable;

/**
 * SmartMeterProtocol is an extension to the standard {@link MeterProtocol} interface.
 * The basic idea is to do more bulk request and adjust our framework to the current smarter meter market.
 */
public interface SmartMeterProtocol extends Pluggable, com.energyict.mdc.upl.SmartMeterProtocol {

}