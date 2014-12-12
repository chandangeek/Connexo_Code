package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.device.data.rest.LogLevelAdapter;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/field")
public class DeviceFieldResource extends FieldResource {

    @Inject
    public DeviceFieldResource(NlsService nlsService) {
        super(nlsService.getThesaurus(DeviceApplication.COMPONENT_NAME, Layer.REST));
    }
    
    @GET
    @Path("/enddevicedomains")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Object getAllEndDeviceDomains() {
        return asJsonArrayObjectWithTranslation("domains", "domain", new EndDeviceDomainAdapter().getClientSideValues());
    }
    
    @GET
    @Path("/enddevicesubdomains")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Object getAllEndDeviceSubDomains() {
        return asJsonArrayObjectWithTranslation("subDomains", "subDomain", new EndDeviceSubDomainAdapter().getClientSideValues());
    }
    
    @GET
    @Path("/enddeviceeventoractions")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Object getAllEndDeviceEventOrActions() {
        return asJsonArrayObjectWithTranslation("eventOrActions", "eventOrAction", new EndDeviceEventOrActionAdapter().getClientSideValues());
    }

    @GET
    @Path("/loglevels")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Object getLogLevels() {
        return asJsonArrayObjectWithTranslation("logLevels", "logLevel", new LogLevelAdapter().getClientSideValues());
    }

}
