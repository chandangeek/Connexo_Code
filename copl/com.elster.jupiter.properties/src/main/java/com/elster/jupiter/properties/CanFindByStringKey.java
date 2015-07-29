package com.elster.jupiter.properties;

import java.util.Optional;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface CanFindByStringKey<T extends HasIdAndName> {

    public abstract Optional<T> find(String key);

    public Class<T> valueDomain();

}
