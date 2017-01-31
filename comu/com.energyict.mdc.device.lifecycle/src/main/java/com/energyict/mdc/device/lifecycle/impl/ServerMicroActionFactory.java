/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.config.MicroAction;

/**
 * Provides factory services to create the appropriate
 * {@link ServerMicroAction} from a {@link MicroAction}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-23 (10:55)
 */
public interface ServerMicroActionFactory {

    public ServerMicroAction from(MicroAction action);

}