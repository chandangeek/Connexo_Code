package com.elster.jupiter.orm.impl;

import java.util.Objects;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.internal.Bus;
import com.google.common.base.Optional;

public class ForeignKeyConstraintImpl extends TableConstraintImpl implements ForeignKeyConstraint {
	// persistent fields
	private DeleteRule deleteRule;
	private String referencedComponentName;
	private String referencedTableName;	
	private String fieldName;
	private String reverseFieldName;
	private String reverseOrderFieldName;
	private String reverseCurrentFieldName;
	
	private Table referencedTable;
	
	@SuppressWarnings("unused")
	private ForeignKeyConstraintImpl() {	
	}
	
	private ForeignKeyConstraintImpl(Table table, String name) {
		super(table,name);
	}

	ForeignKeyConstraintImpl(Table table, String name, Table referencedTable, DeleteRule deleteRule,AssociationMapping mapping) {
		this(table,name);
		setReferencedTable(referencedTable);
		this.deleteRule = deleteRule;
		this.fieldName = mapping.getFieldName();	
		this.reverseFieldName = mapping.getReverseFieldName();
		this.reverseOrderFieldName = mapping.getReverseOrderFieldName();
		this.reverseCurrentFieldName = mapping.getReverseCurrentFieldName();
	}

	private final void setReferencedTable(Table table) {
		this.referencedTable = Objects.requireNonNull(table);
		this.referencedComponentName = table.getComponentName();
		this.referencedTableName = table.getName();
	}
	
	@Override
	public Table getReferencedTable() {
		if (referencedTableName == null || referencedComponentName == null) {
			return null;
		}
		if (referencedTable == null) {
			if (referencedComponentName.equals(getComponentName())) {
				referencedTable = getTable().getDataModel().getTable(referencedTableName);
			} else {
				referencedTable = Bus.getOrmClient().getTableFactory().getExisting(referencedComponentName, referencedTableName);
			}
		}
		return referencedTable;
	}
	
	@Override
	public DeleteRule getDeleteRule() {
		return deleteRule;
	}
	
	@Override 
	public String getFieldName() {
		return fieldName;
	}
	
	@Override 
	public String getReverseFieldName() {
		return reverseFieldName;
	}
	
	@Override 
	public String getReverseCurrentFieldName() {
		return reverseCurrentFieldName;
	}
	
	@Override
	public String getReverseOrderFieldName() {
		return reverseOrderFieldName;
	}

	@Override
	public void postLoad() {	
		super.postLoad();
		if (referencedComponentName != null && !referencedComponentName.equals(getComponentName())) {
			getReferencedTable();
		}
	}
	
	@Override
	public boolean isForeignKey() {
		return true;
	}	
	
	boolean needsIndex() {
		for (TableConstraint constraint : getTable().getConstraints()) {
			if (constraint.isPrimaryKey() || constraint.isUnique()) {
				if (this.isSubset(constraint)) {
					return false;
				}
 			}
		}
		return true;
	}
	
	private boolean isSubset(TableConstraint other) {
		if (other.getColumns().size() < this.getColumns().size()) {
			return false;
		}
		for (int i = 0 ; i < getColumns().size() ; i++) {
			if (!this.getColumns().get(i).equals(other.getColumns().get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	String getTypeString() {
		return "foreign key";
	}
	
	@Override
	void appendDdlTrailer(StringBuilder sb) {
		sb.append(" references ");
		sb.append(getReferencedTable().getQualifiedName());
		sb.append(" ");
		sb.append(getDeleteRule().getDdl());
	}
	
	@Override
	public boolean isOneToOne() {
		for (TableConstraint constraint : getTable().getConstraints()) {
			if (constraint.isUnique() && getColumns().containsAll(constraint.getColumns())) {
					return true;				
			}
		}
		return false;
	}
	
	@Override
	void validate() {
		super.validate();
		Objects.requireNonNull(referencedComponentName);
		Objects.requireNonNull(referencedTableName);
		Objects.requireNonNull(deleteRule);
		Objects.requireNonNull(fieldName);
	}
	
	static class BuilderImpl implements ForeignKeyConstraint.Builder {
		private final ForeignKeyConstraintImpl constraint;
		
		BuilderImpl(Table table, String name) {
			this.constraint = new ForeignKeyConstraintImpl(table,name);
			constraint.deleteRule = DeleteRule.RESTRICT;
		}

		@Override
		public Builder on(Column... columns) {
			constraint.add(columns);
			return this;
		}

		@Override
		public Builder onDelete(DeleteRule deleteRule) {
			constraint.deleteRule = deleteRule;
			return this;
		}
		
		@Override
		public Builder map(String field) {
			constraint.fieldName = field;
			return this;
		}

		@Override
		public Builder references(String name) {
			constraint.setReferencedTable(constraint.getTable().getDataModel().getTable(name));
			return this;
		}

		@Override
		public Builder references(String component, String name) {
			Table table = Bus.getTable(component, name);
			constraint.setReferencedTable(table);
			return this;
		}
		
		@Override
		public Builder reverseMap(String field) {
			constraint.reverseFieldName = field;
			return this;
		}

		@Override
		public Builder reverseMapOrder(String field) {
			constraint.reverseOrderFieldName = field;
			return this;
		}

		@Override
		public Builder reverseMapCurrent(String field) {
			constraint.reverseCurrentFieldName = field;
			return this;
		}
		
		@Override
		public ForeignKeyConstraint add() {
			constraint.validate();
			((TableImpl) constraint.getTable()).add(constraint);
			return constraint;		
		}
	}
	
}
