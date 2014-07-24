package com.energyict.mdc.protocol.api.services;

/**
 * Copyrights EnergyICT
 * Date: 6/3/14
 * Time: 5:31 PM
 */
public interface DeviceCacheMarshallingService {

    /**
     * Load the jsoned Cache object for the given string.
     * The unmarshalling should happen in the bundle which 'knows' the cache objects.
     *
     * @param jsonCache the json representation of the cache
     * @return the unmarshalled object
     */
    Object unMarshallCache(String jsonCache);

    String marshall(Object legacyCache);
}
