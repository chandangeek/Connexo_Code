/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.orm.Blob;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.IOResource;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.geo.SpatialCoordinatesFactory;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.units.Unit;

import javax.inject.Inject;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;

// naming convention is DATABASE TYPE 2 JAVATYPE
public enum ColumnConversionImpl {
	NOCONVERSION { // 0
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value;
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			return rs.getObject(index);
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return in;
		}
	},
	NUMBER2INT { // 1
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value;
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			return rs.getInt(index);
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return Integer.valueOf(in);
		}
	},
	// database null maps to 0 and vice versa
	NUMBER2INTNULLZERO { // 2
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value == null || ((Integer) value) == 0 ? null : value;
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			return rs.getInt(index);
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return Integer.valueOf(in);
		}
	},
	NUMBER2LONG { // 3
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value;
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			return rs.getLong(index);
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return Long.valueOf(in);
		}
	},
	//database null maps to 0 and vice versa
	NUMBER2LONGNULLZERO { // 4
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value == null || ((Long) value) == 0 ? null : value;
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			return rs.getLong(index);
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return Long.valueOf(in);
		}
	},
	CHAR2BOOLEAN { // 5
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value == null ? null : (Boolean) value ? "Y" : "N";
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			return "Y".equals(rs.getString(index)) || "YES".equalsIgnoreCase(rs.getString(index));
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return toBoolean(in);
		}
	},
	NUMBER2BOOLEAN { // 6
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value == null ? null : (Boolean) value ? 1 : 0;
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			return 1 == rs.getInt(index);
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return toBoolean(in);
		}
	},
	// persistence layer will automatically update UtcInstant field to current time
	NUMBER2NOW { // 8
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return getTime(value);
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			return Instant.ofEpochMilli(rs.getLong(index));
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return  Instant.ofEpochMilli(Long.valueOf(in));
		}
	},
	// convert number to enum field by ordinal
	NUMBER2ENUM { // 9
        @Override
        public boolean isEnumRelated() {
            return true;
        }

        @Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value == null ? null : ((Enum<?>) value).ordinal();
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			// returns the enum ordinal , create the enum with correct ordinal is handled by Mapper implementation
			int value = rs.getInt(index);
            if (rs.wasNull()) {
                return null;
            } else {
                return column.createEnum(value);
            }
		}

		public Object convert(ColumnImpl column, String in) {
			return column.createEnum(in);
		}
	},
	// convert number to enum field by ordinal + 1
	// useful in to avoid 0 in database, which often means Not Applicable
	NUMBER2ENUMPLUSONE { // 10
        @Override
        public boolean isEnumRelated() {
            return true;
        }

        @Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value == null ? null : ((Enum<?>) value).ordinal() + 1;
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			// returns the enum ordinal , create the enum with correct ordinal is handled by Mapper implementation
			int value = rs.getInt(index);
            if (rs.wasNull()) {
                return null;
            } else {
                return column.createEnum(value - 1);
            }
		}

		public Object convert(ColumnImpl column, String in) {
			return column.createEnum(in);
		}
	},
	CHAR2ENUM {  // 11
        @Override
        public boolean isEnumRelated() {
            return true;
        }

        @Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value == null ? null : ((Enum<?>) value).name();
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			// returns the enum name , create the enum with correct ordinal is handled by Mapper implementation
			return column.createEnum(rs.getString(index));
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return column.createEnum(in);
		}
	},
	CHAR2PRINCIPAL { // 12
		@Override
		// persistence layer will automatically update String field to current principal
		public Object convertToDb(ColumnImpl column, Object value) {
			return value;
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			return rs.getString(index);
		}
		@Override
		public Object convert(ColumnImpl column, final String in) {
			return in;
		}
	},

	NUMBER2INTWRAPPER { // 13
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return  value;
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			int result = rs.getInt(index);
			return rs.wasNull() ? null : result;
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return Integer.valueOf(in);
		}
	},
	NUMBER2LONGWRAPPER {
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return  value;
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			long result = rs.getLong(index);
			return rs.wasNull() ? null : result;
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return Long.valueOf(in);
		}
	},
	CHAR2UNIT { // 14
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return  value == null ? null : ((Unit) value).getAsciiSymbol();
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			String asciiSymbol = rs.getString(index);
			return asciiSymbol == null ? null : Unit.get(asciiSymbol);
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return Unit.get(in);
		}
	},
	CHAR2CURRENCY {
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return  value == null ? null : ((Currency) value).getCurrencyCode();
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			String iso4217Code = rs.getString(index);
			return iso4217Code == null ? null : Currency.getInstance(iso4217Code);
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return Currency.getInstance(in);
		}
	},
    CHAR2FILE {
        @Override
        public Object convert(ColumnImpl column, String in) {
            return new File(in);
        }

        @Override
        public Object convertToDb(ColumnImpl column, Object value) {
            return ((File) value).getAbsolutePath();
        }

        @Override
        public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
            String fileName = rs.getString(index);
            return fileName == null ? null : convert(column, fileName);
        }
    },
    CHAR2JSON {
        @Override
        public Object convert(ColumnImpl column, String in) {
            return column.jsonConverter().convert(in);
        }

        @Override
        public Object convertToDb(ColumnImpl column, Object value) {
        	return column.jsonConverter().convertToDb(value);
        }

        @Override
        public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
            return column.jsonConverter().convertFromDb(rs, index);
        }
    },
    DATE2INSTANT {
    	@Override
        public Object convert(ColumnImpl column, String in) {
            return Instant.ofEpochMilli(Long.parseLong(in));
        }

        @Override
        public Object convertToDb(ColumnImpl column, Object value) {
        	return value == null ? null : new java.sql.Date(((Instant) value).toEpochMilli());
        }

        @Override
        public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
            java.sql.Date date = rs.getDate(index);
            return date == null ? null : Instant.ofEpochMilli(date.getTime());
        }

    },
    TIMESTAMP2INSTANT {
    	@Override
        public Object convert(ColumnImpl column, String in) {
            return Instant.ofEpochMilli(Long.parseLong(in));
        }

        @Override
        public Object convertToDb(ColumnImpl column, Object value) {
        	return value == null ? null : new java.sql.Timestamp(((Instant) value).toEpochMilli());
        }

        @Override
        public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
            java.sql.Timestamp timestamp = rs.getTimestamp(index);
            return timestamp == null ? null : Instant.ofEpochMilli(timestamp.getTime());
        }
    },
    CLOB2STRING {
    	@Override
        public Object convert(ColumnImpl column, String in) {
            return in;
        }

        @Override
        public Object convertToDb(ColumnImpl column, Object value) {
        	return value;
        }

        @Override
        public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
            return rs.getString(index);
        }

    },
    BLOB2BYTE {
    	@Override
        public Object convert(ColumnImpl column, String in) {
            return in == null ? null : in.getBytes();
        }

        @Override
        public Object convertToDb(ColumnImpl column, Object value) {
        	return value;
        }

        @Override
        public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
            return rs.getBytes(index);
        }
    },
    BLOB2SQLBLOB {
    	@Override
        public Object convert(ColumnImpl column, String in) {
		    return in;
        }

        @Override
        public Object convertToDb(ColumnImpl column, Object value) {
			if (value instanceof LazyLoadingBlob) {
				return value;
            } else if (value instanceof Blob) {
                return LazyLoadingBlob.from((Blob) value, column);
	        } else {
		        throw new IllegalArgumentException("Conversion " + name() + " only supports PersistentBlob and SimpleBlob values but not " + value.getClass().getName());
	        }
        }

        @Override
        public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) {
            return LazyLoadingBlob.from(column);
        }

        @Override
        public Optional<IOResource> setConvertedObject(ColumnImpl column, PreparedStatement statement, int index, Object dbValue) throws SQLException {
            if (dbValue instanceof LazyLoadingBlob) {
                LazyLoadingBlob lazyLoadingBlob = (LazyLoadingBlob) dbValue;
                lazyLoadingBlob.bindTo(statement, index);
                return Optional.of(lazyLoadingBlob);
            } else {
                return super.setConvertedObject(column, statement, index, dbValue);
            }
        }
    },
    NUMBERINUTCSECONDS2INSTANT {
    	@Override
        public Object convert(ColumnImpl column, String in) {
    		return Instant.ofEpochMilli(Long.parseLong(in));
        }

        @Override
        public Object convertToDb(ColumnImpl column, Object value) {
        	return value == null ? null : ((Instant) value).getEpochSecond();
        }

        @Override
        public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
        	long value = rs.getLong(index);
			return rs.wasNull() ? null : Instant.ofEpochSecond(value);
		}
    },
	NUMBER2INSTANT {
		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return getTime(value);
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			long value = rs.getLong(index);
			return rs.wasNull() ? null : Instant.ofEpochMilli(value);
		}

		@Override
		public Object convert(ColumnImpl column, String in) {
			return  Instant.ofEpochMilli(Long.valueOf(in));
		}
	},
	CHAR2PATH {
		@Override
		public Object convert(ColumnImpl column, String in) {
			return column.getTable().getDataModel().getFileSystem().getPath(in);
		}

		@Override
		public Object convertToDb(ColumnImpl column, Object value) {
			return value.toString();
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			String fileName = rs.getString(index);
			return fileName == null ? null : convert(column, fileName);
		}
	},

	SDOGEOMETRY2SPATIALGEOOBJ {
		@Override
		public Object convert(ColumnImpl column, String in) {
			return in == null ? null : new SpatialCoordinatesFactory().fromStringValue(in);
		}

		@Override
		public Object convertToDb(ColumnImpl column, Object value){
			try {
				return new SpatialCoordinatesFactory().valueToDb((SpatialCoordinates) value,column.getTable().getDataModel().getConnection(false));
			} catch (SQLException e) {
				throw new UnderlyingSQLFailedException(e);
			}
		}

		@Override
		public Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException {
			return new SpatialCoordinatesFactory().valueFromDb(rs.getObject(index));
		}

        @Override
        protected Optional<IOResource> setConvertedObject(ColumnImpl column, PreparedStatement statement, int index, Object dbValue) throws SQLException {
            if (dbValue == null) {
                statement.setNull(index, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
                return Optional.empty();
            } else {
                return super.setConvertedObject(column, statement, index, dbValue);
            }
        }
    };

	private static final String[] trueStrings = { "1" , "y" ,"yes" , "on" };

	private static boolean toBoolean(String in) {
		for (String each : trueStrings) {
			if (each.equalsIgnoreCase(in)) {
				return true;
			}
		}
		return Boolean.valueOf(in);
	}

    public boolean isEnumRelated() {
        return false;
    }

	public abstract Object convertToDb(ColumnImpl column, Object value);

	public abstract Object convertFromDb(ColumnImpl column, ResultSet rs, int index) throws SQLException;

	public abstract Object convert(ColumnImpl column, String in);

    public Optional<IOResource> setObject(ColumnImpl column, PreparedStatement statement, int index, Object domainValue) throws SQLException {
        return this.setConvertedObject(column, statement, index, this.convertToDb(column, domainValue));
    }

    protected Optional<IOResource> setConvertedObject(ColumnImpl column, PreparedStatement statement, int index, Object dbValue) throws SQLException {
        statement.setObject(index, dbValue);
        return Optional.empty();
    }

	Long getTime(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Instant) {
			return ((Instant) value).toEpochMilli();
		}
		if (value instanceof Long) {
			return (Long) value;
		} else {
			throw new IllegalArgumentException("" + value);
		}
	}

	public static class JsonConverter {
		private final JsonService jsonService;

		@Inject
		JsonConverter(JsonService jsonService) {
			this.jsonService = jsonService;
		}

        public Object convert(String in) {
            return jsonService.deserialize(in, Object[].class);
        }

        Object convertToDb(Object value) {
            return jsonService.serialize(value);
        }

        Object convertFromDb(ResultSet rs, int index) throws SQLException {
            String jsonString = rs.getString(index);
            return jsonString == null ? null : convert(jsonString);
        }
	}

}