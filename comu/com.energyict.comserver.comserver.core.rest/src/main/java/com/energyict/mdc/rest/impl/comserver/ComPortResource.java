package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.rest.impl.filter.Filter;
import org.json.JSONArray;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/comports")
public class ComPortResource {

    private final EngineModelService engineModelService;

    @Inject
    public ComPortResource(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortsInfo getComPorts(@QueryParam("filter") JSONArray filter) {
        ComPortsInfo wrapper = new ComPortsInfo();
        if (filter != null) {
            Filter comPortFilter = new Filter(filter);
            if(comPortFilter.getFilterProperties().get("comserver_id")!=null){
                ComServer comServer = engineModelService.findComServer(Integer.parseInt(comPortFilter.getFilterProperties().get("comserver_id")));
                for (ComPort comPort : comServer.getComPorts()) {
                    wrapper.comPorts.add(ComPortInfoFactory.asInfo(comPort));
                }
            } else if (comPortFilter.getFilterProperties().get("direction")!=null){
                List<? extends ComPort> comPorts = ("inbound".equals(comPortFilter.getFilterProperties().get("direction")))?
                        engineModelService.findAllInboundComPorts():
                        engineModelService.findAllOutboundComPorts();
                for (ComPort comPort :comPorts) {
                    wrapper.comPorts.add(ComPortInfoFactory.asInfo(comPort));
                }
            }
        } else {
            for (ComPort comPort : engineModelService.findAllWithDeleted()) {
                wrapper.comPorts.add(ComPortInfoFactory.asInfo(comPort));
            }
        }
        return wrapper;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo getComPort(@PathParam("id") long id) {
        ComPort comPort = engineModelService.findComPort(id);
        if (comPort==null) {
            throw new WebApplicationException("No ComPort with id "+id,
                Response.status(Response.Status.NOT_FOUND).build());
        }
        return ComPortInfoFactory.asInfo(comPort);
    }

}
