package com.elster.jupiter.orm;

public interface ForeignKeyConstraint extends TableConstraint {
	Table getReferencedTable();	
	DeleteRule getDeleteRule();
	String getFieldName();
	String getReverseFieldName();
	String getReverseOrderFieldName();
	String getReverseCurrentFieldName();
}
