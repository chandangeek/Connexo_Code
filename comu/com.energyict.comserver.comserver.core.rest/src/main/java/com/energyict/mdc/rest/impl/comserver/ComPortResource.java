package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.security.Privileges;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/comports")
public class ComPortResource {

    private final EngineModelService engineModelService;

    @Inject
    public ComPortResource(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE, Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE})
    public PagedInfoList getComPorts(@BeanParam JsonQueryFilter comPortFilter, @BeanParam QueryParameters queryParameters) {
        List<ComPortInfo> comPortInfos = new ArrayList<>();
        if (comPortFilter.hasFilters()) {
            Long comserverIdProperty = comPortFilter.hasProperty("comserver_id") ? comPortFilter.getLong("comserver_id") : null;
            String directionProperty = comPortFilter.hasProperty("direction") ? comPortFilter.getString("direction") : null;
            if (comserverIdProperty != null) {
                Optional<ComServer> comServer = engineModelService.findComServer(comserverIdProperty);
                List<ComPort> comPorts = comServer.get().getComPorts();
                for (ComPort comPort : comPorts) {
                    comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineModelService));
                }
            } else if (directionProperty != null) {
                List<? extends ComPort> comPorts = ("inbound".equals(directionProperty)) ?
                        engineModelService.findAllInboundComPorts() :
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
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE, Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE})
    public ComPortInfo getComPort(@PathParam("id") long id) {
        Optional<ComPort> comPort = Optional.ofNullable(engineModelService.findComPort(id));
        if (!comPort.isPresent()) {
            throw new WebApplicationException("No ComPort with id " + id,
                    Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id " + id).build());
        }
        return ComPortInfoFactory.asInfo(comPort.get(), engineModelService);
    }

}
