/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.protocol;

import com.energyict.mdc.common.pluggable.Pluggable;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;

/**
 * Defines an Interface between the Data Collection System and a Device. The interface can both be
 * used at operational time and at configuration time.
 */
@ConsumerType
public interface DeviceProtocol extends Pluggable, DeviceProtocolDialectSupport,
        DeviceSecuritySupport, ConnectionTypeSupport, com.energyict.mdc.upl.DeviceProtocol {
}