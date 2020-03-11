package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs;

import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.users.UserService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;

@Component(
        name = "com.elster.jupiter.webservice.inbound.rest.scim.SCIMRESTfulWebServiceProvider",
        service = {
                InboundRestEndPointProvider.class
        },
        immediate = true,
        property = {
                "name=SCIM RESTful Web Service"
        }
)
public class SCIMRESTfulWebServiceProvider implements InboundRestEndPointProvider {

    private volatile UserService userService;

    public SCIMRESTfulWebServiceProvider() {
    }

    @Override
    public Application get() {
        return new SCIMApplication(userService);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
