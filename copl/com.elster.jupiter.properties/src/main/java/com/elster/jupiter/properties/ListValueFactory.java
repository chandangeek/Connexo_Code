package com.elster.jupiter.properties;

import com.google.common.base.Joiner;

import java.util.Optional;

public class ListValueFactory<T extends HasIdAndName> extends AbstractValueFactory<ListValue<T>> {

    public static final int MAX_SIZE = 4000;
    private static final String LIST_SEPARATOR = ",";

    private CanFindByStringKey<T> finder;

    public ListValueFactory(CanFindByStringKey<T> finder) {
        this.finder = finder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<ListValue<T>> getValueType() {
        return (Class<ListValue<T>>) new ListValue<T>().getClass();
    }

    @Override
    public String getDatabaseTypeName() {
        return "varchar2(" + MAX_SIZE + ")";
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public ListValue<T> fromStringValue(String stringValue) {
        if (stringValue == null) {
            return new ListValue<T>();
        }
        String[] keys = stringValue.split(LIST_SEPARATOR);
        ListValue<T> result = new ListValue<T>();
        for (String key : keys) {
            Optional<T> value = finder.find(key);
            if (value.isPresent()) {
                result.addValue(value.get());
            }
        }
        return result;
    }

    @Override
    public String toStringValue(ListValue<T> object) {
        return Joiner.on(LIST_SEPARATOR).skipNulls().join(object.getIds());
    }

    @Override
    public ListValue<T> valueFromDatabase(Object object) {
        return fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(ListValue<T> object) {
        return toStringValue(object);
    }

}