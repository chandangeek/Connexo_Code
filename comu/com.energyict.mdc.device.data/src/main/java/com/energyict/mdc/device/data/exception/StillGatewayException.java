package com.energyict.mdc.device.data.exception;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;

/**
 * Models the exceptional situation that occurs when an attempt is made to try and delete a Device
 * which is still used as some sort of gateway...
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/17/14
 * Time: 1:26 PM
 */
public class StillGatewayException extends LocalizedException {

    private StillGatewayException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    /**
     * Creates a StillGatewayException what models the fact that an attempt was made to delete
     * a Device which is still linked as Physical gateway for some devices
     *
     * @param thesaurus the used Thesaurus
     * @param gateway   the gateway Device that was tried to be deleted
     * @param slaves    the Devices which still use the gateway as physical gateway
     * @return the newly created exception
     */
    public static StillGatewayException forPhysicalGateway(Thesaurus thesaurus, Device gateway, Device... slaves) {
        return new StillGatewayException(thesaurus, MessageSeeds.DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY, gateway.getName(), getSlaveNames(slaves));
    }

    /**
     * Creates a StillGatewayException what models the fact that an attempt was made to delete
     * a Device which is still linked as Communication gateway for some devices
     *
     * @param thesaurus the used Thesaurus
     * @param gateway   the gateway Device that was tried to be deleted
     * @param slaves    the Devices which still use the gateway as communication gateway
     * @return the newly created exception
     */
    public static StillGatewayException forCommunicationGateway(Thesaurus thesaurus, Device gateway, Device... slaves) {
        return new StillGatewayException(thesaurus, MessageSeeds.DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY, gateway.getName(), getSlaveNames(slaves));
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
