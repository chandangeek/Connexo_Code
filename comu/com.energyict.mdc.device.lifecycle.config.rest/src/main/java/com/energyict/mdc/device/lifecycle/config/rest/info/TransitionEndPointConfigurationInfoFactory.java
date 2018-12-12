package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

public class TransitionEndPointConfigurationInfoFactory {

    public TransitionEndPointConfigurationInfo from(EndPointConfiguration endPointConfiguration) {
        return new TransitionEndPointConfigurationInfo(endPointConfiguration.getId(),
                endPointConfiguration.getName(),
                endPointConfiguration.getVersion());
    }
}