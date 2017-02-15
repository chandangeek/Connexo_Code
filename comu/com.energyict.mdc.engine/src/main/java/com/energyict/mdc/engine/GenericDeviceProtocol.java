/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.protocol.api.DeviceProtocol;

public interface GenericDeviceProtocol extends DeviceProtocol {

    /**
     * Some protocol functionality (usually customer specific) needs knowledge of all pending ComCommands.
     * This method provides the ComCommands, and returns an updated version of the root.
     *
     * @param commandRoot this is a list of all ComCommands that the Comserver will execute
     * @return the updated list of ComCommands. Depending on the protocol implementation, the order can be changed or commands can be removed or added.
     */
    public CommandRoot organizeComCommands(CommandRoot commandRoot);

}