package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.Manufacturer;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableServiceImpl;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/deviceprotocol")
public class DeviceProtocolResource {

    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public DeviceProtocolResource(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeviceProtocolInfo> getAllDeviceProtocols() {
        List<DeviceProtocolInfo> deviceProtocolInfos = new ArrayList<>();

        for (DeviceProtocolPluggableClass deviceProtocolPluggableClass : this.protocolPluggableService.findAllDeviceProtocolPluggableClasses()) {
            deviceProtocolInfos.add(new DeviceProtocolInfo(deviceProtocolPluggableClass));
        }
        return deviceProtocolInfos;
    }

}
