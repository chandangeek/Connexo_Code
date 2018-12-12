/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

/**
 * @author Stijn Vanhoorelbeke
 * @since 18.09.17 - 13:51
 */
public interface ServerRegisterService extends RegisterService, RegisterIdentifier.Finder {
}
