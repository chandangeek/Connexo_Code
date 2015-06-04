package com.elster.jupiter.rest.util;

/**
 * Allows creation of an Info object based on a domain object.
 */
public interface InfoFactory<T> {

    /**
     * Converts a DomainObject to an Info object
     * @param domainObject The domain object (DeviceImpl, ComServerImpl, FirmwareVersion, ....
     * @return Some Info object
     */
    public Object from(T domainObject);

    /**
     * Base-class any domain object for this factory should obey IS-A relation to.
     */
    public Class<T> getDomainClass();
}
