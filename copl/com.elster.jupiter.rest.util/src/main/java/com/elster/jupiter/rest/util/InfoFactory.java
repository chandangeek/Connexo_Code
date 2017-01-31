/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Allows creation of an Info object based on a domain object. This interface is required to register factories to be used by dynamic search.
 */
public interface InfoFactory<T> {

    /**
     * Converts a DomainObject to an Info object
     * @param domainObject The domain object (DeviceImpl, ComServerImpl, FirmwareVersion, ....
     * @return Some Info object
     */
    Object from(T domainObject);

    default List<Object> from(List<T> domainObjects) {
        return domainObjects.stream().map(this::from).collect(Collectors.toList());
    }

    List<PropertyDescriptionInfo> modelStructure();

    /**
     * Base-class any domain object for this factory should obey IS-A relation to.
     */
    Class<T> getDomainClass();
}
