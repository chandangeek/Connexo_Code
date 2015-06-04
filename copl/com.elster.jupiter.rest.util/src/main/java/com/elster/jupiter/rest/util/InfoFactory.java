package com.elster.jupiter.rest.util;

/**
 * Created by bvn on 6/4/15.
 */
public interface InfoFactory<T> {

    /**
     * Converts a DomainObject to an Info object
     * @param domainObject The domain object (DeviceImpl, ComServerImpl, FirmwareVersion, ....
     * @return Some Info type
     */
    public Object from(T domainObject);

    public Class<T> getDomainClass();
}
