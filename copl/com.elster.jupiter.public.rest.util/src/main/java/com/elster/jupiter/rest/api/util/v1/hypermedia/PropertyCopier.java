/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.hypermedia;

import javax.ws.rs.core.UriInfo;

/**
 * Functional interface used to copy a single property from a domain object to an info object.
 * UriInfo is passed along should there be a need to create links, without it, Supplier could have been used instead of PropertyCopier
 * Created by bvn on 5/13/15.
 */
@FunctionalInterface
public interface PropertyCopier<D, S> {
    public void copy(D d, S s, UriInfo uriInfo);
}
