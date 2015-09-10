package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.common.BusinessException;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.sql.SQLException;

/**
 * Wraps a {@link BusinessException} or SQLException
 * that occurs in a {@link com.energyict.mdc.dynamic.relation.RelationTransaction}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (15:36)
 */
public class NestedRelationTransactionException extends LocalizedException {

    public NestedRelationTransactionException(BusinessException e, String relationTypeName, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, e);
        this.set("relationTypeName", relationTypeName);
    }

    public NestedRelationTransactionException(Thesaurus thesaurus, SQLException e, String relationTypeName, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, e);
        this.set("relationTypeName", relationTypeName);
    }

}