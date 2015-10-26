package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class ComServerComPortResource {

    private final EngineConfigurationService engineConfigurationService;
    private final ResourceHelper resourceHelper;

    @Inject
    public ComServerComPortResource(EngineConfigurationService engineConfigurationService,
                                    ResourceHelper resourceHelper) {
        this.engineConfigurationService = engineConfigurationService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getComPorts(@PathParam("comServerId") long comServerId, @BeanParam JsonQueryParameters queryParameters) {
        ComServer comServer = resourceHelper.findComServerOrThrowException(comServerId);
        List<ComPort> comPorts = ListPager.of(comServer.getComPorts(), (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                .from(queryParameters).find();
        List<ComPortInfo> comPortInfos = new ArrayList<>(comPorts.size());

        for (ComPort comPort : comPorts) {
            comPortInfos.add(ComPortInfoFactory.asInfo(comPort, engineConfigurationService));
        }

        return PagedInfoList.fromPagedList("data", comPortInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public ComPortInfo getComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id) {
        ComPort comPort = resourceHelper.findComPortOrThrowException(id);
        return ComPortInfoFactory.asInfo(comPort, engineConfigurationService);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public ComPortInfo createOutboundComPort(@PathParam("comServerId") long comServerId, ComPortInfo comPortInfo) {
        ComServer comServer = resourceHelper.findComServerOrThrowException(comServerId);
        ComPort newComPort = comPortInfo.createNew(comServer, engineConfigurationService);
        return ComPortInfoFactory.asInfo(newComPort, engineConfigurationService);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public ComPortInfo updateOutboundComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id, ComPortInfo info) {
        info.id = id;
        ComPort comPort = resourceHelper.lockComPortOrThrowException(info);
        info.writeTo(comPort, engineConfigurationService, resourceHelper);
        comPort.update();
        return ComPortInfoFactory.asInfo(comPort, engineConfigurationService);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response removeComPort(@PathParam("comServerId") long comServerId, @PathParam("id") long id, ComPortInfo info) {
        info.id = id;
        ComPort comPort = resourceHelper.lockComPortOrThrowException(info);
        comPort.getComServer().removeComPort(id);
        return Response.noContent().build();
    }
}
