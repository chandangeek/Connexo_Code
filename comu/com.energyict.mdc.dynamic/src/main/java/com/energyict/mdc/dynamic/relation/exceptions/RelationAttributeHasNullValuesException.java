package com.energyict.mdc.dynamic.relation.exceptions;

import com.energyict.mdc.dynamic.relation.RelationAttributeType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to make a {@link RelationAttributeType} required,
 * i.e. it cannot contain <code>null</code> values,
 * but the existing storage area already contains null values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (13:16)
 */
public class RelationAttributeHasNullValuesException extends LocalizedException {

    public RelationAttributeHasNullValuesException(RelationAttributeType rat, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, rat.getName(), rat.getRelationType().getName());
    }

}