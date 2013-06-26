package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.util.units.Unit;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Date;

// naming convention is DATABASE TYPE 2 JAVATYPE 
public enum ColumnConversionImpl {
	NOCONVERSION { // 0
		@Override
		public Object convertToDb(Object value) {
			return value;
		}
			
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			return rs.getObject(index);
		}
		
		@Override
		public Object convert(String in) {
			return in;
		}
	},
	NUMBER2INT { // 1
		@Override
		public Object convertToDb(Object value) {
			return value;
		}	
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			return rs.getInt(index);
		}
		
		@Override
		public Object convert(String in) {
			return Integer.valueOf(in);
		}
	},
	// database null maps to 0 and vice versa
	NUMBER2INTNULLZERO { // 2
		@Override
		public Object convertToDb(Object value) {
			return ((Integer) value) == 0 ? null : value;
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			return rs.getInt(index);
		}
		
		@Override
		public Object convert(String in) {
			return Integer.valueOf(in);
		}
	},
	NUMBER2LONG { // 3
		@Override
		public Object convertToDb(Object value) {
			return value;
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			return rs.getLong(index);
		}
		
		@Override
		public Object convert(String in) {
			return Long.valueOf(in);
		}
	},
	//database null maps to 0 and vice versa
	NUMBER2LONGNULLZERO { // 4
		@Override
		public Object convertToDb(Object value) {
			return ((Long) value) == 0 ? null : value;
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			return rs.getLong(index); 
		}
		
		@Override
		public Object convert(String in) {
			return Long.valueOf(in);
		}
	},
	CHAR2BOOLEAN { // 5
		@Override
		public Object convertToDb(Object value) {
			return value == null ? null : (Boolean) value ? "Y" : "N";
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			return "Y".equals(rs.getString(index));
		}
		
		@Override
		public Object convert(String in) {		
			return Bus.toBoolean(in);
		}
	},
	NUMBER2BOOLEAN { // 6
		@Override
		public Object convertToDb(Object value) {
			return ((Boolean) value) ? 1 : 0;
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			return 1 == rs.getInt(index);
		}
		
		@Override
		public Object convert(String in) {
			return Bus.toBoolean(in);
		}
	},
	// number to com.elste.jupiter.time.Instant, waiting for java.time.Instant
	NUMBER2UTCINSTANT { // 7
		@Override
		public Object convertToDb(Object value) {
			return getTime(value);
		}	
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			long value = rs.getLong(index);				 
			return rs.wasNull() ? null : new UtcInstant(value);
		}
		
		@Override
		public Object convert(String in) {
			return  new UtcInstant(Long.valueOf(in));
		}
	},
	// persistence layer will automatically update UtcInstant field to current time
	NUMBER2NOW { // 8
		@Override
		public Object convertToDb(Object value) {
			return getTime(value);
		}	
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			return new UtcInstant(rs.getLong(index));
		}		
		
		@Override
		public Object convert(String in) {
			return  new UtcInstant(Long.valueOf(in));
		}
	},
	// convert number to enum field by ordinal
	NUMBER2ENUM { // 9
		@Override
		public Object convertToDb(Object value) {
			return value == null ? null : ((Enum<?>) value).ordinal();			
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			// returns the enum ordinal , create the enum with correct ordinal is handled by Mapper implementation
			int value = rs.getInt(index);
			return rs.wasNull() ? null : value;
		}
		
		public Object convert(String in) {
			throw new UnsupportedOperationException();
		}
		
	},
	// convert number to enum field by ordinal + 1
	// useful in to avoid 0 in database, which often means Not Applicable 
	NUMBER2ENUMPLUSONE { // 10
		@Override
		public Object convertToDb(Object value) {
			return value == null ? null : ((Enum<?>) value).ordinal() + 1;			
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			// returns the enum ordinal , create the enum with correct ordinal is handled by Mapper implementation
			int value = rs.getInt(index);
			return rs.wasNull() ? null : value - 1;
		}
		
		public Object convert(String in) {
			throw new UnsupportedOperationException();
		}
		
	},
	CHAR2ENUM {  // 11
		@Override
		public Object convertToDb(Object value) {
			return value == null ? null : ((Enum<?>) value).name();			
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			// returns the enum name , create the enum with correct ordinal is handled by Mapper implementation
			return rs.getString(index);
		}
		
		@Override
		public Object convert(String in) {
			throw new UnsupportedOperationException();
		}
	},
	CHAR2PRINCIPAL { // 12
		@Override
		// persistence layer will automatically update String field to current principal
		public Object convertToDb(Object value) {
			return value;
		}
		
		@Override
		public Object convertFromDb(ResultSet rs,int index) throws SQLException {
			return rs.getString(index);
		}
		@Override
		public Object convert(final String in) {
			return in;
		}
	},
	
	NUMBER2INTWRAPPER { // 13
		@Override
		public Object convertToDb(Object value) {
			return  value;
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			int result = rs.getInt(index);
			return rs.wasNull() ? null : result;						
		}
		
		@Override
		public Object convert(String in) {
			return Integer.valueOf(in);
		}
	},
	CHAR2UNIT { // 14
		@Override
		public Object convertToDb(Object value) {
			return  value == null ? null : ((Unit) value).getAsciiSymbol();
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			String asciiSymbol = rs.getString(index);
			return asciiSymbol == null ? null : Unit.get(asciiSymbol);						
		}
		
		@Override
		public Object convert(String in) {
			return Unit.get(in);
		}
	},
	CHAR2CURRENCY { 
		@Override
		public Object convertToDb(Object value) {
			return  value == null ? null : ((Currency) value).getCurrencyCode();
		}
		
		@Override
		public Object convertFromDb(ResultSet rs, int index) throws SQLException {
			String iso4217Code = rs.getString(index);
			return iso4217Code == null ? null : Currency.getInstance(iso4217Code);						
		}
		
		@Override
		public Object convert(String in) {
			return Currency.getInstance(in);
		}
	},
    CHAR2FILE {
        @Override
        public Object convert(String in) {
            return new File(in);
        }

        @Override
        public Object convertToDb(Object value) {
            return ((File) value).getAbsolutePath();
        }

        @Override
        public Object convertFromDb(ResultSet rs, int index) throws SQLException {
            String fileName = rs.getString(index);
            return fileName == null ? null : convert(fileName);
        }
    };

	public abstract Object convertToDb(Object value);
	public abstract Object convertFromDb(ResultSet rs, int index) throws SQLException;
	public abstract Object convert(String in);
	
	Long getTime(Object value) {	
		if (value == null) {
			return null;
		}
		if (value instanceof Date) {
			return ((Date) value).getTime();
		} else if (value instanceof Long) {
			return (Long) value;			
		} else {
			return ((UtcInstant) value).getTime();
		}
	}

}
