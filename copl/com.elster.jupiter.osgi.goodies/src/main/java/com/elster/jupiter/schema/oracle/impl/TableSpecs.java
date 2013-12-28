package com.elster.jupiter.schema.oracle.impl;

import static com.elster.jupiter.orm.ColumnConversion.*;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.schema.oracle.UserColumn;
import com.elster.jupiter.schema.oracle.UserConstraint;
import com.elster.jupiter.schema.oracle.UserTable;

public enum TableSpecs {
	USER_TABLES {
		@Override
		public void addTo(DataModel dataModel) {
			Table<UserTable> table = dataModel.addTable(name(), UserTable.class);
			table.map(UserTableImpl.class);
			Column nameColumn = table.column("TABLE_NAME").varChar(128).notNull().map("name").add();
			table.primaryKey("PK_USERTABLES").on(nameColumn).add();
		}
		
	},
	USER_TAB_COLUMNS {
		@Override
		public void addTo(DataModel dataModel) {
			Table<UserColumn> table = dataModel.addTable(name(), UserColumn.class);
			table.map(UserColumnImpl.class);
			Column tableColumn = table.column("TABLE_NAME").varChar(128).notNull().map("tableName").add();
			Column nameColumn = table.column("COLUMN_NAME").varChar(128).notNull().map("name").add();
			table.column("COLUMN_ID").number().notNull().conversion(NUMBER2INT).map("position").add();
			table.column("DATA_TYPE").varChar(128).map("dataType").add();
			table.column("DATA_LENGTH").number().notNull().conversion(NUMBER2INT).map("dataLength").add();
			table.column("NULLABLE").varChar(1).notNull().conversion(CHAR2BOOLEAN).map("nullable").add();
			table.primaryKey("PK_USERCOLUMNS").on(tableColumn,nameColumn).add();
			table.foreignKey("FK_COLUMNTABLE").on(tableColumn).references(USER_TABLES.name())
				.map("table").reverseMap("columns").reverseMapOrder("position").add();
				
		}	
	},
	USER_CONSTRAINTS {
		@Override
		public void addTo(DataModel dataModel) {
			Table<UserConstraint> table = dataModel.addTable(name(), UserConstraint.class);
			table.map(UserConstraintImpl.class);
			Column tableColumn = table.column("TABLE_NAME").varChar(128).notNull().add();
			Column nameColumn = table.column("CONSTRAINT_NAME").varChar(128).notNull().map("name").add();
			table.column("CONSTRAINT_TYPE").varChar(1).notNull().conversion(CHAR2ENUM).map("type").add();
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
			table.column("POSITION").number().notNull().conversion(NUMBER2INT).map("position").add();
			table.primaryKey("PK_USERCONSCOLUMNS").on(constraint,column).add();
			table.foreignKey("FK_CONSCOLUMNSCONSTRAINT").on(constraint).references(USER_CONSTRAINTS.name())
				.map("constraint").reverseMap("columns").reverseMapOrder("position").add();
		}
	};
	
	public abstract void addTo(DataModel dataModel);
}
