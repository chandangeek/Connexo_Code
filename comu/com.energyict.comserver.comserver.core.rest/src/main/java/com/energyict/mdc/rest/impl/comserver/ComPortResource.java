package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/comports")
public class ComPortResource {

    private final EngineModelService engineModelService;

    @Inject
    public ComPortResource(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getComPorts(@BeanParam JsonQueryFilter comPortFilter, @BeanParam QueryParameters queryParameters) {
        List<ComPortInfo> comPortInfos = new ArrayList<>();
        if (!comPortFilter.getFilterProperties().isEmpty()) {
            Optional<String> comserverIdProperty = Optional.fromNullable(comPortFilter.getFilterProperties().get("comserver_id"));
            Optional<String> directionProperty = Optional.fromNullable(comPortFilter.getFilterProperties().get("direction"));
            if(comserverIdProperty.isPresent()){
                Optional<ComServer> comServer = engineModelService.findComServer(Integer.parseInt(comserverIdProperty.get()));
                List<ComPort> comPorts = comServer.get().getComPorts();
                for (ComPort comPort : comPorts) {
                    comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineModelService));
                }
            } else if (directionProperty.isPresent()){
                List<? extends ComPort> comPorts = ("inbound".equals(directionProperty.get()))?
                        engineModelService.findAllInboundComPorts():
                        engineModelService.findAllOutboundComPorts();
                for (ComPort comPort : comPorts) {
                    comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineModelService));
                }
            }
        } else {
            for (ComPort comPort : engineModelService.findAllComPortsWithDeleted()) {
                comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineModelService));
            }
        }
        return PagedInfoList.asJson("data", comPortInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComPortInfo getComPort(@PathParam("id") long id) {
        Optional<ComPort> comPort = Optional.fromNullable(engineModelService.findComPort(id));
        if (!comPort.isPresent()) {
            throw new WebApplicationException("No ComPort with id "+id,
                Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id "+id).build());
        }
        return ComPortInfoFactory.asInfo(comPort.get(), engineModelService);
    }

}
