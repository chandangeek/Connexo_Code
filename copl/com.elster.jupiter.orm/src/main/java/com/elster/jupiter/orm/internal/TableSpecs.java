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

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
	
	ORM_DATAMODEL {		
		public void addTo(DataModel dataModel) {
			Table<DataModel> table = dataModel.addTable(name(),DataModel.class,DataModelImpl.class);
			Column nameColumn = table.column("NAME").type(COMPONENTDBTYPE).notNull().map("name").add();
			table.column("DESCRIPTION").type("varchar2(80)").map("description").add();
			table.primaryKey("ORM_PK_COMPONENT").on(nameColumn).add();
		}
	},
	ORM_TABLE {
		@SuppressWarnings("rawtypes")
		public void addTo(DataModel dataModel) {
			Table<Table> table = dataModel.addTable(name(),Table.class,TableImpl.class);
			Column componentName = table.column("COMPONENT").type(COMPONENTDBTYPE).notNull().add();
			Column nameColumn = table.column("NAME").type(CATALOGDBTYPE).notNull().map("name").add();
			Column positionColumn = table.column("POSITION").number().notNull().conversion(NUMBER2INT).map("position").add();
			Column schemaColumn = table.column("SCHEMAOWNER").type(CATALOGDBTYPE).map("schema").add();
			table.column("JOURNALTABLENAME").type(CATALOGDBTYPE).map("journalTableName").add();
			table.column("CACHED").bool().map("cached").add();
			table.column("INDEXORGANIZED").bool().map("indexOrganized").add();
			table.primaryKey("ORM_PK_TABLE").on(componentName , nameColumn).add();
			table.unique("ORM_U_TABLE").on(schemaColumn , nameColumn).add();
			table.unique("ORM_U_TABLEPOSITION").on(componentName , positionColumn).add();
			table.foreignKey("ORM_FK_TABLEDATAMODEL").on(componentName).references(ORM_DATAMODEL.name()).onDelete(CASCADE).
				map("dataModel").reverseMap("tables").composition().add();
		}
	},
	ORM_COLUMN {	
		public void addTo(DataModel dataModel) {
			Table<Column> table = dataModel.addTable(name(),Column.class, ColumnImpl.class);
			Column componentName = table.column("COMPONENT").type(COMPONENTDBTYPE).notNull().add();
			Column tableName = table.column("TABLENAME").type(CATALOGDBTYPE).notNull().add();		
			Column nameColumn = table.column("NAME").type(CATALOGDBTYPE).notNull().map("name").add();
			Column positionColumn = table.column("POSITION").number().notNull().conversion(NUMBER2INT).map("position").add();
			Column fieldNameColumn = table.column("FIELDNAME").type("varchar2(80)").map("fieldName").add();
			table.column("DBTYPE").type(CATALOGDBTYPE).notNull().map("dbType").add();
			table.column("NOTNULL").bool().map("notNull").add();
			table.column("VERSIONCOUNT").bool().map("versionCount").add();
			table.column("CONVERSION").type("varchar2(30)").notNull().conversion(CHAR2ENUM).map("conversion").add();
			table.column("SKIPONUPDATE").bool().map("skipOnUpdate").add();
			table.column("SEQUENCENAME").type(CATALOGDBTYPE).map("sequenceName").add();
			table.column("INSERTVALUE").type("varchar2(256)").map("insertValue").add();
			table.column("UPDATEVALUE").type("varchar2(256)").map("updateValue").add();
			table.primaryKey("ORM_PK_COLUMNS").on(componentName, tableName, nameColumn).add();
			table.unique("ORM_U_COLUMNPOSITION").on(componentName , tableName , positionColumn).add();
			table.unique("ORM_U_COLUMNFIELDNAME").on(componentName , tableName , fieldNameColumn).add();
			table.foreignKey("ORM_FK_COLUMNTABLE").on(componentName,tableName).references(ORM_TABLE.name()).onDelete(CASCADE).
				map("table").reverseMap("columns").reverseMapOrder("position").composition().add();
		}
	},
	ORM_TABLECONSTRAINT {	
		public void addTo(DataModel dataModel) {
			Table<TableConstraint> table = dataModel.addTable(name(),TableConstraint.class,TableConstraintImpl.class);
			Column componentName = table.column("COMPONENT").type(COMPONENTDBTYPE).notNull().add();
			Column tableName = table.column("TABLEID").type(CATALOGDBTYPE).notNull().add();
			Column nameColumn = table.column("NAME").type(CATALOGDBTYPE).notNull().map("name").add();
			table.addDiscriminatorColumn("CONSTRAINTTYPE", CATALOGDBTYPE);
			Column referencedComponentName = table.column("REFERENCEDCOMPONENT").type(COMPONENTDBTYPE).add();
			Column referencedTableName = table.column("REFERENCEDTABLENAME").type(CATALOGDBTYPE).add();
			table.column("DELETERULE").type(CATALOGDBTYPE).conversion(CHAR2ENUM).map("deleteRule").add();
			table.column("FIELDNAME").type("VARCHAR2(80)").map("fieldName").add();
			table.column("REVERSEFIELDNAME").type("VARCHAR2(80)").map("reverseFieldName").add();
			table.column("REVERSEORDERFIELDNAME").type("VARCHAR2(80)").map("reverseOrderFieldName").add();
			table.column("REVERSECURRENTFIELDNAME").type("VARCHAR2(80)").map("reverseCurrentFieldName").add();
			table.column("COMPOSITION").type("CHAR(1)").conversion(CHAR2BOOLEAN).map("composition").add();
			table.primaryKey("ORM_PK_CONSTRAINT").on(componentName , tableName , nameColumn).add();
			table.unique("ORM_U_CONSTRAINT").on(nameColumn).add();
			table.foreignKey("ORM_FK_CONSTRAINTTABLE").on(componentName , tableName).references(ORM_TABLE.name()).onDelete(CASCADE).
				map("table").reverseMap("constraints").composition().add();	
			table.foreignKey("ORM_FK_CONSTRAINTTABLE2").on(referencedComponentName, referencedTableName).references(ORM_TABLE.name()).onDelete(RESTRICT).map("referencedTable").add();
		}
	},
	ORM_COLUMNINCONSTRAINT {
		public void addTo(DataModel dataModel) {
			Table<ColumnInConstraintImpl> table = dataModel.addTable(name(),ColumnInConstraintImpl.class,ColumnInConstraintImpl.class);
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