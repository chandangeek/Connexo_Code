package com.energyict.mdc.device.data.api.impl;

import java.net.URI;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 4/29/15.
 */
public interface UriProvider {
    public URI uri(UriInfo uriInfo);
}
