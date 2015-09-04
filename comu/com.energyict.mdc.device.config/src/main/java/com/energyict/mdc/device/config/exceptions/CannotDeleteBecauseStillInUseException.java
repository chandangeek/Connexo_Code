package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MeasurementType;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.List;
import java.util.stream.Collectors;

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
     * @param messageSeed The MessageSeed
     * @param thesaurus The Thesaurus
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException registerTypeIsStillInUseByRegisterSpecs(Thesaurus thesaurus, MeasurementType measurementType, List<RegisterSpec> registerSpecs, MessageSeed messageSeed) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, messageSeed, measurementType.getReadingType().getAliasName(), namesToStringListForRegisterSpecs(registerSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link com.energyict.mdc.masterdata.MeasurementType}
     * while it is still used by the specified {@link ChannelSpec}s.
     *
     * @param messageSeed The MessageSeed
     * @param thesaurus The Thesaurus
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException channelTypeIsStillInUseByChannelSpecs(Thesaurus thesaurus, MeasurementType measurementType, List<ChannelSpec> channelSpecs, MessageSeed messageSeed) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, messageSeed, measurementType.getReadingType().getAliasName(), namesToStringListForChannelSpecs(channelSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link com.energyict.mdc.masterdata.MeasurementType}
     * while it is still used by the specified {@link DeviceType}s.
     *
     * @param thesaurus The Thesaurus
     * @param messageSeed The MessageSeed
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException registerTypeIsStillInUseByDeviceTypes(Thesaurus thesaurus, MeasurementType measurementType, List<DeviceType> deviceTypes, MessageSeed messageSeed) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, messageSeed, measurementType.getReadingType().getAliasName(), namesToStringListForDeviceTypes(deviceTypes));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LogBookType}
     * while it is still used by the specified {@link LogBookSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @param messageSeed The MessageSeed
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException logBookTypeIsStillInUseByLogBookSpec(Thesaurus thesaurus, LogBookType logBookType, List<LogBookSpec> logBookSpecs, MessageSeed messageSeed) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, messageSeed, logBookType.getName(), namesToStringListForLogBookSpecs(logBookSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LoadProfileType}
     * while it is still used by the specified {@link LoadProfileSpec}s.
     *
     * @param thesaurus The Thesaurus
     * @param messageSeed The MessageSeed
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException loadProfileTypeIsStillInUseByLoadProfileSpec(LoadProfileType loadProfileType, List<LoadProfileSpec> loadProfileSpecs, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, messageSeed, loadProfileType.getName(), namesToStringListForLoadProfileSpecs(loadProfileSpecs));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link LoadProfileType}
     * while it is still used by the specified {@link DeviceType}s.
     *
     * @param thesaurus The Thesaurus
     * @param messageSeed The MessageSeed
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException loadProfileTypeIsStillInUseByDeviceType(LoadProfileType loadProfileType, List<DeviceType> deviceTypes, Thesaurus thesaurus, MessageSeed messageSeed) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, messageSeed, loadProfileType.getName(), namesToStringListForDeviceTypes(deviceTypes));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link DeviceType}
     * while it has active {@link com.energyict.mdc.device.config.DeviceConfiguration}s.
     *
     * @param messageSeed The MessageSeed
     * @param thesaurus The Thesaurus
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException deviceTypeIsStillInUse(Thesaurus thesaurus, DeviceType deviceType, MessageSeed messageSeed) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, messageSeed, deviceType.getName());
    }

    private static String namesToStringListForChannelSpecs(List<ChannelSpec> channelSpecs) {
        return channelSpecs
                .stream()
                .map(ChannelSpec::getReadingType)
                .map(ReadingType::getAliasName)
                .collect(Collectors.joining(", "));
    }

    private static String namesToStringListForDeviceTypes(List<DeviceType> deviceTypes) {
        return deviceTypes.stream().map(DeviceType::getName).collect(Collectors.joining(", "));
    }

    private static String namesToStringListForLogBookSpecs(List<LogBookSpec> logBookSpecs) {
        return logBookSpecs
                .stream()
                .map(CannotDeleteBecauseStillInUseException::toString)
                .collect(Collectors.joining(", "));
    }

    private static String toString(LogBookSpec logBookSpec) {
        return logBookSpec.getDeviceConfiguration().getName() + ":" + logBookSpec.getDeviceObisCode().toString();
    }

    private static String toString(RegisterSpec registerSpec) {
        return "register configuration with Obis code " + registerSpec.getDeviceObisCode().toString() + " in device configuration " + registerSpec.getDeviceConfiguration().getName();
    }

    private static String namesToStringListForRegisterSpecs(List<RegisterSpec> registerSpecs) {
        return registerSpecs
                .stream()
                .map(CannotDeleteBecauseStillInUseException::toString)
                .collect(Collectors.joining(", "));
    }

    private static String namesToStringListForLoadProfileSpecs(List<LoadProfileSpec> loadProfileSpecs) {
        return loadProfileSpecs
                .stream()
                .map(LoadProfileSpec::getLoadProfileType)
                .map(HasName::getName)
                .collect(Collectors.joining(", "));
    }

    private CannotDeleteBecauseStillInUseException(Thesaurus thesaurus, MessageSeed messageSeed, String registerGroupName, String dependendObjectNames) {
        super(thesaurus, messageSeed, registerGroupName, dependendObjectNames);
        this.set("registerGroupName", registerGroupName);
        this.set("dependendObjectNames", dependendObjectNames);
    }

    private CannotDeleteBecauseStillInUseException(Thesaurus thesaurus, MessageSeed messageSeed, String deviceTypeName) {
        super(thesaurus, messageSeed, deviceTypeName);
        this.set("deviceTypeName", deviceTypeName);
    }

}