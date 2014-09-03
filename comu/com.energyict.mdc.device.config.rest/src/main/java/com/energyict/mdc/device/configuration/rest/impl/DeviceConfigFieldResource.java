package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.masterdata.MasterDataService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
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
    @Path("/unitOfMeasure")
    @RolesAllowed(Privileges.VIEW_DEVICE_CONFIGURATION)
    public Object getUnitValues() {
        List<Long> allUnitsWithPhenomena = new ArrayList<>();
        List<String> translationKeys = new ArrayList<>();
        for (Phenomenon phenomenon : this.masterDataService.findAllPhenomena()) {
            try {
                allUnitsWithPhenomena.add(phenomenon.getId());
                translationKeys.add(phenomenon.getName());
            } catch (Exception e) {
                throw new WebApplicationException("Failed to convert unit into JSON", Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(phenomenon).build());
            }
        }
        return asJsonArrayObjectWithTranslation("units", "id", allUnitsWithPhenomena, translationKeys);
    }

    @GET
    @Path("/timeOfUse")
    @RolesAllowed(Privileges.VIEW_DEVICE_CONFIGURATION)
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
        return map;
    }

    @GET
    @Path("/connectionStrategy")
    @RolesAllowed(Privileges.VIEW_DEVICE_CONFIGURATION)
    public Object getConnectionStrategies() {
        return asJsonArrayObjectWithTranslation("connectionStrategies", "connectionStrategy", new ConnectionStrategyAdapter().getClientSideValues());
    }

}
