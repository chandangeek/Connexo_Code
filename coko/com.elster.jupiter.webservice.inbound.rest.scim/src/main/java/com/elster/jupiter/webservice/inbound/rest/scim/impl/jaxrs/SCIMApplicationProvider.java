package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs;

import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.users.UserService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.ws.rs.core.Application;

@Component(
        name = "com.elster.jupiter.webservice.rest.scim.provider",
        service = {InboundRestEndPointProvider.class},
        immediate = true,
        property = {"name=scim"}
)
public class SCIMApplicationProvider implements InboundRestEndPointProvider {

    private volatile UserService userService;

    @Override
    public Application get() {
        return new SCIMApplication(userService);
    }

    @Reference(policy = ReferencePolicy.DYNAMIC)
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
