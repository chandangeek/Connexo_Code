package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;

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

    public RelationAttributeHasNullValuesException(Thesaurus thesaurus, RelationAttributeType rat) {
        super(thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_STORAGE_CONTAINS_NULL_VALUES, rat.getName(), rat.getRelationType().getName());
    }

}