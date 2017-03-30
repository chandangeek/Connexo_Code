/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;

public class StillGatewayException extends LocalizedException {

    private StillGatewayException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    /**
     * Creates a StillGatewayException what models the fact that an attempt was made to delete
     * a Device which is still linked as Physical gateway for some devices
     *
     * @param thesaurus The Thesaurus
     * @param gateway The gateway Device that was tried to be deleted
     * @param messageSeed The MessageSeed
     * @param slaves The Devices which still use the gateway as physical gateway
     * @return the newly created exception
     */
    public static StillGatewayException forPhysicalGateway(Thesaurus thesaurus, Device gateway, MessageSeed messageSeed, Device... slaves) {
        return new StillGatewayException(thesaurus, messageSeed, gateway.getName(), getSlaveNames(slaves));
    }

    private static String getSlaveNames(Device[] slaves) {
        StringBuilder slaveNames = new StringBuilder();
        for (Device slave : slaves) {
            if (slave.getId() != slaves[0].getId()) {
                slaveNames.append(", ");
            }
            slaveNames.append(slave.getName());
        }
        return slaveNames.toString();
    }
}
