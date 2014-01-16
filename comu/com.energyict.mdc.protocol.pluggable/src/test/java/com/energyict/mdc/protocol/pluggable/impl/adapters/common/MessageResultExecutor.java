package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.protocol.api.device.data.MessageResult;

import java.io.IOException;

/**
 * Provides functionality to perform an action when a MessageResult is expected.
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/03/13
 * Time: 15:35
 */
public interface MessageResultExecutor {

    /**
     * Perform the actual action to return a MessageResult
     *
     * @return the result of the action
     * @throws IOException whenever you want
     */
    public MessageResult performMessageResult() throws IOException;

}