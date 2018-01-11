package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

public class TransitionWebServiceEndpointInfoFactory {

    public TransitionWebServiceEndpointInfo from(EndPointConfiguration endPointConfiguration){
        return new TransitionWebServiceEndpointInfo(endPointConfiguration.getId(),
                endPointConfiguration.getWebServiceName());
    }
}