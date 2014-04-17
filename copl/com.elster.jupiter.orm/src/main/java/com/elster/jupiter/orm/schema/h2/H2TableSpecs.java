package com.elster.jupiter.orm.schema.h2;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.schema.ExistingColumn;
import com.elster.jupiter.orm.schema.ExistingConstraint;
import com.elster.jupiter.orm.schema.ExistingTable;
import com.elster.jupiter.orm.schema.SchemaInfoProvider;

public enum H2TableSpecs implements SchemaInfoProvider.TableSpec {
    TABLES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ExistingTable> table = dataModel.addTable("INFORMATION_SCHEMA", name(), ExistingTable.class);
            table.map(TableImpl.class);
            Column nameColumn = table.column("TABLE_NAME").varChar(128).notNull().map("name").add();
            table.primaryKey("PK_USERTABLES").on(nameColumn).add();
        }

    },
    COLUMNS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ExistingColumn> table = dataModel.addTable("INFORMATION_SCHEMA", name(), ExistingColumn.class);
            table.map(ColumnImpl.class);
            Column tableColumn = table.column("TABLE_NAME").varChar(128).notNull().map("tableName").add();
            Column nameColumn = table.column("COLUMN_NAME").varChar(128).notNull().map("name").add();
            table.column("ORDINAL_POSITION").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("position").add();
            table.column("DATA_TYPE").varChar(128).map("dataType").add();
            table.column("NUMERIC_PRECISION").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("dataLength").add();
            table.column("CHARACTER_MAXIMUM_LENGTH").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("characterLength").add();
            table.column("IS_NULLABLE").varChar(1).notNull().conversion(ColumnConversion.CHAR2BOOLEAN).map("nullable").add();
            table.primaryKey("PK_USERCOLUMNS").on(tableColumn, nameColumn).add();
            table.foreignKey("FK_COLUMNTABLE").on(tableColumn).references(TABLES.name())
                    .map("table").reverseMap("columns").reverseMapOrder("position").add();

        }
    },
    CONSTRAINTS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ExistingConstraint> table = dataModel.addTable("INFORMATION_SCHEMA", name(), ExistingConstraint.class);
            table.map(ConstraintImpl.class);
            Column nameColumn = table.column("CONSTRAINT_NAME").varChar(128).notNull().map("name").add();
            Column tableColumn = table.column("TABLE_NAME").varChar(128).notNull().add();
            table.column("CONSTRAINT_TYPE").varChar(1).notNull().conversion(ColumnConversion.CHAR2ENUM).map("type").add();
            // Column referencedConstraint = table.column("R_CONSTRAINT_NAME").varChar(128).add();
            table.primaryKey("PK_USERCONSTRAINTS").on(nameColumn).add();
            table.foreignKey("FK_CONSTRAINTTABLE").on(tableColumn).references(TABLES.name())
                    .map("table").reverseMap("constraints").add();
            //table.foreignKey("FK_CONSTRAINTCONSTRAINT").on(referencedConstraint).references(USER_CONSTRAINTS.name())
            //      .map("referencedConstraint").add();
        }
    },
    CROSS_REFERENCES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ConstraintColumnImpl> table = dataModel.addTable("INFORMATION_SCHEMA", name(), ConstraintColumnImpl.class);
            table.map(ConstraintColumnImpl.class);
            Column constraint = table.column("FK_NAME").varChar(128).notNull().add();
            Column column = table.column("PKCOLUMN_NAME").varChar(128).notNull().map("columnName").add();
            table.column("ORDINAL_POSITION").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("position").add();
            table.primaryKey("PK_USERCONSCOLUMNS").on(constraint, column).add();
            table.foreignKey("FK_CONSCOLUMNSCONSTRAINT").on(constraint).references(CONSTRAINTS.name())
                    .map("constraint").reverseMap("columns").reverseMapOrder("position").add();
        }
    };

    public abstract void addTo(DataModel dataModel);
}
