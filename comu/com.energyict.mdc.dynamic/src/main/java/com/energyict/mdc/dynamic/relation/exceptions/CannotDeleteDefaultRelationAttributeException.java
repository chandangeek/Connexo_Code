package com.energyict.mdc.dynamic.relation.exceptions;

import com.energyict.mdc.dynamic.relation.RelationAttributeType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to delete a {@link RelationAttributeType }that is
 * considered to be a default attribute.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (13:16)
 */
public class CannotDeleteDefaultRelationAttributeException extends LocalizedException {

    public CannotDeleteDefaultRelationAttributeException(RelationAttributeType rat, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, rat.getName(), rat.getRelationType().getName());
    }

}