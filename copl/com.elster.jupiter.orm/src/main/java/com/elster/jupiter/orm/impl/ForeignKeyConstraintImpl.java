package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalAspect;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.associations.impl.AssociationKind;
import com.elster.jupiter.orm.associations.impl.PersistentReference;
import com.google.common.base.Optional;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.elster.jupiter.util.Checks.is;

public class ForeignKeyConstraintImpl extends TableConstraintImpl implements ForeignKeyConstraint {
	// persistent fields
	private DeleteRule deleteRule;
	private String fieldName;
	private String reverseFieldName;
	private String reverseOrderFieldName;
	private String reverseCurrentFieldName;
	private boolean composition;
	
	private final Reference<TableImpl<?>> referencedTable = ValueReference.absent();
	
	// transient field for reverse mapping
	private AssociationKind associationKind;
	
	@Override
	ForeignKeyConstraintImpl init(TableImpl<?> table, String name) {
        if (is(name).empty()) {
            throw new IllegalTableMappingException("Table " + table.getName() + " : foreign key can not have an empty name.");
        }
		super.init(table, name);
		return this;
	}
	
	static ForeignKeyConstraintImpl from(TableImpl<?> table, String name) {
		return new ForeignKeyConstraintImpl().init(table,name);
	}

	private void setReferencedTable(TableImpl<?> table) {
		this.referencedTable.set(table);
	}
	
	@Override
	public TableImpl<?> getReferencedTable() {
        if (!referencedTable.isPresent()) {
            throw new IllegalTableMappingException("Foreign key " + getName() + " on table " + getTable().getName() + " is missing a referenced table.");
        }
		return referencedTable.get();
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
	public boolean isComposition() {
		return composition;
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
			if ((constraint.isUnique() || constraint.isPrimaryKey()) && getColumns().containsAll(constraint.getColumns())) {
				return true;				
			}
		}
		return false;
	}
	
	@Override
	void validate() {
		super.validate();
		Objects.requireNonNull(getReferencedTable());
		Objects.requireNonNull(deleteRule);
		Objects.requireNonNull(fieldName);
		if (!deleteRule.equals(DeleteRule.RESTRICT) && getTable().hasJournal()) {
			throw new IllegalTableMappingException("Table : " + getTable().getName() + " : A journalled table cannot have a foreign key with cascade or set null delete rule");
		}
	}

    @Override
    public boolean matches(TableConstraintImpl other) {
        if (!super.matches(other)) {
            return false;
        }
        ForeignKeyConstraint fk = (ForeignKeyConstraint) other;
        if (deleteRule != fk.getDeleteRule()) {
            return false;
        }
        if (!getReferencedTable().getName().equals(fk.getReferencedTable().getName())) {
            return false;
        }
        return true;
    }

    public Object domainValue(Column column , Object target) {
		Reference<?> reference = (Reference<?>) getTable().getDomainMapper().get(target, getFieldName());
		if (reference == null || !reference.isPresent()) {
			return null;
		}
		int index = getColumns().indexOf(column);
		return extractKey(reference).get(index);
	}
	
	private KeyValue extractKey(Reference<?> reference) {
		if (reference instanceof PersistentReference<?>) {
			return ((PersistentReference<?>) reference).getKey();
		} else {
			return getReferencedTable().getPrimaryKeyConstraint().getColumnValues(reference.get());
		}
	}
	
	Optional<Type> getReferenceParameterType() {
		Field field = getTable().getField(fieldName);
		if (field == null || field.getType() != Reference.class) {
			return Optional.absent();
		} else {
			return Optional.of(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
		} 
	}
	
	Optional<Type> getListParameterType() {
		if (getReverseFieldName() == null) {
			return Optional.absent();
		}
		Field field = getReferencedTable().getField(getReverseFieldName());
		if (field == null || field.getType() != List.class) {
			return Optional.absent();
		} else {
			return Optional.of(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
		}
	}
	
	private DomainMapper getDomainMapper() {
		return getTable().getDomainMapper();
	}
	
	Field referenceField(Class<?> api) {
		return getDomainMapper().getField(api, getFieldName());
	}
	
	public Field reverseField(Class<?> api) {
		return getReferencedTable().getDomainMapper().getField(api, getReverseFieldName());
	}
	
	public DataMapperImpl<?> reverseMapper(Field field) {
		Class<?> clazz = DomainMapper.extractDomainClass(field);
		return getTable().getDataMapper(clazz);
	}
	
	void prepare() {
		if (reverseFieldName != null) {
			associationKind = AssociationKind.from(this);
		}
	}

	void setField(Object target , KeyValue keyValue) {
		Field field = referenceField(target.getClass());
		if (field != null && Reference.class.isAssignableFrom(field.getType())) {
			Class<?> api = DomainMapper.extractDomainClass(field);
			DataMapperImpl<?> dataMapper = getReferencedTable().getDataMapper(api);
			Reference<?> reference = new PersistentReference<>(keyValue, dataMapper);
			try {
				field.set(target, reference);
			} catch (ReflectiveOperationException ex) {
				throw new MappingException(ex);
			}
		}
	}
	
	public void setReverseField(Object target) {
		doSetReverseField(target,Optional.absent());
	}
	
	public void setReverseField(Object target, Object initialValue) {
		doSetReverseField(target,Optional.fromNullable(initialValue));
	}
	
	private void doSetReverseField(Object target,Optional<?> initialValue) {
		Field field = reverseField(target.getClass());
		if (field == null) {
			return;
		}
		Object value = associationKind.create(this, field, target,initialValue);
		try {
			field.set(target, value);
		} catch (ReflectiveOperationException ex) {
			throw new MappingException(ex);
		}
	}
	
	List<?> added(Object target, boolean needsRefresh) {
		Field field = reverseField(target.getClass());
		if (field == null) {
			return Collections.emptyList();
		} else {
			try {
				return associationKind.added(this,field,target,needsRefresh);
			} catch (ReflectiveOperationException ex) {
				throw new MappingException(ex);
			}
		} 
	}
	
	public boolean isTemporal() {
		if (reverseFieldName == null) {
			return false;
		}
		Field field = getReferencedTable().getField(reverseFieldName);
		if (field == null) {
			return false;
		} else {
			return TemporalAspect.class.isAssignableFrom(field.getType());
		}
	}
	
	public boolean isAutoIndex() {
		return "position".equals(reverseOrderFieldName);
	}
	
	static class BuilderImpl implements ForeignKeyConstraint.Builder {
		private final ForeignKeyConstraintImpl constraint;
		
		BuilderImpl(TableImpl<?> table, String name) {
			this.constraint = ForeignKeyConstraintImpl.from(table,name);
			constraint.deleteRule = DeleteRule.RESTRICT;
		}

		@Override
		public Builder on(Column... columns) {
            for (Column column : columns) {
                if (!constraint.getTable().equals(column.getTable())) {
                    throw new IllegalTableMappingException("Table " + constraint.getTable().getName() + " : foreign key can not have columns from another table as key : " + column.getName() + ".");
                }
            }
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
            TableImpl<?> referencedTable = constraint.getTable().getDataModel().getTable(name);
            if (referencedTable == null) {
                throw new IllegalTableMappingException("Foreign key FK_NAME on table " + constraint.getTable().getName() + " the referenced table " + name + " does not exist.");
            }
            constraint.setReferencedTable(referencedTable);
			return this;
		}

		@Override
		public Builder references(String component, String name) {
			TableImpl<?> table =  constraint.getTable().getDataModel().getOrmService().getTable(component, name);
			constraint.setReferencedTable(table);
			return this;
		}
		
		@Override
		public Builder reverseMap(String field) {
            if (constraint.getReferencedTable().getField(field) == null) {
                throw new IllegalTableMappingException("Foreign key FK_NAME on table " + constraint.getTable().getName() + " the referenced object does not have a field named " + field + ".");
            }
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
		public Builder composition() {
			constraint.composition = true;
			return this;
		}
		
		@Override
		public ForeignKeyConstraint add() {
			constraint.validate();
			constraint.getTable().add(constraint);
			return constraint;		
		}
	}
	
	
}
