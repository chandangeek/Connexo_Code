package com.elster.jupiter.orm;

import java.util.List;
import java.util.Map;

/**
 * Describes a relational table, and its mapping to objects.
 * For all methods, name typically refers to the database name, 
 * while fieldName refers to an java instance field.
 */
public interface Table {	
	DataModel getDataModel();
	String getSchema();	
	String getName();
	/**
	 * 
	 * @return schema == null ? name : schema.name 
	 */
	String getQualifiedName();
    List<Column> getColumns();
    List<TableConstraint> getConstraints();
	TableConstraint getPrimaryKeyConstraint();
    List<ForeignKeyConstraint> getForeignKeyConstraints();
    /**
     * 
     * @param fieldName Name of the field that holds the reference to the object mapped to the corresponding tuple in the referenced (parent) table 
     */
	ForeignKeyConstraint getConstraintForField(String fieldName);
	String getComponentName();
	Column getColumn(String name);
	Column getColumnForField(String fieldName);
    List<Column> getPrimaryKeyColumns();
	<T> DataMapper<T> getDataMapper(Class<T> api , Class<? extends T> implementation);
	<T> DataMapper<T> getDataMapper(Class<T> api , Map<String,Class<? extends T>> implementations);
	/**
	 * Extracts the primary key from the given object
	 * @return an object array if primary key is composite, else the value of the primary key field 
	 */
	Object getPrimaryKey(Object value);
	FieldType getFieldType(String fieldName);
	/**
	 * Adds a column to the table
	 * @param name of the column in the database
	 * @param dbType used when generating the create table ddl
	 * @param notnull
	 * @param conversion
	 * @param fieldName
	 * @return
	 */
	Column addColumn(String name , String dbType , boolean notnull , ColumnConversion conversion , String fieldName);
	/**
	 * 
	 * @param name
	 * @param dbType
	 * @param notnull
	 * @param conversion
	 * @param fieldName
	 * @param insertValue the literal value used when inserting a new tuple. Typical example is SYSTIMESTAMP
	 * @param updateValue the literal value used when updating  a new tuple. Typical example is SYSTIMESTAMP
	 * @return
	 */
	Column addColumn(String name, String dbType, boolean notnull,ColumnConversion conversion, String fieldName, String insertValue,String updateValue);
	/**
	 * 
	 * @param name
	 * @param dbType
	 * @param notnull
	 * @param conversion
	 * @param fieldName
	 * @param insertValue
	 * @param skipOnUpdate if true, skip this column when generating an update sql
	 * @return
	 */
	Column addColumn(String name, String dbType, boolean notnull,ColumnConversion conversion, String fieldName, String insertValue,boolean skipOnUpdate);
	/**
	 * Creates an auto increment column whose value is driven by a sequence. This allows the caller to specify all the details,
	 * but applications should normally use addAutoIdColumn
	 * @param name
	 * @param dbType
	 * @param conversion
	 * @param fieldName
	 * @param sequence name. Must be unqualified and in the same schema as the table
	 * @param skipOnUpdate
	 * @return
	 */
	Column addAutoIncrementColumn(String name , String dbType, ColumnConversion conversion , String fieldName , String sequence, boolean skipOnUpdate);
	/**
	 * Adds a version column, used for optimistic locking. The column is set to 0 on initial insert,
	 * and incremented on every update.
	 * @param name
	 * @param dbType
	 * @param fieldName
	 * @return
	 */
	Column addVersionCountColumn(String name , String dbType , String fieldName );
	/**
	 * Adds a discriminator column when using single table inheritance mapping.
	 * Implementation note: The dummu fieldname of the column is set to the reserved word class 
	 * @param name
	 * @param dbType
	 * @return
	 */
	Column addDiscriminatorColumn(String name, String dbType);
	/**
	 * Add a primary key constraint
	 * @param name must be unique in the schema.
	 * @param columns
	 * @return
	 */
	TableConstraint addPrimaryKeyConstraint(String name , Column... columns);
	/**
	 * Adds a unique constraint. Note that in Oracle you can still have multiple all null values. 
	 * @param name must be unique in the schema
	 * @param colums
	 * @return
	 */
	TableConstraint addUniqueConstraint(String name , Column... colums);
	/**
	 * Adds a foreign key constraint
	 * @param name must be unique in the schema
	 * @param referencedTable parent table
	 * @param deleteRule RESTRICT | CASCADE | SET NULL
	 * @param mapping Contains field mapping info
	 * @param columns
	 * @return
	 */
	TableConstraint addForeignKeyConstraint(String name , Table referencedTable , DeleteRule deleteRule,AssociationMapping mapping , Column ... columns);
	/**
	 * 
	 * @param name
	 * @param tableName is resolved within the current DataModel
	 * @param deleteRule
	 * @param mapping
	 * @param columns
	 * @return
	 */
	TableConstraint addForeignKeyConstraint(String name, String tableName, DeleteRule deleteRule, AssociationMapping mapping , Column... columns);
	/**
	 * Used when the referenced table lives in another DataModel. 
	 * @param name
	 * @param component
	 * @param tableName
	 * @param deleteRule
	 * @param fieldName
	 * @param columns
	 * @return
	 */
	TableConstraint addForeignKeyConstraint(String name , String component , String tableName, DeleteRule deleteRule, String fieldName, Column... columns);
	/**
	 * Adds a column that will contain the tuple's insert time in ms since epoch (1/1/1970 UTC)
	 * @param name
	 * @param fieldName
	 * @return
	 */
	Column addCreateTimeColumn(String name, String fieldName);
	/**
	 * Adds a column that will contain the tuple's last modification time in ms since epoch (1/1/1970 UTC)
	 * @param name
	 * @param fieldName
	 * @return
	 */
	Column addModTimeColumn(String name, String fieldName);
	/**
	 * Adds a column that will contain the name of the users that performed the last modification
	 * @param name
	 * @param fieldName
	 * @return
	 */
	Column addUserNameColumn(String name, String fieldName);
	/**
	 * Adds an auto id column. The column name is set to ID, fieldName to id (a long), sequence name to the unqualified table name followed by ID  
	 */
	Column addAutoIdColumn();
	/**
	 * Shorthand for
	 *  	addVersionCountColumn("VERSIONCOUNT", "number" , "version"));
     *  	addCreateTimeColumn("CREATETIME", "createTime"));
     *   	addModTimeColumn("MODTIME", "modTime"));
     *   	addUserNameColumn("USERNAME", "userName"));
	 */
    List<Column> addAuditColumns();
    /**
     * Activates journaling for this table. When updating a tuple, the updated tuple is first copied to the journal table
     * @param journalTableName
     */
	void setJournalTableName(String journalTableName);
	String getJournalTableName();
	/**
	 * Make the table an index organized table
	 */
	void makeIndexOrganized();
	boolean isIndexOrganized();
	boolean hasJournal();
	/**
	 * Adds a quantity field to the table mapping. This creates three columns (name suffixed with VALUE , MULTIPLIER and UNIT 
	 * @param name
	 * @param notNull
	 * @param fieldName
	 * @return
	 */
    List<Column> addQuantityColumns(String name, boolean notNull, String fieldName);
    /**
     * Adds a money field to the table mapping. This creates two columns (name suffixed with VALUE , CURRENCY)
     * @param name
     * @param notNull
     * @param fieldName
     * @return
     */
    List<Column> addMoneyColumns(String name, boolean notNull, String fieldName);
    /**
     * Adds an interval field to the table mapping. This creates two columns (STARTTIME and STOPTIME) 
     * @param fieldName
     * @return
     */
    List<Column> addIntervalColumns(String fieldName);
}
