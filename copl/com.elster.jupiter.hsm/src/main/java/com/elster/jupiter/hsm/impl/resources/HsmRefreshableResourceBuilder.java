package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.model.HsmBaseException;

public interface HsmRefreshableResourceBuilder<T>  {

    /**
     *  returning the resource itself
     */
    T build() throws HsmBaseException;

    /**
     * timestamp for the resource. If this resource is backed up by a file last update time would be a good return value
     */
    Long timeStamp();

}
