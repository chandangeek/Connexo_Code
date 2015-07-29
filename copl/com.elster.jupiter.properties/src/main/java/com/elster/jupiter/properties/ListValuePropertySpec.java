package com.elster.jupiter.properties;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

public class ListValuePropertySpec<T extends HasIdAndName> extends BasicPropertySpec {

    private static final long serialVersionUID = 1L;

    public ListValuePropertySpec(String name, boolean required, CanFindByStringKey<T> finder, T... possibleValues) {
        super(name, required, new ListValueFactory<>(finder));
        List<ListValue<T>> values = new ArrayList<>(possibleValues.length);
        for (T t : possibleValues) {
            values.add(new ListValue<>(t));
        }
        setPossibleValues(new PropertySpecPossibleValuesImpl(true, values));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean isValuePossible(Object value) {
        if (value instanceof ListValue) {
            ListValue<T> listValue = (ListValue<T>) value;
            return this.isValuePossible(listValue);
        }
        else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isValuePossible(ListValue<T> value) {
        List<ListValue<T>> possibleListValues = (List<ListValue<T>>) possibleValues.getAllValues();
        for (final T v : value.getValues()) {
            Optional<ListValue<T>> found = possibleListValues.stream()
            		.filter(input -> v.getId().equals(input.getValue().getId()))
            		.findFirst();
            if (!found.isPresent()) {
                return false;
            }
        }
        return true;
    }
}
