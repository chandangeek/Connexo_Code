package com.elster.jupiter.orm.plumbing;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.*;

import com.elster.jupiter.orm.*;

public enum TableSpecs {
	
	ORM_COMPONENT {		
		void describeTable(Table table) {
			Column nameColumn = table.addColumn("NAME", COMPONENTDBTYPE , true , NOCONVERSION , "name");
			table.addColumn("DESCRIPTION", "varchar2(80)" , false , NOCONVERSION , "description");
			table.addPrimaryKeyConstraint("ORM_PK_COMPONENT", nameColumn);
		}
	},
	ORM_TABLE {
		void describeTable(Table table) {
			Column componentName = table.addColumn("COMPONENT", COMPONENTDBTYPE , true , NOCONVERSION , "componentName");
			Column nameColumn = table.addColumn("NAME", CATALOGDBTYPE , true , NOCONVERSION , "name");
			Column schemaColumn = table.addColumn("SCHEMAOWNER",CATALOGDBTYPE, false , NOCONVERSION,"schema");
			table.addColumn("JOURNALTABLENAME",CATALOGDBTYPE,false,NOCONVERSION,"journalTableName");
			table.addPrimaryKeyConstraint("ORM_PK_TABLES", componentName , nameColumn);
			table.addUniqueConstraint("ORM_U_TABLES", schemaColumn , nameColumn);
			table.addForeignKeyConstraint("ORM_FK_TABLESCOMPONENTS", ORM_COMPONENT.name(),CASCADE, "component" , "tables" ,  componentName);
		}
	},
	ORM_COLUMN {	
		void describeTable(Table table) {
			Column componentName = table.addColumn("COMPONENT", COMPONENTDBTYPE , true , NOCONVERSION , "componentName");
			Column tableName = table.addColumn("TABLENAME", CATALOGDBTYPE , true , NOCONVERSION, "tableName");		
			Column nameColumn = table.addColumn("NAME", CATALOGDBTYPE , true , NOCONVERSION , "name");
			Column positionColumn = table.addColumn("POSITION", "number" , true , NUMBER2INT , "position");
			Column fieldNameColumn = table.addColumn("FIELDNAME", "varchar2(80)" , true , NOCONVERSION , "fieldName");
			table.addColumn("DBTYPE", CATALOGDBTYPE , true , NOCONVERSION , "dbType");
			table.addColumn("NOTNULL" , "char(1)" , true , CHAR2BOOLEAN , "notNull" );
			table.addColumn("VERSIONCOUNT", "char(1)" , true , CHAR2BOOLEAN , "versionCount");
			table.addColumn("CONVERSION" , "varchar2(30)" , true , CHAR2ENUM , "conversion");
			table.addColumn("SKIPONUPDATE", "char(1)" , true , CHAR2BOOLEAN , "skipOnUpdate");	
			table.addColumn("SEQUENCENAME", CATALOGDBTYPE , false , NOCONVERSION , "sequenceName");
			table.addColumn("INSERTVALUE", "varchar2(80)" , false , NOCONVERSION , "insertValue");
			table.addColumn("UPDATEVALUE", "varchar2(80)" , false , NOCONVERSION , "updateValue");		
			table.addPrimaryKeyConstraint("ORM_PK_COLUMNS", new Column[] { componentName , tableName , nameColumn });
			table.addUniqueConstraint("ORM_U_COLUMNSPOSITION", componentName , tableName , positionColumn);
			table.addUniqueConstraint("ORM_U_COLUMNSFIELDNAME", componentName , tableName , fieldNameColumn);
			table.addForeignKeyConstraint("ORM_FK_COLUMNSTABLES", ORM_TABLE.name() , CASCADE, "table" , "columns" , componentName , tableName );
		}
	},
	ORM_TABLECONSTRAINT {	
		void describeTable(Table table) {
			Column componentName = table.addColumn("COMPONENT", COMPONENTDBTYPE , true , NOCONVERSION , "componentName");
			Column tableName = table.addColumn("TABLEID", CATALOGDBTYPE, true , NOCONVERSION , "tableName");
			Column nameColumn = table.addColumn("NAME", CATALOGDBTYPE , true , NOCONVERSION , "name");
			table.addColumn("CONSTRAINTTYPE", CATALOGDBTYPE , true , CHAR2ENUM , "type");
			Column referencedComponentName = table.addColumn("REFERENCEDCOMPONENT", COMPONENTDBTYPE , false , NOCONVERSION , "referencedComponentName");
			Column referencedTableName = table.addColumn("REFERENCEDTABLENAME", CATALOGDBTYPE , false , NOCONVERSION , "referencedTableName");
			table.addColumn("DELETERULE", CATALOGDBTYPE , false , CHAR2ENUM , "deleteRule");
			table.addColumn("FIELDNAME","VARCHAR2(80)" , false , NOCONVERSION , "fieldName");
			table.addColumn("REVERSEFIELDNAME","VARCHAR2(80)" , false , NOCONVERSION , "reverseFieldName");
			table.addColumn("REVERSECURRENTNAME","VARCHAR2(80)" , false , NOCONVERSION , "reverseCurrentName");
			table.addPrimaryKeyConstraint("ORM_PK_CONSTRAINTS", componentName , tableName , nameColumn);
			table.addUniqueConstraint("ORM_U_CONSTRAINTS", nameColumn);
			table.addForeignKeyConstraint("ORM_FK_CONSTRAINTSTABLES", ORM_TABLE.name() , CASCADE, "table" , "constraints" , componentName , tableName);		
			table.addForeignKeyConstraint("ORM_FK_CONSTRAINTSTABLES2", ORM_TABLE.name() , RESTRICT, "referencedTable" , null , referencedComponentName , referencedTableName );
		}
	},
	ORM_COLUMNINCONSTRAINT {
		void describeTable(Table table) {
			Column componentName = table.addColumn("COMPONENT", COMPONENTDBTYPE , true , NOCONVERSION , "componentName");
			Column tableName = table.addColumn("TABLENAME", CATALOGDBTYPE , true , NOCONVERSION , "tableName");
			Column constraintNameColumn = table.addColumn("CONSTRAINTNAME", CATALOGDBTYPE , true , NOCONVERSION , "constraintName");
			Column columnNameColumn = table.addColumn("COLUMNNAME", CATALOGDBTYPE , true , NOCONVERSION , "columnName");
			Column positionColumn = table.addColumn("POSITION", "number" , true , NUMBER2INT , "position");
			table.addPrimaryKeyConstraint("ORM_PK_COLUMNINCONSTRAINT", componentName , tableName , constraintNameColumn , columnNameColumn);
			table.addUniqueConstraint("ORM_U_COLUMNINCONSTRAINT", componentName , tableName , constraintNameColumn , positionColumn);
			table.addForeignKeyConstraint("ORM_FK_COLUMNINCONSTRAINT1", ORM_TABLECONSTRAINT.name() , CASCADE, "constraint" , null, componentName , tableName , constraintNameColumn);		
			table.addForeignKeyConstraint("ORM_FK_COLUMNINCONSTRAINT2", ORM_COLUMN.name() , RESTRICT, "column", null ,componentName , tableName , columnNameColumn );
		}
	};
	
	private static final String COMPONENTDBTYPE = "varchar2(3)";
	private static final String CATALOGDBTYPE = "varchar2(30)";

	void addTo(Component component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}