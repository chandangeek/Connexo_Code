package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs;

import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
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

    private volatile TokenService tokenService;

    private volatile WebServicesService webServicesService;

    public SCIMRESTfulWebServiceProvider() {
    }

    @Override
    public Application get() {
        return new SCIMApplication(userService, tokenService);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }
}
