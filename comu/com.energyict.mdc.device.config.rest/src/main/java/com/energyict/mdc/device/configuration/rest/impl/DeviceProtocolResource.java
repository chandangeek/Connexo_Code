package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

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

    @GET
    @Path("/{deviceProtocolId}/supportedconnectiontypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ConnectionTypeInfo>getSupportedConnectionTypes(@PathParam("deviceProtocolId") long deviceProtocolId){
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolId);
        List<ConnectionType> supportedConnectionTypes = deviceProtocolPluggableClass.getDeviceProtocol().getSupportedConnectionTypes();
        List<ConnectionTypePluggableClass> allConnectionTypePluggableClassesToCheck = this.protocolPluggableService.findAllConnectionTypePluggableClasses();
        List<ConnectionTypeInfo> infos = new ArrayList<>();
        for(ConnectionType supportedConnectionType: supportedConnectionTypes){
            for(ConnectionTypePluggableClass connectionTypePluggableClass:allConnectionTypePluggableClassesToCheck){
                if(connectionTypePluggableClass.getJavaClassName().equals(supportedConnectionType.getClass().getCanonicalName())){
               // todo
               //     infos.add(ConnectionTypeInfo.from(connectionTypePluggableClass));
                }
            }
        }
       return infos;
    }

}
