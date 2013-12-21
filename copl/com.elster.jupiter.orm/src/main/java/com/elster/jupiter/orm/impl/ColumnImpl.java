package com.elster.jupiter.orm.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.fields.impl.ColumnConversionImpl;

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
	private final Reference<TableImpl> table = ValueReference.absent();

	private ColumnImpl init(TableImpl table, String name) {
		if (name.length() > OrmService.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + name + " too long" );
		}
		this.table.set(table);
		this.name = name;
		return this;
	}
	
	static ColumnImpl from(TableImpl table, String name) {
		return new ColumnImpl().init(table,name);
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
	public TableImpl getTable() {
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
		return getTable().isPrimaryKeyColumn(this);		
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
		return sequenceName == null ? null : getTable().getQualifiedName(sequenceName);		
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
	
	private ColumnConversionImpl.JsonConverter jsonConverter() {
		return getTable().getDataModel().getInstance(ColumnConversionImpl.JsonConverter.class);
	}
	
	public Object convertToDb(Object value) {
		if (conversion == ColumnConversionImpl.CHAR2JSON) {
			return jsonConverter().convertToDb(value);
		} else {
			return conversion.convertToDb(value);
		}
	}
	
	Object convertFromDb(ResultSet rs, int index) throws SQLException {
		if (conversion == ColumnConversionImpl.CHAR2JSON) {
			return jsonConverter().convertFromDb(rs,index);
		} else {
			return conversion.convertFromDb(rs,index);
		}
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
		if (conversion == ColumnConversionImpl.CHAR2JSON) {
			return jsonConverter().convert(in);
		} else {
			return conversion.convert(in);
		}	
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
	
	ForeignKeyConstraintImpl getForeignKeyConstraint() {
		for (ForeignKeyConstraintImpl constraint : getTable().getForeignKeyConstraints()) {
			for (ColumnImpl column : constraint.getColumns()) {
				if (this.equals(column)) {
					return constraint;
				}
			}
		}
		return null;
	}
	
	static class BuilderImpl implements Column.Builder {
		private final ColumnImpl column;
		
		BuilderImpl(ColumnImpl column) {
			this.column = column;
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
			return notNull(true);
		}
		
		@Override
		public Builder notNull(boolean value) {
			column.notNull = value;
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
		public Builder varChar(int length) {
			if (length < 1) {
				throw new IllegalArgumentException("Illegal length: " + length);
			}
			if (length > 4000) {
				// may need to adjust for non oracle or oracle 12
				throw new IllegalArgumentException("" + length + " exceeds max varchar size");
			}
			return this.type("VARCHAR2(" + length + ")");
		}
		
		@Override
		public Column add() {
			column.validate();
			return column.getTable().add(column);
		}
		
	}
}

