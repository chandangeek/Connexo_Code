package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to obsolete {@link com.energyict.mdc.dynamic.relation.Relation}
 * that is in fact already obsolete. That is done from the process
 * that manages the properties of pluggable classes.
 * So if this happens, it is clear that that process is buggy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (15:36)
 */
public class RelationIsAlreadyObsoleteException extends LocalizedException {

    public RelationIsAlreadyObsoleteException(String relationTypeName, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, relationTypeName);
        this.set("relationTypeName", relationTypeName);
    }

}