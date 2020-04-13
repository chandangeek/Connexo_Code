package com.elster.jupiter.util;

/**
 * By implementing this interface, an entity must be extended
 * with additional field "externalId", which is used by external
 * 3-rd party application, e.g. Identity Provider.
 */
public interface HasExternalId {

    /**
     * Get the external id of the object.
     *
     * @return the external id of the object.
     */
    String getExternalId();

}
