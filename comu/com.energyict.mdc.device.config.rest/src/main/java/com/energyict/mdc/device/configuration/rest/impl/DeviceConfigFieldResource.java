package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.masterdata.MasterDataService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class DeviceConfigFieldResource extends FieldResource{

    private final MasterDataService masterDataService;

    @Inject
    public DeviceConfigFieldResource(MasterDataService masterDataService, NlsService nlsService) {
        super(nlsService.getThesaurus(DeviceConfigurationApplication.COMPONENT_NAME, Layer.UI));
        this.masterDataService = masterDataService;
    }

    @GET
    @Path("/unit")
    public Object getUnitValues() {
        List<String> allUnitsWithPhenomena = new ArrayList<>();
        UnitAdapter unitAdapter = new UnitAdapter();
        for (Phenomenon phenomenon : this.masterDataService.findAllPhenomena()) {
            try {
                allUnitsWithPhenomena.add(unitAdapter.marshal(phenomenon.getUnit()));
            } catch (Exception e) {
                throw new WebApplicationException("Failed to convert unit into JSON", Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(phenomenon).build());
            }
        }
        return asJsonArrayObject("units", "unit", allUnitsWithPhenomena);
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

    @GET
    @Path("/connectionStrategy")
    public Object getConnectionStrategies() {
        return asJsonArrayObject("connectionStrategies", "connectionStrategy", new ConnectionStrategyAdapter().getClientSideValues());
    }

}
