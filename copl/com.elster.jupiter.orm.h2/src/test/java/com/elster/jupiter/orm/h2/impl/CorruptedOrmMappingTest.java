/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.h2.impl;

import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Blob;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Encrypter;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataModelImpl;
import com.elster.jupiter.orm.impl.TableImpl;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CorruptedOrmMappingTest {

    private static final String TABLE_NAME = "TABLE_NAME";
    private static final String TABLE_NAME2 = TABLE_NAME + "2";
    private static final long EVICTION_TIME = 300;
    private static final boolean CACHE_IS_ENABLED = false;
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
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": column name 'COL4567890123456789012345678901' is too long, max length is " + ColumnConversion.CATALOGNAMELIMIT + ", actual length is 31.")
    public void testColumnNameTooLong() {
        table.column("COL4567890123456789012345678901").add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": column name 'ALTER' is a reserved word and can't be used as the name of a column.")
    public void testColumnNameIsAReservedWord() {
        table.column("ALTER").add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": implementer(s) already specified.")
    public void testAlreadyHasImplementer1() {
        table.map(Dummy.class);
        table.map(Dummy.class);
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": implementer(s) already specified.")
    public void testAlreadyHasImplementer2() {
        table.map(Dummy.class);
        table.map(Collections.emptyMap());
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": empty map of implementers.")
    public void testEmptyImplementersMap() {
        table.map(Collections.emptyMap());
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": class java.lang.String doesn't implement class com.elster.jupiter.orm.h2.impl.CorruptedOrmMappingTest$Dummy.")
    public void testWrongImplementer1() {
        ((TableImpl) table).map(String.class);
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": class java.lang.Long doesn't implement class com.elster.jupiter.orm.h2.impl.CorruptedOrmMappingTest$Dummy.")
    public void testWrongImplementer2() {
        ((TableImpl) table).map(Collections.singletonMap("ABC", Long.class));
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": column names can't be null.")
    public void testColumnNameNull() {
        table.column(null).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": column ONE hasn't been assigned a DB type.")
    public void testColumnNameTypeUnspecified() {
        table.column("ONE").add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table 12345678901234567890123456789: sequence name 12345678901234567890123456789ID is too long.")
    public void testSequenceNameIsTooLong() {
        table = TableImpl.from(dataModel, "ORM", "12345678901234567890123456789", Dummy.class);
        table.addAutoIdColumn();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + " already has a MAC column.")
    public void testAlreadyHasMACColumn() {
        table.addMessageAuthenticationCodeColumn(mock(Encrypter.class));
        table.addMessageAuthenticationCodeColumn(mock(Encrypter.class));
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": implementation class hasn't been specified (yet?)")
    public void testImplementationIsNotSpecified() {
        table.column("ONE").blob().add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": column ONE: blob() column must map a Blob field.")
    public void testColumnMustMapABlobType() {
        table.map(Dummy.class);
        table.column("ONE").blob().map("field").add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": column ONE has no mapping.")
    public void testColumnMustBeMapped() {
        table.map(Dummy.class);
        table.column("ONE").number().add();
        table.prepare(EVICTION_TIME, CACHE_IS_ENABLED);
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": column TWO: column ONE already maps to field field.")
    public void testColumnAlreadyMapsToField() {
        table.map(Dummy.class);
        table.column("ONE").number().map("field").add();
        table.column("TWO").number().map("field").add();
    }

    @Test
    @Expected(value = IllegalStateException.class, message = "Table " +"'"+ TABLE_NAME +"'"+ " : Primary key columns must be defined in order")
    public void testPrimaryKeyInOrder() {
        table.map(Dummy.class);
        Column one = table.column("ONE").number().notNull().map("field").add();
        Column id = table.column("ID").number().notNull().map("id").add();
        table.primaryKey("PK").on(id, one).add();
        table.prepare(EVICTION_TIME, CACHE_IS_ENABLED);
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": can't journal table without primary key.")
    public void testCannotJournalTableWithoutPrimaryKey() {
        table.map(Dummy.class);
        table.column("ONE").number().map("field").add();
        table.setJournalTableName(TABLE_NAME + "JRNL");
        table.prepare(EVICTION_TIME, CACHE_IS_ENABLED);
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": can't cache table without primary key.")
    public void testCannotCacheTableWithoutPrimaryKey() {
        table.map(Dummy.class);
        table.column("ONE").number().map("field").add();
        table.cache();
        table.prepare(EVICTION_TIME, CACHE_IS_ENABLED);
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": can't cache table without primary key.")
    public void testCannotCacheWholeTableWithoutPrimaryKey() {
        table.map(Dummy.class);
        table.column("ONE").number().map("field").add();
        table.cacheWholeTable(false);
        table.prepare(EVICTION_TIME, CACHE_IS_ENABLED);
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": ColumnConversion.BLOB2SQLBLOB can't be used for table without primary key.")
    public void testCannotUseBlobInTableWithoutPrimaryKey() {
        table.map(Dummy.class);
        table.column("ONE").blob().map("blob").add();
        table.prepare(EVICTION_TIME, CACHE_IS_ENABLED);
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": field ONE: updateValue must be null if skipOnUpdate.")
    public void testColumnUpdateValueMustBeNullIfSkipOnUpdate() {
        table.column("ONE").varChar(256).update("Default").skipOnUpdate().add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": primary key can't have an empty name.")
    public void testPrimaryKeyNullName() {
        Column column = table.column("ONE").number().add();
        table.primaryKey(null).on(column).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": primary key can't have an empty name.")
    public void testPrimaryKeyEmptyName() {
        Column column = table.column("ONE").number().add();
        table.primaryKey("").on(column).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": primary key can't have columns from another table: OTHERTABLESCOLUMN.")
    public void testPrimaryKeyCannotUseOtherTablesColumns() {
        table.primaryKey("PK").on(otherTablesColumn).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": index IDX can't have columns from another table: OTHERTABLESCOLUMN.")
    public void testIndexCannotUseOtherTablesColumns() {
        table.index("IDX").on(otherTablesColumn).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": primary key can't be put on nullable column: ONE.")
    public void testPrimaryKeyCannotUseNullableColumn() {
        Column column = table.column("ONE").number().add();
        table.primaryKey("PK").on(column).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": foreign key can't have an empty name.")
    public void testForeignKeyNullName() {
        Column column = table.column("ONE").number().add();
        table.foreignKey(null).on(column).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": foreign key can't have an empty name.")
    public void testForeignKeyEmptyName() {
        Column column = table.column("ONE").number().add();
        table.foreignKey("").on(column).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": foreign key can't have columns from another table as key: OTHERTABLESCOLUMN.")
    public void testForeignKeyCannotUseOtherTablesColumns() {
        table.foreignKey("FK").on(otherTablesColumn).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Foreign key FK_NAME on table " + TABLE_NAME + " is missing a referenced table.")
    public void testForeignKeyReferencedTableDoesNotExist() {
        when(dataModel.getTable(TABLE_NAME2)).thenReturn(null);

        Column foreignKey = table.column("FK").number().notNull().add();
        table.foreignKey("FK_NAME").references(TABLE_NAME2).on(foreignKey).map("field").reverseMap("fieldName").add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Foreign key FK_NAME on table " + TABLE_NAME + ": the referenced object doesn't have a field named fieldName.")
    public void testForeignKeyMappingReverseFieldDoesNotExist() {
        doReturn(table).when(dataModel).getTable(TABLE_NAME);
        table.map(Dummy.class);

        Column foreignKey = table.column("FK").number().notNull().add();
        table.primaryKey("PK_NAME").on(foreignKey).add();
        table.foreignKey("FK_NAME").references(TABLE_NAME).on(foreignKey).map("field").reverseMap("fieldName").add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": don't specify eager mapping when referencing cached table " + TABLE_NAME + '.')
    public void testEagerMappingForCachedTable() {
        doReturn(table).when(dataModel).getTable(TABLE_NAME);
        table.map(Dummy.class);
        table.cache();

        Column foreignKey = table.column("FK").number().notNull().add();
        table.primaryKey("PK_NAME").on(foreignKey).add();
        table.foreignKey("FK_NAME").references(TABLE_NAME).on(foreignKey).map("field", Dummy.class).add();
    }

    @Test
    @Expected(value = IllegalTableMappingException.class, message = "Foreign key FK_NAME on table " + TABLE_NAME + " can't reference a table without primary key.")
    public void testForeignKeyMappingTableWithoutPrimaryKey() {
        doReturn(table).when(dataModel).getTable(TABLE_NAME);
        table.map(Dummy.class);

        Column foreignKey = table.column("FK").number().notNull().add();
        table.foreignKey("FK_NAME").references(TABLE_NAME).on(foreignKey).map("field").add();
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
    @Expected(value = IllegalTableMappingException.class, message = "Table " + TABLE_NAME + ": constraint name 'COL4567890123456789012345678901' is too long, max length is " + ColumnConversion.CATALOGNAMELIMIT + ", actual length is 31.")
    public void testForeignKeyNameTooLong() {
        table.foreignKey("COL4567890123456789012345678901").add();
    }

    public static class Dummy {
        @SuppressWarnings("unused")
        private Dummy field;
        @SuppressWarnings("unused")
        private long id;
        @SuppressWarnings("unused")
        private Blob blob;
    }
}
