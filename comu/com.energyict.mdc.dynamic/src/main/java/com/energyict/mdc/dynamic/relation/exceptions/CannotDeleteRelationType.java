package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.relation.RelationType;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to delete a {@link RelationType} that has remaining instances.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (14:05)
 */
public class CannotDeleteRelationType extends LocalizedException {

    public CannotDeleteRelationType(Thesaurus thesaurus, RelationType relationType) {
        super(thesaurus, MessageSeeds.RELATION_TYPE_CANNOT_DELETE_WITH_EXISTING_INSTANCES, relationType.getName());
    }

}