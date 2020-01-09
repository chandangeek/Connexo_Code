package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs;

import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Application;

@Component(
        name = "com.elster.jupiter.webservice.rest.scim.provider",
        service = {InboundRestEndPointProvider.class},
        immediate = true,
        property = {"name=scim"}
)
public class SCIMApplicationProvider implements InboundRestEndPointProvider {

    @Override
    public Application get() {
        return new SCIMApplication();
    }

}
