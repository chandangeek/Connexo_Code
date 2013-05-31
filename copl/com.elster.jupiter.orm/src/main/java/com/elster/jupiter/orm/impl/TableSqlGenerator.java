package com.elster.jupiter.orm.impl;

import java.util.List;

import com.elster.jupiter.orm.Column;

public class TableSqlGenerator {
	private final TableImpl table;
	private final ColumnImpl[] allColumns;
	
	TableSqlGenerator(TableImpl table) {
		this.table = table;
		List<Column> columnList = table.getColumns();
		allColumns = new ColumnImpl[columnList.size()];
		columnList.toArray(allColumns);		
	}
	
	void appendColumns(StringBuilder sb , String separator , String alias) {
		appendColumns(sb,separator,alias,allColumns);		
	}
	
	void appendColumns(StringBuilder sb, String separator , String alias , Column[] columns) {
		if (alias == null) {
			alias = "";
		}			
		for (Column each : columns) {
			sb.append(separator);
			sb.append(each.getName(alias));
			separator = ", ";
		}		
	}
	
	public void appendTable(StringBuilder sb, String separator , String alias) {
		sb.append(separator);
		sb.append(table.getQualifiedName());
		if (alias != null ) {
			sb.append(" ");
			sb.append(alias);
		}			
	}
	
	String getSelectFromClause(String alias) {		
		return getSelectFromClause(allColumns,alias);
	}
	
	String getSelectFromClause(Column[] columns , String alias) {				
		StringBuilder sb = new StringBuilder("select");
		appendColumns(sb, " " , alias , columns);		
		sb.append(" from ");
		appendTable(sb," ",alias);
		return sb.toString();
	}
	
	String refreshSql(Column[] columnsToRefresh) {
		StringBuilder sb = new StringBuilder(getSelectFromClause(columnsToRefresh, null));
		sb.append(" where ");
		String separator = "";
		for (Column each : table.getPrimaryKeyColumns()) {
			sb.append(separator);
			sb.append(each.getName());
			sb.append(" = ? ");
			separator = ", ";
		}
		return sb.toString();
	}
	
	String insertSql(boolean useNextVal) {
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(table.getQualifiedName());
		sb.append(" (");
		String separator = "";
		for (Column each : allColumns) {
			sb.append(separator);
			sb.append(each.getName());
			separator = ", ";
		}
		sb.append(") values(");
		separator = "";
		for (Column each : allColumns) {		
			sb.append(separator);
			if (useNextVal && each.isAutoIncrement()) {
				sb.append(each.getSequenceName());
				sb.append(".nextval");
			} else if (each.hasInsertValue()) {
				sb.append(each.getInsertValue());
			} else {
				sb.append("?");
			}
			separator = ", ";
		}
		sb.append(")");
		return sb.toString();
	}
	
	private void addPrimaryKey(StringBuilder sb) {
		String separator = "";
		for (Column each : table.getPrimaryKeyColumns()) {
			sb.append(separator);
			sb.append(each.getName());
			sb.append(" = ?");
			separator = " and ";
		}
	}
	
	String journalSql() {
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(table.getQualifiedName(table.getJournalTableName()));
		sb.append(" (");
		sb.append(" select ");
		String separator = "";
		for (Column each : allColumns) {
			sb.append(separator);
			sb.append(each.getName());
			separator = ", ";
		}
		sb.append(separator);
		sb.append("? as ");
		sb.append(TableImpl.JOURNALTIMECOLUMNNAME);
		sb.append(" from ");
		sb.append(table.getQualifiedName());
		sb.append(" where ");
		addPrimaryKey(sb);
		sb.append(")");
		return sb.toString();
	}
	
	String deleteSql() {
		StringBuilder sb = new StringBuilder("delete from ");
		sb.append(table.getQualifiedName());
		sb.append(" where ");
		addPrimaryKey(sb);
		return sb.toString();
	}
	
	String updateSql(Column[] columns) {
		StringBuilder sb = new StringBuilder("update ");
		sb.append(table.getQualifiedName());
		sb.append(" set ");
		String separator = "";
		for (Column each : columns) {
			sb.append(separator);
			sb.append(each.getName());
			sb.append(" = ?");
			separator = ", ";
		}	
		for (Column each : table.getAutoUpdateColumns()) {
			sb.append(separator);
			sb.append(each.getName());
			sb.append("=  ?");
			separator = ", ";
		}
		for (Column each : table.getUpdateValueColumns()) {
			sb.append(separator);
			sb.append(each.getName());
			sb.append(" = ");			
			sb.append(each.getUpdateValue());
			separator = ", ";
		}	
		for (Column each : table.getVersionColumns()) {
			sb.append(separator);
			sb.append(each.getName());
			sb.append(" = ");
			sb.append(each.getName());
			sb.append(" + 1");
			separator = ", ";
		}		
		sb.append(" where ");
		addPrimaryKey(sb);
		for (Column each : table.getVersionColumns()) {
			sb.append(" and ");
			sb.append(each.getName());
			sb.append(" = ?");
		}
		return sb.toString();
	}

	TableImpl getTable() {
		return table;
	}	
	
	ColumnImpl[] getColumns() {
		return allColumns;
	}
	
	Column[] getPrimaryKeyColumns() {
		List<Column> primaryKeyColumns = table.getPrimaryKeyColumns();
		return primaryKeyColumns.toArray(new Column[primaryKeyColumns.size()]);
	}
	
	
}
