package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.impl.RefAnyImpl;
import com.google.inject.Injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;


public enum DomainMapper {
    FIELDSTRICT(true),
    FIELDLENIENT(false);

    private final boolean strict;

    private DomainMapper(boolean strict) {
        this.strict = strict;
    }

    public Object get(Object target, String fieldPath) {
        for (String fieldName : fieldPath.split("\\.")) {
            target = target == null ? null : basicGet(target, fieldName);
        }
        return target;
    }

    private Object basicGet(Object target, String fieldName) {
        Field field = getField(target.getClass(), fieldName);
        if (field == null) {
            return null;
        } else {
            try {
                return field.get(target);
            } catch (IllegalAccessException e) {
                throw new MappingException(e);
            }
        }
    }

    private Object create(Class<?> clazz, Injector injector) throws ReflectiveOperationException {
        if (clazz == RefAny.class) {
            if (injector == null) {
                throw new IllegalArgumentException("Needs injector");
            } else {
                return injector.getInstance(RefAnyImpl.class);
            }
        }
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private Object getOrCreate(Object target, String fieldName, Injector injector) {
        Field field = getField(target.getClass(), fieldName);
        if (field == null) {
            return null;
        } else {
            try {
                Object result = field.get(target);
                if (result == null) {
                    result = create(field.getType(), injector);
                    field.set(target, result);
                }
                return result;
            } catch (ReflectiveOperationException e) {
                throw new MappingException(e);
            }
        }
    }

    public void set(Object target, String fieldPath, Object value) {
        set(target, fieldPath, value, null);
    }

    private Object traverse(Object target, String fieldName, Injector injector, boolean create) {
        return create ? getOrCreate(target, fieldName, injector) : basicGet(target, fieldName);
    }

    public void set(Object target, String fieldPath, Object value, Injector injector) {
        String[] fieldNames = fieldPath.split("\\.");
        for (int i = 0; (target != null) && (i < fieldNames.length - 1); i++) {
            target = traverse(target, fieldNames[i], injector, value != null);
        }
        if (target != null) {
            basicSet(target, fieldNames[fieldNames.length - 1], value);
        }
    }

    @SuppressWarnings("unchecked")
    private void basicSet(Object target, String fieldName, Object value) {
        Field field = getField(target.getClass(), fieldName);
        if (field != null) {
            try {
                Object currentValue = field.get(target);
                if (currentValue instanceof Reference) {
                    ((Reference<Object>) currentValue).set(value);
                } else {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                throw new MappingException(e);
            }
        }
    }

    Field getField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        do {
            for (Field field : current.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    field.setAccessible(true);
                    return field;
                }
            }
            current = current.getSuperclass();
        } while (current != null);
        if (strict) {
            throw new MappingException(clazz, fieldName);
        } else {
            return null;
        }
    }

    Field getPathField(Class<?> clazz, String fieldPath) {
        Objects.requireNonNull(fieldPath);
        Field field = null;
        for (String fieldName : fieldPath.split("\\.")) {
            field = getField(clazz, fieldName);
            if (field == null) {
                return null;
            } else {
                clazz = field.getType();
            }
        }
        return field;
    }

    Class<?> getType(Class<?> implementation, String fieldPath) {
        Class<?> result = implementation;
        for (String fieldName : fieldPath.split("\\.")) {
            if (fieldName.equals(Column.TYPEFIELDNAME)) {
                result = String.class;
            } else {
                Field field = getField(result, fieldName);
                if (field == null) {
                    return null;
                } else {
                    result = field.getType();
                    if (result.equals(RefAny.class)) {
                        result = RefAnyImpl.class;
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    public static Class<?> extractDomainClass(Field field) {
        Type type = field.getGenericType();
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            Type subType = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (subType instanceof Class<?>) {
                // e.g. Reference<DomainModel>
                return (Class<?>) subType;
            } else if (subType instanceof ParameterizedType) {
                // e.g. Reference<DomainModel<Template>>
                return (Class<?>) ((ParameterizedType) subType).getRawType();
            } else if (subType instanceof TypeVariable) {
                // e.g. Reference<Template extends DomainModel>
                return (Class<?>) ((TypeVariable) subType).getBounds()[0];
            }
        }
        throw new IllegalArgumentException("" + type);
    }
}
