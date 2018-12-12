/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.tasks.support;

import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.UPLConnectionFunction;

import java.util.Collections;
import java.util.List;

/**
 * Defines the supported {@link UPLConnectionFunction ConnectionFunctions} for this {@link DeviceProtocol}.
 *
 * @author Stijn Vanhoorelbeke
 * @since 19.06.17 - 15:55
 */
public interface DeviceProtocolConnectionFunctionSupport {

    /**
     * Provide the list of possible {@link UPLConnectionFunction}s that are provided by this {@link DeviceProtocolConnectionFunctionSupport}
     * and therefore can be used as {@link UPLConnectionFunction} on connections of device(s) having the given ConnectionFunctionSupport
     * (or in other words having the given DeviceProtocol).<br/>
     * In case no specific connection function(s) are supported, an empty list should be returned.
     *
     * @return the possible ConnectionFunctions that are provided
     */
    default List<UPLConnectionFunction> getProvidedConnectionFunctions() {
        return Collections.emptyList();
    }

    /**
     * Provide the list of possible {@link UPLConnectionFunction}s that can be consumed by this {@link DeviceProtocolConnectionFunctionSupport}
     * and therefore  can be used as {@link UPLConnectionFunction} on communication tasks of device(s) having the given ConnectionFunctionSupport
     * (or in other words having the given DeviceProtocol).<br/>
     * In case no specific connection function(s) are supported, an empty list should be returned.<br/>
     *
     * @return the possible ConnectionFunctions that can be consumed
     */
    default List<UPLConnectionFunction> getConsumableConnectionFunctions() {
        return Collections.emptyList();
    }

}