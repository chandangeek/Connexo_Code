/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;

import java.util.List;

/**
 * Adds behavior to {@link OfflineDevice} that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-20 (12:40)
 */
public interface ServerOfflineDevice extends OfflineDevice {
    /**
     * Get a list of {@link OfflineRegister}s which are configured on this {@link OfflineDevice}
     * <b>AND</b> are included in one of the given RegisterGroup that are specified by ID.
     *
     * @param deviceRegisterGroupIds the list ID of RegisterGroup
     * @param mrid                   the mrid of the device
     * @return a list of {@link OfflineRegister}s filtered according to the given RegisterGroup
     */
    List<OfflineRegister> getRegistersForRegisterGroupAndMRID(List<Long> deviceRegisterGroupIds, String mrid);
}