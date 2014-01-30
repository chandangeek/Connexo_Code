package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;

import java.util.List;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to delete an entity within this bundle while it is still in use
 * by another entity within this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:31)
 */
public class CannotDeleteBecauseStillInUseException extends LocalizedException {

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterGroup}
     * while it is still used by the specified {@link RegisterMapping}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerGroupIsStillInUse(Thesaurus thesaurus, RegisterGroup registerGroup, List<RegisterMapping> registerMappings) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_GROUP_STILL_IN_USE, registerGroup.getName(), namesToStringList(registerMappings));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link ProductSpec}
     * while it is still used by the specified {@link RegisterMapping}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException productSpecIsStillInUse (Thesaurus thesaurus, ProductSpec productSpec, List<RegisterMapping> registerMappings) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_GROUP_STILL_IN_USE, productSpec.getDescription(), namesToStringList(registerMappings));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterMapping}
     * while it is still used by the specified {@link RegisterSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerMappingIsStillInUse (Thesaurus thesaurus, RegisterMapping registerMapping, List<RegisterSpec> registerSpecs) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_GROUP_STILL_IN_USE, registerMapping.getDescription(), namesToStringList(registerSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterMapping}
     * while it is still used by the specified {@link ChannelSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerMappingIsStillInUse (Thesaurus thesaurus, RegisterMapping registerMapping, List<ChannelSpec> channelSpecs) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_GROUP_STILL_IN_USE, registerMapping.getDescription(), namesToStringList(channelSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterMapping}
     * while it is still used by the specified {@link LoadProfileType}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerMappingIsStillInUse (Thesaurus thesaurus, RegisterMapping registerMapping, List<LoadProfileType> loadProfileTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_GROUP_STILL_IN_USE, registerMapping.getDescription(), namesToStringList(loadProfileTypes));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterMapping}
     * while it is still used by the specified {@link DeviceType}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerMappingIsStillInUse (Thesaurus thesaurus, RegisterMapping registerMapping, List<DeviceType> deviceTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_GROUP_STILL_IN_USE, registerMapping.getDescription(), namesToStringList(deviceTypes));
    }

    private static String namesToStringList(List<RegisterMapping> registerMappings) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (RegisterMapping registerMapping : registerMappings) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(registerMapping.getName());
            notFirst = true;
        }
        return builder.toString();
    }

    private static String namesToStringList(List<RegisterSpec> registerSpecs) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (RegisterSpec registerSpec : registerSpecs) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(registerSpec.getName());
            notFirst = true;
        }
        return builder.toString();
    }

    private static String namesToStringList(List<ChannelSpec> channelSpecs) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (ChannelSpec channelSpec : channelSpecs) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(channelSpec.getName());
            notFirst = true;
        }
        return builder.toString();
    }

    private static String namesToStringList(List<LoadProfileType> loadProfileTypes) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(loadProfileType.getName());
            notFirst = true;
        }
        return builder.toString();
    }

    private static String namesToStringList(List<DeviceType> deviceTypes) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (DeviceType deviceType : deviceTypes) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(deviceType.getName());
            notFirst = true;
        }
        return builder.toString();
    }

    private CannotDeleteBecauseStillInUseException(Thesaurus thesaurus, MessageSeeds messageSeeds, String registerGroupName, String dependendObjectNames) {
        super(thesaurus, messageSeeds, registerGroupName, dependendObjectNames);
        this.set("registerGroupName", registerGroupName);
        this.set("dependendObjectNames", dependendObjectNames);
    }

}