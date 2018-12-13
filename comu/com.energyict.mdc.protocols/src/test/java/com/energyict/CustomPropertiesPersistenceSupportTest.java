/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import java.util.Collections;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Serves as the root for test classes that will
 * check the implementation of persistent support
 * classes for connection types, security properties or protocol dialects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-04 (16:23)
 */
public abstract class CustomPropertiesPersistenceSupportTest {

    public static final int MAX_TABLE_NAME_LENGTH = 26;
    public static final int MAX_FOREIGN_KEY_NAME_LENGTH = 30;

    protected void allColumnsAreNullable (PersistenceSupport persistenceSupport) {
        Column fakePrimaryKeyColumn = mock(Column.class);

        Table table = mock(Table.class);
        Column column = mock(Column.class);
        Column.Builder columnBuilder = FakeBuilder.initBuilderStub(column, Column.Builder.class);
        when(table.column(anyString())).thenReturn(columnBuilder);

        Column completeColumn = mock(Column.class);
        Column.Builder completeColumnBuilder = FakeBuilder.initBuilderStub(completeColumn, Column.Builder.class);
        when(table.column(CommonBaseDeviceSecurityProperties.Fields.COMPLETE.databaseName())).thenReturn(completeColumnBuilder);

        ForeignKeyConstraint foreignKeyConstraint = mock(ForeignKeyConstraint.class);
        ForeignKeyConstraint.Builder foreignKeyBuilder = FakeBuilder.initBuilderStub(foreignKeyConstraint, ForeignKeyConstraint.Builder.class);
        when(table.foreignKey(anyString())).thenReturn(foreignKeyBuilder);

        // Business method
        persistenceSupport.addCustomPropertyColumnsTo(table, Collections.singletonList(fakePrimaryKeyColumn));

        // Asserts
        verify(columnBuilder, never()).notNull();
    }

}