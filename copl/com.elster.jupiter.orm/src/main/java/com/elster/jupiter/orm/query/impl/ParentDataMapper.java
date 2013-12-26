package com.elster.jupiter.orm.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.util.sql.SqlBuilder;

public class ParentDataMapper<T> extends JoinDataMapper<T> {
	private ForeignKeyConstraintImpl constraint;
	
	public ParentDataMapper(DataMapperImpl<T> dataMapper,ForeignKeyConstraintImpl constraint, String alias) {
		super(dataMapper, alias);	
		this.constraint = constraint;
	}
	
	T set(Object target , ResultSet rs , int index) throws SQLException {
		T value = null;
		if (target != null) {
			KeyValue key = getMapper().getPrimaryKey(rs, index);
			if (key != null) {
				value = get(key);
				if (value == null) {
					value = getMapper().construct(rs,index);
					put(key, value);
				}
				if (constraint.getFieldName() != null) {
					DomainMapper.FIELDSTRICT.set(target,constraint.getFieldName(),value);
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
		List<ColumnImpl> primaryKeyColumns = constraint.getReferencedTable().getPrimaryKeyColumns();
		List<ColumnImpl> foreignKeyColumns = constraint.getColumns();
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

	@Override
	public boolean isReachable() {
		return true;
	}
	
}
