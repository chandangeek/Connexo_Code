package com.energyict.mdc.masterdata.exceptions;

import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MeasurementType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;
import java.util.stream.Collectors;

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
     * situation that occurs when an attempt is made to delete a {@link com.energyict.mdc.masterdata.MeasurementType}
     * while it is still used by the specified {@link LoadProfileType}s.
     *
     * @param thesaurus The Thesaurus
     * @param measurementType The MeasurementType
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException channelTypeIsStillInUseByLoadprofileTypes(Thesaurus thesaurus, MeasurementType measurementType, List<LoadProfileType> loadProfileTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE, measurementType.getReadingType().getAliasName(), namesToStringListForLoadProfileTypes(loadProfileTypes));
    }

    private static String namesToStringListForLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
        return loadProfileTypes
                .stream()
                .map(LoadProfileType::getName)
                .collect(Collectors.joining(", "));
    }

    private CannotDeleteBecauseStillInUseException(Thesaurus thesaurus, MessageSeeds messageSeeds, String registerGroupName, String dependentObjectNames) {
        super(thesaurus, messageSeeds, registerGroupName, dependentObjectNames);
        this.set("registerGroupName", registerGroupName);
        this.set("dependentObjectNames", dependentObjectNames);
    }

}