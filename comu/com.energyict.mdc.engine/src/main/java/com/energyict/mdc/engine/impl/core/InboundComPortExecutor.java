/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

public interface InboundComPortExecutor {

    /**
     * Handles the inbound call.
     *
     * @param comChannel the ComChannel the inbound call has set up
     */
    void execute(ComPortRelatedComChannel comChannel);

}
