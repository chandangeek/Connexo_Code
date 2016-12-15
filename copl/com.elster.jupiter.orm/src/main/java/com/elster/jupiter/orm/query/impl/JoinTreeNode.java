package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.TableImpl;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class JoinTreeNode<T>  {

	private final JoinDataMapper<T> value;
	private final List<JoinTreeNode<?>> children = new ArrayList<>();
	private boolean marked;

	JoinTreeNode(JoinDataMapper<T> value) {
		this.value = value;
	}

	TableImpl<? super T> getTable() {
		return value.getTable();
	}

	void clearCache() {
		value.clearCache();
		children.forEach(JoinTreeNode::clearCache);
	}

	final <R> boolean addMapper(DataMapperImpl<R> newMapper, AliasFactory aliasFactory) {
		// returns true, if mapper was added.
		// take care not to shortcircuit calculations, as map lambdas have side effects.
		return
			Stream.concat(
				children.stream().map(child -> child.addMapper(newMapper, aliasFactory)),
				value.wrap(newMapper, aliasFactory)
						.stream()
                        .map(this::add))
					    .reduce(false, Boolean::logicalOr);
	}

	private <R> boolean add(JoinDataMapper<R> mapper) {
		return add(new JoinTreeNode<>(mapper));
	}

	private <R> boolean add(JoinTreeNode<R> node) {
		return children.add(node);
	}

	private <R> R mark(String fieldName, BiFunction<JoinDataMapper<?>, String, R> biFunction) {
		return execute(fieldName, JoinTreeAction.mark(biFunction));
	}

	private <R> R clear(String fieldName, BiFunction<JoinDataMapper<?>, String, R> biFunction) {
		return execute(fieldName, JoinTreeAction.clear(biFunction));
	}

	private <R> R find(String fieldName, BiFunction<JoinDataMapper<?>, String, R> biFunction) {
		return execute(fieldName, JoinTreeAction.find(biFunction));
	}

	private <R> R execute(String fieldName, JoinTreeAction<R> action) {
		if (fieldName == null) {
			return null;
		}
		String reduced = value.reduce(fieldName);
		if (reduced == null) {
			return null;
		}
		R result = action.apply(value,reduced);
		if (action.isValid(result)) {
			action.matched(this);
		} else {
			result = children.stream()
				.map(child -> child.execute(reduced,action))
				.filter(action::isValid)
				.findFirst()
				.orElse(null);
		}
		return result;
	}

	boolean hasWhereField(String fieldName) {
		return booleanValue(mark(fieldName, JoinDataMapper::hasWhereField));
	}

	Class<?> getType(String fieldName) {
		return find(fieldName, JoinDataMapper::getType);
	}

	ColumnImpl getColumnForField(String fieldName) {
		ColumnAndAlias columnAndAlias = getColumnAndAliasForField(fieldName);
		return columnAndAlias == null ? null : (ColumnImpl) columnAndAlias.getColumn();
	}

	List<ColumnAndAlias> getColumnAndAliases(String fieldName) {
		return mark(fieldName, JoinDataMapper::getColumnAndAliases);
	}

	private ColumnAndAlias getColumnAndAliasForField(String fieldName) {
		return mark(fieldName, JoinDataMapper::getColumnAndAlias);
	}

	SqlFragment getFragment(final Comparison comparison, String fieldName) {
		return find(fieldName, (value,reduced) -> value.getFragment(comparison,reduced));
	}

	SqlFragment getFragment(final Contains contains, String fieldName) {
		return find(fieldName, (value,reduced) -> value.getFragment(contains,reduced));
	}

	DataMapperImpl<?> getDataMapperForField(String fieldName) {
		return find(fieldName, JoinDataMapper::getDataMapperForField);
	}

	int set(Object target, ResultSet rs, int index)  throws SQLException {
		if (skipFetch()) {
			return index;
		}
		target = target == null ? null : value.set(target, rs, index);
		index += this.getRealColumns().size();
		for (JoinTreeNode<?> each : children) {
			index = each.set(target, rs, index);
		}
		return index;
	}

	private List<ColumnImpl> getRealColumns() {
		return this.value.getTable().getRealColumns().collect(Collectors.toList());
	}

	void completeFind(Instant effectiveDate) {
		if (!skipFetch()) {
			// do children first, so they can set collection relations before postLoad does.
			children.forEach(child-> child.completeFind(effectiveDate));
			value.completeFind(effectiveDate);
		}
	}

	String appendColumns (SqlBuilder builder, String separator) {
		if (skipFetch()) {
			return separator;
		} else {
			return children.stream().reduce(
				value.appendColumns(builder,separator),
				(sep, child) -> child.appendColumns(builder, sep),
				(sep1, sep2) -> sep2);
		}
	}

	void appendFromClause(SqlBuilder builder, String parentAlias, boolean forceOuterJoin) {
		boolean force = value.appendFromClause(builder, parentAlias, isMarked(), forceOuterJoin);
		children.forEach(child -> child.appendFromClause(builder, value.getAlias(), force));
	}

	JoinTreeNode<T> copy() {
		JoinTreeNode<T> result = new JoinTreeNode<>(this.value);
		children.forEach(child -> result.add(child.copy()));
		return result;
	}

	void prune() {
		Iterator<JoinTreeNode<?>> it = children.iterator();
		while (it.hasNext()) {
			JoinTreeNode<?> node = it.next();
			if (node.isMarked()){
				node.prune();
			} else {
				it.remove();
			}
		}
		marked = false;
	}

	private boolean isMarked() {
		return marked || isAnyChildMarked();
	}

	private boolean isAnyChildMarked() {
		return children.stream().anyMatch(JoinTreeNode::isMarked);
	}

	void mark() {
		marked = true;
	}

	void markReachable() {
		if (isReachable()) {
			mark();
			children.forEach(JoinTreeNode::markReachable);
		}
	}

	private boolean isReachable() {
		return value.isReachable();
	}

	private boolean booleanValue(Boolean value) {
		return value != null && value;
	}

	private static boolean hasField(JoinDataMapper<?> mapper, String reduced) {
		return reduced.isEmpty() || mapper.getColumnAndAlias(reduced.substring(0,reduced.length()-1)) != null;
	}

	boolean clear(String fieldName) {
		return booleanValue(clear(fieldName, JoinTreeNode::hasField));
	}

	boolean mark(String fieldName) {
		return booleanValue(mark(fieldName, JoinTreeNode::hasField));
	}

	void clearChildMappers() {
		if (value.isChild()) {
			clear();
		} else {
			children.forEach(JoinTreeNode::clearChildMappers);
		}
	}

	void clear() {
		marked = false;
		children.forEach(JoinTreeNode::clear);
	}

    List<String> getQueryFields() {
		String baseName = value.getName();
		String localName = (baseName == null) ? "" : baseName + ".";
		return
			Stream.concat(
					value.getQueryFields().stream(),
					children.stream().flatMap(child -> child.getQueryFields().stream()))
				.map(field -> localName + field)
				.collect(Collectors.toList());
	}

	private boolean skipFetch() {
		return value.skipFetch(marked, isAnyChildMarked());
	}

	boolean needsDistinct() {
		return distinct() || children.stream().anyMatch(JoinTreeNode::needsDistinct);
	}

	private boolean distinct() {
		return value.needsDistinct(marked, isAnyChildMarked());
	}

	String alias() {
		return value.getAlias();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(value.getTable().getName());
		children.forEach(child -> {
			builder.append(" -> (");
			builder.append(child);
			builder.append(")");
		});
		return builder.toString();
	}

}
