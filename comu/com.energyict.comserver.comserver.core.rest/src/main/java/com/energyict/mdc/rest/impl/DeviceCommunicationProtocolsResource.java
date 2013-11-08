package com.energyict.mdc.rest.impl;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.services.DeviceProtocolFactoryService;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.PluggableClass;
import com.energyict.mdw.core.PluggableClassType;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 12:29
 */
@Path("/devicecommunicationprotocols")
public class DeviceCommunicationProtocolsResource {

    private final DeviceProtocolFactoryService deviceProtocolFactoryService;

    public DeviceCommunicationProtocolsResource(@Context Application application) {
        this.deviceProtocolFactoryService = ((MdcApplication) ((ResourceConfig) application).getApplication()).getDeviceProtocolFactoryService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfos getDeviceCommunicationProtocols() {
        Set<SimpleDeviceProtocolPluggableClass> deviceCommunicationProtocolInfoList = new HashSet<>();
        for (PluggableClass pluggableClass : MeteringWarehouse.getCurrent().getPluggableClassFactory().findByType(PluggableClassType.DEVICEPROTOCOL)) {
            try {
                deviceCommunicationProtocolInfoList.add(new SimpleDeviceProtocolPluggableClass(pluggableClass, createDeviceProtocolPluggableClass(pluggableClass)));
            } catch (CodingException e) {
                if (e.getMessageId().equals("CSC-DEV-125")) {
                    // this is jus the genericReflection Error, we could make it more precise by checking if it was a ClassNotFound
                    System.out.println(e.getMessage());
                }
            }
        }
        return new DeviceCommunicationProtocolInfos(deviceCommunicationProtocolInfoList);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo getDeviceCommunicationProtocol(@PathParam("id") int id) {
        PluggableClass pluggableClass = MeteringWarehouse.getCurrent().getPluggableClassFactory().find(id);
        if (pluggableClass == null) {
            throw new WebApplicationException("Unsupported DeviceCommunicationProtocol ID : " + id, Response.Status.NOT_FOUND);
        }
        return new DeviceCommunicationProtocolInfo(new SimpleDeviceProtocolPluggableClass(pluggableClass, createDeviceProtocolPluggableClass(pluggableClass)));
    }

    private DeviceProtocol createDeviceProtocolPluggableClass(PluggableClass pluggableClass) {
        return this.deviceProtocolFactoryService.createDeviceProtocolFor(pluggableClass);
    }

}
