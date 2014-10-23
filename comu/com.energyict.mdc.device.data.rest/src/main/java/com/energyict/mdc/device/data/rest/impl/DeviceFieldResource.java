package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageService;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static java.util.stream.Collectors.toList;

@Path("/field")
public class DeviceFieldResource extends FieldResource {

    private final DeviceMessageService deviceMessageService;
    private final UserService userService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceFieldResource(NlsService nlsService, DeviceMessageService deviceMessageService, UserService userService, ExceptionFactory exceptionFactory) {
        super(nlsService.getThesaurus(DeviceApplication.COMPONENT_NAME, Layer.REST));
        this.deviceMessageService = deviceMessageService;
        this.userService = userService;
        this.exceptionFactory = exceptionFactory;
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
    public Object getAllCommandCategories(@BeanParam JsonQueryFilter jsonQueryFilter) {
        if (jsonQueryFilter.getProperty("user")!=null) {
            Long userId = jsonQueryFilter.getProperty("user");
            User user = userService.getUser(userId).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_USER));
        }
        return asJsonArrayObject("commandCategories", "commandCategory", deviceMessageService.allCategories().stream().map(c -> new IdWithNameInfo(c.getId(), c.getName())).collect(toList()));
    }


}
