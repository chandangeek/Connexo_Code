package com.elster.jupiter.orm;

/**
 * 
 * Describes a column mapping
 *
 */
public interface Column {
	/**
	 * The dummy fieldname of a discriminator column 
	 */
	public static final String TYPEFIELDNAME = "class";
	
	Table getTable();
	String getName();
	String getName(String alias);    
    String getFieldName();
    ColumnConversion getConversion();
    boolean isPrimaryKeyColumn();
	boolean isAutoIncrement();
	String getSequenceName();
	String getQualifiedSequenceName();
	boolean isVersion();
	String getInsertValue();
	String getUpdateValue();
	boolean skipOnUpdate();
	boolean hasUpdateValue();
	boolean hasInsertValue();
	boolean isEnum();
	boolean isNotNull();
	boolean isDiscriminator();
	
	interface Builder {
		Builder type(String type);
		Builder map(String field);
		Builder conversion(ColumnConversion conversion);
		Builder notNull();
		Builder sequence(String name);
		Builder insert(String pseudoLiteral);
		Builder update(String pseudoLiteral);
		Builder version();
		Builder skipOnUpdate();
		Builder bool();
		Column add();
		Builder number();
	}
}
