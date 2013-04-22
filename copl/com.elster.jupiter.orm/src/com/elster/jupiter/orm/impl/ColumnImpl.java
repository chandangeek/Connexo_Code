package com.elster.jupiter.orm.impl;

import java.sql.*;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.orm.plumbing.OrmClient;

public class ColumnImpl implements Column  {
	// persistent fields
	private String componentName;
	private String tableName;
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
	private Table table;

	@SuppressWarnings("unused")
	private ColumnImpl() {		
	}

	ColumnImpl(Table table, String name, String dbType , boolean notNull , ColumnConversion conversion , 
			String fieldName , String sequenceName , boolean versionCount, String insertValue , String updateValue , boolean skipOnUpdate) {
		if (name.length() > Bus.CATALOGNAMELIMIT) {
			throw new IllegalArgumentException("Name " + name + " too long" );
		}
		this.table = table;
		this.componentName = table.getComponentName();
		this.tableName = table.getName();
		this.name = name;
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
	
	@Override
	public Table getTable() {
		if (table == null) {
			return getOrmClient().getTableFactory().get(componentName,tableName);
		}
		return table;
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
	
	Object convertToDb(Object value) {
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
			!hasAutoValue(true);
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
}

