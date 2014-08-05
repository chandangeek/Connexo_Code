package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;

import java.util.List;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to delete an entity within this bundle while it is still in use
 * by another entity within this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (14:01)
 */
public class CannotDeleteBecauseStillInUseException extends LocalizedException {

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterGroup}
     * while it is still used by the specified {@link com.energyict.mdc.masterdata.MeasurementType}s.
     *
     * @param thesaurus The Thesaurus
     * @param registerTypes
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException registerGroupIsStillInUse(Thesaurus thesaurus, RegisterGroup registerGroup, List<RegisterType> registerTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_GROUP_STILL_IN_USE, registerGroup.getName(), namesToStringListForRegisterTypes(registerTypes));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link com.energyict.mdc.masterdata.MeasurementType}
     * while it is still used by the specified {@link LoadProfileType}s.
     *
     * @param thesaurus The Thesaurus
     * @param measurementType
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException channelTypeIsStillInUseByLoadprofileTypes(Thesaurus thesaurus, MeasurementType measurementType, List<LoadProfileType> loadProfileTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE, measurementType.getName(), namesToStringListForLoadProfileTypes(loadProfileTypes));
    }

    private static String namesToStringListForRegisterTypes(List<RegisterType> registerTypes) {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (MeasurementType measurementType : registerTypes) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(measurementType.getName());
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