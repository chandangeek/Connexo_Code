package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.util.SortedSet;

/**
 * Describes a column mapping.
 */
@ProviderType
public interface Column {
	/**
	 * The dummy fieldname of a discriminator column
	 */
	String TYPEFIELDNAME = "class";

	/**
	 * The dummy fieldname of a discriminator column
	 */
	String MACFIELDNAME = "mac";

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
	Object getDatabaseValue(Object target);
	boolean skipOnUpdate();
	boolean hasUpdateValue();
	boolean hasInsertValue();
	boolean isEnum();
	boolean isNotNull();
	boolean isDiscriminator();
	boolean isVirtual();
	boolean isInVersion(Version version);

	Column since(Version version);
	Column upTo(Version version);
	Column during(Range... ranges);
	Column previously(Column column);

	SortedSet<Version> changeVersions();

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
		Builder blob();
		Builder number();
		Builder varChar(int length);
		Builder varChar();
		Builder sdoGeometry();

		Builder audited();

		Builder notAudited();

		Builder since(Version version);

		Builder upTo(Version version);

		Builder during(Range... ranges);

		Builder previously(Column column);

		VirtualBuilder as(String formula);
		Column add();

		/**
		 * This method allows you specify a default value for a to-be-added column. The value will be inserted as-is in SQL,
		 * this is a deliberate sql injection hook that allows calculating initial values using SQL formulas.
		 * As a side effect, if the initial value is a String, it needs to be surrounded by single quotes!
		 * @param value The SQL formula to calculate the initial value, constants are allowed, string constants need to be encapsulated by single quotes
         */
		Builder installValue(String value);
	}

	@ProviderType
	interface VirtualBuilder {
		VirtualBuilder alias(String name);
		Column add();
	}
}
