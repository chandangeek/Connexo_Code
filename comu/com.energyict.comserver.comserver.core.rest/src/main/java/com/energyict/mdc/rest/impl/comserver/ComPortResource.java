package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.rest.impl.filter.Filter;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.services.ComPortService;
import com.energyict.mdc.services.ComServerService;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONArray;

@Path("/comports")
public class ComPortResource {

    private final ComPortService comPortService;
    private final ComServerService comServerService;

    @Inject
    public ComPortResource(ComPortService comPortService, ComServerService comServerService) {
        this.comPortService = comPortService;
        this.comServerService = comServerService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortsInfo getComPorts(@QueryParam("filter") JSONArray filter) {
        ComPortsInfo wrapper = new ComPortsInfo();
        if (filter != null) {
            Filter comPortFilter = new Filter(filter);
            if(comPortFilter.getFilterProperties().get("comserver_id")!=null){
                ComServer comServer = comServerService.find(Integer.parseInt(comPortFilter.getFilterProperties().get("comserver_id")));
                for (ComPort comPort : comPortService.findByComServer(comServer)) {
                    wrapper.comPorts.add(ComPortInfoFactory.asInfo(comPort));
                }
            } else if (comPortFilter.getFilterProperties().get("direction")!=null){
                List<? extends ComPort> comPorts = ("inbound".equals(comPortFilter.getFilterProperties().get("direction")))?
                        comPortService.findAllInboundComPorts():
                        comPortService.findAllOutboundComPorts();
                for (ComPort comPort :comPorts) {
                    wrapper.comPorts.add(ComPortInfoFactory.asInfo(comPort));
                }
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
        ComPort comPort = comPortService.find(id);
        if (comPort==null) {
            throw new WebApplicationException("No ComPort with id "+id,
                Response.status(Response.Status.NOT_FOUND).build());
        }
        return ComPortInfoFactory.asInfo(comPort);
    }

}
