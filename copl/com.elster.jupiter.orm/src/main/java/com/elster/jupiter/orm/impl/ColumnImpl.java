package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.Reference;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.ValueReference;
import com.elster.jupiter.orm.fields.impl.ColumnConversionImpl;
import com.elster.jupiter.orm.internal.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ColumnImpl implements Column  {
	// persistent fields
	
	private String name;
	private int position;
	private String dbType;
	private boolean notNull;
	private ColumnConversionImpl conversion;
	private String fieldName;
	private String sequenceName;
	private boolean versionCount = false;
	private String insertValue;
	private String updateValue;
	private boolean skipOnUpdate;
	
	// associations
	private Reference<Table> table;

	@SuppressWarnings("unused")
	private ColumnImpl() {		
	}

	private ColumnImpl(Table table, String name) {
		if (name.length() > Bus.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + name + " too long" );
		}
		this.table = ValueReference.of(table);
		this.name = name;
	}
	
	ColumnImpl(Table table, String name, String dbType , boolean notNull , ColumnConversion conversion , 
			String fieldName , String sequenceName , boolean versionCount, String insertValue , String updateValue , boolean skipOnUpdate) {
		this(table,name);
		this.dbType = dbType;
		this.notNull = notNull;
		this.conversion = ColumnConversionImpl.valueOf(conversion.name());
		this.fieldName = fieldName;		
		this.sequenceName = sequenceName;				
		this.versionCount = versionCount;
		this.insertValue = insertValue;
		this.updateValue = updateValue;
		this.skipOnUpdate = skipOnUpdate;
	}
	
	private void validate() {
		if (!table.isPresent()) {
			throw new IllegalArgumentException("table must be present");
		}
		Objects.requireNonNull(name);
		Objects.requireNonNull(dbType);
		Objects.requireNonNull(conversion);
		if (skipOnUpdate && updateValue != null) {
			throw new IllegalArgumentException("updateValue must be null if skipOnUpdate");
		}
	}
	
	@Override
	public Table getTable() {
		return table.get();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getName(String alias) {
		return 
			alias == null || alias.length() == 0 ? name : alias + "." + name; 
	}
	
	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public ColumnConversion getConversion() {
		return ColumnConversion.valueOf(conversion.name());
	}
	
	@Override
	public String toString() {
		return 
			"Column " + name + " is " + 
			(isAutoIncrement() ? " auto increment " : "") + 
			"column " + position + " in table " + getTable().getQualifiedName();
	}

	void setPosition(int position) {
		this.position = position;
	}

	@Override
	public boolean isPrimaryKeyColumn() {
		return ((TableImpl) getTable()).isPrimaryKeyColumn(this);		
	}

	public String getDbType() {		
		return dbType;		
	}

	@Override
	public boolean isAutoIncrement() {
		return sequenceName != null && sequenceName.length() > 0;
	}
	
	@Override
	public boolean isVersion() {
		return versionCount;
	}
	
	@Override
	public String getSequenceName() {
		return sequenceName;
	}

	@Override
	public String getQualifiedSequenceName() {
		return sequenceName == null ? null : ((TableImpl) getTable()).getQualifiedName(sequenceName);		
	}
	
	@Override
	public String getInsertValue() {
		return insertValue;
	}
	
	@Override
	public boolean hasInsertValue() {
		return insertValue != null && insertValue.length() > 0;
	}
	
	@Override
	public String getUpdateValue() {
		return updateValue;
	}
	
	@Override
	public boolean skipOnUpdate() {
		return skipOnUpdate;
	}
	
	@Override
	public boolean hasUpdateValue() {
		return updateValue != null && updateValue.length() > 0;
	}
	
	void persist() {
		getOrmClient().getColumnFactory().persist(this);
	}
	
	public Object convertToDb(Object value) {
		return conversion.convertToDb(value);		
	}
	
	Object convertFromDb(ResultSet rs, int index) throws SQLException {
		return conversion.convertFromDb(rs,index);
	}
	
	private OrmClient getOrmClient() {
		return Bus.getOrmClient();
	}
	
	boolean isStandard() {
		return 
			!isPrimaryKeyColumn() &&
			!isVersion() &&
			!hasInsertValue() &&
			!skipOnUpdate() &&
			!hasAutoValue(true) &&
			!isDiscriminator();
	}
	
	boolean hasAutoValue(boolean update) {
		boolean auto = conversion == ColumnConversionImpl.NUMBER2NOW || conversion == ColumnConversionImpl.CHAR2PRINCIPAL;
		return update ? auto && !skipOnUpdate() : auto; 
	}
	
	boolean hasIntValue() {
		return conversion == ColumnConversionImpl.NUMBER2INT || conversion == ColumnConversionImpl.NUMBER2INTNULLZERO;
	}
	
	@Override 
	public boolean isEnum() {
		switch (conversion) {
			case NUMBER2ENUM:
			case NUMBER2ENUMPLUSONE:
			case CHAR2ENUM:
				return true;
			
			default:
				return false;
		}
	}
	
	Object convert(String in) {
		return conversion.convert(in);
	}
	
	@Override
	public boolean isNotNull() {
		return notNull;
	}

	@Override
	public boolean isDiscriminator() {
		return TYPEFIELDNAME.equals(fieldName);
	}
	
	boolean isForeignKeyPart() {
		return getForeignKeyConstraint() != null;
	}
	
	ForeignKeyConstraint getForeignKeyConstraint() {
		for (ForeignKeyConstraint constraint : getTable().getForeignKeyConstraints()) {
			for (Column column : constraint.getColumns()) {
				if (this.equals(column)) {
					return constraint;
				}
			}
		}
		return null;
	}
	
	static class BuilderImpl implements Column.Builder {
		private final ColumnImpl column;
		
		BuilderImpl(Table table , String name) {
			this.column = new ColumnImpl(table,name);
			column.conversion = ColumnConversionImpl.NOCONVERSION;
		}

		@Override
		public Builder type(String type) {
			column.dbType = type;
			return this;
		}

		@Override
		public Builder map(String field) {
			column.fieldName = field;
			return this;
		}

		@Override
		public Builder conversion(ColumnConversion conversion) {
			column.conversion = ColumnConversionImpl.valueOf(conversion.name());
			return this;
		}

		@Override
		public Builder notNull() {
			column.notNull = true;
			return this;
		}

		@Override
		public Builder sequence(String name) {
			column.sequenceName = name;
			return this;
		}

		@Override
		public Builder insert(String pseudoLiteral) {
			column.insertValue = pseudoLiteral;
			return this;
		}

		@Override
		public Builder update(String pseudoLiteral) {
			column.updateValue = pseudoLiteral;
			return this;
		}

		@Override
		public Builder version() {
			column.versionCount = true;
			return this;
		}

		@Override
		public Builder skipOnUpdate() {
			column.skipOnUpdate = true;
			return this;
		}
		
		@Override
		public Builder bool() {
			return this.type("CHAR(1)").notNull().conversion(ColumnConversion.CHAR2BOOLEAN);
		}
		
		@Override
		public Builder number() {
			return this.type("NUMBER");
		}

		@Override
		public Column add() {
			column.validate();
			return ((TableImpl) column.getTable()).add(column);
		}
		
	}
}

