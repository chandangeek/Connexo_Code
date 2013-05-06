package com.elster.jupiter.orm;

public interface Column {
	public static final String TYPEFIELDNAME = "class";
	
	Table getTable();
	String getName();
	String getName(String alias);    
    String getFieldName();
    ColumnConversion getConversion();
    boolean isPrimaryKeyColumn();
	boolean isAutoIncrement();
	String getSequenceName();
	boolean isVersion();
	String getInsertValue();
	String getUpdateValue();
	boolean skipOnUpdate();
	boolean hasUpdateValue();
	boolean hasInsertValue();
	boolean isEnum();
	boolean isNotNull();
	boolean isDiscriminator();
}
