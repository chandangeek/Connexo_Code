package com.elster.jupiter.orm.impl;

import java.sql.*;
import java.util.*;

import com.elster.jupiter.conditions.Condition;
import com.elster.jupiter.sql.util.SqlBuilder;

final class JoinExecutor<T> {
	private final JoinTreeNode<T> root;
	private SqlBuilder builder = new SqlBuilder();
	private final int from;
	private final int to;
	
	JoinExecutor(JoinTreeNode<T> root) {
		this(root,0,0);
		root.mark();		 
	}
 	
	JoinExecutor(JoinTreeNode<T> root, int from , int to) {
		this.root = root;
		root.mark();
		this.from = from;
		this.to = to;
	}
	
	SqlBuilder getSqlBuilder(Condition condition , String[] fieldNames) {
		new JoinTreeMarker(root).visit(condition);
		root.sweep();
		new JoinTreeMarker(root).visit(condition);
		appendSelectClause(fieldNames);
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
	
	private void appendSelectClause(String[] fieldNames) {
		builder.append("select ");
		if (fieldNames.length == 0) {
			builder.append(" NULL ");
		} else {
			String separator = "";
			for (String each : fieldNames) {
				builder.append(separator);
				builder.append(root.getColumnAndAliasForField(each).toString());
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

	List<T> where(Condition condition,boolean eager, String[] exceptions) throws SQLException {
		if (eager) {
			root.markAll();
			clear(exceptions);
		} else {
			mark(exceptions);
		}
		if (from > 0) {
			root.clearChildMappers();
		}
		new JoinTreeMarker(root).visit(condition);
		root.sweep();
		root.clearCache();		
		new JoinTreeMarker(root).visit(condition);
		appendSql(condition, null);
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
		for (String each : exceptions) {
			root.mark(each);
		}
	}
	
	void clear(String[] exceptions) {
		for (String each : exceptions) {
			root.clear(each);
		}
	}
	
}


