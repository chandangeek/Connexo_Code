/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.LoadProfileSpec;
import com.energyict.mdc.common.device.config.LogBookSpec;
import com.energyict.mdc.common.device.config.RegisterSpec;

public class CannotAddToActiveDeviceConfigurationException extends LocalizedException {

    private CannotAddToActiveDeviceConfigurationException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    /**
     * Creates a new CannotAddToActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to add a {@link LoadProfileSpec}
     * to an <i>active</i> {@link DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotAddToActiveDeviceConfigurationException aNewLoadProfileSpec(Thesaurus thesaurus, MessageSeed messageSeed) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, messageSeed);
    }

    /**
     * Creates a new CannotAddToActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to add a {@link ChannelSpec}
     * to an <i>active</i> {@link DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotAddToActiveDeviceConfigurationException aNewChannelSpec(Thesaurus thesaurus, MessageSeed messageSeed) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, messageSeed);
    }

    /**
     * Creates a new CannotAddToActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to add a {@link LogBookSpec}
     * to an <i>active</i> {@link DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotAddToActiveDeviceConfigurationException aNewLogBookSpec(Thesaurus thesaurus, MessageSeed messageSeed) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, messageSeed);
    }

    /**
     * Creates a new CannotAddToActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to add a {@link RegisterSpec}
     * to an <i>active</i> {@link DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @return The CannotAddToActiveDeviceConfigurationException
     */
    public static CannotAddToActiveDeviceConfigurationException aNewRegisterSpec(Thesaurus thesaurus, MessageSeed messageSeed) {
        return new CannotAddToActiveDeviceConfigurationException(thesaurus, messageSeed);
    }

}