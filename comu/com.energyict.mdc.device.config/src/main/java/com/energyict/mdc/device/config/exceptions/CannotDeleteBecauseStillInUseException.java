package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MeasurementType;

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
     * situation that occurs when an attempt is made to delete a {@link com.energyict.mdc.masterdata.MeasurementType}
     * while it is still used by the specified {@link RegisterSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException registerTypeIsStillInUseByRegisterSpecs(Thesaurus thesaurus, MeasurementType measurementType, List<RegisterSpec> registerSpecs) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_TYPE_STILL_USED_BY_REGISTER_SPEC, measurementType.getReadingType().getAliasName(), namesToStringListForRegisterSpecs(registerSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link com.energyict.mdc.masterdata.MeasurementType}
     * while it is still used by the specified {@link ChannelSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException channelTypeIsStillInUseByChannelSpecs(Thesaurus thesaurus, MeasurementType measurementType, List<ChannelSpec> channelSpecs) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.CHANNEL_TYPE_STILL_USED_BY_CHANNEL_SPEC, measurementType.getReadingType().getAliasName(), namesToStringListForChannelSpecs(channelSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link com.energyict.mdc.masterdata.MeasurementType}
     * while it is still used by the specified {@link DeviceType}s.
     *
     * @param thesaurus The Thesaurus
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException registerTypeIsStillInUseByDeviceTypes(Thesaurus thesaurus, MeasurementType measurementType, List<DeviceType> deviceTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_TYPE_STILL_USED_BY_DEVICE_TYPE, measurementType.getReadingType().getAliasName(), namesToStringListForDeviceTypes(deviceTypes));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LogBookType}
     * while it is still used by the specified {@link LogBookSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException logBookTypeIsStillInUseByLogBookSpec(Thesaurus thesaurus, LogBookType logBookType, List<LogBookSpec> logBookSpecs) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS, logBookType.getName(), namesToStringListForLogBookSpecs(logBookSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LoadProfileType}
     * while it is still used by the specified {@link LoadProfileSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @return The CannotDeleteBecauseStillInUseException
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
     * @return The CannotDeleteBecauseStillInUseException
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
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException deviceTypeIsStillInUse (Thesaurus thesaurus, DeviceType deviceType) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.DEVICE_TYPE_STILL_HAS_ACTIVE_CONFIGURATIONS, deviceType.getName());
    }

    private static String namesToStringListForChannelSpecs(List<ChannelSpec> channelSpecs) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (ChannelSpec channelSpec : channelSpecs) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(channelSpec.getReadingType().getAliasName());
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
            builder.append("register configuration with Obis code ");
            builder.append(registerSpec.getDeviceObisCode().toString());
            builder.append(" in device configuration ");
            builder.append(registerSpec.getDeviceConfiguration().getName());
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