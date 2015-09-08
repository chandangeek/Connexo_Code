package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to add a required {@link com.energyict.mdc.dynamic.relation.RelationAttributeType}
 * because there are existing objects and these would
 * necessarily have a <code>null</code> value afterwards.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (13:16)
 */
public class CannotAddRequiredRelationAttributeException extends LocalizedException {

    public CannotAddRequiredRelationAttributeException(String attributeName, String relationTypeName, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, attributeName, relationTypeName);
        this.set("attributeName", attributeName);
        this.set("relationTypeName", relationTypeName);
    }

}