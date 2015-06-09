package com.elster.jupiter.rest.util;

import java.util.List;

/**
 * Allows creation of an Info object based on a domain object. This interface is required to register factories to be used by dynamic search.
 */
public interface InfoFactory<T> {

    /**
     * Converts a DomainObject to an Info object
     * @param domainObject The domain object (DeviceImpl, ComServerImpl, FirmwareVersion, ....
     * @return Some Info object
     */
    public Object from(T domainObject);

    List<PropertyDescriptionInfo> modelStructure();

    /**
     * Base-class any domain object for this factory should obey IS-A relation to.
     */
    public Class<T> getDomainClass();
}
