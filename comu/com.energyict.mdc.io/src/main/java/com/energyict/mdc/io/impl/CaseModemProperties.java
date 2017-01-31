/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ModemProperties;

/**
 * @author sva
 * @since 30/04/13 - 13:22
 */
public interface CaseModemProperties extends ModemProperties {

    /**
     * Getter for the address selector to use
     *
     * @return the address selector to use after a physical connect
     */
    public String getAddressSelector();

}