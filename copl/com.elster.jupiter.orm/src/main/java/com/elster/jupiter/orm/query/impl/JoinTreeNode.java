package com.elster.jupiter.orm.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.TableImpl;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

final class JoinTreeNode<T>  {
	
	private final JoinDataMapper<T> value;
	private final List<JoinTreeNode<?>> children = new ArrayList<>();
	private boolean marked; 
	
	public JoinTreeNode (JoinDataMapper<T> value) {
		this.value = value;
	}

	TableImpl<? super T> getTable() {		
		return value.getTable();
	}
	
	void clearCache() {
		value.clearCache();
		children.forEach(child -> child.clearCache());
	}
	
	final <R> boolean addMapper(DataMapperImpl<R> newMapper , AliasFactory aliasFactory) {
		// returns true, if mapper was added.
		// take care not to shortcircuit calculations, as map lambdas have side effects.
		return 
			Stream.concat(
				children.stream().map(child -> child.addMapper(newMapper,aliasFactory)),
				value.wrap(newMapper, aliasFactory).stream().map( mapper -> add(new JoinTreeNode<>(mapper))))
					.reduce(Boolean.FALSE,Boolean::logicalOr);
	}
	
	private <R> boolean add(JoinTreeNode<R> node) {
		return children.add(node);
	}

	private <R> R mark(String fieldName, BiFunction<String, JoinDataMapper<?>, R> biFunction) {
		return execute(fieldName, TreeAction.mark(biFunction));
	}
	
	private <R> R clear(String fieldName, BiFunction<String, JoinDataMapper<?>, R> biFunction) {
		return execute(fieldName, TreeAction.clear(biFunction));
	} 

	private <R> R find(String fieldName, BiFunction<String, JoinDataMapper<?>, R> biFunction) {
		return execute(fieldName, TreeAction.find(biFunction));
	} 
	
	private <R> R execute(String fieldName , JoinTreeAction<R> action) {
		if (fieldName == null) {
			return null;
		}
		fieldName = value.reduce(fieldName);
		if (fieldName == null) {
			return null;
		}
		R result = action.invoke(fieldName, value);
		if (action.proceed(result)) {
			for (JoinTreeNode<?> each : children) {
				result = each.execute(fieldName , action);
				if (!action.proceed(result)) {
					return result;
				}
			}
		} else {
			if (action.mark()) {
				mark();
			}
			if (action.clear()) {
				clear();
			}
			return result;
		}	
		return result;
	}
	
	final boolean hasWhereField(String fieldName) {
		return booleanValue(mark(fieldName , (reduced,value) -> value.hasWhereField(reduced)));
	}
	
	final Class<?> getType(String fieldName) {
		return find(fieldName , (reduced,value) -> value.getType(reduced));
	}
	
	final ColumnImpl getColumnForField(String fieldName) {
		ColumnAndAlias columnAndAlias = getColumnAndAliasForField(fieldName);
		return columnAndAlias == null ? null : (ColumnImpl) columnAndAlias.getColumn();
	}
	
	final List<ColumnAndAlias> getColumnAndAliases(String fieldName) {
		return mark(fieldName , (reduced,value) -> value.getColumnAndAliases(reduced));
	}
	
	final ColumnAndAlias getColumnAndAliasForField(String fieldName) {
		return mark(fieldName , (reduced,value) -> value.getColumnAndAlias(reduced));
	}	
	
	final SqlFragment getFragment(final Comparison comparison , String fieldName) {
		return find(fieldName , (reduced,value) -> value.getFragment(comparison,reduced));
	}
	
	final SqlFragment getFragment(final Contains contains , String fieldName) {
		return find(fieldName , (reduced,value) -> value.getFragment(contains,reduced));	
	}
	
	final DataMapperImpl<?> getDataMapperForField(String fieldName) {
		return find(fieldName , (reduced, value) -> value.getDataMapperForField(reduced));
	}
		
	final int set(Object target , ResultSet rs, int index)  throws SQLException {
		if (semiJoin()) {
			return index;
		}
		target = target == null  ? null : value.set(target,rs,index);
		index += value.getTable().getRealColumns().size();
		for (JoinTreeNode<?> each : children) {
			index = each.set(target, rs, index);
		}
		return index;		
	}
	
	final void completeFind(Date effectiveDate) {
		if (!semiJoin()) {
			// do children first, so they can set collection relations before postLoad does.
			children.forEach(child-> child.completeFind(effectiveDate));
			value.completeFind(effectiveDate);
		}		
	}
	
	
	final String appendColumns (SqlBuilder builder , String separator) {
		if (semiJoin()) {
			return separator;
		} 
		separator = value.appendColumns(builder,separator);
		for (JoinTreeNode<?> each : children) {
			separator = each.appendColumns(builder, separator);
		}
		return separator;
	}
	
	final void appendFromClause(SqlBuilder builder, String parentAlias , boolean forceOuterJoin) {
		boolean force = value.appendFromClause(builder, parentAlias , isMarked() , forceOuterJoin );
		children.forEach(child -> child.appendFromClause(builder, value.getAlias() , force));		
	}
	
	List<JoinTreeNode<?>> getChildren() {
		return children;
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
		return children.stream().anyMatch(child -> child.isMarked());
	}
	
	void mark() {
		marked = true;
	}
	
	void markReachable() {
		if (isReachable()) {
			mark();
			children.forEach(child -> child.markReachable());
		}
	}
	
	boolean isReachable() {
		return value.isReachable();
	}
	
	private boolean booleanValue(Boolean value) {
		return value == null ? false : value.booleanValue();
	}
	
	boolean clear(String fieldName) {
		return booleanValue(clear(fieldName,(reduced, value) -> reduced.isEmpty() || (value.getColumnAndAlias(reduced.substring(0,reduced.length()-1)) != null)));		
	}
			
	boolean mark(String fieldName) {
		return booleanValue(mark(fieldName, (reduced, value) -> reduced.isEmpty() || (value.getColumnAndAlias(reduced.substring(0,reduced.length()-1)) != null)));		
	}
	
	void clearChildMappers() {
		if (value.isChild()) {
			clear();
		} else {
			children.forEach(child -> child.clearChildMappers());
		}		 
	}	
	
	void clear() {
		marked = false;
		children.forEach(child -> child.clear());			
	}

    List<String> getQueryFields() {
		String baseName = value.getName();
		String localName = (baseName == null) ? "" : baseName + ".";	
		return Stream.concat(value.getQueryFields().stream(), children.stream().flatMap(child -> child.getQueryFields().stream()))
			.map(field -> localName + field)
			.collect(Collectors.toList());
	}
	
	boolean semiJoin() {
		return value.skipFetch(marked,isAnyChildMarked());
	}
	
	boolean hasSemiJoin() {
		return semiJoin() || children.stream().anyMatch(child -> child.hasSemiJoin());
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(value.getTable().getName());
		for (JoinTreeNode<?> each : children) {
			builder.append(" -> (");
			builder.append(each);
			builder.append(")");
		}
		return builder.toString();
	}
			
}

