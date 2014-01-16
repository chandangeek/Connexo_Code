package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.*;
import com.google.common.base.Optional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractFinder<T> implements Finder<T> {

    @Override
    public final List<T> find() {
        return find((String[]) null, (Object[]) null, (String[]) null);
    }

    @Override
    public final List<T> find(String fieldName, Object value) {
        return find(new String[]{fieldName}, new Object[]{value});
    }

    @Override
    public final List<T> find(String fieldName, Object value, String orderBy) {
        return find(new String[]{fieldName}, new Object[]{value}, orderBy);
    }

    @Override
    public final List<T> find(String fieldName1, Object value1, String fieldName2, Object value2) {
        return find(new String[]{fieldName1, fieldName2}, new Object[]{value1, value2});
    }

    @Override
    public final List<T> find(String fieldName1, Object value1, String fieldName2, Object value2, String orderBy) {
        return find(new String[]{fieldName1, fieldName2}, new Object[]{value1, value2}, orderBy);
    }

    @Override
    public final List<T> find(String[] fieldNames, Object[] values) {
        return find(fieldNames, values, (String[]) null);
    }

    @Override
    public final List<T> find(Map<String, Object> valueMap) {
        return find(valueMap, (String[]) null);
    }

    @Override
    public final List<T> find(Map<String, Object> valueMap, String... orderBy) {
        if (valueMap == null) {
            return find((String[]) null, (Object[]) null, orderBy);
        }
        String[] fieldNames = new String[valueMap.size()];
        Object[] values = new Object[valueMap.size()];
        int index = 0;
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            fieldNames[index] = entry.getKey();
            values[index++] = entry.getValue();
        }
        return find(fieldNames, values, orderBy);
    }

    @Override
    public final Optional<T> getUnique(String fieldName, Object value) {
        return getUnique(new String[]{fieldName}, new Object[]{value});
    }


    @Override
    public final Optional<T> getUnique(String fieldName1, Object value1, String fieldName2, Object value2) {
        return getUnique(new String[]{fieldName1, fieldName2}, new Object[]{value1, value2});
    }

    @Override
    public final Optional<T> getUnique(String[] fieldNames, Object[] values) {
        List<T> candidates = find(fieldNames, values);
        if (candidates.size() > 1) {
        	throw new NotUniqueException(Arrays.toString(values));
        }        
        return candidates.isEmpty() ? Optional.<T> absent() : Optional.of(candidates.get(0));
    }

    @Override
    public final Optional<T> getOptional(Object... values) {
        if (getPrimaryKeyLength() != values.length) {
            throw new IllegalArgumentException("Argument array length " + values.length + " does not match Primary Key Field count of " + getPrimaryKeyLength());
        }
        return findByPrimaryKey(KeyValue.of(values));
    }

    @Override
    public T getExisting(Object... values) {
        Optional<T> existing = getOptional(values);
        if (existing.isPresent()) {
            return existing.get();
        }
        throw new DoesNotExistException(Arrays.toString(values));
    }


    abstract int getPrimaryKeyLength();

    abstract Optional<T> findByPrimaryKey(KeyValue value);
}
