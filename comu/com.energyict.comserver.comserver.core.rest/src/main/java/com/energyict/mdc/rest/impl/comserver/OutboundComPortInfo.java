/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.ports.ComPortType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public abstract class OutboundComPortInfo extends ComPortInfo<OutboundComPort, OutboundComPort.OutboundComPortBuilder> {

    public List<VersionInfoWithName> outboundComPortPoolIds = new ArrayList<>();

    public OutboundComPortInfo() {
        this.direction = "outbound";
        this.comPortType = new ComPortTypeInfo(ComPortType.TCP);
    }

    public OutboundComPortInfo(OutboundComPort comPort, EngineConfigurationService engineConfigurationService) {
        super(comPort);
        this.direction = "outbound";
        List<OutboundComPortPool> outboundComPortPools = engineConfigurationService.findContainingComPortPoolsForComPort(comPort);
        outboundComPortPoolIds.addAll(engineConfigurationService.findContainingComPortPoolsForComPort(comPort)
                .stream()
                .map(pool -> new VersionInfoWithName(pool.getId(), pool.getVersion(), pool.getName()))
                .collect(Collectors.toList()));
    }

    private List<Long> createHasIdList(List<OutboundComPortPool> outboundComPortPools) {
        List<Long> ids = new ArrayList<>();
        for (OutboundComPortPool outboundComPortPool : outboundComPortPools) {
            ids.add(outboundComPortPool.getId());
        }
        return ids;
    }

    @Override
    protected void writeTo(OutboundComPort source, EngineConfigurationService engineConfigurationService, ResourceHelper resourceHelper) {
        super.writeTo(source, engineConfigurationService, resourceHelper);
        updateComPortPools(source, engineConfigurationService, resourceHelper);
    }

    private void updateComPortPools(OutboundComPort comPort, EngineConfigurationService engineConfigurationService, ResourceHelper resourceHelper) {
        List<OutboundComPortPool> currentOutboundPools = engineConfigurationService.findContainingComPortPoolsForComPort(comPort);
        List<Long> currentIdList = createHasIdList(currentOutboundPools);
        for (VersionInfoWithName outboundComPortPool : outboundComPortPoolIds) {
            if (!currentIdList.contains(outboundComPortPool.id)) {
                ComPortPool comPortPool = resourceHelper.getLockedComPortPool(outboundComPortPool.id, outboundComPortPool.version)
                        .orElseThrow(resourceHelper.getConcurrentExSupplier(this.name, () -> resourceHelper.getCurrentComPortVersion(this.id)));
                ((OutboundComPortPool) comPortPool).addOutboundComPort(comPort);
                comPortPool.update();
            }
        }
        List<Long> userComPortPoolIdsList = outboundComPortPoolIds.stream()
                .filter(Objects::nonNull)
                .map(pool -> pool.id)
                .collect(Collectors.toList());
        for (OutboundComPortPool oldOutboundPool : currentOutboundPools) {
            if(!userComPortPoolIdsList.contains(oldOutboundPool.getId())){
                resourceHelper.getLockedComPortPool(oldOutboundPool.getId(), oldOutboundPool.getVersion())
                        .orElseThrow(resourceHelper.getConcurrentExSupplier(this.name, () -> resourceHelper.getCurrentComPortVersion(this.id)));
                oldOutboundPool.removeOutboundComPort(comPort);
            }
        }
    }

    @Override
    protected OutboundComPort.OutboundComPortBuilder build(OutboundComPort.OutboundComPortBuilder builder, EngineConfigurationService engineConfigurationService) {
        return super.build(builder.comPortType(comPortType.id), engineConfigurationService);
    }

    @Override
    protected OutboundComPort createNew(ComServer comServer, EngineConfigurationService engineConfigurationService) {
        OutboundComPort outboundComPort = build(comServer.newOutboundComPort(this.name, this.numberOfSimultaneousConnections), engineConfigurationService).add();
        for (VersionInfoWithName outboundComPortPoolInfo : outboundComPortPoolIds) {
            Optional<OutboundComPortPool> outboundComPortPool = engineConfigurationService.findOutboundComPortPool(outboundComPortPoolInfo.id);
            if (outboundComPortPool.isPresent()) {
                outboundComPortPool.get().addOutboundComPort(outboundComPort);
            }
        }
        return outboundComPort;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VersionInfoWithName {
        @JsonProperty("id")
        private long id;
        @JsonProperty("version")
        private Long version;
        @JsonProperty("name")
        private String name;

        public VersionInfoWithName() {
        }

        public VersionInfoWithName(Long id, Long version, String name) {
            this.id = id;
            this.version = version;
            this.name = name;
        }
    }
}
