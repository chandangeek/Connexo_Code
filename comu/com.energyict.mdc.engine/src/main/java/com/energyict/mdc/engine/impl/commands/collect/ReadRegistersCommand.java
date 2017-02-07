/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import java.util.List;

/**
 * Command to the requested registers from a Device
 *
 * @author gna
 * @since 18/06/12 - 13:16
 */
public interface ReadRegistersCommand extends ComCommand {

    /**
     * Add a List of {@link OfflineRegister} which need to be collected from the device
     *
     * @param registersToCollect the registers to collect
     */
    public void addRegisters (final List<OfflineRegister> registersToCollect);

    /**
     * Getter for the {@link CompositeComCommand} that is the owner of this {@link ReadRegistersCommand}
     * @return
     */
    public CompositeComCommand getCommandOwner();

}
