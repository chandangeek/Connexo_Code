/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.IllegalTableMappingException;

import org.junit.Test;

/**
 * Tests that {@link ColumnImpl} checks that reserved words are not allowed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-14 (09:58)
 */
public class ReservedColumnName {

    @Test(expected = IllegalTableMappingException.class)
    public void columnLowercase() {
        TableImpl table = new TableImpl();

        // Business method
        ColumnImpl.from(table, "column");
    }

    @Test(expected = IllegalTableMappingException.class)
    public void columnMixedcase() {
        TableImpl table = new TableImpl();

        // Business method
        ColumnImpl.from(table, "cOluMn");
    }

    @Test(expected = IllegalTableMappingException.class)
    public void columnUppercase() {
        TableImpl table = new TableImpl();

        // Business method
        ColumnImpl.from(table, "COLUMN");
    }

    @Test(expected = IllegalTableMappingException.class)
    public void userLowercase() {
        TableImpl table = new TableImpl();

        // Business method
        ColumnImpl.from(table, "user");
    }

    @Test(expected = IllegalTableMappingException.class)
    public void userMixedcase() {
        TableImpl table = new TableImpl();

        // Business method
        ColumnImpl.from(table, "uSEr");
    }

    @Test(expected = IllegalTableMappingException.class)
    public void userUppercase() {
        TableImpl table = new TableImpl();

        // Business method
        ColumnImpl.from(table, "USER");
    }

}