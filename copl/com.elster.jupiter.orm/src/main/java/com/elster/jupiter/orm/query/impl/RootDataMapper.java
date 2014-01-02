package com.elster.jupiter.orm.query.impl;

import java.sql.*;
import java.util.*;

import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.util.sql.SqlBuilder;

class RootDataMapper<T> extends JoinDataMapper<T> {
	
	RootDataMapper(DataMapperImpl<T> mapper) {
		super(mapper,mapper.getAlias());
	}
	
	@SuppressWarnings("unchecked")
	T set(Object target, ResultSet rs, int index ) throws SQLException {
		KeyValue key = getMapper().getPrimaryKey(rs,1);
		T value = get(key);
		if (value == null) {
			value = getMapper().construct(rs,1);
			put(key, value);
			((List<T>) target).add(value);
		}
		return value;
	}
	
	
	String reduce(String fieldName) {
		return fieldName;
	}

	@Override
	String getName() {
		return null;
	}

	@Override
	boolean canRestrict() {
		return true;
	}

	@Override
	boolean appendFromClause(SqlBuilder builder, String parentAlias, boolean isMarked, boolean forceOuterJoin) {
		appendTable(builder);
		return forceOuterJoin;
	}
	
	@Override
	public boolean isReachable() {
		return true;
	}
}


