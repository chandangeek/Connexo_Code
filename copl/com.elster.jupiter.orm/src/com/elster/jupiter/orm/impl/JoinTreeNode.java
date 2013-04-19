package com.elster.jupiter.orm.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.elster.jupiter.sql.util.SqlBuilder;

final class JoinTreeNode<T>  {
	
	final private JoinDataMapper<T> value;
	final private List<JoinTreeNode<?>> children = new ArrayList<>();
	private boolean marked; 
	
	public JoinTreeNode (JoinDataMapper<T> value) {
		this.value = value;
	}
	
	void clearCache() {
		value.clearCache();
		for (JoinTreeNode<?> each : children) {
			each.clearCache();
		}
	}
		
	final <R> boolean addMapper(DataMapperImpl<R,? extends R> newMapper) {
		JoinDataMapper<R> newNodeValue = value.wrap(newMapper , children.size());
		if (newNodeValue == null) {
			for (JoinTreeNode<?> each : children) {
				if (each.addMapper(newMapper))
					return true;
			}
		} else {
			add(new JoinTreeNode<R>(newNodeValue));
			return true;
		}
		return false;
	}
	
	private <R> void add(JoinTreeNode<R> node) {
		children.add(node);
	}
	
	final ColumnAndAlias getColumnAndAliasForField(String fieldName) {
		if (!value.canRestrict()) {
			return null;
		}
		fieldName = value.reduce(fieldName);
		if (fieldName == null) {
			return null;
		}
		ColumnAndAlias columnAndAlias = value.getColumnAndAlias(fieldName);
		if (columnAndAlias == null) {
			for (JoinTreeNode<?> each : children) {
				columnAndAlias = each.getColumnAndAliasForField(fieldName);
				if (columnAndAlias != null) {					
					return columnAndAlias;
				}
			}
		} else {
			marked = true;
			return columnAndAlias;
		}
		return null;
	}	
	
	final DataMapperImpl<?,?> getDataMapperForField(String fieldName) {
		if (!value.canRestrict()) {
			return null;
		}
		fieldName = value.reduce(fieldName);
		if (fieldName == null) {
			return null;
		}
		DataMapperImpl<?,?> result = value.getDataMapperForField(fieldName);
		if (result == null) {
			for (JoinTreeNode<?> each : children) {
				result = each.getDataMapperForField(fieldName);
				if (result != null) {
					return result;
				}	
			}
		} else {
			return result;
		}
		return null;
	}
		
	final int set(Object target , ResultSet rs, int index)  throws SQLException {
		target = (target == null) ? null : value.set(target,rs,index);
		index += value.getTable().getColumns().size();
		for (JoinTreeNode<?> each : children) {
			index = each.set(target, rs, index);
		}
		return index;		
	}
	
	final void completeFind() {
		value.completeFind();
		for (JoinTreeNode<?> each : children) {
			each.completeFind();
		}
	}
	
	
	final String appendColumns (SqlBuilder builder , String separator) {
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
		JoinTreeNode<T> result = new JoinTreeNode<T>(this.value);
		for (JoinTreeNode<?> each : children) {
			result.add(each.copy());
		}
		return result;
	}

	void sweep() {
		Iterator<JoinTreeNode<?>> it = children.iterator();
		while (it.hasNext()) {
			JoinTreeNode<?> node = it.next();
			if (node.isMarked()){
				node.sweep();
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
	
	boolean clear(String fieldName) {
		if (fieldName.equals(value.getName())) {
			clear();
			return true;
		}
		fieldName = value.reduce(fieldName);
		if (fieldName == null) {
			return false;
		}
		boolean result = value.hasField(fieldName);
		if (result) {
			clear();
			return true;
		} else {
			for (JoinTreeNode<?> each : children) {
				result = each.clear(fieldName);
				if (result) {
					return true;
				}
			}
		} 
		return false;
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
			each.marked = false;
		}			
	}
	
	boolean mark(String fieldName) {
		if (fieldName.equals(value.getName())) {
			mark();
			return true;
		}
		fieldName = value.reduce(fieldName);
		if (fieldName == null) {
			return false;
		}
		boolean result = value.hasField(fieldName);
		if (result) {
			marked = true;
			return true;
		} else {
			for (JoinTreeNode<?> each : children) {
				result = each.mark(fieldName);
				if (result) {
					return true;
				}
			}
		} 
		return false;
	}	
	
}
