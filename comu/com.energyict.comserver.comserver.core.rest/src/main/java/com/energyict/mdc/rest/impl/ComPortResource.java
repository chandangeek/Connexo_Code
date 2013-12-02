package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.services.ComPortService;
import com.energyict.mdc.services.ComServerService;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;

@Path("/comports")
public class ComPortResource {

    @Inject
    private ComPortService comPortService;
    @Inject
    private ComServerService comServerService;

    public ComPortResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortsInfo getComPorts(@QueryParam("filter") JSONArray filter) {
        ComPortsInfo wrapper = new ComPortsInfo();
        if (filter != null) {
            Filter comPortFilter = new Filter(filter);
            ComServer comServer = comServerService.find(Integer.parseInt(comPortFilter.getFilterProperties().get("comserver_id")));
            for (ComPort comPort : comPortService.findByComServer(comServer)) {
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

}
