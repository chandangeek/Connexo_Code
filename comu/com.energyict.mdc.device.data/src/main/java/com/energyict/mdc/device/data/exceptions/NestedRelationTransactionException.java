package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.BusinessException;

import java.sql.SQLException;

/**
 * Wraps a {@link BusinessException} or SQLException
 * that occurs in a {@link com.energyict.mdc.dynamic.relation.RelationTransaction}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (15:36)
 */
public class NestedRelationTransactionException extends LocalizedException {

    public NestedRelationTransactionException(Thesaurus thesaurus, BusinessException e, String relationTypeName) {
        super(thesaurus, MessageSeeds.UNEXPECTED_RELATION_TRANSACTION_ERROR, e);
        this.set("relationTypeName", relationTypeName);
    }

    public NestedRelationTransactionException(Thesaurus thesaurus, SQLException e, String relationTypeName) {
        super(thesaurus, MessageSeeds.UNEXPECTED_RELATION_TRANSACTION_ERROR, e);
        this.set("relationTypeName", relationTypeName);
    }

}