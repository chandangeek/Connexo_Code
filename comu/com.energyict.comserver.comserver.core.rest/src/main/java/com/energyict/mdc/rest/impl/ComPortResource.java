package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.shadow.ports.TCPBasedInboundComPortShadow;
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

    public ComPortResource(@Context Application application) {
//        deviceProtocolFactoryService=((ServiceLocator)((ResourceConfig)application).getApplication()).getDeviceProtocolFactoryService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortsInfo getComPorts(@QueryParam("filter") JSONArray filter) {
        ComPortsInfo comPorts = new ComPortsInfo();
        if(filter!=null){
            Filter comPortFilter = new Filter(filter);
            if(comPortFilter.getFilterProperties().get("comserver_id")!="null"){
                ComServer comServer = ManagerFactory.getCurrent().getComServerFactory().find(Integer.parseInt(comPortFilter.getFilterProperties().get("comserver_id")));
                for (ComPort comPort : ManagerFactory.getCurrent().getComPortFactory().findByComServer(comServer)){
                    comPorts.comPorts.add(new ComPortInfo(comPort));
                }
            }
        } else {
            for (ComPort comPort : ManagerFactory.getCurrent().getComPortFactory().findAll()) {
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
