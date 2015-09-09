package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.device.config.security.Privileges;

import com.elster.jupiter.nls.Thesaurus;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Why the wrapped return value? JavaScript people didn't want to see a naked JSON list, had to be
 * wrapped with meaningful field name.
 */

@Path("/field")
public class DeviceConfigFieldResource extends FieldResource {

    @Inject
    public DeviceConfigFieldResource(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/timeOfUse")
    public Object getTimeOfUseValues() {
        List<Map<String, Object>> list = new ArrayList<>(255);
        Map<String, Object> map = new HashMap<>();
        map.put("timeOfUse", list);
        for (int i = 0; i < 255; i++) {
            Map<String, Object> subMap = new HashMap<>();
            subMap.put("timeOfUse", i);
            subMap.put("localizedValue", i);
            list.add(subMap);
        }
        return map;
    }

    @GET
    @Path("/connectionStrategy")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Map<String, Object> getConnectionStrategies() {
        return asJsonArrayObjectWithTranslation("connectionStrategies", "connectionStrategy", this.clientSideConnectionStrategyValues());
    }

    private Collection<String> clientSideConnectionStrategyValues() {
        return Stream
                .of(ConnectionStrategyTranslationKeys.values())
                .map(ConnectionStrategyTranslationKeys::getKey)
                .collect(Collectors.toList());
    }

}
