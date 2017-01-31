/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema.oracle;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.schema.ExistingColumn;
import com.elster.jupiter.orm.schema.ExistingConstraint;
import com.elster.jupiter.orm.schema.ExistingIndex;
import com.elster.jupiter.orm.schema.ExistingTable;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;

public enum OracleTableSpecs implements SchemaInfoProvider.TableSpec {
    USER_TABLES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ExistingTable> table = dataModel.addTable(name(), ExistingTable.class);
            table.map(UserTableImpl.class);
            Column nameColumn = table.column("TABLE_NAME").varChar(128).notNull().map("name").add();
            table.primaryKey("PK_USERTABLES").on(nameColumn).add();
        }

    },
    USER_TAB_COLS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ExistingColumn> table = dataModel.addTable(name(), ExistingColumn.class);
            table.map(UserColumnImpl.class);
            Column tableColumn = table.column("TABLE_NAME").varChar(128).notNull().map("tableName").add();
            Column nameColumn = table.column("COLUMN_NAME").varChar(128).notNull().map("name").add();
            table.column("COLUMN_ID").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("position").add();
            table.column("DATA_TYPE").varChar(128).map("dataType").add();
            table.column("DATA_LENGTH").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("dataLength").add();
            table.column("CHAR_LENGTH").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("characterLength").add();
            table.column("NULLABLE").varChar(1).notNull().conversion(ColumnConversion.CHAR2BOOLEAN).map("nullable").add();
            table.column("DATA_DEFAULT").type("long()").map("dataDefault").add();
            table.column("VIRTUAL_COLUMN").varChar(3).map("virtual").add();
            table.column("HIDDEN_COLUMN").varChar(3).map("hidden").add();
            table.primaryKey("PK_USERCOLUMNS").on(tableColumn, nameColumn).add();
            table.foreignKey("FK_COLUMNTABLE").on(tableColumn).references(USER_TABLES.name())
                    .map("table").reverseMap("columns").reverseMapOrder("position").add();

        }
    },
    USER_CONSTRAINTS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ExistingConstraint> table = dataModel.addTable(name(), ExistingConstraint.class);
            table.map(UserConstraintImpl.class);
            Column nameColumn = table.column("CONSTRAINT_NAME").varChar(128).notNull().map("name").add();
            Column tableColumn = table.column("TABLE_NAME").varChar(128).notNull().add();
            table.column("DELETE_RULE").varChar(9).map("deleteRule").add();
            table.column("CONSTRAINT_TYPE").varChar(1).notNull().conversion(ColumnConversion.CHAR2ENUM).map("type").add();
            Column referencedConstraint = table.column("R_CONSTRAINT_NAME").varChar(128).add();
            table.primaryKey("PK_USERCONSTRAINTS").on(nameColumn).add();
            table.foreignKey("FK_CONSTRAINTTABLE").on(tableColumn).references(USER_TABLES.name())
                    .map("table").reverseMap("constraints").add();
            table.foreignKey("FK_CONSTRAINTCONSTRAINT").on(referencedConstraint).references(USER_CONSTRAINTS.name())
                    .map("referencedConstraint").add();
        }
    },
    USER_CONS_COLUMNS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<UserConstraintColumnImpl> table = dataModel.addTable(name(), UserConstraintColumnImpl.class);
            table.map(UserConstraintColumnImpl.class);
            Column constraint = table.column("CONSTRAINT_NAME").varChar(128).notNull().add();
            Column column = table.column("COLUMN_NAME").varChar(128).notNull().map("columnName").add();
            table.column("POSITION").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("position").add();
            table.primaryKey("PK_USERCONSCOLUMNS").on(constraint, column).add();
            table.foreignKey("FK_CONSCOLUMNSCONSTRAINT").on(constraint).references(USER_CONSTRAINTS.name())
                    .map("constraint").reverseMap("columns").reverseMapOrder("position").add();
        }
    },
    USER_INDEXES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ExistingIndex> table = dataModel.addTable(name(), ExistingIndex.class);
            table.map(UserIndexImpl.class);
            Column nameColumn = table.column("INDEX_NAME").varChar(128).notNull().map("name").add();
            Column tableColumn = table.column("TABLE_NAME").varChar(128).notNull().add();
            table.column("PREFIX_LENGTH").number().conversion(ColumnConversion.NUMBER2INTNULLZERO).map("compression").add();
            table.column("INDEX_TYPE").varChar(27).map("type").add();
            table.primaryKey("PK_USERINDEXES").on(nameColumn).add();
            table.foreignKey("FK_INDEXTABLE").on(tableColumn).references(USER_TABLES.name())
                    .map("table").reverseMap("indexes").add();
        }
    },
    USER_IND_COLUMNS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<UserIndexColumnImpl> table = dataModel.addTable(name(), UserIndexColumnImpl.class);
            table.map(UserIndexColumnImpl.class);
            Column index = table.column("INDEX_NAME").varChar(128).notNull().add();
            Column column = table.column("COLUMN_NAME").varChar(128).notNull().map("columnName").add();
            table.column("COLUMN_POSITION").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("position").add();
            table.primaryKey("PK_USERCONSCOLUMNS").on(index, column).add();
            table.foreignKey("FK_INDSCOLUMNSCONSTRAINT").on(index).references(USER_INDEXES.name())
                    .map("index").reverseMap("columns").reverseMapOrder("position").add();
        }
    };

    public abstract void addTo(DataModel dataModel);
}
