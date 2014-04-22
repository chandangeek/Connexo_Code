package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import java.util.List;

public class TableSqlGenerator {
	private final TableImpl<?> table;
	
	TableSqlGenerator(TableImpl<?> table) {
		this.table = table;		
	}
	
	void appendColumns(StringBuilder sb , String separator , String alias) {
		appendColumns(sb,separator,alias,table.getRealColumns());		
	}
	
	void appendColumns(StringBuilder sb, String separator , String alias , List<? extends Column> columns) {
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

    public void appendJournalTable(StringBuilder sb, String separator , String alias) {
        sb.append(separator);
        sb.append(table.getJournalTableName());
        if (alias != null ) {
            sb.append(" ");
            sb.append(alias);
        }
    }

    String getSelectFromClause(String alias) {
		return getSelectFromClause(table.getRealColumns(),alias);
	}

    String getSelectFromJournalClause(String alias) {
        return getSelectFromJournalClause(table.getRealColumns(),alias);
    }

    String getSelectFromClause(List<? extends Column> columns , String alias) {
		StringBuilder sb = new StringBuilder("select");
		appendColumns(sb, " " , alias , columns);		
		sb.append(" from ");
		appendTable(sb," ",alias);
		return sb.toString();
	}

    String getSelectFromJournalClause(List<? extends Column> columns , String alias) {
        StringBuilder sb = new StringBuilder("select ");
        if (alias != null && alias.length() > 0) {
        	sb.append(alias);
        	sb.append(".");
        }
        sb.append(TableImpl.JOURNALTIMECOLUMNNAME);
        appendColumns(sb, "," , alias , columns);        
        sb.append(" from ");
        appendJournalTable(sb," ",alias);
        return sb.toString();
    }

    String refreshSql(List<ColumnImpl> columnsToRefresh) {
		StringBuilder sb = new StringBuilder(getSelectFromClause(columnsToRefresh, null));
		sb.append(" where ");
		String separator = "";
		for (Column each : table.getPrimaryKeyColumns()) {
			sb.append(separator);
			sb.append(each.getName());
			sb.append(" = ? ");
			separator = " and ";
		}
		return sb.toString();
	}
	
	String insertSql(boolean useNextVal) {
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(table.getQualifiedName());
		sb.append(" (");
		String separator = "";
		for (Column each : table.getRealColumns()) {
			sb.append(separator);
			sb.append(each.getName());
			separator = ", ";
		}
		sb.append(") values(");
		separator = "";
		for (Column each : table.getRealColumns()) {		
			sb.append(separator);
			if (useNextVal && each.isAutoIncrement()) {
				sb.append(each.getQualifiedSequenceName());
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
		String separator = "";
		for (Column each : table.getRealColumns()) {
			sb.append(separator);
			sb.append(each.getName());
			separator = ", ";
		}
		sb.append(separator);
		sb.append(TableImpl.JOURNALTIMECOLUMNNAME);
		sb.append(") ( select ");
		separator = "";
		for (Column each : table.getRealColumns()) {
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
	
	String updateSql(List<ColumnImpl> columns) {
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
	
	
}
