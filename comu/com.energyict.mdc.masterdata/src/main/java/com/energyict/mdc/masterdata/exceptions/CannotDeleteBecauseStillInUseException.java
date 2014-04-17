package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;

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
     * while it is still used by the specified {@link RegisterMapping}s.
     *
     * @param thesaurus The Thesaurus
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException registerGroupIsStillInUse(Thesaurus thesaurus, RegisterGroup registerGroup, List<RegisterMapping> registerMappings) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_GROUP_STILL_IN_USE, registerGroup.getName(), namesToStringListForRegisterMappings(registerMappings));
    }

    /**
     * Creates a new CannotDeleteBecauseStillInUseException that models the exceptional
     * situation that occurs when an attempt is made to delete a {@link RegisterMapping}
     * while it is still used by the specified {@link LoadProfileType}s.
     *
     * @param thesaurus The Thesaurus
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException registerMappingIsStillInUseByLoadprofileTypes(Thesaurus thesaurus, RegisterMapping registerMapping, List<LoadProfileType> loadProfileTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE, registerMapping.getName(), namesToStringListForLoadProfileTypes(loadProfileTypes));
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