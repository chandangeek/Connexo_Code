package com.elster.jupiter.orm.internal;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
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
		void describeTable(Table table) {
			table.map(DataModelImpl.class);
			Column nameColumn = table.column("NAME").type(COMPONENTDBTYPE).notNull().map("name").add();
			table.column("DESCRIPTION").type("varchar2(80)").map("description").add();
			table.primaryKey("ORM_PK_COMPONENT").on(nameColumn).add();
		}
	},
	ORM_TABLE {
		void describeTable(Table table) {
			table.map(TableImpl.class);
			Column componentName = table.column("COMPONENT").type(COMPONENTDBTYPE).notNull().map("componentName").add();
			Column nameColumn = table.column("NAME").type(CATALOGDBTYPE).notNull().map("name").add();
			Column schemaColumn = table.column("SCHEMAOWNER").type(CATALOGDBTYPE).map("schema").add();
			table.column("JOURNALTABLENAME").type(CATALOGDBTYPE).map("journalTableName").add();
			table.column("INDEXORGANIZED").bool().map("indexOrganized").add();
			table.primaryKey("ORM_PK_TABLES").on(componentName , nameColumn).add();
			table.unique("ORM_U_TABLES").on(schemaColumn , nameColumn).add();
			table.foreignKey("ORM_FK_TABLESCOMPONENTS").on(componentName).references(ORM_DATAMODEL.name()).onDelete(CASCADE).map("component").reverseMap("tables").add();
		}
	},
	ORM_COLUMN {	
		void describeTable(Table table) {
			table.map(ColumnImpl.class);
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
			table.unique("ORM_U_COLUMNSPOSITION").on(componentName , tableName , positionColumn).add();
			table.unique("ORM_U_COLUMNSFIELDNAME").on(componentName , tableName , fieldNameColumn).add();
			table.foreignKey("ORM_FK_COLUMNSTABLES").on(componentName,tableName).references(ORM_TABLE.name()).onDelete(CASCADE).map("table").reverseMap("columns").reverseMapOrder("position").add();
		}
	},
	ORM_TABLECONSTRAINT {	
		void describeTable(Table table) {
			table.map(TableConstraintImpl.implementers);
			Column componentName = table.addColumn("COMPONENT", COMPONENTDBTYPE , true , NOCONVERSION , "componentName");
			Column tableName = table.addColumn("TABLEID", CATALOGDBTYPE, true , NOCONVERSION , "tableName");
			Column nameColumn = table.addColumn("NAME", CATALOGDBTYPE , true , NOCONVERSION , "name");
			table.addDiscriminatorColumn("CONSTRAINTTYPE", CATALOGDBTYPE);
			Column referencedComponentName = table.addColumn("REFERENCEDCOMPONENT", COMPONENTDBTYPE , false , NOCONVERSION , "referencedComponentName");
			Column referencedTableName = table.addColumn("REFERENCEDTABLENAME", CATALOGDBTYPE , false , NOCONVERSION , "referencedTableName");
			table.addColumn("DELETERULE", CATALOGDBTYPE , false , CHAR2ENUM , "deleteRule");
			table.addColumn("FIELDNAME","VARCHAR2(80)" , false , NOCONVERSION , "fieldName");
			table.addColumn("REVERSEFIELDNAME","VARCHAR2(80)" , false , NOCONVERSION , "reverseFieldName");
			table.addColumn("REVERSEORDERFIELDNAME","VARCHAR2(80)" , false , NOCONVERSION , "reverseOrderFieldName");
			table.addColumn("REVERSECURRENTFIELDNAME","VARCHAR2(80)" , false , NOCONVERSION , "reverseCurrentFieldName");
			table.primaryKey("ORM_PK_CONSTRAINTS").on(componentName , tableName , nameColumn).add();
			table.unique("ORM_U_CONSTRAINTS").on(nameColumn).add();
			table.foreignKey("ORM_FK_CONSTRAINTSTABLES").on(componentName , tableName).references(ORM_TABLE.name()).onDelete(CASCADE).map("table").reverseMap("constraints").add();	
			table.foreignKey("ORM_FK_CONSTRAINTSTABLES2").on(referencedComponentName, referencedTableName).references(ORM_TABLE.name()).onDelete(RESTRICT).map("referencedTable").add();
		}
	},
	ORM_COLUMNINCONSTRAINT {
		void describeTable(Table table) {
			table.map(ColumnInConstraintImpl.class);
			Column componentName = table.addColumn("COMPONENT", COMPONENTDBTYPE , true , NOCONVERSION , "componentName");
			Column tableName = table.addColumn("TABLENAME", CATALOGDBTYPE , true , NOCONVERSION , "tableName");
			Column constraintNameColumn = table.addColumn("CONSTRAINTNAME", CATALOGDBTYPE , true , NOCONVERSION , "constraintName");
			Column columnNameColumn = table.addColumn("COLUMNNAME", CATALOGDBTYPE , true , NOCONVERSION , "columnName");
			Column positionColumn = table.addColumn("POSITION", "number" , true , NUMBER2INT , "position");
			table.primaryKey("ORM_PK_COLUMNINCONSTRAINT").on(componentName , tableName , constraintNameColumn , columnNameColumn).add();
			table.unique("ORM_U_COLUMNINCONSTRAINT").on(componentName , tableName , constraintNameColumn , positionColumn).add();
			table.foreignKey("ORM_FK_COLUMNINCONSTRAINT1").on(componentName, tableName, constraintNameColumn).references(ORM_TABLECONSTRAINT.name()).onDelete(CASCADE).map("constraint").add();		
			table.foreignKey("ORM_FK_COLUMNINCONSTRAINT2").on(componentName, tableName, columnNameColumn ).references(ORM_COLUMN.name()).onDelete(RESTRICT).map("column").add();
		}
	};
	
	private static final String COMPONENTDBTYPE = "varchar2(3)";
	private static final String CATALOGDBTYPE = "varchar2(30)";

	public void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}