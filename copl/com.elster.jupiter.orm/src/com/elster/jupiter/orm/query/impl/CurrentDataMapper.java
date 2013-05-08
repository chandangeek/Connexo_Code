package com.elster.jupiter.orm.query.impl;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

public class CurrentDataMapper<T> extends JoinDataMapper<T> implements SqlFragment {
	private ForeignKeyConstraint constraint;
	
	public CurrentDataMapper(DataMapperImpl<T> dataMapper,ForeignKeyConstraint constraint, String alias) {
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
				if (constraint.getReverseCurrentFieldName() != null) {
					DomainMapper.FIELDSTRICT.set(target,constraint.getReverseCurrentFieldName(),value);
				}
			}
		}
		return value;
	}
	
	@Override
	boolean appendFromClause(SqlBuilder builder, String parentAlias, boolean marked, boolean forceOuterJoin) {
		boolean outerJoin = forceOuterJoin || !marked;
		if (outerJoin) {
			builder.append(" LEFT");
		}
		builder.append(" JOIN ");
		appendTable(builder);
		builder.append(" ON ");
		builder.openBracket();
		List<Column> primaryKeyColumns = constraint.getReferencedTable().getPrimaryKeyColumns();
		List<Column> foreignKeyColumns = constraint.getColumns();
		String separator = "";
		for ( int i = 0 ; i < primaryKeyColumns.size() ; i++) {
			builder.append(separator);
			builder.append(primaryKeyColumns.get(i).getName(parentAlias));			
			builder.append(" = ");
			builder.append(foreignKeyColumns.get(i).getName(getAlias()));			
			separator = " AND ";
		}
		builder.append(" AND ");
		builder.add(this);
		builder.closeBracketSpace();
		return outerJoin;
	}
	

	@Override
	String getName() {		
		return constraint.getReverseCurrentFieldName();
	}

	@Override
	boolean canRestrict() {	
		return true;
	}

	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		long now = System.currentTimeMillis(); 
		statement.setLong(position++,now);
		statement.setLong(position++,now);
		return position;
	}

	@Override
	public String getText() {
		return getAlias() + ".STARTTIME <= ? AND ? < " + getAlias() + ".ENDTIME";
	}
	
}
