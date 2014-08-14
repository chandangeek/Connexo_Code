package com.elster.jupiter.properties;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ListValuePropertySpec<T extends ListValueEntry> extends BasicPropertySpec<ListValue<T>> {

    private static final long serialVersionUID = 1L;

    public ListValuePropertySpec(String name, boolean required, FindById<T> finder, T... possibleValues) {
        super(name, required, new ListValueFactory<T>(finder));
        List<ListValue<T>> values = new ArrayList<>(possibleValues.length);
        for (T t : possibleValues) {
            values.add(new ListValue<T>(t));
        }
        setPossibleValues(new PropertySpecPossibleValuesImpl<>(true, values));
    }
    
    @Override
    protected boolean isValuePossible(ListValue<T> value) {
        List<ListValue<T>> possibleListValues = (List<ListValue<T>>) possibleValues.getAllValues();
        for (final T v : value.getValues()) {
            Optional<ListValue<T>> found = Iterables.<ListValue<T>>tryFind(possibleListValues, new Predicate<ListValue<T>>() {

                @Override
                public boolean apply(ListValue<T> input) {
                    return v.getId().equals(input.getValue().getId());
                }
            });
            if (!found.isPresent()) {
                return false;
            }
        }
        return true;
    }
}
