package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.FieldResource;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class DeviceConfigFieldResource extends FieldResource{

    @GET
    @Path("/unitOfMeasure")
    public Object getUnitValues() {
        return asJsonArrayObject("units", "unit", new ReadingTypeUnitAdapter().getClientSideValues());
    }

    @GET
    @Path("/timeOfUse")
    public Object getTimeOfUseValues() {
        List<Integer> ints = new ArrayList<>();
        for (int i=0; i< 255; i++) {
            ints.add(i);
        }
        return asJsonArrayObject("timeOfUse", "timeOfUse", ints);
    }


}
