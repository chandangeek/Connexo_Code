/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.h2.impl;

import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataModelImpl;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
import com.elster.jupiter.orm.impl.TableImpl;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * Tests that {@link ColumnImpl} checks that reserved words are not allowed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-14 (09:58)
 */
@RunWith(MockitoJUnitRunner.class)
public class ReservedColumnNameTest {
    @Mock
    private TableImpl table;
    @Mock
    private DataModelImpl dataModel;
    @Mock
    private SchemaInfoProvider schemaInfoProvider;

    @Before
    public void setUp() {
        when(schemaInfoProvider.isTestSchemaProvider()).thenReturn(false);
        when(table.getName()).thenReturn("TABLE_NAME");
        when(table.getDataModel()).thenReturn(dataModel);
        OrmServiceImpl ormService = new OrmServiceImpl();
        ormService.setSchemaInfoProvider(schemaInfoProvider);
        when(dataModel.getOrmService()).thenReturn(ormService);
    }

    @Test(expected = IllegalTableMappingException.class)
    public void columnLowercase() {
        // Business method
        ColumnImpl.from(table, "column");
    }

    @Test(expected = IllegalTableMappingException.class)
    public void columnMixedcase() {
        // Business method
        ColumnImpl.from(table, "cOluMn");
    }

    @Test(expected = IllegalTableMappingException.class)
    public void columnUppercase() {
        // Business method
        ColumnImpl.from(table, "COLUMN");
    }

    @Test(expected = IllegalTableMappingException.class)
    public void userLowercase() {
        // Business method
        ColumnImpl.from(table, "user");
    }

    @Test(expected = IllegalTableMappingException.class)
    public void userMixedcase() {
        // Business method
        ColumnImpl.from(table, "uSEr");
    }

    @Test(expected = IllegalTableMappingException.class)
    public void userUppercase() {
        // Business method
        ColumnImpl.from(table, "USER");
    }
}
