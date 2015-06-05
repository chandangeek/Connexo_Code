package com.elster.jupiter.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class ListValue<T extends HasIdAndName> {

    private List<T> values = new ArrayList<>();

    public ListValue() {
    }

    public ListValue(T value) {
        this();
        addValue(value);
    }

    public T getValue() {
        return values.size() > 0 ? values.get(0) : null;
    }

    public List<T> getValues() {
        return values;
    }

    public void addValue(T value) {
        values.add(value);
    }

    public void addValue(ListValue<T> value) {
        values.addAll(value.getValues());
    }

    public List<Object> getIds() {
        return values.stream().map(HasIdAndName::getId).collect(Collectors.toList());
    }
    
    public boolean hasSingleValue() {
        return values.size() == 1;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ListValue)) {
            return false;
        }
        ListIterator<T> e1 = values.listIterator();
        ListIterator<T> e2 = ((ListValue) obj).values.listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            T o1 = e1.next();
            T o2 = e2.next();
            //using t.getId() instead of t.equals()
            if (!(o1 == null ? o2 == null : (o1.getId() == null ? o2.getId() == null : o1.getId().equals(o2.getId())))) {
                return false;
            }
        }
        return !(e1.hasNext() || e2.hasNext());
    }
    
    @Override
    public int hashCode() {
        int i = 1;
        for (T value : values) {
            //using t.getId() instead of t.hashCode() 
            i = 31 * i + (value != null && value.getId() != null ? value.getId().hashCode() : 0);
        }
        return i;
    }
}
