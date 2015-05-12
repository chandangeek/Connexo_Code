package com.elster.jupiter.properties;

/**
 * Created by dvy on 11/05/2015.
 */
public interface BoundedLongPropertySpec extends PropertySpec{
    @Override
    public ValueFactory<Long> getValueFactory();

    public Long getLowerLimit ();

    public Long getUpperLimit ();
}
