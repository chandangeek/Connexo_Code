package com.energyict.mdc.dynamic.relation.exceptions;

import com.energyict.mdc.dynamic.relation.RelationType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to delete a {@link RelationType} that has remaining instances.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (14:05)
 */
public class CannotDeleteRelationType extends LocalizedException {

    public CannotDeleteRelationType(RelationType relationType, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, relationType.getName());
    }

}