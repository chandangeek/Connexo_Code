package com.energyict.mdc.device.config;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

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
     * while it is still in use the specified {@link RegisterMapping}s.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static CannotDeleteBecauseStillInUseException registerGroupIsTillInUse (Thesaurus thesaurus, RegisterGroup registerGroup, List<RegisterMapping> registerMappings) {
        return new CannotDeleteBecauseStillInUseException(thesaurus, MessageSeeds.REGISTER_GROUP_STILL_IN_USE, registerGroup.getName(), namesToStringList(registerMappings));
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

    private CannotDeleteBecauseStillInUseException(Thesaurus thesaurus, MessageSeeds messageSeeds, String registerGroupName, String dependendObjectNames) {
        super(thesaurus, messageSeeds, registerGroupName, dependendObjectNames);
        this.set("registerGroupName", registerGroupName);
        this.set("dependendObjectNames", dependendObjectNames);
    }

}