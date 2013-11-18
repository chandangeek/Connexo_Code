package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.services.ComPortService;
import com.energyict.mdc.services.ComServerService;
import com.energyict.mdc.shadow.ports.TCPBasedInboundComPortShadow;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/comports")
public class ComPortResource {

    private final ComPortService comPortService;
    private final ComServerService comServerService;

    public ComPortResource(@Context Application application) {
//        deviceProtocolFactoryService=((ServiceLocator)((ResourceConfig)application).getApplication()).getDeviceProtocolFactoryService();
        comPortService = ((MdcApplication) ((ResourceConfig) application).getApplication()).getComPortService();
        comServerService = ((MdcApplication) ((ResourceConfig) application).getApplication()).getComServerService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortsInfo getComPorts(@QueryParam("filter") JSONArray filter) {
        ComPortsInfo comPorts = new ComPortsInfo();
        if(filter!=null){
            Filter comPortFilter = new Filter(filter);
            ComServer comServer = comServerService.find(Integer.parseInt(comPortFilter.getFilterProperties().get("comserver_id")));
            for (ComPort comPort : comPortService.findByComServer(comServer)){
                comPorts.comPorts.add(new ComPortInfo(comPort));
            }
        } else {
            for (ComPort comPort : comPortService.findAll()) {
                    comPorts.comPorts.add(new ComPortInfo(comPort));
            }
        }
        return comPorts;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo getComPort(@PathParam("id") int id) {
        return new ComPortInfo(ManagerFactory.getCurrent().getComPortFactory().find(id));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ComPortInfo createComPort(ComPortInfo comPortInfo) {
        if (comPortInfo.comPortType.equals(ComPortType.TCP.toString())) {
            TCPBasedInboundComPortShadow comPortShadow = new TCPBasedInboundComPortShadow();
            comPortShadow.setName(comPortInfo.name);
            comPortShadow.setActive(comPortInfo.active);
            comPortShadow.setComServerId(comPortInfo.comserver_id);
            comPortShadow.setNumberOfSimultaneousConnections(comPortInfo.numberOfSimultaneousConnections);
//            ManagerFactory.getCurrent().getComPortFactory()

        }
        return null;
    }
}
