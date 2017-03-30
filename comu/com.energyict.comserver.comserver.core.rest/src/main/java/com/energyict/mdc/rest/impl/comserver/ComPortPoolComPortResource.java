/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.config.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ComPortPoolComPortResource {

    private final EngineConfigurationService engineConfigurationService;
    private final ResourceHelper resourceHelper;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final ComPortInfoFactory comPortInfoFactory;

    @Inject
    public ComPortPoolComPortResource(EngineConfigurationService engineConfigurationService, ResourceHelper resourceHelper, ConcurrentModificationExceptionFactory conflictFactory, ComPortInfoFactory comPortInfoFactory) {
        this.engineConfigurationService = engineConfigurationService;
        this.resourceHelper = resourceHelper;
        this.conflictFactory = conflictFactory;
        this.comPortInfoFactory = comPortInfoFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getComPorts(@PathParam("comPortPoolId") long comPortPoolId, @BeanParam JsonQueryParameters queryParameters) {
        ComPortPool comPortPool = resourceHelper.findComPortPoolOrThrowException(comPortPoolId);
        List<ComPort> comPorts = new ArrayList<>(comPortPool.getComPorts());

        comPorts = ListPager.of(comPorts, Comparator.comparing(ComPort::getName, String.CASE_INSENSITIVE_ORDER)).from(queryParameters).find();

        List<ComPortInfo> comPortInfos = comPorts.stream()
                .map(comPort -> comPortInfoFactory.asInfo(comPort, engineConfigurationService))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", comPortInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION})
    public ComPortInfo getComPort(@PathParam("comPortPoolId") long comPortPoolId, @PathParam("id") long id) {
        ComPortPool comPortPool = resourceHelper.findComPortPoolOrThrowException(comPortPoolId);
        ComPort comPort = resourceHelper.findComPortOrThrowException(id, comPortPool);
        return comPortInfoFactory.asInfo(comPort, engineConfigurationService);
    }

    @DELETE @Transactional
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response removeComPort(@PathParam("comPortPoolId") long comPortPoolId, @PathParam("id") long id, ComPortPoolInfo info) {
        ComPortPool comPortPool = resourceHelper.getLockedComPortPool(info.id, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> resourceHelper.getCurrentComPortPoolVersion(info.id))
                        .withMessageTitle(MessageSeeds.EDIT_POOL_CONCURRENT_TITLE, info.name)
                        .withMessageBody(MessageSeeds.EDIT_POOL_CONCURRENT_BODY, info.name)
                        .supplier());
        removeComPortFromComPortPool(comPortPool, id);
        comPortPool.update();
        return Response.noContent().build();
    }

    private void removeComPortFromComPortPool(ComPortPool comPortPool, long comPortId) {
        if(OutboundComPortPool.class.isAssignableFrom(comPortPool.getClass())) {
            ComPort comPort = resourceHelper.findComPortOrThrowException(comPortId, comPortPool);
            ((OutboundComPortPool)comPortPool).removeOutboundComPort((OutboundComPort) comPort);
            return;
        }
        if(InboundComPortPool.class.isAssignableFrom(comPortPool.getClass())) {
            ComPort comPort = resourceHelper.findComPortOrThrowException(comPortId, comPortPool);
            if(InboundComPort.class.isAssignableFrom(comPort.getClass())) {
                ((InboundComPort)comPort).setComPortPool(null);
                comPort.update();
            }
        }
    }
}
