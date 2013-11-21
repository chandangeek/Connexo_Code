package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.services.ComPortService;
import com.energyict.mdc.services.ComServerService;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;

@Path("/comports")
public class ComPortResource {

    private final ComPortService comPortService;
    private final ComServerService comServerService;

    public ComPortResource(@BeanParam ComServerServiceHolder comServerServiceHolder, @Context Application application) {
//        deviceProtocolFactoryService=((ServiceLocator)((ResourceConfig)application).getApplication()).getDeviceProtocolFactoryService();
        comPortService = ((MdcApplication) ((ResourceConfig) application).getApplication()).getComPortService();
        comServerService = comServerServiceHolder.getComServerService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortsInfo getComPorts(@QueryParam("filter") JSONArray filter) {
        ComPortsInfo wrapper = new ComPortsInfo();
        if(filter!=null){
            Filter comPortFilter = new Filter(filter);
            ComServer comServer = comServerService.find(Integer.parseInt(comPortFilter.getFilterProperties().get("comserver_id")));
            for (ComPort comPort : comPortService.findByComServer(comServer)){
                wrapper.comPorts.add(ComPortInfoFactory.asInfo(comPort));
            }
        } else {
            for (ComPort comPort : comPortService.findAll()) {
                    wrapper.comPorts.add(ComPortInfoFactory.asInfo(comPort));
            }
        }
        return wrapper;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo getComPort(@PathParam("id") int id) {
        return ComPortInfoFactory.asInfo(comPortService.find(id));
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ComPortInfo updateComPort(@PathParam("id") int id, ComPortInfo comPortInfo) {
        System.err.println("ComPort class=" + comPortInfo.asShadow().getClass().getSimpleName());
        return comPortInfo;
    }

}
