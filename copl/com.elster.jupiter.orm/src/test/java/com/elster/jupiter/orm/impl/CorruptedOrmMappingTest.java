/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.Version;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CorruptedOrmMappingTest {

    private static final String TABLE_NAME = "TABLE_NAME";
    private static final String TABLE_NAME2 = TABLE_NAME + "2";
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();

    @Mock
    private DataModelImpl dataModel;
    @Mock
    private TableImpl<?> otherTable;
    @Mock
    private ColumnImpl otherTablesColumn;

    private TableImpl<Dummy> table;

    @Before
    public void setUp() {
        table = TableImpl.from(dataModel, "ORM", TABLE_NAME, Dummy.class);

        doReturn(otherTable).when(otherTablesColumn).getTable();
        when(otherTablesColumn.getName()).thenReturn("OTHERTABLESCOLUMN");
        when(dataModel.getVersion()).thenReturn(Version.version(1, 0));
    }

    @After
    public void tearDown() {

    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Builder in progress for table " + TABLE_NAME + ", invoke add() first.")
    public void testForgettingToAdd() {
        doReturn(table).when(dataModel).getTable(TABLE_NAME);

        table.column("ONE");
        table.column("TWO");
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : column name 'COL4567890123456789012345678901' is too long, max length is " + ColumnConversion.CATALOGNAMELIMIT + " actual length is 31.")
    public void testColumnNameTooLong() {
        table.column("COL4567890123456789012345678901").add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : column names cannot be null.")
    public void testColumnNameNull() {
        table.column(null).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : column ONE was not assigned a DB type.")
    public void testColumnNameTypeUnspecified() {
        table.column("ONE").add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : field ONE : updateValue must be null if skipOnUpdate")
    public void testColumnUpdateValueMustBeNullIfSkipOnUpdate() {
        table.column("ONE").varChar(256).update("Default").skipOnUpdate().add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : primary key can not have an empty name.")
    public void testPrimaryKeyNullName() {
        Column column = table.column("ONE").number().add();
        table.primaryKey(null).on(column).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : primary key can not have an empty name.")
    public void testPrimaryKeyEmptyName() {
        Column column = table.column("ONE").number().add();
        table.primaryKey("").on(column).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : primary key can not have columns from another table : OTHERTABLESCOLUMN.")
    public void testPrimaryKeyCannotUseOtherTablesColumns() {
        table.primaryKey("PK").on(otherTablesColumn).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : foreign key can not have an empty name.")
    public void testForeignKeyNullName() {
        Column column = table.column("ONE").number().add();
        table.foreignKey(null).on(column).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : foreign key can not have an empty name.")
    public void testForeignKeyEmptyName() {
        Column column = table.column("ONE").number().add();
        table.foreignKey("").on(column).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : foreign key can not have columns from another table as key : OTHERTABLESCOLUMN.")
    public void testForeignKeyCannotUseOtherTablesColumns() {
        table.foreignKey("FK").on(otherTablesColumn).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Foreign key FK_NAME on table " + TABLE_NAME + " the referenced table " + TABLE_NAME2 + " does not exist.")
    public void testForeignKeyReferencedTableDoesNotExist() {
        when(dataModel.getTable(TABLE_NAME2)).thenReturn(null);

        Column foreignKey = table.column("FK").number().notNull().add();
        table.foreignKey("FK_NAME").references(TABLE_NAME2).on(foreignKey).map("field").reverseMap("fieldName").add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Foreign key FK_NAME on table " + TABLE_NAME + " the referenced object does not have a field named fieldName.")
    public void testForeignKeyMappingReverseFieldDoesNotExist() {
        doReturn(table).when(dataModel).getTable(TABLE_NAME);
        table.map(Dummy.class);

        Column foreignKey = table.column("FK").number().notNull().add();
        table.foreignKey("FK_NAME").references(TABLE_NAME).on(foreignKey).map("field").reverseMap("fieldName").add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Foreign key FK_NAME on table " + TABLE_NAME + " is missing a referenced table.")
    public void testForeignKeyMappingMissesField() {
        doReturn(table).when(dataModel).getTable(TABLE_NAME);
        table.map(Dummy.class);

        Column foreignKey = table.column("FK").number().notNull().add();
        table.foreignKey("FK_NAME").on(foreignKey).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " : constraint name 'COL4567890123456789012345678901' is too long, max length is " + ColumnConversion.CATALOGNAMELIMIT + " actual length is 31.")
    public void testForeignKeyNameTooLong() {
        table.foreignKey("COL4567890123456789012345678901").add();
    }


    public static class Dummy {
        @SuppressWarnings("unused")
		private Dummy field;
    }
}
