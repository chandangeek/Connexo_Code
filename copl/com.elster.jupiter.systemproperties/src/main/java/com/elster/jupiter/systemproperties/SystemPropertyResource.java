package com.elster.jupiter.systemproperties;



import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;


import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/systemproperties")
public class SystemPropertyResource {

    private final SystemPropertyService systemPropertyService;

    @Inject
    public SystemPropertyResource(SystemPropertyService systemPropertyService) {
        /*this.meteringGroupsService = meteringGroupsService;
        this.favoritesService = favoritesService;
        this.conflictFactory = conflictFactory;*/
        System.out.println("SystemPropertyResource!!!!!!!!!!!!!!!!!!!");
        this.systemPropertyService = systemPropertyService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getSystemProperties(@BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        System.out.println("GET!!!!!!!!!!!!!!!!!!!");

        List<SystemProperty> sysPropsList = systemPropertyService.getAllSystemProperties();
        System.out.println("sysPropsList ="+sysPropsList);

        List<PropertyInfo> propertyInfos = new ArrayList<>();

        for (SystemProperty sp : sysPropsList){
            System.out.println("SP name ="+sp.getName() + "SP value = "+sp.getValue());
            SystemPropertySpec spec = systemPropertyService.getPropertySpec(sp.getName()).get();
            PropertyValueInfo propertyValueInfo = new PropertyValueInfo(sp.getValue(),
                    null,
                    spec.getDefaultValue(),
                    null);
            PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
            propertyTypeInfo.simplePropertyType = spec.getPropertyType();
            PropertyInfo info  = new PropertyInfo(sp.getName(),
                    spec.getKey(),
                    spec.getDescription(),
                    propertyValueInfo,
                    propertyTypeInfo,
                    true);
            propertyInfos.add(info);
        }

        return Response.ok().entity(propertyInfos).build();
    }


    @GET
    @Transactional
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getSystemProperty(@BeanParam JsonQueryParameters queryParameters,
                                      @Context SecurityContext securityContext,
                                      @PathParam("name") String name) {
        System.out.println("GET!!!!!!!!!!!!!!!!!!!");

        Optional<SystemPropertySpec> spec =  systemPropertyService.getPropertySpec(name);

        if(spec.isPresent() == false){
            return Response.status(Response.Status.BAD_REQUEST).entity("Unsupported property requested").build();
        }

        Optional<SystemProperty> sysProp = systemPropertyService.getSystemPropertiesByName(name);

        if(sysProp.isPresent() == false){
            return Response.status(Response.Status.NOT_FOUND).entity("Requested property "+name+" not found").build();
        }

        System.out.println("SystemProperty"+ sysProp.get().getName() + " value = "+sysProp.get().getValue());

        PropertyValueInfo propertyValueInfo = new PropertyValueInfo(sysProp.get().getValue(),
                null,
                spec.get().getDefaultValue(),
                null);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = spec.get().getPropertyType();
        PropertyInfo info  = new PropertyInfo(sysProp.get().getName(),
                spec.get().getKey(),
                spec.get().getDescription(),
                propertyValueInfo,
                propertyTypeInfo,
                true);


        return Response.ok().entity(info).build();
    }


    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response updateSystemProperties(PropertyInfo property){

        System.out.println("PROPERTY ="+property.key+" NAME ="+property.name+" value ="+property.getPropertyValueInfo().getValue());

        SystemProperty sysprop = systemPropertyService.getSystemPropertiesByName(property.name).get();

        System.out.println("NAME ="+sysprop.getName() + "OLD VALUE = "+sysprop.getValue());

        sysprop.setValue((String)property.getPropertyValueInfo().getValue());

        sysprop.update();

        SystemPropertySpec spec = systemPropertyService.getPropertySpec(property.name).get();

        spec.actionOnChange(sysprop);

        return Response.ok().build();
    }

}
