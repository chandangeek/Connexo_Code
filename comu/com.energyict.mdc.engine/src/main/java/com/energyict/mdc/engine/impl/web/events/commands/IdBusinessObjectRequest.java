package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.IdBusinessObject;

import java.util.Set;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a number of {@link IdBusinessObject}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:58)
 */
public abstract class IdBusinessObjectRequest implements Request {

    private Set<Long> businessObjectIds;

    public IdBusinessObjectRequest (Set<Long> businessObjectIds) {
        super();
        this.businessObjectIds = businessObjectIds;
    }

    public Set<Long> getBusinessObjectIds () {
        return businessObjectIds;
    }

}