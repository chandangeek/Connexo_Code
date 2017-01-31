/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

final class JoinExecutor<T> {

	private final JoinTreeNode<T> root;
	private SqlBuilder builder;
	private final int from;
	private final int to;
	private final Instant effectiveDate;

	JoinExecutor(JoinTreeNode<T> root, Instant effectiveDate) {
		this(root,effectiveDate,0,0);
	}

	JoinExecutor(JoinTreeNode<T> root, Instant effectiveDate, int from , int to) {
		this.root = root;
		this.effectiveDate = effectiveDate;
		this.from = from;
		this.to = to;
	}

	SqlBuilder getSqlBuilder(Condition condition, String[] fieldNames, Order[] orderBy) {
		builder = new SqlBuilder();
		JoinTreeMarker.on(root).visit(condition);
		List<String> selectColumns = new ArrayList<>();
		for (String name : fieldNames) {
			List<ColumnAndAlias> columnAndAliases = root.getColumnAndAliases(name);
			if (columnAndAliases == null) {
				selectColumns.add(name);
			} else {
				for (ColumnAndAlias columnAndAlias : columnAndAliases) {
					selectColumns.add(columnAndAlias.toString());
				}
			}
		}
		root.prune();
		JoinTreeMarker.on(root).visit(condition);
		appendSelectClause(selectColumns);
		appendWhereClause(builder, condition , " where ");
		appendOrderByClause(builder, orderBy);
		return from == 0 ? builder : builder.asPageBuilder(from, to, fieldNames);
	}

	private void appendSql(Condition condition , Order[] orderBy) {
		appendSelectClause();
		appendWhereClause(builder, condition , " where ");
		appendOrderByClause(builder,orderBy);
		if (from != 0) {
			this.builder = builder.asPageBuilder(from,to);
		}
	}

	private void appendCountSql(Condition condition) {
		builder.append("select count(distinct ");
		builder.append(root.alias());
		builder.append(".");
		builder.append(root.getTable().getDataModel().getSqlDialect().rowId());
		builder.append(") from ");
		root.appendFromClause(builder,null,false);
		appendWhereClause(builder, condition , " where ");
	}

	private void appendSelectClause() {
		builder.append("select ");
		if (needsDistinct()) {
			builder.append("distinct ");
		}
		root.appendColumns(builder, "");
		builder.append(" from ");
		root.appendFromClause(builder,null,false);
	}

	private void appendSelectClause(List<String> selectColumns) {
		builder.append("select ");
		if (selectColumns.isEmpty()) {
			builder.append(" NULL ");
		} else {
			String separator = "";
			for (String selectColumn : selectColumns) {
				builder.append(separator);
				builder.append(selectColumn);
				separator = ", ";
			}
		}
		builder.append(" from ");
		root.appendFromClause(builder,null,false);
	}

	private void appendWhereClause(SqlBuilder builder , Condition condition , String separator) {
		if (condition != null && condition != Condition.TRUE) {
			builder.append(separator);
			WhereClauseBuilder.from(root, builder, effectiveDate).visit(condition);
		}
	}

	private void appendOrderByClause(SqlBuilder builder, Order[] orderBy) {
		if (orderBy == null || orderBy.length == 0) {
            return;
        }
		builder.append(" order by ");
		String separator = "";
		for (Order each : orderBy) {
			separator = appendOrder(builder,each,separator);
		}
	}

	private String appendOrder(SqlBuilder builder, Order order,String separator) {
		List<ColumnAndAlias> columnAndAliases = root.getColumnAndAliases(order.getName());
		if (columnAndAliases == null || columnAndAliases.isEmpty()) {
			builder.append(separator);
			separator = ", ";
			builder.append(order.getClause(order.getName()));
			builder.space();
		} else {
			for (ColumnAndAlias columnAndAlias : columnAndAliases) {
				builder.append(separator);
				separator = ", ";
				builder.append(order.getClause(columnAndAlias.toString()));
				builder.space();
			}
		}
		return separator;
	}

	long count(Condition condition) throws SQLException {
		builder = new SqlBuilder();
		JoinTreeMarker.on(root).visit(condition);
		root.prune();
		root.clearCache();
		// remark all nodes with a where clause contribution.
		JoinTreeMarker.on(root).visit(condition);
		appendCountSql(condition);
		try (Connection connection = root.getTable().getDataModel().getConnection(false)) {
			try(PreparedStatement statement = builder.prepare(connection)) {
				try (ResultSet resultSet = statement.executeQuery()) {
					resultSet.next();
					return resultSet.getLong(1);
				}
			}
		}
	}

    List<T> select(Condition condition,Order[] orderBy , boolean eager, String[] exceptions) throws SQLException {
		builder = new SqlBuilder();
		boolean initialMarkDone = false;
		if (eager) {
			// mark all nodes that have a where clause contribution.
			// we need to do this first, as markReachable depends on the marked state for temporal relations.
			JoinTreeMarker.on(root).visit(condition);
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
			JoinTreeMarker.on(root).visit(condition);
		}
		// prune unneeded tree branches, and clears mark state
		root.prune();
		root.clearCache();
		// remark all nodes with a where clause contribution.
		JoinTreeMarker.on(root).visit(condition);
		appendSql(condition, orderBy);
		List<T> result = new ArrayList<>();
		try (Connection connection = root.getTable().getDataModel().getConnection(false)) {
			try (PreparedStatement statement = builder.prepare(connection)) {
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						construct(resultSet, result);
					}
				}
			}
		}
		root.completeFind(effectiveDate);
		// complete result with foreign key values obtained from condition
		ParentSetter.on(root, result).visit(condition);
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

	private boolean needsDistinct() {
		return root.needsDistinct();
	}

}
