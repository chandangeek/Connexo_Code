package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.ServerDeviceProtocolPluggableClass;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.PluggableClass;
import com.energyict.mdw.core.PluggableClassType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 12:29
 */
@Path("/devicecommunicationprotocols")
public class DeviceCommunicationProtocolsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfos getDeviceCommunicationProtocols() {
        Set<DeviceProtocolPluggableClass> deviceCommunicationProtocolInfoList = new HashSet<>();
        for (PluggableClass pluggableClass : MeteringWarehouse.getCurrent().getPluggableClassFactory().findByType(PluggableClassType.DEVICEPROTOCOL)) {
            deviceCommunicationProtocolInfoList.add(createDeviceProtocolPluggableClass(pluggableClass));
        }
        return new DeviceCommunicationProtocolInfos(deviceCommunicationProtocolInfoList);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo getComServer(@PathParam("id") int id) {
        return new DeviceCommunicationProtocolInfo(createDeviceProtocolPluggableClass(MeteringWarehouse.getCurrent().getPluggableClassFactory().find(id)));
    }

    private DeviceProtocolPluggableClass createDeviceProtocolPluggableClass(PluggableClass pluggableClass) {
        return Bus.getDeviceProtocolFactoryService().createDeviceProtocolPluggableClassFor(pluggableClass);
//        return ManagerFactory.getCurrent().getDeviceProtocolPluggableClassFactory().newForPluggableClass(pluggableClass);
    }

}
