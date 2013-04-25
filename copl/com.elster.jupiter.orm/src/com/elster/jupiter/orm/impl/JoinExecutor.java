package com.elster.jupiter.orm.impl;

import java.sql.*;
import java.util.*;

import com.elster.jupiter.conditions.Condition;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.sql.util.SqlBuilder;

final class JoinExecutor<T> {
		
	private final JoinTreeNode<T> root;
	private SqlBuilder builder;
	private final int from;
	private final int to;
	
	JoinExecutor(JoinTreeNode<T> root) {
		this(root,0,0);		 
	}
 	
	JoinExecutor(JoinTreeNode<T> root, int from , int to) {
		this.root = root;
		this.from = from;
		this.to = to;
	}
	
	SqlBuilder getSqlBuilder(Condition condition , String[] fieldNames) {
		builder = new SqlBuilder();
		new JoinTreeMarker(root,1).visit(condition);
		ColumnAndAlias[] columnAndAliases = new ColumnAndAlias[fieldNames.length];
		for (int i = 0 ; i < fieldNames.length ; i++) {
			columnAndAliases[i] = root.getColumnAndAliasForField(fieldNames[i]);
		}
		root.prune();
		new JoinTreeMarker(root,2).visit(condition);
		appendSelectClause(columnAndAliases);
		appendWhereClause(builder, condition , " where ");
		appendOrderByClause(builder, null);
		printSql(builder.toString());
		return from == 0 ? builder : builder.asPageBuilder(from, to);
	}
	
	void appendSql(Condition condition , String[] orderBy) {
		appendSelectClause();
		appendWhereClause(builder, condition , " where ");
		appendOrderByClause(builder,orderBy);
		if (from != 0) {
			this.builder = builder.asPageBuilder(from,to);
		}
		printSql(builder.toString());		
	}
	
	private void printSql(String sql) {
		for (int i = 0 ; i < sql.length() ; i += 80) {
			System.out.println(sql.substring(i, Math.min(sql.length(), i+80)));
		}
	}
	
	private void appendSelectClause() {
		builder.append("select ");
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
			new WhereClauseBuilder(this.root, builder).visit(condition);
		}
	}
	
	private void appendOrderByClause(SqlBuilder builder, String[] orderBy) {
		if (orderBy == null || orderBy.length == 0)
			return;
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
		if (eager) {
			root.markAll();
			clear(exceptions);
		} else {
			mark(exceptions);
		}
		new JoinTreeMarker(root,1).visit(condition);
		if (from > 0) {
			root.clearChildMappers();
		}
		root.prune();
		root.clearCache();		
		new JoinTreeMarker(root,2).visit(condition);
		appendSql(condition, orderBy);
		List<T> result = new ArrayList<>();	
		try (Connection connection = Bus.getConnection(false)) {				
			try(PreparedStatement statement = builder.prepare(connection)) {
				try (ResultSet resultSet = statement.executeQuery()) {
					while(resultSet.next()) {
						construct(resultSet,result);
					}
				}				
			} 
		}
		root.completeFind();
		return result;				
	}
	
	private void construct(ResultSet rs,List<T> results) throws SQLException {		
		root.set(results, rs, 1);					
	}

	void mark(String[] exceptions) {
		if (exceptions != null) {
			for (String each : exceptions) {
				root.mark(each + ".");
			}
		}
	}
	
	void clear(String[] exceptions) {
		if (exceptions != null) {
			for (String each : exceptions) {
				root.clear(each + ".");
			}
		}
	}
	
}


