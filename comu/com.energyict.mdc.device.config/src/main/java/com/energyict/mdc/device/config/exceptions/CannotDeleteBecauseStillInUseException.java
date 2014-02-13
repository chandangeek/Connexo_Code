package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.Phenomenon;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;

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
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_GROUP_STILL_IN_USE, registerGroup.getName(), namesToStringListForRegisterMappings(registerMappings));
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
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.PRODUCT_SPEC_STILL_IN_USE, productSpec.getDescription(), namesToStringListForRegisterMappings(registerMappings));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterMapping}
     * while it is still used by the specified {@link RegisterSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerMappingIsStillInUseByRegisterSpecs(Thesaurus thesaurus, RegisterMapping registerMapping, List<RegisterSpec> registerSpecs) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_REGISTER_SPEC, registerMapping.getDescription(), namesToStringListForRegisterSpecs(registerSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterMapping}
     * while it is still used by the specified {@link ChannelSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerMappingIsStillInUseByChannelSpecs(Thesaurus thesaurus, RegisterMapping registerMapping, List<ChannelSpec> channelSpecs) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_CHANNEL_SPEC, registerMapping.getDescription(), namesToStringListForChannelSpecs(channelSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterMapping}
     * while it is still used by the specified {@link LoadProfileType}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerMappingIsStillInUseByLoadprofileTypes(Thesaurus thesaurus, RegisterMapping registerMapping, List<LoadProfileType> loadProfileTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE, registerMapping.getDescription(), namesToStringListForLoadProfileTypes(loadProfileTypes));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterMapping}
     * while it is still used by the specified {@link DeviceType}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerMappingIsStillInUseByDeviceTypes(Thesaurus thesaurus, RegisterMapping registerMapping, List<DeviceType> deviceTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_DEVICE_TYPE, registerMapping.getDescription(), namesToStringListForDeviceTypes(deviceTypes));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LogBookType}
     * while it is still used by the specified {@link LogBookSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException logBookTypeIsStillInUseByLogBookSpec(Thesaurus thesaurus, LogBookType logBookType, List<LogBookSpec> logBookSpecs) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS, logBookType.getName(), namesToStringListForLogBookSpecs(logBookSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LogBookType}
     * while it is still used by the specified {@link LogBookSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerMappingIsStillInUseByRegisterSpec(Thesaurus thesaurus, RegisterMapping registerMapping, List<RegisterSpec> registerSpecs) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS, registerMapping.getName(), namesToStringListForRegisterSpecs(registerSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LoadProfileType}
     * while it is still used by the specified {@link LoadProfileSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException loadProfileTypeIsStillInUseByLoadProfileSpec(Thesaurus thesaurus, LoadProfileType loadProfileType, List<LoadProfileSpec> loadProfileSpecs) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_STILL_IN_USE_BY_LOAD_PROFILE_SPECS, loadProfileType.getName(), namesToStringListForLoadProfileSpecs(loadProfileSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LoadProfileType}
     * while it is still used by the specified {@link DeviceType}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException loadProfileTypeIsStillInUseByDeviceType(Thesaurus thesaurus, LoadProfileType loadProfileType, List<DeviceType> deviceTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_STILL_IN_USE_BY_DEVICE_TYPES, loadProfileType.getName(), namesToStringListForDeviceTypes(deviceTypes));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link DeviceType}
     * while it has active {@link com.energyict.mdc.device.config.DeviceConfiguration}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException deviceTypeIsStillInUse (Thesaurus thesaurus, DeviceType deviceType) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.DEVICE_TYPE_STILL_HAS_ACTIVE_CONFIGURATIONS, deviceType.getName());
    }

    public static CannotDeleteBecauseStillInUseException phenomenonIsStillInUse(Thesaurus thesaurus, Phenomenon phenomenon) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.PHENOMENON_STILL_IN_USE, phenomenon.getName());

    }

    private static String namesToStringListForRegisterMappings(List<RegisterMapping> registerMappings) {
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

    private static String namesToStringListForChannelSpecs(List<ChannelSpec> channelSpecs) {
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

    private static String namesToStringListForLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
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

    private static String namesToStringListForDeviceTypes(List<DeviceType> deviceTypes) {
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

    private static String namesToStringListForLogBookSpecs(List<LogBookSpec> logBookSpecs) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (LogBookSpec logBookSpec : logBookSpecs) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(logBookSpec.getDeviceConfiguration().getName());
            builder.append(":");
            builder.append(logBookSpec.getDeviceObisCode().toString());
            notFirst = true;
        }
        return builder.toString();
    }

    private static String namesToStringListForRegisterSpecs(List<RegisterSpec> registerSpecs) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (RegisterSpec registerSpec : registerSpecs) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(registerSpec.getDeviceConfiguration().getName());
            builder.append(":");
            builder.append(registerSpec.getDeviceObisCode().toString());
            notFirst = true;
        }
        return builder.toString();
    }

    private static String namesToStringListForLoadProfileSpecs(List<LoadProfileSpec> loadProfileSpecs) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (LoadProfileSpec loadProfileSpec : loadProfileSpecs) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(loadProfileSpec.getLoadProfileType().getName());
            notFirst = true;
        }
        return builder.toString();
    }

    private CannotDeleteBecauseStillInUseException(Thesaurus thesaurus, MessageSeeds messageSeeds, String registerGroupName, String dependendObjectNames) {
        super(thesaurus, messageSeeds, registerGroupName, dependendObjectNames);
        this.set("registerGroupName", registerGroupName);
        this.set("dependendObjectNames", dependendObjectNames);
    }

    private CannotDeleteBecauseStillInUseException(Thesaurus thesaurus, MessageSeeds messageSeeds, String deviceTypeName) {
        super(thesaurus, messageSeeds, deviceTypeName);
        this.set("deviceTypeName", deviceTypeName);
    }
}