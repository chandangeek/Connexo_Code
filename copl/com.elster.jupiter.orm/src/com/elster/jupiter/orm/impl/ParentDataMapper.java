package com.elster.jupiter.orm.impl;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.util.SqlBuilder;

public class ParentDataMapper<T> extends JoinDataMapper<T> {
	private TableConstraint constraint;
	
	public ParentDataMapper(DataMapperImpl<T,? extends T> dataMapper,TableConstraint constraint, String alias) {
		super(dataMapper, alias);	
		this.constraint = constraint;
	}
	
	T set(Object target , ResultSet rs , int index) throws SQLException {
		T value = null;
		if (target != null) {
			Object key = getMapper().getPrimaryKey(rs, index);
			if (key != null) {
				value = get(key);
				if (value == null) {
					value = getMapper().construct(rs,index);
					put(key, value);
				}
				if (constraint.getFieldName() != null) {
					new FieldMapper().set(target,constraint.getFieldName(),value);
				}
			}
		}
		return value;
	}
	
	@Override
	boolean appendFromClause(SqlBuilder builder, String parentAlias, boolean marked, boolean forceOuterJoin) {
		boolean outerJoin = forceOuterJoin || (!constraint.isNotNull() && !marked);
		if (outerJoin) {
			builder.append(" LEFT");
		}
		builder.append(" JOIN ");
		appendTable(builder);
		builder.append(" ON ");
		builder.openBracket();
		List<Column> primaryKeyColumns = getTable().getPrimaryKeyColumns();
		List<Column> foreignKeyColumns = constraint.getColumns();
		String separator = "";
		for ( int i = 0 ; i < primaryKeyColumns.size() ; i++) {
			builder.append(separator);
			builder.append(foreignKeyColumns.get(i).getName(parentAlias));
			builder.append(" = ");
			builder.append(primaryKeyColumns.get(i).getName(getAlias()));
			separator = " AND ";
		}	
		builder.closeBracketSpace();
		return outerJoin;
	}
	

	@Override
	String getName() {		
		return constraint.getFieldName();
	}

	@Override
	boolean canRestrict() {	
		return true;
	}

	
	
}
