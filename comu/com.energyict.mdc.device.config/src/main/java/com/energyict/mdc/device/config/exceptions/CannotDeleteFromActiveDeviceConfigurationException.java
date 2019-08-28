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

public class CannotDeleteFromActiveDeviceConfigurationException extends LocalizedException {

    private CannotDeleteFromActiveDeviceConfigurationException(Thesaurus thesaurus, MessageSeed messageSeed, Object... arguments) {
        super(thesaurus, messageSeed, arguments);
    }

    /**
     * Creates a new CannotDeleteFromActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LoadProfileSpec} from an
     * <i>active</i> {@link DeviceConfiguration}
     *
     * @param loadProfileSpec     the LoadProfileSpec that you tried to delete
     * @param deviceConfiguration the active DeviceConfiguration
     * @param thesaurus           the Thesaurus
     * @param messageSeed The MessageSeed
     * @return the newly created exception
     */
    public static CannotDeleteFromActiveDeviceConfigurationException forLoadProfileSpec(LoadProfileSpec loadProfileSpec, DeviceConfiguration deviceConfiguration, Thesaurus thesaurus, MessageSeed messageSeed) {
        CannotDeleteFromActiveDeviceConfigurationException cannotDeleteFromActiveDeviceConfigurationException = new CannotDeleteFromActiveDeviceConfigurationException(thesaurus, messageSeed);
        cannotDeleteFromActiveDeviceConfigurationException.set("loadProfileSpec", loadProfileSpec);
        cannotDeleteFromActiveDeviceConfigurationException.set("deviceConfiguration", deviceConfiguration);
        return cannotDeleteFromActiveDeviceConfigurationException;
    }

    /**
     * Creates a new CannotDeleteFromActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LogBookSpec} from an
     * <i>active</i> {@link DeviceConfiguration}
     *
     * @param thesaurus           the Thesaurus
     * @param logBookSpec         the LogBookSpec that you tried to delete
     * @param deviceConfiguration the active DeviceConfiguration
     * @param messageSeed The MessageSeed
     * @return the newly created exception
     */
    public static CannotDeleteFromActiveDeviceConfigurationException forLogbookSpec(Thesaurus thesaurus, LogBookSpec logBookSpec, DeviceConfiguration deviceConfiguration, MessageSeed messageSeed) {
        CannotDeleteFromActiveDeviceConfigurationException cannotDeleteFromActiveDeviceConfigurationException = new CannotDeleteFromActiveDeviceConfigurationException(thesaurus, messageSeed);
        cannotDeleteFromActiveDeviceConfigurationException.set("logBookSpec", logBookSpec);
        cannotDeleteFromActiveDeviceConfigurationException.set("deviceConfiguration", deviceConfiguration);
        return cannotDeleteFromActiveDeviceConfigurationException;
    }

    /**
     * Creates a new CannotDeleteFromActiveDeviceConfigurationException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link ChannelSpec} from an
     * <i>active</i> {@link DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @param channelSpec The ChannelSpec that you tried to delete
     * @param deviceConfiguration The active DeviceConfiguration
     * @param messageSeed The MessageSeed
     * @return The newly created exception
     */
    public static CannotDeleteFromActiveDeviceConfigurationException forChannelSpec(Thesaurus thesaurus, ChannelSpec channelSpec, DeviceConfiguration deviceConfiguration, MessageSeed messageSeed) {
        CannotDeleteFromActiveDeviceConfigurationException cannotDeleteFromActiveDeviceConfigurationException = new CannotDeleteFromActiveDeviceConfigurationException(thesaurus, messageSeed);
        cannotDeleteFromActiveDeviceConfigurationException.set("channelSpec", channelSpec);
        cannotDeleteFromActiveDeviceConfigurationException.set("deviceConfiguration", deviceConfiguration);
        return cannotDeleteFromActiveDeviceConfigurationException;
    }

    /**
     * Creates a new CannotDeleteFromActiveDeviceConfigurationException that models the
     * exceptional situation that occurs when an attempt is made
     * to delete a {@link RegisterSpec} from an
     * <i>active</i> {@link DeviceConfiguration}
     *
     * @param thesaurus The Thesaurus
     * @param deviceConfiguration The DeviceConfiguration
     * @param registerSpec The RegisterSpec
     * @param messageSeed The MessageSeed
     * @return The CannotDeleteFromActiveDeviceConfigurationException
     */
    public static CannotDeleteFromActiveDeviceConfigurationException canNotDeleteRegisterSpec(Thesaurus thesaurus, DeviceConfiguration deviceConfiguration, RegisterSpec registerSpec, MessageSeed messageSeed) {
        CannotDeleteFromActiveDeviceConfigurationException cannotDeleteFromActiveDeviceConfigurationException = new CannotDeleteFromActiveDeviceConfigurationException(thesaurus, messageSeed);
        cannotDeleteFromActiveDeviceConfigurationException.set("registerSpec", registerSpec);
        cannotDeleteFromActiveDeviceConfigurationException.set("deviceConfiguration", deviceConfiguration);
        return cannotDeleteFromActiveDeviceConfigurationException;
    }

}
