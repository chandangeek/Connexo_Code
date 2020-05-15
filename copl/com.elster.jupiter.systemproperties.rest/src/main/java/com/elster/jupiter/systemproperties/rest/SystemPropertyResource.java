package com.elster.jupiter.systemproperties.rest;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.systemproperties.SystemProperty;
import com.elster.jupiter.systemproperties.SystemPropertyService;
import com.elster.jupiter.systemproperties.SystemPropertySpec;
import com.elster.jupiter.systemproperties.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("/systemproperties")
public class SystemPropertyResource {

    private final SystemPropertyService systemPropertyService;
    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public SystemPropertyResource(SystemPropertyService systemPropertyService,
                                  PropertyValueInfoService propertyValueInfoService) {
        this.systemPropertyService = systemPropertyService;
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_SYS_PROPS, Privileges.Constants.VIEW_SYS_PROPS})
    public Response getSystemProperties() {

        List<SystemProperty> sysPropsList = systemPropertyService.getAllSystemProperties();
        List<PropertyInfo> propertyInfos = new ArrayList<>();

        for (SystemProperty sp : sysPropsList){
            SystemPropertySpec spec = systemPropertyService.findPropertySpec(sp.getKey()).get();
            Object val = spec.getPropertySpec().getValueFactory().fromStringValue(sp.getValue());
            PropertyInfo info = propertyValueInfoService.getPropertyInfo(spec.getPropertySpec(), key -> val);
            propertyInfos.add(info);
        }

        SystemPropertiesInfo propsInfos = new SystemPropertiesInfo();
        propsInfos.properties = propertyInfos;

        return Response.ok().entity(propsInfos).build();
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_SYS_PROPS})
    public Response updateSystemProperties(SystemPropertiesInfo propertiesInfo){
        List<PropertyInfo> propertiesToUpdate = propertiesInfo.properties;

        for (PropertyInfo property : propertiesToUpdate) {
            SystemPropertySpec spec = systemPropertyService.findPropertySpec(property.key).get();
            List<PropertyInfo> propses = new ArrayList();
            propses.add(property);
            Object value = propertyValueInfoService.findPropertyValue(spec.getPropertySpec(), (Collection<PropertyInfo>) propses);

            String valueStr =  spec.getPropertySpec().getValueFactory().toStringValue(value);

            systemPropertyService.setPropertyValue(property.key, valueStr /*spec.convertValueToString(property)*/);
        }

        return Response.ok().build();
    }
}
