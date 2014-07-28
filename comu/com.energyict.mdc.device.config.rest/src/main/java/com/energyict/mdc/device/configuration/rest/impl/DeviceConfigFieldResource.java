package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.masterdata.MasterDataService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public DeviceConfigFieldResource(MasterDataService masterDataService, Thesaurus thesaurus) {
        super(thesaurus);
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
        return asJsonArrayObjectWithTranslation("units", "unit", allUnitsWithPhenomena);
    }

    @GET
    @Path("/timeOfUse")
    public Object getTimeOfUseValues() {
        List<Map<String, Object>> list = new ArrayList<>(255);
        HashMap<String, Object> map = new HashMap<>();
        map.put("timeOfUse", list);
        for (int i=0; i< 255; i++) {
            HashMap<String, Object> subMap = new HashMap<>();
            subMap.put("timeOfUse", i);
            subMap.put("localizedValue", i);
            list.add(subMap);
        }
        return list;
    }

    @GET
    @Path("/connectionStrategy")
    public Object getConnectionStrategies() {
        return asJsonArrayObjectWithTranslation("connectionStrategies", "connectionStrategy", new ConnectionStrategyAdapter().getClientSideValues());
    }

}
