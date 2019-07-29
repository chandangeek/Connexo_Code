/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import aQute.bnd.annotation.ConsumerType;
import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ConsumerType
public interface FirmwareCheck {
    TranslationKey CHECK_PREFIX = new SimpleTranslationKey("checkPrefix", "Check: {0}");

    default String getKey() {
        return getClass().getSimpleName();
    }

    default String getTitle(Thesaurus thesaurus) {
        return thesaurus.getFormat(CHECK_PREFIX).format(getName());
    }

    String getName();

    void execute(Optional<FirmwareManagementOptions> options, FirmwareManagementDeviceUtils deviceUtils, FirmwareVersion firmwareVersion) throws FirmwareCheckException;

    @ProviderType
    class FirmwareCheckException extends LocalizedException {
        public FirmwareCheckException(Thesaurus thesaurus, MessageSeed messageSeed) {
            super(thesaurus, messageSeed);
        }

        public FirmwareCheckException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
            super(thesaurus, messageSeed, args);
        }

        public FirmwareCheckException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause) {
            super(thesaurus, messageSeed, cause);
        }

        public FirmwareCheckException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... args) {
            super(thesaurus, messageSeed, cause, args);
        }
    }
}
