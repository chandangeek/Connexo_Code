/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.upl.UPLConnectionFunction;

/**
 * Connexo specific implementation of the {@link UPLConnectionFunction} interface,
 * adding methods for localization
 *
 * @author Stijn Vanhoorelbeke
 * @since 23.06.17 - 14:56
 */
public interface ConnectionFunction extends UPLConnectionFunction {

    /**
     * The display name of this ConnectionFunction
     *
     * @return the name the User will see for this ConnectionFunction
     */
    String getConnectionFunctionDisplayName();

}