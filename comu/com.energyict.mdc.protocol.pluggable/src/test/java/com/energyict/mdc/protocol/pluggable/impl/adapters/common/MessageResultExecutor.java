/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.protocol.api.device.data.MessageResult;

import java.io.IOException;

public interface MessageResultExecutor {

    /**
     * Perform the actual action to return a MessageResult
     *
     * @return the result of the action
     * @throws IOException whenever you want
     */
    public MessageResult performMessageResult() throws IOException;

}