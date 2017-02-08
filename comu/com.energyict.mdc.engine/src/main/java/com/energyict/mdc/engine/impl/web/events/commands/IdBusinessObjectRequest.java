/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import java.util.Set;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a number of business objects
 * that are uniquely identified by a single long.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:58)
 */
abstract class IdBusinessObjectRequest implements Request {

    private Set<Long> businessObjectIds;

    IdBusinessObjectRequest(Set<Long> businessObjectIds) {
        super();
        this.businessObjectIds = businessObjectIds;
    }

    public Set<Long> getBusinessObjectIds () {
        return businessObjectIds;
    }

}