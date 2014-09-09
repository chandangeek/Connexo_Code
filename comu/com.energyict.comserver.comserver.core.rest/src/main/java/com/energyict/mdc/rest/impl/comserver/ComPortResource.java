package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.security.Privileges;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
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
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public PagedInfoList getComPorts(@BeanParam JsonQueryFilter comPortFilter, @BeanParam QueryParameters queryParameters) {
        List<ComPortInfo> comPortInfos = new ArrayList<>();
        if (!comPortFilter.getFilterProperties().isEmpty()) {
            Long comserverIdProperty = comPortFilter.getFilterProperties().get("comserver_id")!=null?comPortFilter.getLong("comserver_id"):null;
            String directionProperty = comPortFilter.getFilterProperties().get("direction")!=null?comPortFilter.<String>getProperty("direction"):null;
            if(comserverIdProperty!=null){
                Optional<ComServer> comServer = engineModelService.findComServer(comserverIdProperty);
                List<ComPort> comPorts = comServer.get().getComPorts();
                for (ComPort comPort : comPorts) {
                    comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineModelService));
                }
            } else if (directionProperty!=null){
                List<? extends ComPort> comPorts = ("inbound".equals(directionProperty))?
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
    @RolesAllowed(Privileges.VIEW_COMSERVER)
    public ComPortInfo getComPort(@PathParam("id") long id) {
        Optional<ComPort> comPort = Optional.fromNullable(engineModelService.findComPort(id));
        if (!comPort.isPresent()) {
            throw new WebApplicationException("No ComPort with id "+id,
                Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id "+id).build());
        }
        return ComPortInfoFactory.asInfo(comPort.get(), engineModelService);
    }

}
