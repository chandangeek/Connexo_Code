package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to delete a {@link RelationAttributeType }that is
 * considered to be a default attribute.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (13:16)
 */
public class CannotDeleteDefaultRelationAttributeException extends LocalizedException {

    public CannotDeleteDefaultRelationAttributeException(Thesaurus thesaurus, RelationAttributeType rat) {
        super(thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_CANNOT_DELETE_DEFAULT, rat.getName(), rat.getRelationType().getName());
    }

}