package com.elster.jupiter.systemproperties;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;


import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

@Path("/systemproperties")
public class SystemPropertyResource {

    private final SystemPropertyService systemPropertyService;

    @Inject
    public SystemPropertyResource(SystemPropertyService systemPropertyService) {
        this.systemPropertyService = systemPropertyService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getSystemProperties(@BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {

        List<SystemProperty> sysPropsList = systemPropertyService.getAllSystemProperties();

        List<PropertyInfo> propertyInfos = new ArrayList<>();

        for (SystemProperty sp : sysPropsList){
            SystemPropertySpec spec = systemPropertyService.getPropertySpec(sp.getName()).get();
            PropertyInfo info = spec.preparePropertyInfo(sp);
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
    public Response updateSystemProperties(SystemPropertiesInfo propertiesInfo){

        List<PropertyInfo> propertiesToUpdate = propertiesInfo.properties;

        for (PropertyInfo property : propertiesToUpdate) {
            SystemProperty sysprop = systemPropertyService.getSystemPropertiesByName(property.key).get();
            SystemPropertySpec spec = systemPropertyService.getPropertySpec(property.key).get();
            //Update system property if value changed.
            if (!spec.convertValueToString(property).equals(sysprop.getValue())) {
                sysprop.setValue(spec.convertValueToString(property));
                systemPropertyService.actionOnPropertyChange(sysprop, spec);
            }
        }
        return Response.ok().build();
    }
}
