package com.elster.jupiter.orm.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.impl.DataMapperImpl;
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

	Table getTable() {		
		return value.getTable();
	}
	
	void clearCache() {
		value.clearCache();
		for (JoinTreeNode<?> each : children) {
			each.clearCache();
		}
	}
	
	final <R> boolean addMapper(DataMapperImpl<R> newMapper , AliasFactory aliasFactory) {
		boolean result = false;
		for (JoinTreeNode<?> each : children) {
			if (each.addMapper(newMapper,aliasFactory)){
				result = true;
			}
		}
		for (JoinDataMapper<R> newNodeValue : value.wrap(newMapper, aliasFactory)) {				
			add(new JoinTreeNode<>(newNodeValue));
			result = true;
		}
		return result;
	}
	
	private <R> void add(JoinTreeNode<R> node) {
		children.add(node);
	}


	final <R> R execute(String fieldName , JoinTreeAction<R> action) {
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
		return booleanValue(execute(fieldName , new JoinTreeAction<Boolean>(true,false) {			
			@Override
			Boolean invoke(String fieldName , JoinDataMapper<?> value) {
				return value.hasWhereField(fieldName);
			}
		}));
	}
	
	final Class<?> getType(String fieldName) {
		return execute(fieldName , new JoinTreeAction<Class<?>>(false,false) {			
			@Override
			Class<?> invoke(String fieldName , JoinDataMapper<?> value) {
				return value.getType(fieldName);
			}
		});
	}
	
	final Column getColumnForField(String fieldName) {
		ColumnAndAlias columnAndAlias = getColumnAndAliasForField(fieldName);
		return columnAndAlias == null ? null : columnAndAlias.getColumn();
	}
	
	final ColumnAndAlias getColumnAndAliasForField(String fieldName) {
		return execute(fieldName , new JoinTreeAction<ColumnAndAlias>(true,false) {
			@Override
			ColumnAndAlias invoke(String fieldName, JoinDataMapper<?> value) {
				return value.getColumnAndAlias(fieldName);
			}
		});			
	}	
	
	final SqlFragment getFragment(final Comparison comparison , String fieldName) {
		return execute(fieldName , new JoinTreeAction<SqlFragment> (false,false) {
			@Override
			SqlFragment invoke(String fieldName, JoinDataMapper<?> value) {
				return value.getFragment(comparison,fieldName);
			}		
		});
	}
	
	final SqlFragment getFragment(final Contains contains , String fieldName) {
		return execute(fieldName , new JoinTreeAction<SqlFragment> (false,false) {
			@Override
			SqlFragment invoke(String fieldName, JoinDataMapper<?> value) {
				return value.getFragment(contains,fieldName);
			}		
		});
	
	}
	
	final DataMapperImpl<?> getDataMapperForField(String fieldName) {
		return execute(fieldName , new JoinTreeAction<DataMapperImpl<?>> (false,false) {
			@Override
			DataMapperImpl<?> invoke(String fieldName, JoinDataMapper<?> value) {
				return value.getDataMapperForField(fieldName);
			}		
		});
	}
		
	final int set(Object target , ResultSet rs, int index)  throws SQLException {
		if (semiJoin()) {
			return index;
		}
		target = target == null  ? null : value.set(target,rs,index);
		index += value.getTable().getColumns().size();
		for (JoinTreeNode<?> each : children) {
			index = each.set(target, rs, index);
		}
		return index;		
	}
	
	final void completeFind() {
		if (!(value.isChild() && isMarked())) {
			value.completeFind();
		}
		for (JoinTreeNode<?> each : children) {			
			each.completeFind();
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
		forceOuterJoin = value.appendFromClause(builder, parentAlias , isMarked() , forceOuterJoin );
		for (JoinTreeNode<?> each : children) {
			each.appendFromClause(builder, value.getAlias() , forceOuterJoin);		
		}
	}
	
	List<JoinTreeNode<?>> getChildren() {
		return children;
	}

	JoinTreeNode<T> copy() {
		JoinTreeNode<T> result = new JoinTreeNode<>(this.value);
		for (JoinTreeNode<?> each : children) {
			result.add(each.copy());
		}
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
		if (marked)
			return true;
		for (JoinTreeNode<?> each : children) {
			if (each.marked) {
				return true;
			}
		} 
		return false;
	}
	
	void mark() {
		marked = true;
	}
	
	void markAll() {
		mark();
		for (JoinTreeNode<?> each : children) {
			each.mark();
		}
	}
	
	private boolean booleanValue(Boolean value) {
		return value == null ? false : value.booleanValue();
	}
	
	boolean clear(String fieldName) {
		return booleanValue(execute(fieldName , new JoinTreeAction<Boolean>(false,true) {			
			@Override
			Boolean invoke(String fieldName, JoinDataMapper<?> value) {
				return fieldName.isEmpty() || (value.getColumnAndAlias(fieldName.substring(0,fieldName.length()-1)) != null);
			}			
		}));
	}
			
	boolean mark(String fieldName) {
		return booleanValue(execute(fieldName , new JoinTreeAction<Boolean>(true,false) {			
			@Override
			Boolean invoke(String fieldName, JoinDataMapper<?> value) {
				return fieldName.isEmpty() || (value.getColumnAndAlias(fieldName.substring(0,fieldName.length()-1)) != null);
			}
		}));		
	}
	
	void clearChildMappers() {
		if (value.isChild()) {
			clear();
		} else {
			for (JoinTreeNode<?> each : children) {
				each.clearChildMappers();
			}
		}		 
	}	
	
	void clear() {
		marked = false;
		for (JoinTreeNode<?> each : children) {
			each.clear();
		}			
	}
	
	List<String> getQueryFields() {		
		List<String> result = new ArrayList<>();
		if (!value.canRestrict()) {
			return result;
		}
		String localName = value.getName();
		localName = (localName == null) ? "" : localName + ".";		
		for (String each : value.getQueryFields()) {
			result.add(localName+each);
		}		
		for (JoinTreeNode<?> each : children) {
			for (String field : each.getQueryFields()) {
				result.add(localName + field);
			}
		}
		return result;		
	}
	
	boolean semiJoin() {
		return value.isChild() && isMarked();
	}
	
	boolean hasSemiJoin() {
		if (semiJoin()) {
			return true;
		}
		for (JoinTreeNode<?> each : children) {
			if (each.hasSemiJoin()) {
				return true;
			}
		}
		return false;
	}
			
}

