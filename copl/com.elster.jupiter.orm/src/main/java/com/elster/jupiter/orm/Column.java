package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

/**
 * Describes a column mapping.
 */
@ProviderType
public interface Column {
	/**
	 * The dummy fieldname of a discriminator column
	 */
	public static final String TYPEFIELDNAME = "class";

	Table<?> getTable();
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
	boolean isVirtual();

	@ProviderType
	interface Builder {
		Builder type(String type);
		Builder map(String field);
		Builder conversion(ColumnConversion conversion);
		Builder notNull();
		Builder notNull(boolean value);
		Builder sequence(String name);
		Builder insert(String pseudoLiteral);
		Builder update(String pseudoLiteral);
		Builder version();
		Builder skipOnUpdate();
		Builder bool();
		Builder number();
		Builder varChar(int length);
		Builder varChar();
		VirtualBuilder as(String formula);
		Column add();
	}

	@ProviderType
	interface VirtualBuilder {
		VirtualBuilder alias(String name);
		Column add();
	}
}
