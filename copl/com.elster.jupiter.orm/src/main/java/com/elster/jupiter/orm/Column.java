package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

/**
 * Describes a column mapping.
 */
@ProviderType
public interface Column {
	/**
	 * The dummy fieldname of a discriminator column
	 */
	String TYPEFIELDNAME = "class";

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

		Builder installValue(String value);
	}

	@ProviderType
	interface VirtualBuilder {
		VirtualBuilder alias(String name);
		Column add();
	}
}
