/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

import java.util.Optional;

public interface DeviceCacheMarshallingService {

    /**
     * Marshalls a cache.
     * The DeviceCacheMarshallingService should be prepared
     * to receive caches that are not managed in the bundle and
     * will throw a {@link DeviceCacheMarshallingException}
     * when that is the case.
     *
     * @param cache The cache that needs marshalling
     * @return The marshalled version of the cache
     * @throws DeviceCacheMarshallingException Indicates failure to marshall the cache
     * @throws NotAppropriateDeviceCacheMarshallingTargetException Indicates that this service does not recognize the cache and therefore cannot marshall it
     */
    public String marshall(Object cache) throws DeviceCacheMarshallingException, NotAppropriateDeviceCacheMarshallingTargetException;

    /**
     * Unmarshalls a previously marshalled cache.
     * The DeviceCacheMarshallingService should be prepared
     * to receive marshalled results from other services and
     * will throw a {@link DeviceCacheMarshallingException}
     * when that is the case.
     *
     * @param marshalledCache the json representation of the cache
     * @return the unmarshalled object
     * @throws DeviceCacheMarshallingException Indicates failure to unmarshall the cache
     * @throws NotAppropriateDeviceCacheMarshallingTargetException Indicates that this service does not recognize the marshalled cache and therefore cannot unmarshall it
     */
    public Optional<Object> unMarshallCache(String marshalledCache) throws DeviceCacheMarshallingException, NotAppropriateDeviceCacheMarshallingTargetException;

}