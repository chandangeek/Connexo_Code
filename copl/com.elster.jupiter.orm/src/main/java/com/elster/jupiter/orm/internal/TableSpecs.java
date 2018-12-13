/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.internal;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.ColumnInConstraintImpl;
import com.elster.jupiter.orm.impl.DataModelImpl;
import com.elster.jupiter.orm.impl.TableConstraintImpl;
import com.elster.jupiter.orm.impl.TableImpl;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.CHAR2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;

public enum TableSpecs {

	ORM_DATAMODEL {
		public void addTo(DataModel dataModel) {
			Table<DataModel> table = dataModel.addTable(name(),DataModel.class);
			table.map(DataModelImpl.class);
			Column name = table.column("NAME").type(COMPONENTDBTYPE).notNull().map("name").add();
			table.column("DESCRIPTION").varChar(NAME_LENGTH).map("description").add();
			table.primaryKey("ORM_PK_COMPONENT").on(name).add();
		}
	},
	ORM_TABLE {
		@SuppressWarnings("rawtypes")
		public void addTo(DataModel dataModel) {
			Table<Table> table = dataModel.addTable(name(),Table.class);
			table.map(TableImpl.class);
			Column component = table.column("COMPONENT").type(COMPONENTDBTYPE).notNull().add();
			Column name = table.column("NAME").type(CATALOGDBTYPE).notNull().map("name").add();
			Column position = table.addPositionColumn();
			Column schema = table.column("SCHEMAOWNER").type(CATALOGDBTYPE).map("schema").add();
			table.column("CACHED").bool().map("cached").add();
			table.column("INDEXORGANIZED").number().notNull().conversion(NUMBER2INT).map("indexOrganized").add();
			table.primaryKey("ORM_PK_TABLE").on(component , name).add();
			table.unique("ORM_U_TABLE").on(schema , name).add();
			table.unique("ORM_U_TABLEPOSITION").on(component , position).add();
			table.foreignKey("ORM_FK_TABLEDATAMODEL").on(component).references(ORM_DATAMODEL.name()).onDelete(CASCADE).
				map("dataModel").reverseMap("tables").reverseMapOrder("position").composition().add();
		}
	},
	ORM_COLUMN {
		public void addTo(DataModel dataModel) {
			Table<Column> table = dataModel.addTable(name(),Column.class);
			table.map(ColumnImpl.class);
			Column component = table.column("COMPONENT").type(COMPONENTDBTYPE).notNull().add();
			Column tableName = table.column("TABLENAME").type(CATALOGDBTYPE).notNull().add();
			Column name= table.column("NAME").type(CATALOGDBTYPE).notNull().map("name").add();
			Column position = table.addPositionColumn();
			table.column("FIELDNAME").varChar(NAME_LENGTH).map("fieldName").add();
			table.column("DBTYPE").type(CATALOGDBTYPE).notNull().map("dbType").add();
			table.column("NOTNULL").bool().map("notNull").add();
			table.column("VERSIONCOUNT").bool().map("versionCount").add();
			table.column("CONVERSION").varChar(30).notNull().conversion(CHAR2ENUM).map("conversion").add();
			table.column("SKIPONUPDATE").bool().map("skipOnUpdate").add();
			table.column("SEQUENCENAME").type(CATALOGDBTYPE).map("sequenceName").add();
			table.column("INSERTVALUE").varChar(SHORT_DESCRIPTION_LENGTH).map("insertValue").add();
			table.column("UPDATEVALUE").varChar(SHORT_DESCRIPTION_LENGTH).map("updateValue").add();
			table.primaryKey("ORM_PK_COLUMNS").on(component, tableName, name).add();
			table.unique("ORM_U_COLUMNPOSITION").on(component, tableName , position).add();
			table.foreignKey("ORM_FK_COLUMNTABLE").on(component,tableName).references(ORM_TABLE.name()).onDelete(CASCADE).
				map("table").reverseMap("columns").reverseMapOrder("position").composition().add();
		}
	},
	ORM_TABLECONSTRAINT {
		public void addTo(DataModel dataModel) {
			Table<TableConstraint> table = dataModel.addTable(name(),TableConstraint.class);
			table.map(TableConstraintImpl.implementers);
			Column component = table.column("COMPONENT").type(COMPONENTDBTYPE).notNull().add();
			Column tableName = table.column("TABLEID").type(CATALOGDBTYPE).notNull().add();
			Column name = table.column("NAME").type(CATALOGDBTYPE).notNull().map("name").add();
			table.addDiscriminatorColumn("CONSTRAINTTYPE", CATALOGDBTYPE);
			Column position = table.addPositionColumn();
			Column referencedComponent = table.column("REFERENCEDCOMPONENT").type(COMPONENTDBTYPE).add();
			Column referencedTable = table.column("REFERENCEDTABLENAME").type(CATALOGDBTYPE).add();
			table.column("DELETERULE").type(CATALOGDBTYPE).conversion(CHAR2ENUM).map("deleteRule").add();
			table.column("FIELDNAME").varChar(NAME_LENGTH).map("fieldName").add();
			table.column("REVERSEFIELDNAME").varChar(NAME_LENGTH).map("reverseFieldName").add();
			table.column("REVERSEORDERFIELDNAME").varChar(NAME_LENGTH).map("reverseOrderFieldName").add();
			table.column("REVERSECURRENTFIELDNAME").varChar(NAME_LENGTH).map("reverseCurrentFieldName").add();
			table.column("COMPOSITION").type("CHAR(1)").conversion(CHAR2BOOLEAN).map("composition").add();
			table.primaryKey("ORM_PK_CONSTRAINT").on(component , tableName , name).add();
			table.unique("ORM_U_CONSTRAINT").on(name).add();
			table.unique("ORM_U_CONSTRAINT2").on(component,tableName,position).add();
			table.foreignKey("ORM_FK_CONSTRAINTTABLE").on(component , tableName).references(ORM_TABLE.name()).onDelete(CASCADE).
				map("table").reverseMap("constraints").reverseMapOrder("position").composition().add();
			table.foreignKey("ORM_FK_CONSTRAINTTABLE2").on(referencedComponent, referencedTable).references(ORM_TABLE.name()).onDelete(RESTRICT).map("referencedTable").add();
		}
	},
	ORM_COLUMNINCONSTRAINT {
		public void addTo(DataModel dataModel) {
			Table<ColumnInConstraintImpl> table = dataModel.addTable(name(),ColumnInConstraintImpl.class);
			table.map(ColumnInConstraintImpl.class);
			Column componentName = table.column("COMPONENT").type(COMPONENTDBTYPE).notNull().add();
			Column tableName = table.column("TABLENAME").type(CATALOGDBTYPE).notNull().add();
			Column constraintNameColumn = table.column("CONSTRAINTNAME").type(CATALOGDBTYPE).notNull().add();
			Column columnNameColumn = table.column("COLUMNNAME").type(CATALOGDBTYPE).notNull().map("columnName").add();
			Column positionColumn = table.column("POSITION").number().notNull().conversion(NUMBER2INT).map("position").add();
			table.primaryKey("ORM_PK_COLUMNINCONSTRAINT").on(componentName , tableName , constraintNameColumn , columnNameColumn).add();
			table.unique("ORM_U_COLUMNINCONSTRAINT").on(componentName , tableName , constraintNameColumn , positionColumn).add();
			table.foreignKey("ORM_FK_COLUMNINCONSTRAINT1").on(componentName, tableName, constraintNameColumn).references(ORM_TABLECONSTRAINT.name()).onDelete(CASCADE).
				map("constraint").reverseMap("columnHolders").reverseMapOrder("position").composition().add();
			table.foreignKey("ORM_FK_COLUMNINCONSTRAINT2").on(componentName, tableName, columnNameColumn ).references(ORM_COLUMN.name()).onDelete(RESTRICT).map("column").add();
		}
	};

	private static final String COMPONENTDBTYPE = "varchar2(3)";
	private static final String CATALOGDBTYPE = "varchar2(30)";

	public abstract void addTo(DataModel dataModel);

}