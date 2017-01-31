/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.PEMPModemConfiguration;

/**
 * @author sva
 * @since 29/04/13 - 14:19
 */
public interface PEMPModemProperties extends PaknetModemProperties {

    /**
     * Getter for the the PEMP modem configuration to use
     *
     * @return the PEMP modem configuration
     */
    public PEMPModemConfiguration getConfiguration();

}