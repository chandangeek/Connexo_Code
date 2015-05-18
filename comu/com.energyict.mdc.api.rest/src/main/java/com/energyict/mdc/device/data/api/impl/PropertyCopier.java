package com.energyict.mdc.device.data.api.impl;

import java.util.Optional;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 5/13/15.
 */
public interface PropertyCopier<D,S> {
    public void copy(D d, S s, Optional<UriInfo> uriInfo);
}
