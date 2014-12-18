package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;

/**
 * Generate exceptions for situations where an identifier could not find a corresponding object based on
 * the parameters in the identifier
 *
 * Copyrights EnergyICT
 * Date: 12/18/14
 * Time: 1:15 PM
 */
public class CanNotFindForIdentifier extends ComServerRuntimeException {

    private CanNotFindForIdentifier(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public static CanNotFindForIdentifier device(DeviceIdentifier deviceIdentifier){
        return new CanNotFindForIdentifier(MessageSeeds.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER, deviceIdentifier);
    }

    public static CanNotFindForIdentifier loadProfile(LoadProfileIdentifier loadProfileIdentifier){
        return new CanNotFindForIdentifier(MessageSeeds.CAN_NOT_FIND_FOR_LOADPROFILE_IDENTIFIER, loadProfileIdentifier);
    }

    public static CanNotFindForIdentifier logBook(LogBookIdentifier logBookIdentifier){
        return new CanNotFindForIdentifier(MessageSeeds.CAN_NOT_FIND_FOR_LOGBOOK_IDENTIFIER, logBookIdentifier);
    }

    public static CanNotFindForIdentifier message(MessageIdentifier messageIdentifier){
        return new CanNotFindForIdentifier(MessageSeeds.CAN_NOT_FIND_FOR_MESSAGE_IDENTIFIER, messageIdentifier);
    }


}
