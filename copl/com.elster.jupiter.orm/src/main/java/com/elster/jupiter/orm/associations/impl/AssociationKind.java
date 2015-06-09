package com.elster.jupiter.orm.associations.impl;

import com.elster.jupiter.orm.associations.AbstractTemporalAspect;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalList;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public enum AssociationKind {
	UNMANAGEDVALUE {
		@Override
		public Object create(ForeignKeyConstraintImpl constraint,Field field, Object owner, Optional<?> initialValue) {
			return initialValue.orElse(null);
		}
	},
	REFERENCE {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Object create(ForeignKeyConstraintImpl constraint,Field field, Object owner, Optional<?> initialValue) {
			if (initialValue.isPresent()) {
				return new ReversePersistentReference(constraint,constraint.reverseMapper(field),owner,initialValue.get());
			} else {
				return new ReversePersistentReference(constraint,constraint.reverseMapper(field),owner);
			}
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public List<?> added(ForeignKeyConstraintImpl constraint, Field field, Object owner, boolean refresh) throws ReflectiveOperationException {
			Reference reference = (Reference) field.get(owner);
			if (refresh) {	
				field.set(owner,create(constraint, field, owner,Optional.empty()));				
			} else {
				field.set(owner,create(constraint, field, owner, reference.getOptional()));
			}
			if (reference.isPresent()) {
				return ImmutableList.of(reference.get());
			} else {
				return Collections.emptyList();
			}
		}
	},
	UNMANAGEDLIST {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Object create(ForeignKeyConstraintImpl constraint, Field field, Object owner,Optional<?> initialValue) {
			if (initialValue.isPresent()) {
				return new UnmanagedPersistentList(constraint,constraint.reverseMapper(field), owner, (List<?>) initialValue.get());
			} else {
				return new UnmanagedPersistentList(constraint,constraint.reverseMapper(field), owner);
			}
		}

	},
	MANAGEDLIST {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object create(ForeignKeyConstraintImpl constraint,Field field, Object owner,Optional<?> initialValue) {
			if (initialValue.isPresent()) {
				return new ManagedPersistentList(constraint, constraint.reverseMapper(field), owner, (List) initialValue.get());
			} else {
				return new ManagedPersistentList(constraint, constraint.reverseMapper(field), owner);
			}
		}
		
		@Override
		public List<?> added(ForeignKeyConstraintImpl constraint, Field field, Object owner,boolean refresh) throws ReflectiveOperationException {
			List<?> parts = (List<?>) field.get(owner);
			if (constraint.isAutoIndex()) {
				for (int i = 0 ; i < parts.size(); i++) {
					DomainMapper.FIELDSTRICT.set(parts.get(i), "position" , i+1);
				}
			}
			if (refresh) {						
				field.set(owner, create(constraint, field, owner,Optional.empty()));
			} else {
				field.set(owner, create(constraint, field, owner, Optional.of(parts)));				
			}
			return parts;
		}
	},
	
	TEMPORALREFERENCE {
		@SuppressWarnings("unchecked")
		@Override
		public Object create(ForeignKeyConstraintImpl constraint, Field field, Object owner, Optional<?> initialValue) {
			return new PersistentTemporalReference<>(constraint,(DataMapperImpl<? extends Effectivity>) constraint.reverseMapper(field),owner);
		}
		
		public List<?> added(ForeignKeyConstraintImpl constraint, Field field, Object owner,boolean refresh) throws ReflectiveOperationException {
			List<?>  result = ((AbstractTemporalAspect<?>) field.get(owner)).all();
			field.set(owner,create(constraint,field,owner,Optional.empty()));
			return result;
		}
	},
	TEMPORALLIST {
		@SuppressWarnings("unchecked")
		@Override
		public Object create(ForeignKeyConstraintImpl constraint, Field field, Object owner, Optional<?> initialValue) {
			return new PersistentTemporalList<>(constraint, (DataMapperImpl<? extends Effectivity>) constraint.reverseMapper(field), owner);
		}
		
		public List<?> added(ForeignKeyConstraintImpl constraint, Field field, Object owner,boolean refresh) throws ReflectiveOperationException {
			List<?>  result = ((AbstractTemporalAspect<?>) field.get(owner)).all();
			field.set(owner,create(constraint,field,owner,Optional.empty()));
			return result;
		}
	};
	
	abstract public Object create(ForeignKeyConstraintImpl constraint, Field field, Object owner, Optional<?> initialValue);
	
	public List<?> added(ForeignKeyConstraintImpl constraint, Field field, Object owner, boolean refresh) throws ReflectiveOperationException {
		return Collections.emptyList();
	}
		
	public static AssociationKind from (ForeignKeyConstraintImpl constraint) {
		Field field = constraint.getReferencedTable().getField(constraint.getReverseFieldName());
		if (constraint.isOneToOne()) {
			return Reference.class.isAssignableFrom(field.getType()) ? REFERENCE : UNMANAGEDVALUE;
		} else if (List.class.isAssignableFrom(field.getType())) {
			return constraint.isComposition() ? MANAGEDLIST : UNMANAGEDLIST; 
		} else if (TemporalReference.class.isAssignableFrom(field.getType())) {
			return TEMPORALREFERENCE;
		} else if (TemporalList.class.isAssignableFrom(field.getType())) {
			return TEMPORALLIST;
		}
		throw new IllegalArgumentException();
	}
}
