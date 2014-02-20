package com.elster.jupiter.orm;

import java.util.List;
import java.util.Map;

/**
 * Describes a relational table, and its mapping to objects.
 * For all methods, name typically refers to the database name, 
 * while fieldName refers to an java instance field.
 */
public interface Table<T> {	
	// datamodel construction api
    Column.Builder column(String name);
    PrimaryKeyConstraint.Builder primaryKey(String name);
    UniqueConstraint.Builder unique(String name);
    ForeignKeyConstraint.Builder foreignKey(String name);
    Column addVersionCountColumn(String name , String dbType , String fieldName );
	Column addDiscriminatorColumn(String name, String dbType);
	Column addCreateTimeColumn(String name, String fieldName);
	Column addModTimeColumn(String name, String fieldName);
	Column addUserNameColumn(String name, String fieldName);
	Column addAutoIdColumn();
	Column addPositionColumn();
	List<Column> addAuditColumns();
    List<Column> addQuantityColumns(String name, boolean notNull, String fieldName);
    List<Column> addMoneyColumns(String name, boolean notNull, String fieldName);
    List<Column> addIntervalColumns(String fieldName);
    List<Column> addRefAnyColumns(String name , boolean notNull, String fieldName);
    void setJournalTableName(String journalTableName);
	void cache();
	void makeIndexOrganized();
    // meta data api
	List<String> getDdl();
	DataModel getDataModel();
	String getSchema();	
	String getName();
	String getQualifiedName();
	String getJournalTableName();
	boolean hasJournal();
	boolean isCached();
	boolean isIndexOrganized();
    List<? extends Column> getColumns();
    List<? extends TableConstraint> getConstraints();
	TableConstraint getPrimaryKeyConstraint();
    List<? extends ForeignKeyConstraint> getForeignKeyConstraints();
	String getComponentName();
	List<? extends Column> getPrimaryKeyColumns();
    Table<T> map(Class<? extends T> implementer);
    Table<T> map(Map<String,Class<? extends T>> implementers);
	boolean maps(Class<?> implementer);

}
