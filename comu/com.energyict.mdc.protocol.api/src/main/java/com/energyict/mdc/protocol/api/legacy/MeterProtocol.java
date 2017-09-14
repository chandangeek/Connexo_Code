/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy;

import com.energyict.mdc.protocol.api.legacy.dynamic.Pluggable;

/**
 * Adds configuration support to the {@link com.energyict.mdc.upl.MeterProtocol} interface.
 * <p>
 * At configuration time, the getRequiredKeys, getOptionalKeys and setProperties methods
 * can be called in any sequence </p><p>
 * <p>
 * During normal operations the data collection system will first set the configured properties
 * and will then call the methods of the com.energyict.mdc.upl.MeterProtocol interface as defined.
 *
 * @author Karel
 *         KV 15122003 serialnumber of the device
 */
public interface MeterProtocol extends Pluggable, com.energyict.mdc.upl.cache.CachingProtocol, com.energyict.mdc.upl.MeterProtocol {

}