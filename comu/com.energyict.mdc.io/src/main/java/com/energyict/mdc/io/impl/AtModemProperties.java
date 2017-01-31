/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ModemProperties;

import java.util.List;

/**
 * @author sva
 * @since 23/11/12 (8:53)
 */
public interface AtModemProperties extends ModemProperties {

    /**
     * Getter for the address selector to use
     *
     * @return the address selector to use after a physical connect
     */
    public String getAddressSelector();

    /**
     * Getter for the list of post dial command(s) to use
     *
     * @return the post dial command(s) to execute after a physical connect
     */
    public List<AtPostDialCommand> getPostDialCommands();

}