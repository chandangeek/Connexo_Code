package com.elster.jupiter.ids;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public enum FieldType {
	NUMBER {
		@Override
		public Object getValue(ResultSet resultSet, int i) throws SQLException {
			return resultSet.getBigDecimal(i);
		}
		
		@Override
		public void bind(PreparedStatement statement, int offset, Object object) throws SQLException {
			assert(object instanceof BigDecimal);
			statement.setObject(offset, object);
		}
	},
	DATE {
		@Override
		public Object getValue(ResultSet resultSet, int i) throws SQLException {
			long value = resultSet.getLong(i);
			return resultSet.wasNull() ? null : new Date(value);
		}
		
		@Override
		public void bind(PreparedStatement statement, int offset, Object object) throws SQLException {
			assert(object instanceof Date);
			statement.setLong(offset , ((Date) object).getTime());								
		}
	},
	LONGINTEGER {
		@Override
		public Object getValue(ResultSet resultSet, int i) throws SQLException {
			return resultSet.getLong(i);
		}
			
		@Override
		public void bind(PreparedStatement statement, int offset, Object object) throws SQLException {
			assert(object instanceof Long);
			statement.setObject(offset, object);
		}
	};		
	
	public abstract Object getValue(ResultSet resultSet, int i) throws SQLException;
	public abstract void bind(PreparedStatement statement, int offset, Object object) throws SQLException;
}
