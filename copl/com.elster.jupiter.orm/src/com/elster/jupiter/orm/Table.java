package com.elster.jupiter.orm;

import java.util.List;

public interface Table {	
	Component getComponent();
	String getSchema();	
	String getName();
	String getQualifiedName();
	List<Column> getColumns();
	List<TableConstraint> getConstraints();
	String getComponentName();
	Column getColumn(String name);	
	Column getColumnForField(String fieldName);
	Column[] getPrimaryKeyColumns();
	<T, S extends T> DataMapper<T> getDataMapper(Class<T> api , Class<S> implementation);
	
	// install time api
	Column addColumn(String name , String dbType , boolean notnull , ColumnConversion conversion , String fieldName);
	Column addColumn(String name, String dbType, boolean notnull,ColumnConversion conversion, String fieldName, String insertValue,String updateValue);
	Column addColumn(String name, String dbType, boolean notnull,ColumnConversion conversion, String fieldName, String insertValue,boolean skipOnUpdate);
	Column addAutoIncrementColumn(String name , String dbType, ColumnConversion conversion , String fieldName , String sequence, boolean skipOnUpdate);
	Column addVersionCountColumn(String name , String dbType , String fieldName );
	TableConstraint addPrimaryKeyConstraint(String name , Column... columns);	
	TableConstraint addUniqueConstraint(String name , Column... colums);
	TableConstraint addForeignKeyConstraint(String name , Table referencedTable , DeleteRule deleteRule, String fieldName , String reverseFieldName, Column... columns);
	TableConstraint addForeignKeyConstraint(String name , String tableName , DeleteRule deleteRule, String fieldName ,String reverseFieldName , Column... columns);
	TableConstraint addForeignKeyConstraint(String name , String component , String tableName, DeleteRule deleteRule, String fieldName , String reverseFieldName , Column... columns);
	Column addCreateTimeColumn(String name, String fieldName);
	Column addModTimeColumn(String name, String fieldName);
	Column addUserNameColumn(String name, String fieldName);
	Column addAutoIdColumn();
	void addAuditColumns();
	void setJournalTableName(String journalTableName);
	String getJournalTableName();
	boolean hasJournal();
	void addQuantityColumns(String name, boolean notNull, String fieldName);
	void addMoneyColumns(String name, boolean notNull, String fieldName);
	
}
