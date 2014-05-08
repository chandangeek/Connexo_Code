package com.energyict.mdc.engine;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * In order to have some custom functionally available in the protocol, we add an extra method.
 * This properly replaces the functionality of the old 'generic' protocols
 * <p/>
 * The idea is that the protocol implementer can use this method to reorganize the ComCommands of the root and add Commands if necessary
 * <p/>
 * Copyrights EnergyICT
 * Date: 29/01/13
 * Time: 13:26
 * Author: khe
 */
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