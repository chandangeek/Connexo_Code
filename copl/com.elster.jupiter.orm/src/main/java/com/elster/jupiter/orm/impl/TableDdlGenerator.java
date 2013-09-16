package com.elster.jupiter.orm.impl;

import java.util.ArrayList;
import java.util.List;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.TableConstraint;

class TableDdlGenerator {
	private final TableImpl table;
	private List<String> ddl;
	
	TableDdlGenerator(TableImpl table) {
		this.table = table;				
	}
	
	List<String> getDdl() {
		ddl = new ArrayList<>();
		ddl.add(getTableDdl());
		if (table.hasJournal() ) {
			ddl.add(getJournalTableDdl());
		}
		for (TableConstraint constraint : table.getConstraints()) {
			if (((TableConstraintImpl) constraint).needsIndex()) {
				ddl.add(getConstraintIndexDdl(constraint));
			}
		}		
		for (Column column : table.getColumns()) {
			if (column.isAutoIncrement()) {
				ddl.add(getSequenceDdl(column));
			}
		}
		
		return ddl;		
	}
	
	private String getTableDdl() {
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(table.getQualifiedName());
		sb.append("(");
		doAppendColumns(sb, table.getColumns(), true);
		for (TableConstraint constraint : table.getConstraints()) {
			sb.append(", ");
			sb.append(getConstraintFragment(constraint));			
		}
		sb.append(")");
		if (table.isIndexOrganized()) {
			sb.append(" index organized ");
		}
		return sb.toString();
	}
	
	private String getJournalTableDdl() {
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(table.getQualifiedName(table.getJournalTableName()));
		sb.append(" (");
		doAppendColumns(sb, table.getColumns(), true);
		String separator = ", ";
		sb.append(separator);
		sb.append(TableImpl.JOURNALTIMECOLUMNNAME);
		sb.append(" NUMBER NOT NULL");		
		TableConstraint constraint = table.getPrimaryKeyConstraint();
		if (constraint != null) {
			sb.append(separator);
			sb.append(getJournalConstraint(constraint));
			}
		sb.append(")");
		return sb.toString();		
	}
	
	private String getJournalConstraint(TableConstraint constraint) {
		StringBuilder sb = new StringBuilder("constraint ");
		sb.append(constraint.getName() + "_JRNL");
		sb.append(" PRIMARY KEY ");
		sb.append("(");
		doAppendColumns(sb, constraint.getColumns(), false);		
		sb.append(", ");
		sb.append(table.getExtraJournalPrimaryKeyColumnName());
		sb.append(")");	
		return sb.toString();
	}
	
	private String getConstraintFragment(TableConstraint constraint) {
		return ((TableConstraintImpl) constraint).getDdl();
	}
	
	
	private String getConstraintIndexDdl(TableConstraint constraint) {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE INDEX ");
		builder.append(constraint.getName());
		builder.append(" ON ");
		builder.append(table.getQualifiedName());
		appendColumns(builder,constraint.getColumns(), false);
		return builder.toString();		
	}
	
	private String getSequenceDdl(Column column) {
		// cache 1000 for performance in RAC environments
		return 
			"create sequence " +
		   column.getQualifiedSequenceName() +
		    " cache 1000";
	}
	
	private void appendColumns(StringBuilder builder , List<Column> columns, boolean addType) {
		builder.append(" (");
		doAppendColumns(builder, columns, addType);
		builder.append(") ");
	}
	
	private void doAppendColumns(StringBuilder builder , List<Column> columns, boolean addType) {
		String separator = "";
		for (Column column : columns) {
			builder.append(separator);
			builder.append(column.getName());
			if (addType) {
				builder.append(" ");
				builder.append(((ColumnImpl) column).getDbType());
				if (column.isNotNull()) {
					builder.append(" NOT NULL");
				}
			}
			separator = ", ";			
		}
	}
}
