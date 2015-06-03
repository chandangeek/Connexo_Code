package com.elster.jupiter.properties;

import java.util.Optional;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface FindById<T extends IdWithNameValue> {

    public abstract Optional<T> findById(Object id);

}
