package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.model.HsmBaseException;

public interface HsmReloadableResource<T>  {

    /**
     *  returning the resource itself. Called first time.
     */
    T load() throws HsmBaseException;

    /**
     *  returning the resource itself. Called when observer based on timestamp decides we need to reload.
     */
    T reload() throws HsmBaseException;

    /**
     * close resource if needed.
     */
    void close();

    /**
     *
     * @return if the backing resource was changed, meaning we use different resource (like file used to load  was changed)
     */
    boolean changed();

    /**
     * timestamp for the resource. If this resource is backed up by a file last update time would be a good return value.
     * If implementation receives a different real resources we could use a fake timestamp to force reload (like NOW)
     */
    Long timeStamp();

}
