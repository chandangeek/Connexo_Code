package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.FieldResource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class DeviceConfigFieldResource extends FieldResource{

    @GET
    @Path("/unit")
    public Object getLogLevelValues() {
        return asJsonArrayObject("units", "unit", new ReadingTypeUnitAdapter().getClientSideValues());
    }

}
