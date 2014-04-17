package com.elster.jupiter.orm.impl;

import java.util.ArrayList;
import java.util.List;

class TableDdlGenerator {
	private final TableImpl<?> table;
	private List<String> ddl;
	
	TableDdlGenerator(TableImpl<?> table) {
		this.table = table;				
	}
	
	List<String> getDdl() {
		ddl = new ArrayList<>();
		ddl.add(getTableDdl());
		if (table.hasJournal() ) {
			ddl.add(getJournalTableDdl());
		}
		for (TableConstraintImpl constraint : table.getConstraints()) {
			if (constraint.needsIndex()) {
				ddl.add(getConstraintIndexDdl(constraint));
			}
		}		
		for (IndexImpl index : table.getIndexes()) {
			ddl.add(getIndexDdl(index));
		}
		for (ColumnImpl column : table.getColumns()) {
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
		for (TableConstraintImpl constraint : table.getConstraints()) {
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
		TableConstraintImpl constraint = table.getPrimaryKeyConstraint();
		if (constraint != null) {
			sb.append(separator);
			sb.append(getJournalConstraint(constraint));
			}
		sb.append(")");
		return sb.toString();		
	}
	
	private String getJournalConstraint(TableConstraintImpl constraint) {
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
	
	private String getConstraintFragment(TableConstraintImpl constraint) {
		return constraint.getDdl();
	}
	
	
	private String getConstraintIndexDdl(TableConstraintImpl constraint) {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE INDEX ");
		builder.append(constraint.getName());
		builder.append(" ON ");
		builder.append(table.getQualifiedName());
		appendColumns(builder,constraint.getColumns(), false);
		return builder.toString();		
	}
	
	private String getIndexDdl(IndexImpl index) {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE INDEX ");
		builder.append(index.getName());
		builder.append(" ON ");
		builder.append(table.getQualifiedName());
		appendColumns(builder,index.getColumns(), false);
		if (index.getCompress() > 0) {
			builder.append(" COMPRESS ");
			builder.append(index.getCompress());
		}
		return builder.toString();		
	}
	
	private String getSequenceDdl(ColumnImpl column) {
		// cache 1000 for performance in RAC environments
		return 
			"create sequence " +
		   column.getQualifiedSequenceName() +
		    " cache 1000";
	}
	
	private void appendColumns(StringBuilder builder , List<ColumnImpl> columns, boolean addType) {
		builder.append(" (");
		doAppendColumns(builder, columns, addType);
		builder.append(") ");
	}
	
	private void doAppendColumns(StringBuilder builder , List<ColumnImpl> columns, boolean addType) {
		String separator = "";
		for (ColumnImpl column : columns) {
			builder.append(separator);
			builder.append(column.getName());
			if (addType) {
				builder.append(" ");
				builder.append(column.getDbType());
				if (column.isNotNull()) {
					builder.append(" NOT NULL");
				}
			}
			separator = ", ";			
		}
	}
}
