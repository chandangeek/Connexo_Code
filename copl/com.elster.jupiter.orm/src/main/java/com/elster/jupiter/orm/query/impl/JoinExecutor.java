package com.elster.jupiter.orm.query.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;

final class JoinExecutor<T> {
		
	private final JoinTreeNode<T> root;
	private SqlBuilder builder;
	private final int from;
	private final int to;
	private final Date effectiveDate;
	
	JoinExecutor(JoinTreeNode<T> root, Date effectiveDate) {
		this(root,effectiveDate,0,0);		 
	}
 	
	JoinExecutor(JoinTreeNode<T> root, Date effectiveDate, int from , int to) {
		this.root = root;
		this.effectiveDate = effectiveDate;
		this.from = from;
		this.to = to;
	}
	
	SqlBuilder getSqlBuilder(Condition condition , String[] fieldNames) {
		builder = new SqlBuilder();
		new JoinTreeMarker(root).visit(condition);
		ColumnAndAlias[] columnAndAliases = new ColumnAndAlias[fieldNames.length];
		for (int i = 0 ; i < fieldNames.length ; i++) {
			columnAndAliases[i] = root.getColumnAndAliasForField(fieldNames[i]);
		}
		root.prune();
		new JoinTreeMarker(root).visit(condition);
		appendSelectClause(columnAndAliases);
		appendWhereClause(builder, condition , " where ");
		appendOrderByClause(builder, null);
		return from == 0 ? builder : builder.asPageBuilder(from, to);
	}
	
	private void appendSql(Condition condition , String[] orderBy) {
		appendSelectClause();
		appendWhereClause(builder, condition , " where ");
		appendOrderByClause(builder,orderBy);
		if (from != 0) {
			this.builder = builder.asPageBuilder(from,to);
		}	
	}
	
	private void appendSelectClause() {
		builder.append("select ");
		if (hasSemiJoin()) {
			builder.append("distinct ");
		}
		root.appendColumns(builder, "");
		builder.append(" from ");
		root.appendFromClause(builder,null,false);		
	}
	
	private void appendSelectClause(ColumnAndAlias[] columnAndAliases) {
		builder.append("select ");
		if (columnAndAliases.length == 0) {
			builder.append(" NULL ");
		} else {
			String separator = "";
			for (ColumnAndAlias columnAndAlias : columnAndAliases) {
				builder.append(separator);
				builder.append(columnAndAlias.toString());
				separator = ", ";
			}
		}
		builder.append(" from ");
		root.appendFromClause(builder,null,false);
	}
	
	private void appendWhereClause(SqlBuilder builder , Condition condition , String separator) {
		if (condition != null && condition != Condition.TRUE) {
			builder.append(separator);
			new WhereClauseBuilder(this.root, builder, effectiveDate).visit(condition);
		}
	}
	
	private void appendOrderByClause(SqlBuilder builder, String[] orderBy) {
		if (orderBy == null || orderBy.length == 0) {
            return;
        }
		builder.append(" order by ");
		String separator = "";
		for (String each : orderBy) {
			builder.append(separator);
			builder.append(getOrderBy(each));
			separator = ", ";
		}				
	}
		
	private String getOrderBy(String fieldName) {
		ColumnAndAlias columnAndAlias = root.getColumnAndAliasForField(fieldName);
		return columnAndAlias == null ? fieldName : columnAndAlias.toString();		
	}

    List<T> select(Condition condition,String[] orderBy , boolean eager, String[] exceptions) throws SQLException {
		builder = new SqlBuilder();
		boolean initialMarkDone = false;
		if (eager) {
			// mark all nodes that have a where clause contribution.
			// we need to do this first, as markReachable depends on the marked state for temporal relations.	
			new JoinTreeMarker(root).visit(condition);
			initialMarkDone = true;
			root.markReachable();
			clear(exceptions);
		} else {
			mark(exceptions);
		}
		// if rownum query we cannot use rows from 1:n relations as they mess up the row count
		// we will only include them if we need to, because they have a where clause contribution,
		// and eliminate duplicate row with select distinct
		if (from > 0) {
			root.clearChildMappers();
			// need to remark 
			initialMarkDone = false;
		}
		if (!initialMarkDone) {
			new JoinTreeMarker(root).visit(condition);
		}
		// prune unneeded tree parts, and clear mark state
		root.prune();
		root.clearCache();
		// remark all nodes with a where clause contribution.
		new JoinTreeMarker(root).visit(condition);
		appendSql(condition, orderBy);		
		List<T> result = new ArrayList<>();
		try (Connection connection = root.getTable().getDataModel().getConnection(false)) {				
			try(PreparedStatement statement = builder.prepare(connection)) {
				try (ResultSet resultSet = statement.executeQuery()) {
					while(resultSet.next()) {
						construct(resultSet,result);			
					}
				}				
			} 
		}
		root.completeFind(effectiveDate);
		return result;
	}
	
	private void construct(ResultSet rs,List<T> results) throws SQLException {
		root.set(results, rs, 1);					
	}

	private void mark(String[] exceptions) {
		if (exceptions != null) {
			for (String each : exceptions) {
				root.mark(each + ".");
			}
		}
	}
	
	private void clear(String[] exceptions) {
		if (exceptions != null) {
			for (String each : exceptions) {
				root.clear(each + ".");
			}
		}
	}
	
	private boolean hasSemiJoin() {
		return root.hasSemiJoin();
	}
	
}


