package com.energyict.mdc.dynamic.relation.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.sql.SQLException;

/**
 * Models the exceptional situation that occurs when a DDL statement
 * that was issued to the database to modify the storage area
 * associated with a relation type failed unexpectedly.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (13:11)
 */
public class RelationTypeDDLException extends LocalizedException {

    public RelationTypeDDLException(SQLException cause, String relationTypeName, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, cause, relationTypeName);
        this.set("relationTypeName", relationTypeName);
    }

}