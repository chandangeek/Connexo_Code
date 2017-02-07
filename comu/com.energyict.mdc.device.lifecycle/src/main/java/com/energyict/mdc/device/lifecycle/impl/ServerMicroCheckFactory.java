/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.config.MicroCheck;

/**
 * Provides factory services to create the appropriate
 * {@link ServerMicroCheck} from a {@link MicroCheck}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-23 (10:55)
 */
public interface ServerMicroCheckFactory {

    public ServerMicroCheck from(MicroCheck check);

}