/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.MeasurementType;
import com.energyict.mdc.common.masterdata.RegisterGroup;
import com.energyict.mdc.common.masterdata.RegisterType;

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
     * situation that occurs when an attempt is made to delete a {@link MeasurementType}
     * while it is still used by the specified {@link LoadProfileType}s.
     *
     * @param thesaurus The Thesaurus
     * @param measurementType The MeasurementType
     * @return The CannotDeleteBecauseStillInUseException
     */
    public static CannotDeleteBecauseStillInUseException channelTypeIsStillInUseByLoadprofileTypes(Thesaurus thesaurus, MeasurementType measurementType, List<LoadProfileType> loadProfileTypes) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE, measurementType.getReadingType().getAliasName(), namesToStringListForLoadProfileTypes(loadProfileTypes));
    }

    public static CannotDeleteBecauseStillInUseException registerTypeIsStillInUseByRegisterGroup(Thesaurus thesaurus, RegisterType registerType, List<RegisterGroup> registerGroups) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_MAPPING_STILL_USED_BY_REGISTER_GROUP, registerType.getReadingType().getAliasName(), namesToStringListForRegisterGroups(registerGroups));
    }

    private static String namesToStringListForLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
        return loadProfileTypes
                .stream()
                .map(LoadProfileType::getName)
                .collect(Collectors.joining(", "));
    }

    private static String namesToStringListForRegisterGroups(List<RegisterGroup> registerGroups) {
        return registerGroups
                .stream()
                .map(RegisterGroup::getName)
                .collect(Collectors.joining(", "));
    }

    private CannotDeleteBecauseStillInUseException(Thesaurus thesaurus, MessageSeeds messageSeeds, String registerGroupName, String dependentObjectNames) {
        super(thesaurus, messageSeeds, registerGroupName, dependentObjectNames);
        this.set("registerGroupName", registerGroupName);
        this.set("dependentObjectNames", dependentObjectNames);
    }

}