/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannel;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-31 (16:33)
 */
public interface AtPostDialCommand {
    void initAndVerifyCommand();

    void execute(AtModemComponent modemComponent, ComChannel comChannel);

    String getCommand();

}