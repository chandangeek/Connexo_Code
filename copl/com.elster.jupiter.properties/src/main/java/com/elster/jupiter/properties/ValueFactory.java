package com.elster.jupiter.properties;

public interface ValueFactory<T> {

    public T fromStringValue(String stringValue);

    public String toStringValue(T object);

    public Class<T> getValueType();
    
    public boolean isReference ();
    
}