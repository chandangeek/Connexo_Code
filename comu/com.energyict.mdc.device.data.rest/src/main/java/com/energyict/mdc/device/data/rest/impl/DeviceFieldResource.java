package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageService;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static java.util.stream.Collectors.toList;

@Path("/field")
public class DeviceFieldResource extends FieldResource {

    private final DeviceMessageService deviceMessageService;

    @Inject
    public DeviceFieldResource(NlsService nlsService, DeviceMessageService deviceMessageService) {
        super(nlsService.getThesaurus(DeviceApplication.COMPONENT_NAME, Layer.REST));
        this.deviceMessageService = deviceMessageService;
    }
    
    @GET
    @Path("/enddevicedomains")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getAllEndDeviceDomains() {
        return asJsonArrayObjectWithTranslation("domains", "domain", new EndDeviceDomainAdapter().getClientSideValues());
    }
    
    @GET
    @Path("/enddevicesubdomains")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getAllEndDeviceSubDomains() {
        return asJsonArrayObjectWithTranslation("subDomains", "subDomain", new EndDeviceSubDomainAdapter().getClientSideValues());
    }
    
    @GET
    @Path("/enddeviceeventoractions")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getAllEndDeviceEventOrActions() {
        return asJsonArrayObjectWithTranslation("eventOrActions", "eventOrAction", new EndDeviceEventOrActionAdapter().getClientSideValues());
    }

    @GET
    @Path("/commandcategories")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getAllCommandCategories() {
        return asJsonArrayObjectWithTranslation("commandCategories", "commandCategory", deviceMessageService.allCategories().stream().map(c->new IdWithNameInfo(c.getId(),c.getName())).collect(toList()));
    }


}
