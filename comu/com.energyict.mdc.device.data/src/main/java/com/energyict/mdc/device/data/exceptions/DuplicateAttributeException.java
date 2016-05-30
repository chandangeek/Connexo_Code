package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.MessageSeeds;

/**
 * Models the exceptions due to duplicate attributes
 * <p>
 * Copyrights EnergyICT
 * Date: 27.05.16
 * Time: 10:19
 */
public class DuplicateAttributeException extends LocalizedException {
    private DuplicateAttributeException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    /**
     * Creates a proper duplicate exception for the given register
     */
    public static DuplicateAttributeException forRegisterObisCode(Thesaurus thesaurus, Register register) {
        return new DuplicateAttributeException(thesaurus, MessageSeeds.DUPLICATE_REGISTER_OBISCODE, register.getDeviceObisCode());
    }

    /**
     * Creates a proper duplicate exception for the given channel
     */
    public static DuplicateAttributeException forChannelObisCode(Thesaurus thesaurus, Channel channel) {
        return new DuplicateAttributeException(thesaurus, MessageSeeds.DUPLICATE_CHANNEL_OBISCODE, channel.getObisCode());
    }
}
