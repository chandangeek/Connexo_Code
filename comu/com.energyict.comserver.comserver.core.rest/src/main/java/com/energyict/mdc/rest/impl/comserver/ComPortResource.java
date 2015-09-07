package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.security.Privileges;

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

    private final EngineConfigurationService engineConfigurationService;

    @Inject
    public ComPortResource(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getComPorts(@BeanParam JsonQueryFilter comPortFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<ComPortInfo> comPortInfos = new ArrayList<>();
        if (comPortFilter.hasFilters()) {
            Long comserverIdProperty = comPortFilter.hasProperty("comserver_id") ? comPortFilter.getLong("comserver_id") : null;
            String directionProperty = comPortFilter.hasProperty("direction") ? comPortFilter.getString("direction") : null;
            if (comserverIdProperty != null) {
                Optional<ComServer> comServer = engineConfigurationService.findComServer(comserverIdProperty);
                List<ComPort> comPorts = comServer.get().getComPorts();
                for (ComPort comPort : comPorts) {
                    comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineConfigurationService));
                }
            } else if (directionProperty != null) {
                List<? extends ComPort> comPorts = ("inbound".equals(directionProperty)) ?
                        engineConfigurationService.findAllInboundComPorts() :
                        engineConfigurationService.findAllOutboundComPorts();
                for (ComPort comPort : comPorts) {
                    comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineConfigurationService));
                }
            }
        } else {
            for (ComPort comPort : engineConfigurationService.findAllComPortsWithDeleted()) {
                comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineConfigurationService));
            }
        }
        return PagedInfoList.fromPagedList("data", comPortInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public ComPortInfo getComPort(@PathParam("id") long id) {
        Optional<? extends ComPort> comPort = engineConfigurationService.findComPort(id);
        if (!comPort.isPresent()) {
            throw new WebApplicationException("No ComPort with id " + id,
                    Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id " + id).build());
        }
        return ComPortInfoFactory.asInfo(comPort.get(), engineConfigurationService);
    }

}
