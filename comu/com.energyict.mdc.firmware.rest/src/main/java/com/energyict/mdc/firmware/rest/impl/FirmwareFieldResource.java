package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.FieldResource;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Path("/field")
public class FirmwareFieldResource extends FieldResource {
    private final Thesaurus thesaurus;

    @Inject
    public FirmwareFieldResource(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @GET
    @Path("/firmwareStatuses")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Object getFirmwareStatuses() {
        return asJsonArrayObjectWithTranslation("firmwareStatuses", "id", new FirmwareStatusFieldAdapter().getClientSideValues());
    }

    @GET
    @Path("/firmwareTypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Object getFirmwareTypes() {
        return asJsonArrayObjectWithTranslation("firmwareTypes", "id", new FirmwareTypeFieldAdapter().getClientSideValues());
    }

    @Override
    protected <T> Map<String, Object> asJsonArrayObjectWithTranslation(String fieldName, String valueName, Collection<T> values) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        map.put(fieldName, list);
        for (final T value : values) {
            Map<String, Object> subMap = new HashMap<>();
            subMap.put(valueName, value);
            subMap.put("displayValue", thesaurus.getString(value.toString(), value.toString()));
            list.add(subMap);
        }
        return map;
    }
}
