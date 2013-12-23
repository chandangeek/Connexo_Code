package com.elster.jupiter.orm.associations;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.elster.jupiter.util.Pair;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

public class ModuleCreator {
	
	public static Module create(final Class<?> ... classes) {
		
		return new AbstractModule() {
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected void configure() {				
				Set<Pair<TypeLiteral<?>, Class<?>>> bindings = new HashSet<>();
				for (Class<?> clazz : classes) {
					bindings.addAll(bindings(clazz));
				}
				for (Pair<TypeLiteral<?>, Class<?>> binding : bindings) {
					bind(binding.getFirst()).to((Class) binding.getLast());
				}
			}
		};
	}
	
	static Set<Pair<TypeLiteral<?>, Class<?>>> bindings(Class<?> clazz) {
		Set<Pair<TypeLiteral<?>, Class<?>>> bindings = new HashSet<>();
		while (clazz != Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.getAnnotation(Inject.class) != null) {
					Pair<TypeLiteral<?>, Class<?>> binding = binding(field);
					if (binding != null) {
						bindings.add(binding);
					}
				}
			}
		clazz = clazz.getSuperclass();
		}
		return bindings;
	}
		
	private static final Map<Class<?>,Class<?>> bindTypes = 
			ImmutableMap.of(Reference.class,ValueReference.class,List.class,ArrayList.class);
	
	
	static Pair<TypeLiteral<?>,Class <?>> binding(Field field) {
		if (bindTypes.containsKey(field.getType())) {
			Type referenceType = Types.newParameterizedType(field.getType(), extractTemplateType(field));
			return Pair.<TypeLiteral<?>,Class <?>> of(TypeLiteral.get(referenceType), bindTypes.get(field.getType()));
		} else {
			return null;
		}
	}
	
	static Type extractTemplateType(Field field) {
		return ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
	}
	


}
