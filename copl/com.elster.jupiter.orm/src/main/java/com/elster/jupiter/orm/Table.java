package com.elster.jupiter.orm;

import java.util.List;
import java.util.Map;

public interface Table {	
	DataModel getDataModel();
	String getSchema();	
	String getName();
	String getQualifiedName();
    List<Column> getColumns();
    List<TableConstraint> getConstraints();
	TableConstraint getPrimaryKeyConstraint();
    List<ForeignKeyConstraint> getForeignKeyConstraints();
	ForeignKeyConstraint getConstraintForField(String fieldName);
	String getComponentName();
	Column getColumn(String name);	
	Column getColumnForField(String fieldName);
    List<Column> getPrimaryKeyColumns();
	<T> DataMapper<T> getDataMapper(Class<T> api , Class<? extends T> implementation);
	<T> DataMapper<T> getDataMapper(Class<T> api , Map<String,Class<? extends T>> implementations);
	Object getPrimaryKey(Object value);
	FieldType getFieldType(String fieldName);
	
	// install time api
	Column addColumn(String name , String dbType , boolean notnull , ColumnConversion conversion , String fieldName);
	Column addColumn(String name, String dbType, boolean notnull,ColumnConversion conversion, String fieldName, String insertValue,String updateValue);
	Column addColumn(String name, String dbType, boolean notnull,ColumnConversion conversion, String fieldName, String insertValue,boolean skipOnUpdate);
	Column addAutoIncrementColumn(String name , String dbType, ColumnConversion conversion , String fieldName , String sequence, boolean skipOnUpdate);
	Column addVersionCountColumn(String name , String dbType , String fieldName );
	Column addDiscriminatorColumn(String name, String dbType);
	TableConstraint addPrimaryKeyConstraint(String name , Column... columns);	
	TableConstraint addUniqueConstraint(String name , Column... colums);
	TableConstraint addForeignKeyConstraint(String name , Table referencedTable , DeleteRule deleteRule,AssociationMapping mapping , Column ... columns); 			
	TableConstraint addForeignKeyConstraint(String name, String tableName, DeleteRule deleteRule, AssociationMapping mapping , Column... columns);
	TableConstraint addForeignKeyConstraint(String name , String component , String tableName, DeleteRule deleteRule, String fieldName, Column... columns);
	Column addCreateTimeColumn(String name, String fieldName);
	Column addModTimeColumn(String name, String fieldName);
	Column addUserNameColumn(String name, String fieldName);
	Column addAutoIdColumn();
    List<Column> addAuditColumns();
	void setJournalTableName(String journalTableName);
	String getJournalTableName();
	void makeIndexOrganized();
	boolean isIndexOrganized();
	boolean hasJournal();
    List<Column> addQuantityColumns(String name, boolean notNull, String fieldName);
    List<Column> addMoneyColumns(String name, boolean notNull, String fieldName);
    List<Column> addIntervalColumns(String fieldName);
}
